package org.gauntletbuddy;

import com.google.inject.Provides;
import javax.inject.Inject;

import lombok.Getter;
import lombok.Setter;
import net.runelite.api.*;
import net.runelite.api.events.*;
import net.runelite.api.widgets.InterfaceID;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import org.gauntletbuddy.config.GauntletBuddyConfig;
import org.gauntletbuddy.modules.GauntletModule;
import org.gauntletbuddy.modules.HunllefModule;
import org.gauntletbuddy.overlays.DebugOverlay;
import org.gauntletbuddy.overlays.TimerOverlay;

import java.time.Instant;

import static net.runelite.api.gameval.VarbitID.*;

@PluginDescriptor(
		name = "Gauntlet Buddy",
		description = "Helper plugin for The Gauntlet",
		tags = {"combat","the","gauntlet"}
)

public final class GauntletBuddy extends Plugin
{
	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Provides
	GauntletBuddyConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(GauntletBuddyConfig.class);
	}

	@Inject
	private HunllefModule hunllefModule;

	@Inject
	private GauntletModule gauntletModule;

	//TODO Total time in gauntlet overlay

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private DebugOverlay debugOverlay;

    @Inject
    private TimerOverlay timerOverlay;

	@Override
	protected void startUp()
	{
		resetVars();
		overlayManager.add(debugOverlay);
        overlayManager.add(timerOverlay);

		// Do not check if the player is inside if the plugin doesn't start while the player is logged in
		if (client.getGameState() != GameState.LOGGED_IN)
		{
			return;
		}

		clientThread.invoke(() -> {
			if (client.getVarbitValue(GAUNTLET_BOSS_STARTED) != 0)
			{
				inside = true;
				bossing = true;
				hunllefModule.start();
			}
			else if (client.getVarbitValue(GAUNTLET_START) != 0)
			{
				inside = true;
				corrupted = client.getVarbitValue(GAUNTLET_CORRUPTED) != 0;
				gauntletModule.start();
			}
		});
	}

	@Override
	protected void shutDown()
	{
		overlayManager.remove(debugOverlay);
        overlayManager.remove(timerOverlay);
		hunllefModule.stop();
		gauntletModule.stop();
	}

	@Getter
	private boolean bossing = false;
	@Getter
	private boolean inside = false;
	@Getter
	private boolean corrupted = false;
    @Getter
    private long mazeStart = -1;
    @Getter
    private long bossStart = -1;

	/**
	 * Method to initialize all starting values on plugin load
	 */
	private void resetVars()
	{
		bossing = false;
		inside = false;
        corrupted = false;
        mazeStart = -1;
        bossStart = -1;
	}

	@Subscribe
	void onGameStateChanged(GameStateChanged gameStateChanged)
	{
		boolean current_status = client.getVarbitValue(GAUNTLET_START) != 0;
		if (inside)
		{
			// If marked as inside but current_status is false (now outside)
			if (!current_status)
			{
				resetVars();
				gauntletModule.stop();
				hunllefModule.stop();
			}
		} else if (current_status) {
			// If not marked as inside but current_status is true (now inside) start plugins
			inside = true;
			corrupted = client.getVarbitValue(GAUNTLET_CORRUPTED) != 0;
			gauntletModule.start();
		}
	}

    @Subscribe
    void onWidgetLoaded(final WidgetLoaded event)
    {
        if (event.getGroupId() == InterfaceID.GAUNTLET_TIMER) mazeStart = Instant.now().getEpochSecond();
    }

	@Subscribe
	void onGameTick(GameTick gameTick)
	{
		// Early exit if not inside the gauntlet or already in boss no checks need to be run
		if (!inside || bossing) return;
		// If inside but not bossing check if the boss has been started in the last game tick to start boss checks
		bossing = client.getVarbitValue(GAUNTLET_BOSS_STARTED) != 0;
		if (bossing) {
			hunllefModule.start();
			gauntletModule.stop();
            bossStart = Instant.now().getEpochSecond();
		}
	}
}
