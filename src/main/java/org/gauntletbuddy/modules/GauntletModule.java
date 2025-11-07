package org.gauntletbuddy.modules;

import net.runelite.api.*;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.*;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import org.gauntletbuddy.GauntletBuddy;
import org.gauntletbuddy.config.GauntletBuddyConfig;
import org.gauntletbuddy.config.types.GauntletItem;
import org.gauntletbuddy.config.types.SpecificationModeType;
import org.gauntletbuddy.utility.InstanceTileUtil;
import static net.runelite.api.ItemID.RAW_PADDLEFISH;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;

/**
 * TODO - resource highlighting, resource minimap icons
 * TODO - npc highlighting, npc minimap icons
 * TODO - add further support for changing item tracking config settings mid run or enabling the plugin mid run
 */
@Singleton
public final class GauntletModule implements PluginModule {
    @Inject
    private Client client;
    @Inject
    private EventBus eventBus;
    @Inject
    private ItemTracker itemTracker;
    @Inject
    private InstanceTileUtil instanceTileUtil;
    @Inject
    private GauntletBuddyConfig config;
    @Inject
    private GauntletBuddy gauntletBuddy;
    @Inject
    private ClientThread clientThread;

    private HashMap<Integer, Integer> itemCounts = new HashMap<>();
    private HashMap<Integer, Integer> groundItems = new HashMap<>();
    private HashMap<Integer, Integer> bankedItems = new HashMap<>();

    private WorldArea spawnRoomArea;
    private final WorldPoint CORRUPTED_SPAWN_COORDINATE = new WorldPoint(1970, 5666, 1);
    private final WorldPoint NORMAL_SPAWN_COORDINATE = new WorldPoint(1906, 5666, 1);

    /**
     * Set up constants that depend on which Gauntlet is being run
     */
    private void setConstants() {
        if (gauntletBuddy.isCorrupted()) {
            spawnRoomArea = new WorldArea(CORRUPTED_SPAWN_COORDINATE, 12, 12);
        } else {
            spawnRoomArea = new WorldArea(NORMAL_SPAWN_COORDINATE, 12, 12);
        }
    }

    @Override
    public void start()
    {
        setConstants();
        itemTracker.init();
        eventBus.register(this);
        clientThread.invokeLater(this::initializeInventories);
    }

    @Override
    public void stop()
    {
        eventBus.unregister(this);
        itemTracker.reset();
        itemCounts.clear();
    }

    /**
     * Standalone method that removes the ability to enter the boss room voluntarily if the player
     * has not cooked all the food gathered in the gauntlet yet (common and annoying mistake)
     * @param postMenuSort - Unused, simply triggers the event
     */
    @Subscribe
    public void onPostMenuSort(PostMenuSort postMenuSort) {
        if (!config.mustCookFish()) return;
        final ItemContainer container = client.getItemContainer(InventoryID.INVENTORY);
        if (container == null) return;

        boolean hasRawFish = false;
        for (Item item : container.getItems()) {
            if (item.getId() == RAW_PADDLEFISH) {
                hasRawFish = true;
                break;
            }
        }

        if (!hasRawFish) return;

        ArrayList<MenuEntry> filteredEntries = new ArrayList<>();

        for (MenuEntry entry : client.getMenuEntries()) {
            if (entry.getOption().equals("Quick-pass") || entry.getOption().equals("Pass")) continue;
            filteredEntries.add(entry);
        }

        MenuEntry[] filteredArray = new MenuEntry[filteredEntries.size()];
        client.setMenuEntries(filteredEntries.toArray(filteredArray));
    }

    /**
     * Whenever an Item Container changes updated itemCounts to match the current state of player inventory/equipment
     * @param itemContainerChanged - Unused, simply triggers the event
     */
    @Subscribe
    public void onItemContainerChanged(ItemContainerChanged itemContainerChanged) {
        itemCounts = updateItemCounts();
    }

    /**
     * Event fires when a new item appears on the ground, passes event information to a helper method that handles
     * ground item events
     * @param itemSpawned - Contains ItemID, WorldPoint, and Quantity
     */
    @Subscribe
    public void onItemSpawned(ItemSpawned itemSpawned) {
        updateGroundItems(itemSpawned.getItem().getId(), itemSpawned.getTile().getWorldLocation(), itemSpawned.getItem().getQuantity());
    }

