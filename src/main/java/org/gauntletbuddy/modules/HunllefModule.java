package org.gauntletbuddy.modules;

import lombok.Getter;
import net.runelite.api.*;
import net.runelite.api.events.AnimationChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.NpcDespawned;
import net.runelite.api.events.NpcSpawned;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.ui.overlay.OverlayManager;
import org.gauntletbuddy.GauntletBuddy;
import org.gauntletbuddy.config.types.AttackStyleType;
import org.gauntletbuddy.overlays.CounterOverlay;
import org.gauntletbuddy.overlays.HunllefHitboxOverlay;
import org.gauntletbuddy.overlays.PrayerOverlay;
import org.gauntletbuddy.overlays.WarningOverlay;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;

@Singleton
public final class HunllefModule implements PluginModule {
    @Inject
    private EventBus eventBus;
    @Inject
    private Client client;
    @Inject
    private OverlayManager overlayManager;
    @Inject
    private WarningOverlay warningOverlay;
    @Inject
    private PrayerOverlay prayerOverlay;
    @Inject
    private CounterOverlay counterOverlay;
    @Inject
    private HunllefHitboxOverlay hunllefHitboxOverlay;
    @Inject
    private TornadoTracker tornadoTracker;
    @Inject
    private GauntletBuddy gauntletBuddy;


    @Override
    public void start()
    {
        eventBus.register(this);

        for (final NPC npc : client.getTopLevelWorldView().npcs())
        {
            if (npc != null) {
                String npcName = npc.getName();
                if (npcName != null && npcName.contains("Hunllef")) hunllef = npc;
            }
        }

        overlayManager.add(warningOverlay);
        overlayManager.add(prayerOverlay);
        overlayManager.add(counterOverlay);
        overlayManager.add(hunllefHitboxOverlay);

        tornadoTicks = 0;
        tornadoTracker.setConstants(gauntletBuddy.isCorrupted());

        if (hunllef != null) {
            currentHunllefPrayer = getHunllefPrayerStyle(hunllef);
            previousHunllefPrayer = getHunllefPrayerStyle(hunllef);
        }
    }

    @Override
    public void stop()
    {
        resetBossState();
        hunllef = null;
        tornadoList.clear();
        eventBus.unregister(this);
        overlayManager.remove(warningOverlay);
        overlayManager.remove(prayerOverlay);
        overlayManager.remove(counterOverlay);
        overlayManager.remove(hunllefHitboxOverlay);
    }

    private void resetBossState()
    {
        hitsLanded = 0;
        prayStyle = AttackStyleType.RANGE;
    }

    @Getter
    private AttackStyleType prayStyle = AttackStyleType.RANGE;
    @Getter
    private int hitsLanded;
    @Getter
    private AttackStyleType previousHunllefPrayer = AttackStyleType.NONE;
    @Getter
    private AttackStyleType currentHunllefPrayer = AttackStyleType.NONE;

    @Nullable
    @Getter
    private NPC hunllef;

    private static final List<Integer> TORNADO_IDS = List.of(9025, 9039);
    @Getter
    private List<NPC> tornadoList = new ArrayList<>();
    @Getter
    private long tornadoSpawned;
    @Getter
    private int tornadoTicks;
    @Getter
    private int x_offset;
    @Getter
    private int y_offset;


    //TODO Add highlighting for hunllef hitbox and tornadoes

