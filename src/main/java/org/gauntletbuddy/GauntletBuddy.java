package org.gauntletbuddy;

import com.google.inject.Provides;
import javax.inject.Inject;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.*;
import net.runelite.api.events.*;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import org.gauntletbuddy.config.GauntletBuddyConfig;
import org.gauntletbuddy.config.types.AttackStyleType;

import static java.util.Objects.nonNull;
import static net.runelite.api.gameval.VarbitID.GAUNTLET_BOSS_STARTED;
import static net.runelite.api.gameval.VarbitID.GAUNTLET_START;

@Slf4j
@PluginDescriptor(
		name = "Gauntlet Buddy",
		description = "Helper plugin for The Gauntlet",
		tags = "combat"
)

public class GauntletBuddy extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private GauntletBuddyConfig config;

	@Provides
	GauntletBuddyConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(GauntletBuddyConfig.class);
	}

	@Inject
	private WarningOverlay warningOverlay;
	@Inject
	private PrayerOverlay prayerOverlay;
	@Inject
	private CounterOverlay counterOverlay;
	@Inject
	private DebugOverlay debugOverlay;

	@Inject
	private OverlayManager overlayManager;

	@Override
	protected void startUp() throws Exception
	{
		initVars();
		overlayManager.add(warningOverlay);
		overlayManager.add(prayerOverlay);
		overlayManager.add(counterOverlay);
		overlayManager.add(debugOverlay);
	}

	@Override
	protected void shutDown() throws Exception
	{
		overlayManager.remove(warningOverlay);
		overlayManager.remove(prayerOverlay);
		overlayManager.remove(counterOverlay);
		overlayManager.remove(debugOverlay);
	}

	/**
	 * Method to initialize all starting values on plugin load
	 */
	private void initVars()
	{
		prayStyle = AttackStyleType.RANGE;
		hitsLanded = 0;
		gatheredOre = 0;
		gatheredBark = 0;
		gatheredLinen = 0;
		gatheredFrames = 0;
		gatheredShards = 0;
		gatheredFish = 0;
		hasString = false;
		hasOrb = false;
		hasSpike = false;
		bossing = false;
		inside = false;
	}

	private void resetVars()
	{
		prayStyle = AttackStyleType.RANGE;
		hitsLanded = 0;
		gatheredOre = 0;
		gatheredBark = 0;
		gatheredLinen = 0;
		gatheredFrames = 0;
		gatheredFish = 0;
		hasString = false;
		hasOrb = false;
		hasSpike = false;
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		if (gameStateChanged.getGameState() == GameState.LOGGED_IN)
		{
			inside = client.getVarbitValue(GAUNTLET_START) != 0;
			if (!inside) {
				bossing = false;
				resetVars();
			}
		}
	}

	@Subscribe
	public  void onGameTick(GameTick gameTick)
	{
		// Early exit if not inside the gauntlet or already in boss no checks need to be run
		if (!inside || bossing) return;
		// If inside but not bossing check if the boss has been started in the last game tick to start boss checks
		bossing = client.getVarbitValue(GAUNTLET_BOSS_STARTED) != 0;
	}

	private final boolean DEBUG = false;

	@Getter
	private AttackStyleType prayStyle;
	@Getter
	private int hitsLanded;
	@Getter
	private int gatheredOre;
	@Getter
	private int gatheredBark;
	@Getter
	private int gatheredLinen;
	@Getter
	private int gatheredFrames;
	@Getter
	private int gatheredShards;
	@Getter
	private int gatheredFish;
	@Getter
	private boolean hasString;
	@Getter
	private boolean hasOrb;
	@Getter
	private boolean hasSpike;
	@Getter
	private boolean bossing;
	@Getter
	private boolean inside;

	@Subscribe
	public void onAnimationChanged(AnimationChanged animationChanged)
	{
		// Only need to listen when in the boss fight, all animation related info is only used there
		if (!bossing) return;

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

			// Logging line to identify animation IDs if DEBUG is true
			if (DEBUG) client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Player Animation ID : " + animId, null);

			// If the current animation is not an attack return early
			if (currentAttack == AttackStyleType.NONE) {
				return;
			}

			// Now that we know the player was attacking check if we're hitting the Hunllef
			Actor target = player.getInteracting();

			if (target instanceof NPC) {
				NPC npc = (NPC) target;
				String npcName = npc.getName();

				if (npcName.contains("Hunllef")) {
					int currentOverhead = npc.getOverheadSpriteIds()[0];
					AttackStyleType prayerStyle = AttackStyleType.NONE;

					if (nonNull(currentOverhead)) {
						switch(currentOverhead) {
							case 0:
								//melee prayer
								prayerStyle = AttackStyleType.MELEE;
								break;
							case 1:
								//range prayer
								prayerStyle = AttackStyleType.RANGE;
								break;
							case 2:
								//mage prayer
								prayerStyle = AttackStyleType.MAGIC;
								break;
						}
					}

					// Record hits that land as they cause the Hunllef to swap prayers
					if (prayerStyle != currentAttack) {
						hitsLanded += 1;
						// After 5 hits the next will trigger a prayer swap
						if (hitsLanded == 6) {
							// 6th hit will have rolled prayers over so reset the count and stop the warning flash
							hitsLanded = 0;
						}
					}
				}
			}
		} else if (actor instanceof NPC) {
			// Handle Hunllef animations since it tells what attack style is going to be used
			// Make sure it's the Hunllef
			NPC npc = (NPC) actor;
			String npcName = npc.getName();
			if (npcName.contains("Hunllef")) {
				int animId = npc.getAnimation();
				//8754 attack style changed ranged -> magic
				//8755 attack style changed magic -> ranged
				if (animId == 8754) {
					prayStyle = AttackStyleType.MAGIC;
				} else if (animId == 8755) {
					prayStyle = AttackStyleType.RANGE;
				}
				client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "PRAY : " + prayStyle, null);
				if (DEBUG) client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Hunllef Animation ID : " + animId, null);
			}
		}
	}
}