    /**
     * Event fires when an item disappears from the ground, passes event information to a helper method that handles
     * ground item events
     * @param itemDespawned - Contains ItemID, WorldPoint, and Quantity
     */
    @Subscribe
    public void onItemDespawned(ItemDespawned itemDespawned) {
        updateGroundItems(itemDespawned.getItem().getId(), itemDespawned.getTile().getWorldLocation(), itemDespawned.getItem().getQuantity() * -1);
    }

    /**
     * Process updates to the config that concern the item tracker
     * @param configChanged - Contains information about the config fields that are updated
     */
    @Subscribe
    public void onConfigChanged(ConfigChanged configChanged) {
        // Make sure the config value being changed belongs to this plugin
        if (!configChanged.getGroup().equals(GauntletBuddyConfig.CONFIG_GROUP)) return;
        String key = configChanged.getKey();

        // Immediately update the item target if the specification mode matches
        if (key.startsWith("specified") && config.itemTrackerSpecificationMode() == SpecificationModeType.MANUAL) {
            itemTracker.updateSpecificTarget(key, Integer.valueOf(configChanged.getNewValue()));
        } else if (key.startsWith("calculated") &&
                config.itemTrackerSpecificationMode() == SpecificationModeType.CALCULATED) {

        }

        switch (key) {
            case "centerBanking":
                boolean centerBanking = Boolean.parseBoolean(configChanged.getNewValue());
                if (centerBanking) {
                    countGroundItems();
                } else {
                    clearBankedItems();
                }
                break;
            case "specificationMode":
                itemTracker.reset();
                itemTracker.init();
                break;
            default:
                break;
        }
    }

    /**
     * Helper method to handle process ground items, spawn vs despawn is handled by negating quantity using the same logic
     * @param itemID - int representing the ItemID of the spawned item
     * @param eventLocation - WorldPoint where the item spawned
     * @param diff - Quantity of item showing up on the ground, negative if the item is disappearing (being picked up)
     */
    private void updateGroundItems(int itemID, WorldPoint eventLocation, int diff) {
        // TODO - Add support for enabling the plugin mid run with center banking enabled
        // Mark a set of WorldPoints within the spawn to be checked for items
        // The first time those tiles are rendered count all their items
        WorldPoint playerLocation = client.getLocalPlayer().getWorldLocation();
        // If center banking is off or the item is not dropped at spawn skip these calculations
        // Also, if the event location is not under the player ignore it -
        // Player dropped or picked up items are always on the player location
        if (is_in_spawn(eventLocation) && eventLocation.equals(playerLocation)) {
            if (config.centerBanking()) {
                bankedItems.put(itemID, bankedItems.getOrDefault(itemID, 0) + diff);
                updateSingleTrackerCount(itemID, diff);
            } else {
                groundItems.put(itemID, groundItems.getOrDefault(itemID, 0) + diff);
            }
        }
    }

    /**
     * Helper method to determine if a given WorldPoint is within the spawn room
     * @param checkLocation - WorldPoint an event occurs at
     * @return - T/F is the checkLocation within the spawn room
     */
    private boolean is_in_spawn(WorldPoint checkLocation) {
        WorldPoint trueTile = instanceTileUtil.getTrueTile(checkLocation);
        if (trueTile == null) return false;
        return trueTile.isInArea(spawnRoomArea);
    }

    /**
     * Helper method to process the items in a container into one collective count
     * @param updatedCounts - Map to collect item counts across containers
     * @param container - Container whose items are being added to the map
     */
    private void collectUpdatedCounts(HashMap<Integer, Integer> updatedCounts, ItemContainer container) {
        for (Item item : container.getItems()) {
            updatedCounts.put(item.getId(), updatedCounts.getOrDefault(item.getId(), 0) + item.getQuantity());
        }
    }

