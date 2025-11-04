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
    @Getter
    private SpecificationModeType itemSpecificationMode;

    @Inject
    public ItemTracker(final GauntletBuddyConfig config, final GauntletBuddy gauntletBuddy,
                       final ItemManager itemManager, final InfoBoxManager infoBoxManager) {
        this.config = config;
        this.gauntletBuddy = gauntletBuddy;
        this.itemManager = itemManager;
        this.infoBoxManager = infoBoxManager;
        this.itemSpecificationMode = config.itemTrackerSpecificationMode();
        this.itemTrackingMode = config.itemTrackingMode();
        this.hideCompleted = config.hideCompleted();
    }

    public int getResourceCount(GauntletItem item) {
        return gatheredResourceMap.get(item).getGathered();
    }

    public void updateResourceCount(GauntletItem item, int count) {
        gatheredResourceMap.get(item).updateGathered(count);
    }

    public void init() {
        for (GauntletItem item : GauntletItem.getGAUNTLET_ITEMS()) {
            BufferedImage resourceImage = itemManager.getImage(item.getItemIds()[0]);
            Resource gathered = new Resource(resourceImage, infoBoxManager, gauntletBuddy, this);
            gatheredResourceMap.put(item, gathered);
        }
        this.itemSpecificationMode = config.itemTrackerSpecificationMode();
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
            if (entry.getValue() > targetResource.getGathered()) targetResource.createResourceBox();
        }
    }

    private EnumMap<GauntletItem, Integer> loadConfigTargets() {
        EnumMap<GauntletItem, Integer> configCounts = new EnumMap<>(GauntletItem.class);
        if (this.itemSpecificationMode == SpecificationModeType.MANUAL) {
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
            GearTierType helmetTier = config.calculatorHelmetTier();
            if (helmetTier != GearTierType.None) gearPieces.add(GauntletItem.valueOf("HELMET_" + helmetTier.toString()));
            GearTierType chestTier = config.calculatorBodyTier();
            if (chestTier != GearTierType.None) gearPieces.add(GauntletItem.valueOf("CHEST_" + chestTier.toString()));
            GearTierType legTier = config.calculatorLegsTier();
            if (legTier != GearTierType.None) gearPieces.add(GauntletItem.valueOf("LEGS_" + legTier.toString()));
            GearTierType bowTier = config.calculatorBowTier();
            if (bowTier != GearTierType.None) gearPieces.add(GauntletItem.valueOf("BOW_" + bowTier.toString()));
            GearTierType staffTier = config.calculatorStaffTier();
            if (staffTier != GearTierType.None) gearPieces.add(GauntletItem.valueOf("STAFF_" + staffTier.toString()));
            GearTierType halberdTier = config.calculatorHalberdTier();
            if (halberdTier != GearTierType.None) gearPieces.add(GauntletItem.valueOf("HALBERD_" + halberdTier.toString()));

            for (GauntletItem item : gearPieces) {
                incrementItemComponents(item, configCounts);
            }

            incrementItemComponents(GauntletItem.ENIGOL_POTION, config.calculatorPotions(), configCounts);

            configCounts.put(GauntletItem.PADDLEFISH, config.calculatorPaddlefish());
        }
        return configCounts;
    }

    private void incrementItemComponents(GauntletItem item, EnumMap<GauntletItem, Integer> counter) {
        incrementItemComponents(item, 1, counter);
    }

    private void incrementItemComponents(GauntletItem item, int count, EnumMap<GauntletItem, Integer> counter) {
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
        @Setter
        private int target = 0;
        private ResourceOverlayBox resourceBox;
        private final BufferedImage resourceImage;
        private final InfoBoxManager infoBoxManager;
        private final GauntletBuddy gauntletBuddy;
        private final ItemTracker itemTracker;

        private Resource(BufferedImage resourceImage, InfoBoxManager infoBoxManager, GauntletBuddy gauntletBuddy, ItemTracker itemTracker) {
            this.resourceImage = resourceImage;
            this.infoBoxManager = infoBoxManager;
            this.gauntletBuddy = gauntletBuddy;
            this.itemTracker = itemTracker;
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

            if (this.gathered >= target && (itemTracker.getItemTrackingMode() == TrackingModeType.COUNTDOWN || itemTracker.isHideCompleted())) {
                destroyOverlayBox();
            } else if (this.gathered >= target) {
                resourceBox.setColor(Color.GRAY);
            }
        }

        public int getInfoText() {
            if (itemTracker.getItemTrackingMode() == TrackingModeType.COUNTDOWN) return target - gathered;
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