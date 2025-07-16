package org.gauntletbuddy;

import net.runelite.api.Client;
import net.runelite.api.widgets.Widget;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import org.gauntletbuddy.config.types.AttackStyleType;

import javax.inject.Inject;
import java.awt.*;

public class PrayerOverlay extends Overlay
{
    private final Client client;
    @Inject
    public PrayerOverlay(Client client)
    {
        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.ABOVE_WIDGETS);
        this.client = client;
    }

    // Default to protect from missiles
    private int prayerId = 22;

    public void setPrayer(AttackStyleType newPrayer)
    {
        if (newPrayer == AttackStyleType.MAGIC)
        {
            // Protect from Magic prayer ID
            prayerId = 21;
        } else {
            prayerId = 22;
        }
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        Widget prayerGroupWidget = client.getWidget(541, prayerId);
        if (prayerGroupWidget != null)
        {
            Rectangle bounds = prayerGroupWidget.getBounds();
            int x = bounds.x;
            int y = bounds.y;
            int width = bounds.width;
            int height = bounds.height;
            graphics.setColor(new Color(0, 255, 255, 80));
            graphics.setStroke(new BasicStroke(3.0f));
            graphics.drawRect(x, y, width, height);
        }
        return null;
    }
}