    /**
     * Method that updates the Map tracking the state of player inventories to match the current player inventories
     * Also handles batched updates to the Item Tracker so the overlay displays updated information
     * @return - Map representing the state of player inventories after processing updates
     */
    private HashMap<Integer, Integer> updateItemCounts() {
        HashMap<Integer, Integer> updatedCounts = new HashMap<>();

        ItemContainer inventory = client.getItemContainer(InventoryID.INVENTORY);
        ItemContainer equipment = client.getItemContainer(InventoryID.EQUIPMENT);

        if (inventory == null || equipment == null) return itemCounts;

        collectUpdatedCounts(updatedCounts, inventory);
        collectUpdatedCounts(updatedCounts, equipment);

        HashMap<Integer, Integer> missingCounts = new HashMap<>(itemCounts);
        HashMap<Integer, Integer> countDiffs = new HashMap<>();

        for (Map.Entry<Integer, Integer> entry : updatedCounts.entrySet()) {
            int itemId = entry.getKey();
            int itemCount = entry.getValue();
            GauntletItem.itemFromId(itemId).ifPresent(item -> {
                int diff = itemCount;

                if (itemCounts.containsKey(itemId)) {
                    diff -= itemCounts.get(itemId);
                    missingCounts.remove(itemId);
                }

                countDiffs.put(itemId, diff);
            });
        }

        updateBatchedTrackerCounts(countDiffs, 1);
        updateBatchedTrackerCounts(missingCounts, -1);

        return updatedCounts;
    }

    /**
     * Helper method to translate Gauntlet Items into their base components and update the tracker
     * @param parent
     * @param diff
     */
    private void refundComponents(GauntletItem parent, int diff) {
        if (diff == 0) return;
        Map<GauntletItem, Integer> itemComponents = parent.getComponents();
        for (Map.Entry<GauntletItem, Integer>entry : itemComponents.entrySet()) {
            GauntletItem component = entry.getKey();
            int componentCount = entry.getValue();
            updateSingleTrackerCount(component, componentCount * diff);
        }
    }

    /**
     * Helper method to load current player inventory and equipment into item tracker on plugin load
     * Uses ClientThread to process the player inventory on the next available tick for consistency
     */
    private void initializeInventories() {
        itemCounts = updateItemCounts();
    }

    /**
     * Helper method to reinitialize item tracker on config change
     */
    private void updateTracker() {

    }

    /**
     * Helper method to remove banked ground items from tracker counts on config change
     */
    private void clearBankedItems() {
        updateBatchedTrackerCounts(bankedItems, -1);
        groundItems = bankedItems;
        bankedItems.clear();
    }

    /**
     * Helper method to add ground items which are now banked to the tracker counts on config change
     */
    private void countGroundItems() {
        bankedItems = groundItems;
        groundItems.clear();
        updateBatchedTrackerCounts(bankedItems, 1);
    }

    /**
     * Helper method to update the count of a single item in the tracker
     * @param itemID - ItemID that's being updated
     * @param diff - Amount to update the item count by
     */
    private void updateSingleTrackerCount(int itemID, int diff) {
        GauntletItem.itemFromId(itemID).ifPresent(item -> {
            itemTracker.updateResourceCount(item, diff);
            if (item.isCraftable() && item != GauntletItem.TELEPORT_CRYSTAL) {
                refundComponents(item, diff);
            }
        });
    }

    /**
     * Overloaded helper method accepts a direct Gauntlet Item rather than Item ID
     * @param gauntletItem - Gauntlet Item that's being updated
     * @param diff - Amount to update the item count by
     */
    private void updateSingleTrackerCount(GauntletItem gauntletItem, int diff) {
        itemTracker.updateResourceCount(gauntletItem, diff);
        if (gauntletItem.isCraftable() && gauntletItem != GauntletItem.TELEPORT_CRYSTAL) {
            refundComponents(gauntletItem, diff);
        }
    }

    /**
     * Helper method to process maps of item counts because inventory changes frequently remove or add multiple items
     * @param counts - Map that stores the ItemIDs and amounts to update the tracker
     * @param modifier - Sometimes all the item counts should decrement rather than add to count
     */
    private void updateBatchedTrackerCounts(HashMap<Integer, Integer> counts, int modifier) {
        for (Map.Entry<Integer, Integer> entry : counts.entrySet()) {
            int itemID = entry.getKey();
            int itemCount = modifier * entry.getValue();
            updateSingleTrackerCount(itemID, itemCount);
        }
    }
}
