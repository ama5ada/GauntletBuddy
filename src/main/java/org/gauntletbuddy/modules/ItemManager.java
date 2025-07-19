package org.gauntletbuddy.modules;

import lombok.Getter;
import lombok.Setter;
import org.gauntletbuddy.config.GauntletBuddyConfig;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.runelite.api.gameval.ItemID;

import static java.util.Map.entry;

@Singleton
public class ItemManager {
    @Inject
    private GauntletBuddyConfig config;
    @Getter
    private static final List<String> resources = Arrays.asList("Ore", "Bark", "Linum", "Grym Leaf", "Weapon Frame",
            "Paddlefish", "Shards", "Bow String", "Orb", "Spike");
    private static final Map<String, GatheredResource> gatheredResourceMap = new HashMap<>();
    private static int Linum = ItemID.GAUNTLET_TELEPORT_CRYSTAL_HM;
    //TODO Convert resources to enums
    @Getter
    private static final Map<Integer, String> idToResource = Map.ofEntries(
            entry(ItemID.GAUNTLET_CRYSTAL_SHARD_HM, "Shards"),
            entry(ItemID.GAUNTLET_CRYSTAL_SHARD_HM_2, "Shards"),
            entry(ItemID.GAUNTLET_CRYSTAL_SHARD_HM_3, "Shards"),
            entry(ItemID.GAUNTLET_CRYSTAL_SHARD_HM_4, "Shards"),
            entry(ItemID.GAUNTLET_CRYSTAL_SHARD_HM_5, "Shards"),
            entry(ItemID.GAUNTLET_CRYSTAL_SHARD_HM_25, "Shards"),
            entry(ItemID.GAUNTLET_CRYSTAL_SHARD, "Shards"),
            entry(ItemID.GAUNTLET_MELEE_COMPONENT_HM, "Spike"),
            entry(ItemID.GAUNTLET_MELEE_COMPONENT, "Spike"),
            entry(ItemID.GAUNTLET_RANGED_COMPONENT_HM, "Bow String"),
            entry(ItemID.GAUNTLET_RANGED_COMPONENT, "Bow String"),
            entry(ItemID.GAUNTLET_MAGIC_COMPONENT_HM, "Orb"),
            entry(ItemID.GAUNTLET_MAGIC_COMPONENT, "Orb"),
            entry(ItemID.GAUNTLET_HERB_HM, "Grym Leaf"),
            entry(ItemID.GAUNTLET_HERB, "Grym Leaf"),
            entry(ItemID.GAUNTLET_RAW_FOOD, "Paddlefish"),
            entry(ItemID.GAUNTLET_GENERIC_COMPONENT_HM, "Weapon Frame"),
            entry(ItemID.GAUNTLET_GENERIC_COMPONENT, "Weapon Frame"),
            entry(ItemID.GAUNTLET_ORE_HM, "Ore"),
            entry(ItemID.GAUNTLET_ORE, "Ore"),
            entry(ItemID.GAUNTLET_BARK_HM, "Bark"),
            entry(ItemID.GAUNTLET_BARK, "Bark"),
            entry(ItemID.GAUNTLET_FIBRE_HM, "Linum"),
            entry(ItemID.GAUNTLET_FIBRE, "Linum")
    );

    public static int getResourceCount(String resource) {
        if (gatheredResourceMap.containsKey(resource)) return gatheredResourceMap.get(resource).getCount();
        return 0;
    }

    public static void updateResourceCount(String resource, int count) {
        gatheredResourceMap.get(resource).updateCount(count);
    }

    public void init() {
        for (String resource : resources) {
            GatheredResource gathered = new GatheredResource(resource);
            gatheredResourceMap.put(resource, gathered);
        }
    }

    public void reset() {
        for (String resource : resources) {
            gatheredResourceMap.get(resource).setCount(0);
        }
    }

    @Getter
    private static class GatheredResource {
        @Setter
        private int count = 0;
        private final String itemName;

        private GatheredResource(String resource) {
            this.itemName = resource;
        }

        public void updateCount(final int count) {
            this.count += count;
        }
    }
}