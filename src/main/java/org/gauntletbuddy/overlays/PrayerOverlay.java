package org.gauntletbuddy.overlays;

import net.runelite.api.Client;
import net.runelite.api.widgets.Widget;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import org.gauntletbuddy.config.GauntletBuddyConfig;
import org.gauntletbuddy.config.types.AttackStyleType;
import org.gauntletbuddy.config.types.PrayerHighlightModeType;
import org.gauntletbuddy.modules.HunllefModule;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.awt.*;
import java.util.Map;

import static net.runelite.api.gameval.VarbitID.PRAYER_PROTECTFROMMAGIC;
import static net.runelite.api.gameval.VarbitID.PRAYER_PROTECTFROMMISSILES;

@Singleton
public class PrayerOverlay extends Overlay
{
    private final HunllefModule hunllefModule;
    private final GauntletBuddyConfig config;
    private final Client client;
    private final Map<AttackStyleType, Integer> prayToID = Map.of(
            AttackStyleType.MAGIC , 21,
            AttackStyleType.RANGE , 22);

    @Inject
    public PrayerOverlay(final Client client, final GauntletBuddyConfig config, final HunllefModule hunllefModule)
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
        if (config.hunllefPrayerHighlightMode() == PrayerHighlightModeType.OFF) return null;

        Widget prayerGroupWidget = client.getWidget(541, prayToID.get(hunllefModule.getPrayStyle()));

        if (prayerGroupWidget != null)
        {
            // IN_TAB mode means that when the prayer tab is closed the highlight should not show
            if (config.hunllefPrayerHighlightMode() == PrayerHighlightModeType.IN_TAB && prayerGroupWidget.isHidden()) return null;
            // MISMATCH mode means that the highlight should only appear when the players set prayer is not correct
            if (config.hunllefPrayerHighlightMode() == PrayerHighlightModeType.MISMATCH) {
                if (client.getVarbitValue(PRAYER_PROTECTFROMMAGIC) == 1 &&
                        hunllefModule.getPrayStyle() == AttackStyleType.MAGIC) return null;
                if (client.getVarbitValue(PRAYER_PROTECTFROMMISSILES) == 1 &&
                        hunllefModule.getPrayStyle() == AttackStyleType.RANGE) return null;
            }
            Rectangle bounds = prayerGroupWidget.getBounds();
            graphics.setColor(new Color(0, 255, 255, 100));
            graphics.setStroke(new BasicStroke(3.0f));
            graphics.draw(bounds);
        }
        return null;
    }
}
