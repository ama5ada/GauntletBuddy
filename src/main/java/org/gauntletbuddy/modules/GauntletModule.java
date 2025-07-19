package org.gauntletbuddy.modules;

import net.runelite.api.Client;
import net.runelite.api.InventoryID;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.List;
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
    private List<String> resources = ItemManager.getResources();

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
    @Subscribe
    public void onItemContainerChanged(ItemContainerChanged itemContainerChanged) {
        HashMap<Integer, Integer> updatedInventoryCounts = new HashMap<>();
        if (itemContainerChanged.getContainerId() == InventoryID.INVENTORY.getId()) {
            ItemContainer inventory = itemContainerChanged.getItemContainer();
            for (Item item : inventory.getItems()) {
                updatedInventoryCounts.put(item.getId(), updatedInventoryCounts.getOrDefault(item.getId(), 0) + item.getQuantity());
            }
            for (Map.Entry<Integer, Integer> entry : updatedInventoryCounts.entrySet()) {
                int itemId = entry.getKey();
                int itemCount = entry.getValue();
                if (ItemManager.getIdToResource().containsKey(itemId)) {
                    int diff = itemCount - inventoryCounts.getOrDefault(itemId, 0);
                    ItemManager.updateResourceCount(ItemManager.getIdToResource().get(itemId), diff);
                }
            }
            inventoryCounts = updatedInventoryCounts;
        }
    }
}
