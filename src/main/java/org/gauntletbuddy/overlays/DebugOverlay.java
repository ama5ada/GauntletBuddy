package org.gauntletbuddy.overlays;

import net.runelite.api.Client;
import net.runelite.api.Point;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;
import org.gauntletbuddy.GauntletBuddy;
import org.gauntletbuddy.config.GauntletBuddyConfig;
import org.gauntletbuddy.config.types.AttackStyleType;
import org.gauntletbuddy.config.types.GauntletItem;
import org.gauntletbuddy.modules.HunllefModule;
import org.gauntletbuddy.modules.ItemTracker;
import org.gauntletbuddy.modules.TornadoTracker;

import java.util.LinkedHashMap;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.awt.*;
import java.util.Map;

@Singleton
public class DebugOverlay extends Overlay
{
    private final GauntletBuddy plugin;
    private final HunllefModule hunllefModule;
    private final GauntletBuddyConfig config;
    private final ItemTracker itemTracker;
    private final Client client;
    private final Map<AttackStyleType, Integer> prayToID = Map.of(
            AttackStyleType.MAGIC , 21,
            AttackStyleType.RANGE , 22);
    private final TornadoTracker tornadoTracker;

    @Inject
    public DebugOverlay(final GauntletBuddy plugin, final Client client, final GauntletBuddyConfig config,
                        final HunllefModule hunllefModule, final ItemTracker itemTracker, TornadoTracker tornadoTracker)
    {
        this.tornadoTracker = tornadoTracker;
        setPosition(OverlayPosition.TOP_CENTER);
        setLayer(OverlayLayer.UNDER_WIDGETS);
        this.hunllefModule = hunllefModule;
        this.itemTracker = itemTracker;
        this.plugin = plugin;
        this.client = client;
        this.config = config;
    }

    @Override
    public Dimension render(final Graphics2D graphics)
    {
        if (!config.debugList()) return null;
        LinkedHashMap<String, Object> fieldValues = new LinkedHashMap<>();

        fieldValues.put("Inside", plugin.isInside());
        fieldValues.put("Bossing", plugin.isBossing());
        fieldValues.put("Tracking Mode", itemTracker.getItemTrackingMode());
        fieldValues.put("Specification Mode", itemTracker.getItemSpecificationMode());
        fieldValues.put("Hide Completed Items", itemTracker.isHideCompleted());

        if (plugin.isInside() && !plugin.isBossing())
        {
            fieldValues.put("Corrupted", plugin.isCorrupted());
            for (GauntletItem item : GauntletItem.getGAUNTLET_ITEMS())
            {
                int itemCount = itemTracker.getResourceCount(item);
                if (itemCount == 0 && config.hideMissingItems()) continue;
                fieldValues.put(item.toString(), itemTracker.getResourceCount(item));
            }
        }

        if (plugin.isBossing())
        {
            fieldValues.put("Player Prayer Style", hunllefModule.getPrayStyle());
            fieldValues.put("Hits Landed on Hunllef", hunllefModule.getHitsLanded());
            fieldValues.put("Current Hunllef Prayer", hunllefModule.getCurrentHunllefPrayer());
            fieldValues.put("Previous Hunllef Prayer", hunllefModule.getPreviousHunllefPrayer());
        }

        graphics.setFont(new Font("Arial", Font.PLAIN, 12));
        int y = 15;
        for (Map.Entry<String, Object> entry : fieldValues.entrySet())
        {
            String text = entry.getKey() + " : " + entry.getValue();
            OverlayUtil.renderTextLocation(graphics, new Point(10, y), text, Color.WHITE);
            y += 15;
        }
        return null;
    }
}
