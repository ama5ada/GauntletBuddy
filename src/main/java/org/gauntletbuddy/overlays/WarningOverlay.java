package org.gauntletbuddy.overlays;

import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.api.Client;
import org.gauntletbuddy.config.GauntletBuddyConfig;
import org.gauntletbuddy.modules.HunllefModule;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.awt.*;

@Singleton
public class WarningOverlay extends Overlay
{
    private final HunllefModule hunllefModule;
    private final GauntletBuddyConfig config;
    private final Client client;

    private long lastFlash = 0;
    private final int flashIntervalMS = 500;
    private boolean flash = false;

    @Inject
    public WarningOverlay(final Client client, final GauntletBuddyConfig config, final HunllefModule hunllefModule)
    {
        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.ABOVE_WIDGETS);
        this.hunllefModule = hunllefModule;
        this.client = client;
        this.config = config;
    }

    @Override
    public Dimension render(final Graphics2D graphics)
    {
        if (!config.hunllefPrayerSwapAlert() || !(hunllefModule.getHitsLanded() == 5)) return null;

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
