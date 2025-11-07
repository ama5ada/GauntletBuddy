package org.gauntletbuddy.utility;

import org.gauntletbuddy.config.types.GauntletItem;

import java.util.Map;

public class GauntletItemMapper {
    private GauntletItemMapper(){}

    private static final Map<String, GauntletItem> SPECIFIED_KEY_TO_ITEM = Map.ofEntries(
            Map.entry("specifiedCrystalOre", GauntletItem.ORE),
            Map.entry("specifiedPhrenBark", GauntletItem.BARK),
            Map.entry("specifiedLinumTirinium", GauntletItem.LINUM),
            Map.entry("specifiedGrymLeaf", GauntletItem.GRYM_LEAF),
            Map.entry("specifiedWeaponFrames", GauntletItem.WEAPON_FRAME),
            Map.entry("specifiedCrystalShards", GauntletItem.CRYSTAL_SHARDS),
            Map.entry("specifiedPaddlefish", GauntletItem.PADDLEFISH),
            Map.entry("specifiedBowString", GauntletItem.BOW_STRING),
            Map.entry("specifiedOrb", GauntletItem.ORB),
            Map.entry("specifiedSpike", GauntletItem.SPIKE)
    );

    public static GauntletItem specifiedFromKey(String configKey)
    {
        return SPECIFIED_KEY_TO_ITEM.get(configKey);
    }
}
