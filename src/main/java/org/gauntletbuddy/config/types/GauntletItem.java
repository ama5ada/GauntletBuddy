package org.gauntletbuddy.config.types;

import lombok.Getter;
import net.runelite.api.gameval.ItemID;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public enum GauntletItem {
    CRYSTAL_SHARDS("Crystal Shards", new int[]{ItemID.GAUNTLET_CRYSTAL_SHARD_HM, ItemID.GAUNTLET_CRYSTAL_SHARD}),
    SPIKE("Spike", new int[]{ItemID.GAUNTLET_MELEE_COMPONENT_HM, ItemID.GAUNTLET_MELEE_COMPONENT}),
    BOW_STRING("Bowstring", new int[]{ItemID.GAUNTLET_RANGED_COMPONENT_HM, ItemID.GAUNTLET_RANGED_COMPONENT}),
    ORB("Orb", new int[]{ItemID.GAUNTLET_MAGIC_COMPONENT_HM, ItemID.GAUNTLET_MAGIC_COMPONENT}),
    GRYM_LEAF("Grym Leaf", new int[]{ItemID.GAUNTLET_HERB_HM, ItemID.GAUNTLET_HERB}),
    PADDLEFISH("Paddlefish", new int[]{ItemID.GAUNTLET_RAW_FOOD, ItemID.GAUNTLET_FOOD}),
    WEAPON_FRAME("Weapon Frame", new int[]{ItemID.GAUNTLET_GENERIC_COMPONENT_HM, ItemID.GAUNTLET_GENERIC_COMPONENT}),
    ORE("Ore", new int[]{ItemID.GAUNTLET_ORE_HM, ItemID.GAUNTLET_ORE}),
    BARK("Bark", new int[]{ItemID.GAUNTLET_BARK_HM, ItemID.GAUNTLET_BARK}),
    LINUM("Linum", new int[]{ItemID.GAUNTLET_FIBRE_HM, ItemID.GAUNTLET_FIBRE}),
    TELEPORT_CRYSTAL("Teleport Crystal", new int[]{ItemID.GAUNTLET_TELEPORT_CRYSTAL_HM, ItemID.GAUNTLET_TELEPORT_CRYSTAL});

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
    }

    private final int[] itemIds;
    private final String name;

    GauntletItem(String name, final int[] itemIds)
    {
        this.itemIds = itemIds;
        this.name = name;
    }

    public static Optional<GauntletItem> itemFromId(int itemId)
    {
        return Optional.ofNullable(ID_TO_ITEM.get(itemId));
    }

    @Override
    public String toString()
    {
        return this.name;
    }
}

