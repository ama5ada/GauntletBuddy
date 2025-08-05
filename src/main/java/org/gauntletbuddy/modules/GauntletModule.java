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
import java.util.Set;

@Singleton
public final class GauntletModule implements PluginModule {
    @Inject
    private Client client;
    @Inject
    private EventBus eventBus;
    @Inject
    private ItemTracker itemTracker;

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
    public void onItemContainerChanged(ItemContainerChanged itemContainerChanged) {
        //TODO Skip most calculations in the spawn room since people frequently bank items there and crafting occurs there
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
                ItemTracker.updateResourceCount(item, diff);
            });
        }

        if (!missingKeys.isEmpty()) {
            for (int itemId : missingKeys) {
                int diff = itemCounts.get(itemId);
                GauntletItem.itemFromId(itemId).ifPresent( item -> {
                    if (item.isCraftable() && item != GauntletItem.TELEPORT_CRYSTAL) refundComponents(item, diff);
                    ItemTracker.updateResourceCount(item, diff);
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
            ItemTracker.updateResourceCount(component, componentCount * diff);
        }
    }
}
