package catserver.server.inventory;

import javax.annotation.Nullable;

import org.bukkit.craftbukkit.inventory.CraftInventory;
import org.bukkit.craftbukkit.inventory.CraftInventoryCustom;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import net.minecraft.inventory.IInventory;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import net.minecraftforge.items.wrapper.SidedInvWrapper;

public class CatCustomInventory implements InventoryHolder{
    private final IInventory inventory;
    private final CraftInventory container;

    public CatCustomInventory(IInventory inventory) {
        this.container = new CraftInventory(inventory);
        this.inventory = inventory;
    }

    public CatCustomInventory(ItemStackHandler handler) {
        this.container = new CraftInventoryCustom(this, handler.getStacksList());
        this.inventory = this.container.getInventory();
    }

    @Override
    public Inventory getInventory() {
        return this.container;
    }

    // TODO: support all types
    @Nullable
    public static InventoryHolder holderFromForge(IItemHandler handler) {
        if (handler == null) return null;
        if (handler instanceof ItemStackHandler) return new CatCustomInventory((ItemStackHandler) handler);
        if (handler instanceof SlotItemHandler) return new CatCustomInventory(((SlotItemHandler) handler).inventory);
        if (handler instanceof InvWrapper) return new CatCustomInventory(((InvWrapper) handler).getInv());
        if (handler instanceof SidedInvWrapper) return new CatCustomInventory(((SidedInvWrapper) handler).getInv());
        return null;
    }

    @Nullable
    public static Inventory inventoryFromForge(IItemHandler handler) {
        InventoryHolder holder = holderFromForge(handler);
        return holder != null ? holder.getInventory() : null;
    }
    
}
