package org.gauntletbuddy;

import net.runelite.api.Client;
import net.runelite.api.widgets.Widget;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import org.gauntletbuddy.config.GauntletBuddyConfig;
import org.gauntletbuddy.config.types.AttackStyleType;

import javax.inject.Inject;
import java.awt.*;
import java.util.Map;

public class PrayerOverlay extends Overlay
{
    private final GauntletBuddy plugin;
    private final GauntletBuddyConfig config;
    private final Client client;
    private final Map<AttackStyleType, Integer> prayToID = Map.of(
            AttackStyleType.MAGIC , 21,
            AttackStyleType.RANGE , 22);

    @Inject
    public PrayerOverlay(GauntletBuddy plugin, Client client, GauntletBuddyConfig config)
    {
        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.ABOVE_WIDGETS);
        this.plugin = plugin;
        this.client = client;
        this.config = config;
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        if (!plugin.isInside() || !plugin.isBossing() || !config.prayerHighlight()) return null;

        Widget prayerGroupWidget = client.getWidget(541, prayToID.get(plugin.getPrayStyle()));
        if (prayerGroupWidget != null)
        {
            Rectangle bounds = prayerGroupWidget.getBounds();
            graphics.setColor(new Color(0, 255, 255, 100));
            graphics.setStroke(new BasicStroke(3.0f));
            graphics.draw(bounds);
        }
        return null;
    }
}
