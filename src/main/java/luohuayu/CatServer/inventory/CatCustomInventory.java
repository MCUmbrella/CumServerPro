package luohuayu.CatServer.inventory;

import org.bukkit.craftbukkit.inventory.CraftInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import net.minecraft.inventory.IInventory;

public class CatCustomInventory extends CraftInventory {
    private final IInventory inventory;
    private final InventoryHolder holder;

    public CatCustomInventory(IInventory inventory) {
        super(inventory);
        this.inventory = inventory;
        this.holder = new Holder();
    }

    @Override
    public InventoryHolder getHolder() {
        return this.holder;
    }

    class Holder implements InventoryHolder {
        @Override
        public Inventory getInventory() {
            return CatCustomInventory.this;
        }
    }
}
