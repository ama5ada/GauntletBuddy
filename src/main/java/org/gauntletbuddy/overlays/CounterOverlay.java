package org.gauntletbuddy.overlays;

import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.Point;
import net.runelite.api.WorldView;
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
        if (!config.hunllefHitDisplay()) return null;
        WorldView worldView = client.getTopLevelWorldView();

        for (NPC npc : worldView.npcs())
        {
            if (npc == null || npc.getName() == null) continue;
            if (!npc.getName().contains("Hunllef")) continue;
            int displayHits = hunllefModule.getHitsLanded();
            if (config.hunllefHitCountMode() == TrackingModeType.COUNTDOWN) displayHits = 5 - displayHits;
            String counterString = Integer.toString(displayHits);
            Point textLoc = npc.getCanvasTextLocation(graphics, counterString, 0);
            if (textLoc != null)
            {
                graphics.setFont(new Font("Arial", Font.BOLD, 16));
                graphics.setColor(Color.YELLOW);
                graphics.drawString(counterString, textLoc.getX(), textLoc.getY());
            }
        }
        return null;
    }
}
