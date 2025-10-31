package org.gauntletbuddy.modules;

import net.runelite.api.*;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.*;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import org.gauntletbuddy.GauntletBuddy;
import org.gauntletbuddy.config.GauntletBuddyConfig;
import org.gauntletbuddy.config.types.GauntletItem;
import org.gauntletbuddy.utility.InstanceTileUtil;
import static net.runelite.api.ItemID.RAW_PADDLEFISH;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;
import java.util.concurrent.CompletableFuture;

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

    private HashMap<Integer, Integer> itemCounts = new HashMap<>();
    private final HashMap<Integer, Integer> groundItems = new HashMap<>();

    private WorldArea spawnRoomArea;
    private final WorldPoint CORRUPTED_SPAWN_COORDINATE = new WorldPoint(1970, 5666, 1);
    private final WorldPoint NORMAL_SPAWN_COORDINATE = new WorldPoint(1906, 5666, 1);

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
    }

    @Override
    public void stop()
    {
        eventBus.unregister(this);
        itemTracker.reset();
        itemCounts.clear();
    }

    //TODO resource highlighting, resource minimap icons

    @Subscribe
    public void onGameStateChanged(GameStateChanged gameStateChanged) {
        Tile[][][] tiles = client.getScene().getTiles();
        CompletableFuture.runAsync(() -> {
            processTiles(tiles);
        });
    }

    public void processTiles(Tile[][][] tiles) {
        for (Tile[][] value : tiles) {
            for (Tile[] item : value) {
                for (Tile tile : item) {
                    if (tile != null) {
                        // Get LocalLocation (relative to scene base)
                        LocalPoint local = tile.getLocalLocation();
                        // Get WorldPoint (absolute location on the world map)
                        WorldPoint world = tile.getWorldLocation();
                        WorldPoint relative = instanceTileUtil.getTrueTile(world);
                        instanceTileUtil.addPoint(relative, world);
                    }
                }
            }
        }
    }

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

    @Subscribe
    public void onItemContainerChanged(ItemContainerChanged itemContainerChanged) {
        itemCounts = updateItemCounts();
    }

    @Subscribe
    public void onItemSpawned(ItemSpawned itemSpawned) {
        System.out.println("SPAWN EVENT");
        updateGroundItems(itemSpawned.getItem().getId(), itemSpawned.getTile().getWorldLocation(), itemSpawned.getItem().getQuantity());
    }

    @Subscribe
    public void onItemDespawned(ItemDespawned itemDespawned) {
        System.out.println("DESPAWN EVENT");
        updateGroundItems(itemDespawned.getItem().getId(), itemDespawned.getTile().getWorldLocation(), itemDespawned.getItem().getQuantity() * -1);
    }

    /**
     *
     * @param itemID - int representing the itemID of the spawned item
     * @param eventLocation - WorldPoint where the item spawned
     * @param diff - quantity of item showing up on the ground, negative if the item is disappearing (being picked up)
     */
    private void updateGroundItems(int itemID, WorldPoint eventLocation, int diff) {
        // If center banking is off or the item is not dropped at spawn skip these calculations
        // Also, if the event location is not under the player ignore it (dropped or picked up items are always on the player location)
        if (!config.centerBanking() || !is_in_spawn(eventLocation) || !(eventLocation.equals(client.getLocalPlayer().getWorldLocation()))) return;

        GauntletItem.itemFromId(itemID).ifPresent(item -> {
            // Track the ground item counts in a map in case the setting is toggled during a run so this information can be used to reconcile tracked items
            groundItems.put(itemID, groundItems.getOrDefault(itemID, 0) + diff);
            itemTracker.updateResourceCount(item, diff);
            if (item.isCraftable() && item != GauntletItem.TELEPORT_CRYSTAL) {
                refundComponents(item, diff);
            }
        });
    }

    private boolean is_in_spawn(WorldPoint checkLocation) {
        WorldPoint trueTile = instanceTileUtil.getTrueTile(checkLocation);
        if (trueTile == null) return false;
        return trueTile.isInArea(spawnRoomArea);
    }

    private HashMap<Integer, Integer> updateItemCounts() {
        HashMap<Integer, Integer> updatedCounts = new HashMap<>();

        ItemContainer inventory = client.getItemContainer(InventoryID.INVENTORY);
        ItemContainer equipment = client.getItemContainer(InventoryID.EQUIPMENT);

        if (inventory == null || equipment == null) return itemCounts;

        for (Item item : inventory.getItems()) {
            updatedCounts.put(item.getId(), updatedCounts.getOrDefault(item.getId(), 0) + item.getQuantity());
        }

        for (Item item : equipment.getItems()) {
            updatedCounts.put(item.getId(), updatedCounts.getOrDefault(item.getId(), 0) + item.getQuantity());
        }

        Set<Integer> missingKeys = itemCounts.keySet();

        for (Map.Entry<Integer, Integer> entry : updatedCounts.entrySet()) {
            int itemId = entry.getKey();
            int itemCount = entry.getValue();
            GauntletItem.itemFromId(itemId).ifPresent(item -> {
                int diff = itemCount;

                if (itemCounts.containsKey(itemId)) {
                    diff -= itemCounts.get(itemId);
                    missingKeys.remove(itemId);
                }

                itemTracker.updateResourceCount(item, diff);

                if (item.isCraftable() && item != GauntletItem.TELEPORT_CRYSTAL) {
                        refundComponents(item, diff);
                }
            });
        }

        if (!missingKeys.isEmpty()) {
            for (int itemId : missingKeys) {
                int diff = -itemCounts.get(itemId);
                GauntletItem.itemFromId(itemId).ifPresent( item -> {
                    if (item.isCraftable() && item != GauntletItem.TELEPORT_CRYSTAL) refundComponents(item, diff);
                    itemTracker.updateResourceCount(item, diff);
                });
            }
        }

        return updatedCounts;
    }

    private void refundComponents(GauntletItem parent, int diff) {
        if (diff == 0) return;
        Map<GauntletItem, Integer> itemComponents = parent.getComponents();
        for (Map.Entry<GauntletItem, Integer>entry : itemComponents.entrySet()) {
            GauntletItem component = entry.getKey();
            int componentCount = entry.getValue();
            itemTracker.updateResourceCount(component, componentCount * diff);
        }
    }
}
