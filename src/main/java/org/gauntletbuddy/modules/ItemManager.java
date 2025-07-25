package org.gauntletbuddy.modules;

import lombok.Getter;
import lombok.Setter;
import org.gauntletbuddy.config.GauntletBuddyConfig;
import org.gauntletbuddy.config.types.GauntletItem;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;

@Singleton
public class ItemManager {
    @Inject
    private GauntletBuddyConfig config;
    private static final EnumMap<GauntletItem, Resource> gatheredResourceMap = new EnumMap<>(GauntletItem.class);
    //TODO Load resources from config in manual and calculated mode
    private static final EnumMap<GauntletItem, Resource> requiredResourceMap = new EnumMap<>(GauntletItem.class);

    public static int getResourceCount(GauntletItem item) {
        return gatheredResourceMap.get(item).getCount();
    }

    public static void updateResourceCount(GauntletItem item, int count) {
        gatheredResourceMap.get(item).updateCount(count);
    }

    public void init() {
        for (GauntletItem item : GauntletItem.getGAUNTLET_ITEMS()) {
            Resource gathered = new Resource(item);
            gatheredResourceMap.put(item, gathered);
        }
    }

    public void reset() {
        for (GauntletItem item : GauntletItem.getGAUNTLET_ITEMS()) {
            gatheredResourceMap.get(item).setCount(0);
        }
    }

    @Getter
    private static class Resource {
        @Setter
        private int count = 0;
        private final GauntletItem item;

        private Resource(GauntletItem item) {
            this.item = item;
        }

        public void updateCount(final int count) {
            this.count += count;
        }
    }
}