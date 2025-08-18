package org.gauntletbuddy.modules;

import net.runelite.api.*;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.events.PostMenuSort;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
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

    private HashMap<Integer, Integer> itemCounts = new HashMap<>();

    @Override
    public void start()
    {
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
                        instanceTileUtil.addPoint(local, world, client);
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
        // TODO Skip most calculations in the spawn room since people frequently bank items there and crafting occurs there
        // Exception to this rule is crafting teleport crystals which should add shards to the required total
        itemCounts = updateItemCounts();
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

                if (item.isCraftable() && item != GauntletItem.TELEPORT_CRYSTAL) refundComponents(item, diff);
                itemTracker.updateResourceCount(item, diff);
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
