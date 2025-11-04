package org.gauntletbuddy.config.types;

import lombok.Getter;
import net.runelite.api.gameval.ItemID;

import java.util.*;

public enum GauntletItem {
    CRYSTAL_SHARDS("Crystal Shards", new int[]{ItemID.GAUNTLET_CRYSTAL_SHARD_HM, ItemID.GAUNTLET_CRYSTAL_SHARD}, false),
    WEAPON_FRAME("Weapon Frame", new int[]{ItemID.GAUNTLET_GENERIC_COMPONENT_HM, ItemID.GAUNTLET_GENERIC_COMPONENT}, false),
    ORB("Orb", new int[]{ItemID.GAUNTLET_MAGIC_COMPONENT_HM, ItemID.GAUNTLET_MAGIC_COMPONENT}, false),
    SPIKE("Spike", new int[]{ItemID.GAUNTLET_MELEE_COMPONENT_HM, ItemID.GAUNTLET_MELEE_COMPONENT}, false),
    BOW_STRING("Bowstring", new int[]{ItemID.GAUNTLET_RANGED_COMPONENT_HM, ItemID.GAUNTLET_RANGED_COMPONENT}, false),
    PADDLEFISH("Paddlefish", new int[]{ItemID.GAUNTLET_RAW_FOOD, ItemID.GAUNTLET_FOOD}, false),
    ORE("Ore", new int[]{ItemID.GAUNTLET_ORE_HM, ItemID.GAUNTLET_ORE}, false),
    BARK("Bark", new int[]{ItemID.GAUNTLET_BARK_HM, ItemID.GAUNTLET_BARK}, false),
    LINUM("Linum", new int[]{ItemID.GAUNTLET_FIBRE_HM, ItemID.GAUNTLET_FIBRE}, false),
    GRYM_LEAF("Grym Leaf", new int[]{ItemID.GAUNTLET_HERB_HM, ItemID.GAUNTLET_HERB}, false),
    // Craftable Items (Made up of one or more base gauntlet resources)
    VIAL("Empty Vial", new int[]{ItemID.GAUNTLET_VIAL_EMPTY_HM, ItemID.GAUNTLET_VIAL_EMPTY, ItemID.GAUNTLET_VIAL_WATER}, true),
    UNFINISHED_POTION("Unfinished Potion", new int[]{ItemID.GAUNTLET_POTION_UNFINISHED}, true),
    ENIGOL_POTION("Enigol Potion", new int[]{ItemID.GAUNTLET_POTION_1, ItemID.GAUNTLET_POTION_2,
            ItemID.GAUNTLET_POTION_3, ItemID.GAUNTLET_POTION_4},  true),
    CRUSHED_SHARDS("Crushed Shards", new int[]{ItemID.GAUNTLET_CRYSTAL_SHARD_CRUSHED_HM, ItemID.GAUNTLET_CRYSTAL_SHARD}, true),
    TELEPORT_CRYSTAL("Teleport Crystal", new int[]{ItemID.GAUNTLET_TELEPORT_CRYSTAL_HM, ItemID.GAUNTLET_TELEPORT_CRYSTAL}, true),
    HELMET_T1("Helmet (basic)", new int[]{ItemID.GAUNTLET_HELMET_T1_HM, ItemID.GAUNTLET_HELMET_T1}, true),
    HELMET_T2("Helmet (attuned)", new int[]{ItemID.GAUNTLET_HELMET_T2_HM, ItemID.GAUNTLET_HELMET_T2}, true),
    HELMET_T3("Helmet (perfected)", new int[]{ItemID.GAUNTLET_HELMET_T3_HM, ItemID.GAUNTLET_HELMET_T3}, true),
    CHEST_T1("Chest (basic)", new int[]{ItemID.GAUNTLET_CHESTPLATE_T1_HM, ItemID.GAUNTLET_CHESTPLATE_T1}, true),
    CHEST_T2("Chest (attuned)", new int[]{ItemID.GAUNTLET_CHESTPLATE_T2_HM, ItemID.GAUNTLET_CHESTPLATE_T2}, true),
    CHEST_T3("Chest (perfected)", new int[]{ItemID.GAUNTLET_CHESTPLATE_T3_HM, ItemID.GAUNTLET_CHESTPLATE_T3}, true),
    LEGS_T1("Legs (basic)", new int[]{ItemID.GAUNTLET_PLATELEGS_T1_HM, ItemID.GAUNTLET_PLATELEGS_T1}, true),
    LEGS_T2("Legs (attuned)", new int[]{ItemID.GAUNTLET_PLATELEGS_T2_HM, ItemID.GAUNTLET_PLATELEGS_T2}, true),
    LEGS_T3("Legs (perfected)", new int[]{ItemID.GAUNTLET_PLATELEGS_T3_HM, ItemID.GAUNTLET_PLATELEGS_T3}, true),
    BOW_T1("Bow (basic)", new int[]{ItemID.GAUNTLET_RANGED_T1_HM, ItemID.GAUNTLET_RANGED_T1}, true),
    BOW_T2("Bow (attuned)", new int[]{ItemID.GAUNTLET_RANGED_T2_HM, ItemID.GAUNTLET_RANGED_T2}, true),
    BOW_T3("Bow (perfected)", new int[]{ItemID.GAUNTLET_RANGED_T3_HM, ItemID.GAUNTLET_RANGED_T3}, true),
    STAFF_T1("Staff (basic)", new int[]{ItemID.GAUNTLET_MAGIC_T1_HM, ItemID.GAUNTLET_MAGIC_T1}, true),
    STAFF_T2("Staff (attuned)", new int[]{ItemID.GAUNTLET_MAGIC_T2_HM, ItemID.GAUNTLET_MAGIC_T2}, true),
    STAFF_T3("Staff (perfected)", new int[]{ItemID.GAUNTLET_MAGIC_T3_HM, ItemID.GAUNTLET_MAGIC_T3}, true),
    HALBERD_T1("Halberd (basic)", new int[]{ItemID.GAUNTLET_MELEE_T1_HM, ItemID.GAUNTLET_MELEE_T1}, true),
    HALBERD_T2("Halberd (attuned)", new int[]{ItemID.GAUNTLET_MELEE_T2_HM, ItemID.GAUNTLET_MELEE_T2}, true),
    HALBERD_T3("Halberd (perfected)", new int[]{ItemID.GAUNTLET_MELEE_T3_HM, ItemID.GAUNTLET_MELEE_T3}, true);


