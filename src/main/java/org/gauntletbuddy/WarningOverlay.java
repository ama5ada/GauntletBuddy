package org.gauntletbuddy;

import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.api.Client;

import javax.inject.Inject;
import java.awt.*;

public class WarningOverlay extends Overlay {
    private boolean flash = false;
    private boolean visible = true;
    private long lastFlash = 0;
    private final int flashIntervalMS = 500;
    private final Client client;


    @Inject
    public WarningOverlay(Client client)
    {
        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.ABOVE_WIDGETS);
        this.client = client;
    }

    public void trigger()
    {
        flash = true;
        lastFlash = System.currentTimeMillis();
    }

    public void halt()
    {
        flash = false;
        visible = false;
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        if (!flash) return null;

        long now = System.currentTimeMillis();
        long elapsed = now - lastFlash;

        if (elapsed > flashIntervalMS)
        {
            visible = !visible;
            lastFlash = now;
        }

        if (!visible) return null;

        Point offset = client.getCanvas().getLocation();
        graphics.setColor(new Color(255, 0, 0, 50));
        graphics.fillRect(-offset.x, -offset.y, client.getCanvas().getWidth(), client.getCanvas().getHeight());

        return null;
    }
}
