package org.gauntletbuddy.modules;

import net.runelite.api.Client;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import org.gauntletbuddy.config.types.GauntletItem;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;

@Singleton
public final class GauntletModule implements PluginModule {
    @Inject
    private Client client;
    @Inject
    private EventBus eventBus;
    @Inject
    private ItemManager itemManager;

    private HashMap<Integer, Integer> inventoryCounts = new HashMap<>();
    private HashMap<Integer, Integer> equipmentCounts = new HashMap<>();

    @Override
    public void start()
    {
        itemManager.init();
        ItemContainer inventory = client.getItemContainer(InventoryID.INVENTORY);
        for (Item item : inventory.getItems()) {
            inventoryCounts.put(item.getId(), inventoryCounts.getOrDefault(item.toString(), 0) + item.getQuantity());
        }
        eventBus.register(this);
    }

    @Override
    public void stop()
    {
        eventBus.unregister(this);
        itemManager.reset();
        inventoryCounts.clear();
    }

    //TODO Better item tracking overlay, resource highlighting, resource minimap icons

    @Subscribe
    public void onItemContainerChanged(ItemContainerChanged itemContainerChanged) {
        //TODO Skip most calculations in the spawn room since people frequently bank items there and crafting occurs there
        // Exception to this rule is crafting teleport crystals which should add shards to the required total
        if (itemContainerChanged.getContainerId() == InventoryID.INVENTORY.getId()) {
            inventoryCounts = updateItemCounts(inventoryCounts, itemContainerChanged.getItemContainer());
        } else if (itemContainerChanged.getContainerId() == InventoryID.EQUIPMENT.getId()) {
            equipmentCounts = updateItemCounts(equipmentCounts, itemContainerChanged.getItemContainer());
        }
    }

    private HashMap<Integer, Integer> updateItemCounts(HashMap<Integer, Integer> targetMap, ItemContainer changed) {
        HashMap<Integer, Integer> updatedCounts = new HashMap<>();

        for (Item item : changed.getItems()) {
            updatedCounts.put(item.getId(), updatedCounts.getOrDefault(item.getId(), 0) + item.getQuantity());
        }

        for (Map.Entry<Integer, Integer> entry : updatedCounts.entrySet()) {
            int itemId = entry.getKey();
            int itemCount = entry.getValue();
            GauntletItem.itemFromId(itemId).ifPresent(item -> {
                int diff = itemCount;

                if (targetMap.containsKey(itemId)) {
                    diff -= targetMap.get(itemId);
                    targetMap.remove(itemId);
                }

                if (item.isCraftable() && item != GauntletItem.TELEPORT_CRYSTAL) refundComponents(item, diff);
                ItemManager.updateResourceCount(item, diff);
            });
        }

        if (!targetMap.isEmpty()) {
            for (Map.Entry<Integer, Integer> entry : targetMap.entrySet()) {
                int itemId = entry.getKey();
                int diff = -entry.getValue();
                GauntletItem.itemFromId(itemId).ifPresent( item -> {
                    if (item.isCraftable() && item != GauntletItem.TELEPORT_CRYSTAL) refundComponents(item, diff);
                    ItemManager.updateResourceCount(item, diff);
                });
            }
        }

        return updatedCounts;
    }

    private void refundComponents(GauntletItem parent, int diff) {
        Map<GauntletItem, Integer> itemComponents = parent.getComponents();
        System.out.println(parent);
        System.out.println(diff);
        for (Map.Entry<GauntletItem, Integer>entry : itemComponents.entrySet()) {
            GauntletItem component = entry.getKey();
            int componentCount = entry.getValue();
            ItemManager.updateResourceCount(component, componentCount * diff);
        }
    }
}