    @Getter
    private static final GauntletItem[] GAUNTLET_ITEMS = GauntletItem.values();
    private static final Map<Integer, GauntletItem> ID_TO_ITEM = new HashMap<>();

    static
    {
        for (GauntletItem gauntletItem : values())
        {
            for (int itemId : gauntletItem.itemIds)
            {
                ID_TO_ITEM.put(itemId, gauntletItem);
            }
        }

        // Initialize component costs for misc crafted items
        VIAL.components = new EnumMap<>(Map.ofEntries(
                Map.entry(CRYSTAL_SHARDS, 10)
        ));
        UNFINISHED_POTION.components = new EnumMap<>(Map.ofEntries(
                Map.entry(VIAL, 1),
                Map.entry(GRYM_LEAF, 1)
        ));
        ENIGOL_POTION.components = new EnumMap<>(Map.ofEntries(
                Map.entry(UNFINISHED_POTION, 1),
                Map.entry(CRUSHED_SHARDS, 10)
        ));
        CRUSHED_SHARDS.components = new EnumMap<>(Map.ofEntries(
                Map.entry(CRYSTAL_SHARDS, 1)
        ));
        TELEPORT_CRYSTAL.components = new EnumMap<>(Map.ofEntries(
                Map.entry(CRYSTAL_SHARDS, 50)
        ));

        // Component costs for armor items
        GauntletItem[] T1_ARMOR = { HELMET_T1, CHEST_T1, LEGS_T1 };
        Map.Entry[] T1_ARMOR_COSTS = { Map.entry(ORE, 1), Map.entry(BARK, 1), Map.entry(LINUM, 1),
                Map.entry(CRYSTAL_SHARDS, 40)};
        addComponents(T1_ARMOR, T1_ARMOR_COSTS);

        GauntletItem[] T2_FILLERS = { HELMET_T2, LEGS_T2 };
        Map.Entry[] T2_FILLER_COSTS = { Map.entry(ORE, 1), Map.entry(BARK, 1), Map.entry(LINUM, 1),
                Map.entry(CRYSTAL_SHARDS, 60), Map.entry(HELMET_T1, 1)};
        addComponents(T2_FILLERS, T2_FILLER_COSTS);

        GauntletItem[] T3_FILLERS = { HELMET_T3, LEGS_T3 };
        Map.Entry[] T3_FILLER_COSTS = { Map.entry(ORE, 2), Map.entry(BARK, 2), Map.entry(LINUM, 2),
                Map.entry(CRYSTAL_SHARDS, 80), Map.entry(HELMET_T2, 1)};
        addComponents(T3_FILLERS, T3_FILLER_COSTS);

        // Handle T2 and T3 chests separately since they do not share costs with helmet and legs
        CHEST_T2.components = new EnumMap<>(Map.ofEntries(Map.entry(ORE, 2), Map.entry(BARK, 2),
                Map.entry(LINUM, 2), Map.entry(CRYSTAL_SHARDS, 60), Map.entry(CHEST_T1, 1)));
        CHEST_T3.components = new EnumMap<>(Map.ofEntries(Map.entry(ORE, 2), Map.entry(BARK, 2),
                Map.entry(LINUM, 2), Map.entry(CRYSTAL_SHARDS, 80), Map.entry(CHEST_T2, 1)));

        GauntletItem[] T1_WEAPONS = { BOW_T1, STAFF_T1, HALBERD_T1 };
        Map.Entry[] T1_WEAPON_COSTS = { Map.entry(WEAPON_FRAME, 1), Map.entry(CRYSTAL_SHARDS, 20) };
        addComponents(T1_WEAPONS, T1_WEAPON_COSTS);

        GauntletItem[] T2_T3_WEAPONS = { BOW_T2, STAFF_T2, HALBERD_T2, BOW_T3, STAFF_T3, HALBERD_T3 };
        Map.Entry[] T2_T3_WEAPON_COSTS = { Map.entry(CRYSTAL_SHARDS, 60), Map.entry(BOW_T1, 1) };
        addComponents(T2_T3_WEAPONS, T2_T3_WEAPON_COSTS);

        BOW_T3.components.put(BOW_STRING, 1);
        STAFF_T3.components.put(ORB, 1);
        HALBERD_T3.components.put(SPIKE, 1);
    }

    private static void addComponents(GauntletItem[] items, Map.Entry[] costs) {
        for (GauntletItem item : items) {
            item.components = new EnumMap<GauntletItem, Integer>(Map.ofEntries(
                    costs
            ));
        }
    }

    @Getter
    private final int[] itemIds;
    private final String name;
    @Getter
    private EnumMap<GauntletItem, Integer> components;
    @Getter
    private final boolean craftable;
    @Override
    public String toString() { return this.name; }

    GauntletItem(String name, final int[] itemIds, final boolean craftable)
    {
        this(name, itemIds, craftable,null);
    }

    GauntletItem(String name, final int[] itemIds, final boolean craftable, EnumMap<GauntletItem, Integer> itemComponents)
    {
        this.components = itemComponents;
        this.itemIds = itemIds;
        this.name = name;
        this.craftable = craftable;
    }

    public static Optional<GauntletItem> itemFromId(int itemId)
    {
        return Optional.ofNullable(ID_TO_ITEM.get(itemId));
    }
}

