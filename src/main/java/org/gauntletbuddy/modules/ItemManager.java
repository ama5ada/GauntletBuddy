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
    private static final EnumMap<GauntletItem, GatheredResource> gatheredResourceMap = new EnumMap<>(GauntletItem.class);

    public static int getResourceCount(GauntletItem item) {
        return gatheredResourceMap.get(item).getCount();
    }

    public static void updateResourceCount(GauntletItem item, int count) {
        gatheredResourceMap.get(item).updateCount(count);
    }

    public void init() {
        for (GauntletItem item : GauntletItem.getGAUNTLET_ITEMS()) {
            GatheredResource gathered = new GatheredResource(item);
            gatheredResourceMap.put(item, gathered);
        }
    }

    public void reset() {
        for (GauntletItem item : GauntletItem.getGAUNTLET_ITEMS()) {
            gatheredResourceMap.get(item).setCount(0);
        }
    }

    @Getter
    private static class GatheredResource {
        @Setter
        private int count = 0;
        private final GauntletItem item;

        private GatheredResource(GauntletItem item) {
            this.item = item;
        }

        public void updateCount(final int count) {
            this.count += count;
        }
    }
}