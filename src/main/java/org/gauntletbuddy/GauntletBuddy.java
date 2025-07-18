package org.gauntletbuddy;

import com.google.inject.Provides;
import javax.inject.Inject;

import lombok.Getter;
import net.runelite.api.*;
import net.runelite.api.events.*;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import org.gauntletbuddy.config.GauntletBuddyConfig;
import org.gauntletbuddy.modules.GauntletModule;
import org.gauntletbuddy.modules.HunllefModule;
import org.gauntletbuddy.overlays.DebugOverlay;

import static net.runelite.api.gameval.VarbitID.GAUNTLET_BOSS_STARTED;
import static net.runelite.api.gameval.VarbitID.GAUNTLET_START;

@PluginDescriptor(
		name = "Gauntlet Buddy",
		description = "Helper plugin for The Gauntlet",
		tags = {"combat","the","gauntlet"}
)

public final class GauntletBuddy extends Plugin
{
	@Inject
	private Client client;

	@Provides
	GauntletBuddyConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(GauntletBuddyConfig.class);
	}

	@Inject
	private HunllefModule hunllefModule;

	@Inject
	private GauntletModule gauntletModule;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private DebugOverlay debugOverlay;

	@Override
	protected void startUp()
	{
		initVars();
		overlayManager.add(debugOverlay);
	}

	@Override
	protected void shutDown()
	{
		overlayManager.remove(debugOverlay);
		hunllefModule.stop();
		gauntletModule.stop();
	}

	@Getter
	private boolean bossing = false;
	@Getter
	private boolean inside = false;

	/**
	 * Method to initialize all starting values on plugin load
	 */
	private void initVars()
	{
		bossing = false;
		inside = false;
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		if (gameStateChanged.getGameState() == GameState.LOGGED_IN)
		{
			inside = client.getVarbitValue(GAUNTLET_START) != 0;
			if (!inside) {
				gauntletModule.stop();
				hunllefModule.stop();
			} else {
				gauntletModule.start();
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
		if (bossing) {
			hunllefModule.start();
			gauntletModule.stop();
		}
	}
}
