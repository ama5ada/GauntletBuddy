package org.gauntletbuddy;

import net.runelite.api.Client;
import net.runelite.api.Point;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;
import org.gauntletbuddy.config.GauntletBuddyConfig;
import org.gauntletbuddy.config.types.AttackStyleType;

import java.util.LinkedHashMap;

import javax.inject.Inject;
import java.awt.*;
import java.util.Map;

public class DebugOverlay extends Overlay
{
    private final GauntletBuddy plugin;
    private final GauntletBuddyConfig config;
    private final Client client;
    private final Map<AttackStyleType, Integer> prayToID = Map.of(
            AttackStyleType.MAGIC , 21,
            AttackStyleType.RANGE , 22);

    @Inject
    public DebugOverlay(GauntletBuddy plugin, Client client, GauntletBuddyConfig config)
    {
        setPosition(OverlayPosition.TOP_LEFT);
        setLayer(OverlayLayer.ABOVE_WIDGETS);
        this.plugin = plugin;
        this.client = client;
        this.config = config;
    }

    @Override
    public Dimension render(Graphics2D graphics)
    {
        if (!config.debugList()) return null;
        LinkedHashMap<String, Object> fieldValues = new LinkedHashMap<>();

        fieldValues.put("Player Prayer Style", plugin.getPrayStyle());
        fieldValues.put("Hits Landed on Hunllef", plugin.getHitsLanded());
        fieldValues.put("Inside", plugin.isInside());
        fieldValues.put("Bossing", plugin.isBossing());
        fieldValues.put("Gathered Ore", plugin.getGatheredOre());
        fieldValues.put("Gathered Bark", plugin.getGatheredBark());
        fieldValues.put("Gathered Linen", plugin.getGatheredLinen());
        fieldValues.put("Gathered Frames", plugin.getGatheredFrames());
        fieldValues.put("Gathered Shards", plugin.getGatheredShards());
        fieldValues.put("Gathered Fish", plugin.getGatheredFish());
        fieldValues.put("Has Bowstring", plugin.isHasString());
        fieldValues.put("Has Orb", plugin.isHasOrb());
        fieldValues.put("Has Spike", plugin.isHasSpike());

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
