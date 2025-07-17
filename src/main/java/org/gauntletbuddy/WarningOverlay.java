package org.gauntletbuddy;

import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.api.Client;
import org.gauntletbuddy.config.GauntletBuddyConfig;

import javax.inject.Inject;
import java.awt.*;

public class WarningOverlay extends Overlay {
    private final Client client;
    private final GauntletBuddyConfig config;
    private final GauntletBuddy plugin;

    private long lastFlash = 0;
    private final int flashIntervalMS = 500;
    private boolean flash = false;

    @Inject
    public WarningOverlay(Client client, GauntletBuddyConfig config, GauntletBuddy plugin)
    {
        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.ABOVE_WIDGETS);
        this.client = client;
        this.config = config;
        this.plugin = plugin;
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        if (!plugin.isInside() || !plugin.isBossing() || !config.hunllefPrayerSwapAlert() || !(plugin.getHitsLanded() == 5)) return null;

        long now = System.currentTimeMillis();
        long elapsed = now - lastFlash;

        if (elapsed > flashIntervalMS)
        {
            lastFlash = now;
            flash = !flash;
        }

        if (!flash) return null;

        Canvas canvas = client.getCanvas();
        Point offset = canvas.getLocation();
        graphics.setColor(new Color(255, 0, 0, 50));
        graphics.fillRect(offset.x, offset.y, canvas.getWidth(), canvas.getHeight());

        return null;
    }
}
