package org.gauntletbuddy.modules;

import lombok.Getter;
import lombok.Setter;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.overlay.infobox.InfoBox;
import net.runelite.client.ui.overlay.infobox.InfoBoxManager;
import org.gauntletbuddy.GauntletBuddy;
import org.gauntletbuddy.config.GauntletBuddyConfig;
import org.gauntletbuddy.config.types.GauntletItem;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;

@Singleton
public class ItemTracker {
    @Inject
    private GauntletBuddyConfig config;
    @Inject
    private GauntletBuddy gauntletBuddy;
    @Inject
    private ItemManager itemManager;
    @Inject
    private InfoBoxManager infoBoxManager;
    private static final EnumMap<GauntletItem, Resource> gatheredResourceMap = new EnumMap<>(GauntletItem.class);
    //TODO Load resources from config in manual and calculated mode

    public static int getResourceCount(GauntletItem item) {
        return gatheredResourceMap.get(item).getCount();
    }

    public static void updateResourceCount(GauntletItem item, int count) {
        gatheredResourceMap.get(item).updateCount(count);
    }

    public void init() {
        for (GauntletItem item : GauntletItem.getGAUNTLET_ITEMS()) {
            BufferedImage resourceImage = itemManager.getImage(item.getItemIds()[0]);
            Resource gathered = new Resource(item, resourceImage, infoBoxManager, gauntletBuddy);
            gatheredResourceMap.put(item, gathered);
        }
    }

    public void reset() {
        for (GauntletItem item : gatheredResourceMap.keySet()) {
            Resource resource = gatheredResourceMap.get(item);
            resource.removeOverlayBox();
        }
        gatheredResourceMap.clear();
    }

    @Getter
    private static class Resource {
        @Setter
        private int count = 0;
        private final GauntletItem item;
        private final InfoBox resourceBox;
        private final BufferedImage resourceImage;
        private final InfoBoxManager infoBoxManager;

        private Resource(GauntletItem item, BufferedImage resourceImage, InfoBoxManager infoBoxManager, GauntletBuddy gauntletBuddy) {
            this.item = item;
            this.resourceImage = resourceImage;
            this.infoBoxManager = infoBoxManager;
            this.resourceBox = new ResourceOverlayBox(resourceImage, gauntletBuddy, this);
            infoBoxManager.addInfoBox(this.resourceBox);
        }

        public void updateCount(final int count) {
            this.count += count;
        }

        public void removeOverlayBox() {
            infoBoxManager.removeInfoBox(this.resourceBox);
        }
    }

    private static class ResourceOverlayBox extends InfoBox
    {
        private final Resource resource;
        private Color color = Color.white;
        private ResourceOverlayBox(final BufferedImage resourceImage, GauntletBuddy gauntletBuddy, Resource resource)
        {
            super(resourceImage, gauntletBuddy);
            this.resource = resource;
        }

        @Override
        public String getText() {
            return String.valueOf(this.resource.getCount());
        }

        @Override
        public Color getTextColor() {
            return color;
        }
    }
}