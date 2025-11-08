package org.gauntletbuddy.modules;

import lombok.Getter;
import lombok.Setter;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.overlay.infobox.InfoBox;
import net.runelite.client.ui.overlay.infobox.InfoBoxManager;
import org.gauntletbuddy.GauntletBuddy;
import org.gauntletbuddy.config.GauntletBuddyConfig;
import org.gauntletbuddy.config.types.GauntletItem;
import org.gauntletbuddy.config.types.GearTierType;
import org.gauntletbuddy.config.types.SpecificationModeType;
import org.gauntletbuddy.config.types.TrackingModeType;
import org.gauntletbuddy.utility.GauntletItemMapper;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;

@Singleton
public final class  ItemTracker {
    private final GauntletBuddyConfig config;
    private final GauntletBuddy gauntletBuddy;
    private final ItemManager itemManager;
    private final InfoBoxManager infoBoxManager;

    private final EnumMap<GauntletItem, Resource> gatheredResourceMap = new EnumMap<>(GauntletItem.class);

    @Getter
    private TrackingModeType itemTrackingMode;
    @Getter
    private boolean hideCompleted;

    @Inject
    public ItemTracker(final GauntletBuddyConfig config, final GauntletBuddy gauntletBuddy,
                       final ItemManager itemManager, final InfoBoxManager infoBoxManager) {
        this.config = config;
        this.gauntletBuddy = gauntletBuddy;
        this.itemManager = itemManager;
        this.infoBoxManager = infoBoxManager;
        this.itemTrackingMode = config.itemTrackingMode();
        this.hideCompleted = config.hideCompleted();
    }

    public int getResourceCount(GauntletItem item) {
        return gatheredResourceMap.get(item).getGathered();
    }

    public void updateResourceCount(GauntletItem item, int count) {
        gatheredResourceMap.get(item).updateGathered(count);
    }

    public void updateCalculatedTarget(String configKey, String newTarget, String oldTarget) {
        GauntletItem newItem = GauntletItemMapper.calculatedFromKey(configKey, newTarget);
        // Handle paddlefish as a direct update
        if (newItem.equals(GauntletItem.PADDLEFISH)) {
            setResourceTargetFromItem(newItem, Integer.parseInt(newTarget));
            return;
        }
        // Other updates require figuring out the difference between the new and old target to calculate the new value
        EnumMap<GauntletItem, Integer> resourceDelta = new EnumMap<>(GauntletItem.class);
        // If the changed item is Enigol potions there's no need to pull the old item separately
        if (newItem.equals(GauntletItem.ENIGOL_POTION)) {
            incrementItemComponents(GauntletItem.ENIGOL_POTION, Integer.parseInt(newTarget), resourceDelta);
            incrementItemComponents(GauntletItem.ENIGOL_POTION, -Integer.parseInt(oldTarget), resourceDelta);
            for (Map.Entry<GauntletItem, Integer> entry : resourceDelta.entrySet()) {
                Resource targetResource = gatheredResourceMap.get(entry.getKey());
                setResourceTargetFromItem(entry.getKey(), targetResource.getTarget() + entry.getValue());
            }
            return;
        }
        // Lastly handle equipment items where the old item is different and needs to be pulled
        GauntletItem oldItem = GauntletItemMapper.calculatedFromKey(configKey, oldTarget);
    }

    public void updateSpecificTarget(String configKey, int target) {
        GauntletItem targetItem = GauntletItemMapper.specifiedFromKey(configKey);
        setResourceTargetFromItem(targetItem, target);
    }

    private void setResourceTargetFromItem(GauntletItem item, int target) {
        Resource targetResource = gatheredResourceMap.get(item);
        targetResource.setTarget(target);
        targetResource.updateGathered(0);
    }

    public void init() {
        for (GauntletItem item : GauntletItem.getGAUNTLET_ITEMS()) {
            BufferedImage resourceImage = itemManager.getImage(item.getItemIds()[0]);
            Resource gathered = new Resource(config, resourceImage, infoBoxManager, gauntletBuddy);
            gatheredResourceMap.put(item, gathered);
        }
        this.itemTrackingMode = config.itemTrackingMode();
        this.hideCompleted = config.hideCompleted();
        setTargets();
    }

    public void reset() {
        for (GauntletItem item : gatheredResourceMap.keySet()) {
            Resource resource = gatheredResourceMap.get(item);
            resource.destroyOverlayBox();
        }
        gatheredResourceMap.clear();
    }

    public void setTargets() {
        EnumMap<GauntletItem, Integer> configTargets = loadConfigTargets();

        for (Map.Entry<GauntletItem, Integer> entry : configTargets.entrySet()) {
            Resource targetResource = gatheredResourceMap.get(entry.getKey());
            targetResource.setTarget(entry.getValue());
            targetResource.createResourceBox();
        }
    }

