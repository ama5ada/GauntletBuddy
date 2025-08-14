package org.gauntletbuddy.overlays;

import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.Point;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import org.gauntletbuddy.config.GauntletBuddyConfig;
import org.gauntletbuddy.config.types.TrackingModeType;
import org.gauntletbuddy.modules.HunllefModule;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.awt.*;

@Singleton
public class CounterOverlay extends Overlay {
    private final HunllefModule hunllefModule;
    private final GauntletBuddyConfig config;
    private final Client client;

    @Inject
    public CounterOverlay(final GauntletBuddyConfig config, final HunllefModule hunllefModule, final Client client)
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
        if (config.hunllefHitTrackerMode() == TrackingModeType.OFF) return null;

        NPC hunllef = hunllefModule.getHunllef();
        if (hunllef == null || hunllef.isDead()) return null;

        int displayHits = hunllefModule.getHitsLanded();
        if (config.hunllefHitTrackerMode() == TrackingModeType.COUNTDOWN) displayHits = 5 - displayHits;
        String counterString = Integer.toString(displayHits);

        Point textLoc = hunllef.getCanvasTextLocation(graphics, counterString, 0);

        if (textLoc != null)
        {
            graphics.setFont(FontManager.getRunescapeFont());
            graphics.setColor(Color.YELLOW);
            int textWidth = graphics.getFontMetrics().stringWidth(counterString);
            int textHeight = graphics.getFontMetrics().getHeight();
            graphics.drawString(counterString, textLoc.getX() - (textWidth / 2), textLoc.getY() + (textHeight / 2));
        }

        return null;
    }
}