    @Subscribe
    public void onAnimationChanged(AnimationChanged animationChanged)
    {
        Actor actor = animationChanged.getActor();
        if (actor instanceof Player) {
            Player player = (Player) actor;
            AttackStyleType currentAttack = AttackStyleType.NONE;
            int animId = player.getAnimation();

            /**
             * Punch/Block attack ID 422, Kick attack ID 423
             * Sceptre Attacks, Axe Smash, Pickaxe Attacks ID 401
             * Axe Attacks ID 395
             * Pickaxe Smash ID 400
             * Harpoon Attacks ID 386, Harpoon Slash ID 390
             * Halberd Attacks ID 428, Halberd Swipe ID 440
             */

            int[] meleeIds = {422, 423, 401, 395, 400, 386, 390, 428, 440};

            // Check what attack style the player is using to attack the Hunllef
            switch(animId) {
                case 426:
                    //Bow fired ID 426
                    currentAttack = AttackStyleType.RANGE;
                    break;
                case 1167:
                    //Staff cast ID 1167
                    currentAttack = AttackStyleType.MAGIC;
                    break;
            }

            for (int meleeId : meleeIds) {
                if (animId == meleeId) {
                    currentAttack = AttackStyleType.MELEE;
                    break;
                }
            }

            // If the current animation is not an attack return early
            if (currentAttack == AttackStyleType.NONE) return;

            // Now that we know the player was attacking check if we're hitting the Hunllef
            Actor target = player.getInteracting();

            if (target instanceof NPC) {
                NPC npc = (NPC) target;
                String npcName = npc.getName();

                if (npcName != null && npcName.contains("Hunllef")) {
                    currentHunllefPrayer = getHunllefPrayerStyle(npc);

                    // Fail-safe in case the plugin starts up when the player is already bossing this syncs the
                    // Consecutive hits made on the Hunllef
                    if (previousHunllefPrayer != currentHunllefPrayer) {
                        hitsLanded = 0;
                    }

                    // Record hits that land as they cause the Hunllef to swap prayers
                    if (currentHunllefPrayer != currentAttack) {
                        hitsLanded += 1;
                        // After 5 hits the next will trigger a prayer swap
                        if (hitsLanded == 6) {
                            // 6th hit will have rolled prayers over so reset the count and stop the warning flash
                            hitsLanded = 0;
                        }
                    }

                    previousHunllefPrayer = currentHunllefPrayer;
                }
            }
        } else if (actor instanceof NPC) {
            // Handle Hunllef animations since it tells what attack style is going to be used
            // Make sure it's the Hunllef
            NPC npc = (NPC) actor;
            String npcName = npc.getName();
            if (npcName != null && npcName.contains("Hunllef")) {
                int animId = npc.getAnimation();
                //8754 attack style changed ranged -> magic
                //8755 attack style changed magic -> ranged
                if (animId == 8754) {
                    prayStyle = AttackStyleType.MAGIC;
                } else if (animId == 8755) {
                    prayStyle = AttackStyleType.RANGE;
                }
            }
        }
    }

    private AttackStyleType getHunllefPrayerStyle(NPC npc) {
        short[] overHeadSprites = npc.getOverheadSpriteIds();
        AttackStyleType currentPrayer = AttackStyleType.NONE;
        if (overHeadSprites != null) {
            int currentOverhead = overHeadSprites[0];
            switch(currentOverhead) {
                case 0:
                    //melee prayer
                    currentPrayer = AttackStyleType.MELEE;
                    break;
                case 1:
                    //range prayer
                    currentPrayer = AttackStyleType.RANGE;
                    break;
                case 2:
                    //mage prayer
                    currentPrayer = AttackStyleType.MAGIC;
                    break;
            }
        }
        return currentPrayer;
    }

    @Subscribe
    void onNpcSpawned(final NpcSpawned npcSpawned)
    {
        final NPC npc = npcSpawned.getNpc();

        if (TORNADO_IDS.contains(npc.getId())) {
            if (tornadoList.isEmpty()) {
                tornadoSpawned = System.currentTimeMillis();
                tornadoTicks = 20;
            }
            tornadoList.add(npc);
        }

    }

    @Subscribe
    void onGameTick(final GameTick gameTick)
    {
        if (tornadoTicks > 0) {
            tornadoTicks -= 1;
            tornadoTracker.updateCache(tornadoTicks, tornadoList);
        }
    }

    @Subscribe
    void onNpcDespawned(final NpcDespawned npcDespawned)
    {
        final NPC npc = npcDespawned.getNpc();

        if (TORNADO_IDS.contains(npc.getId())) {
            tornadoList.clear();
        }
    }
}