    private EnumMap<GauntletItem, Integer> loadConfigTargets() {
        EnumMap<GauntletItem, Integer> configCounts = new EnumMap<>(GauntletItem.class);
        if (config.itemTrackerSpecificationMode() == SpecificationModeType.MANUAL) {
            configCounts.put(GauntletItem.ORE, config.specifiedCrystalOre());
            configCounts.put(GauntletItem.BARK, config.specifiedPhrenBark());
            configCounts.put(GauntletItem.LINUM, config.specifiedLinumTirinium());
            configCounts.put(GauntletItem.GRYM_LEAF, config.specifiedGrymLeaf());
            configCounts.put(GauntletItem.WEAPON_FRAME, config.specifiedWeaponFrames());
            configCounts.put(GauntletItem.CRYSTAL_SHARDS, config.specifiedCrystalShards());
            configCounts.put(GauntletItem.PADDLEFISH, config.specifiedPaddlefish());
            configCounts.put(GauntletItem.BOW_STRING, config.specifiedBowString() ? 1 : 0);
            configCounts.put(GauntletItem.ORB, config.specifiedOrb() ? 1 : 0);
            configCounts.put(GauntletItem.SPIKE, config.specifiedSpike() ? 1 : 0);
        } else {
            ArrayList<GauntletItem> gearPieces = new ArrayList<>();
            GearTierType helmetTier = config.calculatedHelmetTier();
            if (helmetTier != GearTierType.None) gearPieces.add(GauntletItem.valueOf("HELMET_" + helmetTier.toString()));
            GearTierType chestTier = config.calculatedBodyTier();
            if (chestTier != GearTierType.None) gearPieces.add(GauntletItem.valueOf("CHEST_" + chestTier.toString()));
            GearTierType legTier = config.calculatedLegsTier();
            if (legTier != GearTierType.None) gearPieces.add(GauntletItem.valueOf("LEGS_" + legTier.toString()));
            GearTierType bowTier = config.calculatedBowTier();
            if (bowTier != GearTierType.None) gearPieces.add(GauntletItem.valueOf("BOW_" + bowTier.toString()));
            GearTierType staffTier = config.calculatedStaffTier();
            if (staffTier != GearTierType.None) gearPieces.add(GauntletItem.valueOf("STAFF_" + staffTier.toString()));
            GearTierType halberdTier = config.calculatedHalberdTier();
            if (halberdTier != GearTierType.None) gearPieces.add(GauntletItem.valueOf("HALBERD_" + halberdTier.toString()));

            for (GauntletItem item : gearPieces) {
                incrementItemComponents(item, configCounts);
            }

            incrementItemComponents(GauntletItem.ENIGOL_POTION, config.calculatedPotions(), configCounts);

            configCounts.put(GauntletItem.PADDLEFISH, config.calculatedPaddlefish());
        }
        return configCounts;
    }

    private void incrementItemComponents(GauntletItem item, EnumMap<GauntletItem, Integer> counter) {
        incrementItemComponents(item, 1, counter);
    }

    private void incrementItemComponents(GauntletItem item, int count, EnumMap<GauntletItem, Integer> counter) {
        if (item == null) return;
        for (Map.Entry<GauntletItem, Integer> entry : item.getComponents().entrySet()) {
            GauntletItem componentKey = entry.getKey();
            int componentCount = entry.getValue() * count;
            if (componentKey.isCraftable()) {
                incrementItemComponents(componentKey, componentCount, counter);
            } else {
                counter.merge(componentKey, componentCount, Integer::sum);
            }
        }
    }

    private static class Resource {
        @Getter
        private int gathered = 0;
        @Setter @Getter
        private int target = 0;
        private final GauntletBuddyConfig config;
        private ResourceOverlayBox resourceBox;
        private final BufferedImage resourceImage;
        private final InfoBoxManager infoBoxManager;
        private final GauntletBuddy gauntletBuddy;

        private Resource(GauntletBuddyConfig config, BufferedImage resourceImage, InfoBoxManager infoBoxManager, GauntletBuddy gauntletBuddy) {
            this.config = config;
            this.resourceImage = resourceImage;
            this.infoBoxManager = infoBoxManager;
            this.gauntletBuddy = gauntletBuddy;
        }

        private void createResourceBox() {
            if (resourceBox == null) {
                resourceBox = new ResourceOverlayBox(resourceImage, gauntletBuddy, this);
                infoBoxManager.addInfoBox(resourceBox);
            }
        }

        private void destroyOverlayBox() {
            if (resourceBox != null) {
                infoBoxManager.removeInfoBox(resourceBox);
                resourceBox = null;
            }
        }

        public void updateGathered(int gathered) {
            this.gathered += gathered;

            if (this.gathered < target) {
                if (resourceBox == null) {
                    createResourceBox();
                } else {
                    resourceBox.setColor(Color.WHITE);
                }
            }

            if (this.gathered >= target && (config.itemTrackingMode() == TrackingModeType.COUNTDOWN || config.hideCompleted())) {
                destroyOverlayBox();
            } else if (this.gathered >= target) {
                resourceBox.setColor(Color.GRAY);
            }
        }

        public int getInfoText() {
            if (config.itemTrackingMode() == TrackingModeType.COUNTDOWN) return target - gathered;
            return gathered;
        }
    }

    private static class ResourceOverlayBox extends InfoBox
    {
        private final Resource resource;
        @Setter
        private Color color = Color.WHITE;
        private ResourceOverlayBox(final BufferedImage resourceImage, GauntletBuddy gauntletBuddy, Resource resource)
        {
            super(resourceImage, gauntletBuddy);
            this.resource = resource;
        }

        @Override
        public String getText() {
            return String.valueOf(this.resource.getInfoText());
        }

        @Override
        public Color getTextColor() {
            return color;
        }
    }
}