package org.gauntletbuddy;

import com.google.inject.Provides;
import javax.inject.Inject;
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

@Slf4j
@PluginDescriptor(
	name = "Gauntlet Buddy"
)
public class GauntletBuddy extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private GauntletBuddyConfig config;

	@Inject
	private WarningOverlay warningOverlay;
	@Inject
	private PrayerOverlay prayerOverlay;

	@Inject
	private OverlayManager overlayManager;

	@Override
	protected void startUp() throws Exception
	{
		log.info("Gauntlet Buddy started!");
		overlayManager.add(warningOverlay);
		overlayManager.add(prayerOverlay);
	}

	@Override
	protected void shutDown() throws Exception
	{
		log.info("Gauntlet Buddy stopped!");
		overlayManager.remove(warningOverlay);
	}

	private void triggerWarning()
	{
		warningOverlay.trigger();
	}

	private void haltWarning()
	{
		warningOverlay.halt();
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		if (gameStateChanged.getGameState() == GameState.LOGGED_IN)
		{
			client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Tracking Items : " + config.trackItems(), null);
		}
		AttackStyleType prayStyle = AttackStyleType.RANGE;
		int hitsLanded = 0;
		haltWarning();
	}

	AttackStyleType prayStyle = AttackStyleType.RANGE;
	int hitsLanded = 0;
	boolean DEBUG = false;

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

			// Logging line to identify animation IDs if DEBUG is true
			if (DEBUG) client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Animation ID : " + animId, null);

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
						// After 5 hits the next will trigger a prayer swap so log a warning message and flash the screen
						// TODO : Config options for this
						if (hitsLanded == 5) {
							client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "USE ALTERNATE ATTACK STYLE",
									null);
							triggerWarning();
						} else if (hitsLanded == 6) {
							// 6th hit will have rolled prayers over so reset the count and stop the warning flash
							hitsLanded = 0;
							haltWarning();
						}
						// Chat log line for the count of hits landed
						client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Hits landed : " +
								Integer.toString(hitsLanded), null);
					}
				}
			}
			// Handle Hunllef animations since it tells what attack style is going to be used
		} else if (actor instanceof NPC) {
			// Make sure it's the Hunllef
			NPC npc = (NPC) actor;
			String npcName = npc.getName();

			if (npcName.contains("Hunllef")) {
				int animId = npc.getAnimation();
				//8754 attack style changed
				//8755 alternate attack style changed
				// TODO : Map to the proper swap magic -> ranged or ranged -> magic for each anim ID
				if (animId == 8754 || animId == 8755) {
					if (prayStyle == AttackStyleType.RANGE) {
						prayStyle = AttackStyleType.MAGIC;
					} else {
						prayStyle = AttackStyleType.RANGE;
					}
					prayerOverlay.setPrayer(prayStyle);
					client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "PRAY : " + prayStyle, null);
				}
				if (DEBUG) client.addChatMessage(ChatMessageType.GAMEMESSAGE, "", "Animation ID : " + animId, null);
			}
		}
	}

	@Provides
	GauntletBuddyConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(GauntletBuddyConfig.class);
	}
}
