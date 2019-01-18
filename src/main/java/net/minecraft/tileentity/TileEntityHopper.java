package net.minecraft.tileentity;

import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockChest;
import net.minecraft.block.BlockHopper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityMinecartHopper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ContainerHopper;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.InventoryLargeChest;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.datafix.DataFixer;
import net.minecraft.util.datafix.FixTypes;
import net.minecraft.util.datafix.walkers.ItemStackDataLists;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import org.bukkit.craftbukkit.entity.CraftHumanEntity;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.inventory.Inventory;

import catserver.server.inventory.CatCustomInventory;

public class TileEntityHopper extends TileEntityLockableLoot implements IHopper, ITickable
{
    private NonNullList<ItemStack> inventory = NonNullList.<ItemStack>with(5, ItemStack.EMPTY, true);
    public int transferCooldown = -1;
    public long tickedGameTime;
    // CraftBukkit start - add fields and methods
    public List<HumanEntity> transaction = new java.util.ArrayList<HumanEntity>();
    private int maxStack = MAX_STACK;
    
    public List<ItemStack> getContents() {
        return this.inventory;
    }
        
    public void onOpen(CraftHumanEntity who) {
        transaction.add(who);
    }
    
    public void onClose(CraftHumanEntity who) {
        transaction.remove(who);
    }
        
    public List<HumanEntity> getViewers() {
        return transaction;
    }
        
    public void setMaxStackSize(int size) {
        maxStack = size;
    }
    // CraftBukkit end
    public static void registerFixesHopper(DataFixer fixer)
    {
        fixer.registerWalker(FixTypes.BLOCK_ENTITY, new ItemStackDataLists(TileEntityHopper.class, new String[] {"Items"}));
    }

    public void readFromNBT(NBTTagCompound compound)
    {
        super.readFromNBT(compound);
        this.inventory = NonNullList.<ItemStack>withSize(this.getSizeInventory(), ItemStack.EMPTY);

        if (!this.checkLootAndRead(compound))
        {
            ItemStackHelper.loadAllItems(compound, this.inventory);
        }

        if (compound.hasKey("CustomName", 8))
        {
            this.customName = compound.getString("CustomName");
        }

        this.transferCooldown = compound.getInteger("TransferCooldown");
    }

    public NBTTagCompound writeToNBT(NBTTagCompound compound)
    {
        super.writeToNBT(compound);

        if (!this.checkLootAndWrite(compound))
        {
            ItemStackHelper.saveAllItems(compound, this.inventory);
        }

        compound.setInteger("TransferCooldown", this.transferCooldown);

        if (this.hasCustomName())
        {
            compound.setString("CustomName", this.customName);
        }

        return compound;
    }

    public int getSizeInventory()
    {
        return this.inventory.size();
    }

    public ItemStack decrStackSize(int index, int count)
    {
        this.fillWithLoot((EntityPlayer)null);
        ItemStack itemstack = ItemStackHelper.getAndSplit(this.getItems(), index, count);
        return itemstack;
    }

    public void setInventorySlotContents(int index, ItemStack stack)
    {
        this.fillWithLoot((EntityPlayer)null);
        this.getItems().set(index, stack);

        if (stack.getCount() > this.getInventoryStackLimit())
        {
            stack.setCount(this.getInventoryStackLimit());
        }
    }

    public String getName()
    {
        return this.hasCustomName() ? this.customName : "container.hopper";
    }

    public int getInventoryStackLimit()
    {
        return maxStack;
    }

    public void update()
    {
        this.world.addHopperQueue(this);
    }

    public boolean updateHopper()
    {
        if (this.world != null && !this.world.isRemote)
        {
            if (!this.isOnTransferCooldown() && BlockHopper.isEnabled(this.getBlockMetadata()))
            {
                boolean flag = false;

                if (!this.isInventoryEmpty())
                {
                    flag = this.transferItemsOut();
                }

                if (!this.isFull())
                {
                    flag = pullItems(this) || flag;
                }

                if (flag)
                {
                    this.setTransferCooldown(world.spigotConfig.hopperTransfer); // Spigot
                    this.markDirty();
                    return true;
                }
            }

            return false;
        }
        else
        {
            return false;
        }
    }

    private boolean isInventoryEmpty()
    {
        for (ItemStack itemstack : this.inventory)
        {
            if (!itemstack.isEmpty())
            {
                return false;
            }
        }

        return true;
    }

    public boolean isEmpty()
    {
        return this.isInventoryEmpty();
    }

    private boolean isFull()
    {
        for (ItemStack itemstack : this.inventory)
        {
            if (itemstack.isEmpty() || itemstack.getCount() != itemstack.getMaxStackSize())
            {
                return false;
            }
        }

        return true;
    }

    private boolean transferItemsOut()
    {
        if (net.minecraftforge.items.VanillaInventoryCodeHooks.insertHook(this)) { return true; }
        IInventory iinventory = this.getInventoryForHopperTransfer();

        if (iinventory == null)
        {
            return false;
        }
        else
        {
            EnumFacing enumfacing = BlockHopper.getFacing(this.getBlockMetadata()).getOpposite();

            if (this.isInventoryFull(iinventory, enumfacing))
            {
                return false;
            }
            else
            {
                for (int i = 0; i < this.getSizeInventory(); ++i)
                {
                    if (!this.getStackInSlot(i).isEmpty())
                    {
                        ItemStack itemstack = this.getStackInSlot(i).copy();
                        ItemStack itemstack1 = putStackInInventoryAllSlots(this, iinventory, this.decrStackSize(i, 1), enumfacing);

                        if (itemstack1.isEmpty())
                        {
                            iinventory.markDirty();
                            return true;
                        }

                        this.setInventorySlotContents(i, itemstack);
                    }
                }

                return false;
            }
        }
    }

    private boolean isInventoryFull(IInventory inventoryIn, EnumFacing side)
    {
        if (inventoryIn instanceof ISidedInventory)
        {
            ISidedInventory isidedinventory = (ISidedInventory)inventoryIn;
            int[] aint = isidedinventory.getSlotsForFace(side);

            for (int k : aint)
            {
                ItemStack itemstack1 = isidedinventory.getStackInSlot(k);

                if (itemstack1.isEmpty() || itemstack1.getCount() != itemstack1.getMaxStackSize())
                {
                    return false;
                }
            }
        }
        else
        {
            int i = inventoryIn.getSizeInventory();

            for (int j = 0; j < i; ++j)
            {
                ItemStack itemstack = inventoryIn.getStackInSlot(j);

                if (itemstack.isEmpty() || itemstack.getCount() != itemstack.getMaxStackSize())
                {
                    return false;
                }
            }
        }

        return true;
    }

    private static boolean isInventoryEmpty(IInventory inventoryIn, EnumFacing side)
    {
        if (inventoryIn instanceof ISidedInventory)
        {
            ISidedInventory isidedinventory = (ISidedInventory)inventoryIn;
            int[] aint = isidedinventory.getSlotsForFace(side);

            for (int i : aint)
            {
                if (!isidedinventory.getStackInSlot(i).isEmpty())
                {
                    return false;
                }
            }
        }
        else
        {
            int j = inventoryIn.getSizeInventory();

            for (int k = 0; k < j; ++k)
            {
                if (!inventoryIn.getStackInSlot(k).isEmpty())
                {
                    return false;
                }
            }
        }

        return true;
    }

    public static boolean pullItems(IHopper hopper)
    {
        Boolean ret = net.minecraftforge.items.VanillaInventoryCodeHooks.extractHook(hopper);
        if (ret != null) return ret;
        IInventory iinventory = getSourceInventory(hopper);

        if (iinventory != null)
        {
            EnumFacing enumfacing = EnumFacing.DOWN;

            if (isInventoryEmpty(iinventory, enumfacing))
            {
                return false;
            }

            if (iinventory instanceof ISidedInventory)
            {
                ISidedInventory isidedinventory = (ISidedInventory)iinventory;
                int[] aint = isidedinventory.getSlotsForFace(enumfacing);

                for (int i : aint)
                {
                    if (pullItemFromSlot(hopper, iinventory, i, enumfacing))
                    {
                        return true;
                    }
                }
            }
            else
            {
                int j = iinventory.getSizeInventory();

                for (int k = 0; k < j; ++k)
                {
                    if (pullItemFromSlot(hopper, iinventory, k, enumfacing))
                    {
                        return true;
                    }
                }
            }
        }
        else
        {
            for (EntityItem entityitem : getCaptureItems(hopper.getWorld(), hopper.getXPos(), hopper.getYPos(), hopper.getZPos()))
            {
                if (putDropInInventoryAllSlots((IInventory)null, hopper, entityitem))
                {
                    return true;
                }
            }
        }

        return false;
    }

    // CatServer - TODO: move into Forge, support handle Forge container
    private static boolean pullItemFromSlot(IHopper hopper, IInventory inventoryIn, int index, EnumFacing direction)
    {
        ItemStack itemstack = inventoryIn.getStackInSlot(index);

        if (!itemstack.isEmpty() && canExtractItemFromSlot(inventoryIn, itemstack, index, direction))
        {
            ItemStack itemstack1 = itemstack.copy();
            // ItemStack itemstack2 = putStackInInventoryAllSlots(inventoryIn, hopper, inventoryIn.decrStackSize(index, 1), (EnumFacing)null);
            // CraftBukkit start - Call event on collection of items from inventories into the hopper
            CraftItemStack oitemstack = CraftItemStack.asCraftMirror(inventoryIn.decrStackSize(index, hopper.getWorld().spigotConfig.hopperAmount)); // Spigot

            Inventory sourceInventory;
            // Have to special case large chests as they work oddly
            if (inventoryIn instanceof InventoryLargeChest) {
                sourceInventory = new org.bukkit.craftbukkit.inventory.CraftInventoryDoubleChest((InventoryLargeChest) inventoryIn);
            } else {
                sourceInventory = inventoryIn.getOwner() != null ? inventoryIn.getOwner().getInventory() : new CatCustomInventory(inventoryIn).getInventory(); // CatServer
            }

            InventoryMoveItemEvent event = new InventoryMoveItemEvent(sourceInventory, oitemstack.clone(), hopper.getOwner().getInventory(), false);

            if (sourceInventory != null) hopper.getWorld().getServer().getPluginManager().callEvent(event); // CatServer
            if (event.isCancelled()) {
                inventoryIn.setInventorySlotContents(index, itemstack1);

                if (hopper instanceof TileEntityHopper) {
                    ((TileEntityHopper) hopper).setTransferCooldown(hopper.getWorld().spigotConfig.hopperTransfer); // Spigot
                } else if (hopper instanceof EntityMinecartHopper) {
                    ((EntityMinecartHopper) hopper).setTransferTicker(hopper.getWorld().spigotConfig.hopperTransfer / 2); // Spigot
                }

                return false;
            }
            int origCount = event.getItem().getAmount(); // Spigot
            ItemStack itemstack2 = putStackInInventoryAllSlots(inventoryIn, hopper, CraftItemStack.asNMSCopy(event.getItem()), null);

            if (itemstack2.isEmpty())
            {
                // inventoryIn.markDirty();
                if (event.getItem().equals(oitemstack)) {
                    inventoryIn.markDirty();
                } else {
                    inventoryIn.setInventorySlotContents(index, itemstack1);
                }
                // CraftBukkit end
                return true;
            }
            itemstack1.shrink(origCount - itemstack2.getCount()); // Spigot
            inventoryIn.setInventorySlotContents(index, itemstack1);
        }

        return false;
    }

    public static boolean putDropInInventoryAllSlots(IInventory source, IInventory destination, EntityItem entity)
    {
        boolean flag = false;

        if (entity == null)
        {
            return false;
        }
        else
        {
            InventoryPickupItemEvent event = new InventoryPickupItemEvent(destination.getOwner().getInventory(), (org.bukkit.entity.Item) entity.getBukkitEntity());
            entity.world.getServer().getPluginManager().callEvent(event);
            if (event.isCancelled()) {
                return false;
            }
            ItemStack itemstack = entity.getItem().copy();
            ItemStack itemstack1 = putStackInInventoryAllSlots(source, destination, itemstack, (EnumFacing)null);

            if (itemstack1.isEmpty())
            {
                flag = true;
                entity.setDead();
            }
            else
            {
                entity.setItem(itemstack1);
            }

            return flag;
        }
    }


    protected net.minecraftforge.items.IItemHandler createUnSidedHandler()
    {
        return new net.minecraftforge.items.VanillaHopperItemHandler(this);
    }

    public static ItemStack putStackInInventoryAllSlots(IInventory source, IInventory destination, ItemStack stack, @Nullable EnumFacing direction)
    {
        if (destination instanceof ISidedInventory && direction != null)
        {
            ISidedInventory isidedinventory = (ISidedInventory)destination;
            int[] aint = isidedinventory.getSlotsForFace(direction);

            for (int k = 0; k < aint.length && !stack.isEmpty(); ++k)
            {
                stack = insertStack(source, destination, stack, aint[k], direction);
            }
        }
        else
        {
            int i = destination.getSizeInventory();

            for (int j = 0; j < i && !stack.isEmpty(); ++j)
            {
                stack = insertStack(source, destination, stack, j, direction);
            }
        }

        return stack;
    }

    private static boolean canInsertItemInSlot(IInventory inventoryIn, ItemStack stack, int index, EnumFacing side)
    {
        if (!inventoryIn.isItemValidForSlot(index, stack))
        {
            return false;
        }
        else
        {
            return !(inventoryIn instanceof ISidedInventory) || ((ISidedInventory)inventoryIn).canInsertItem(index, stack, side);
        }
    }

    private static boolean canExtractItemFromSlot(IInventory inventoryIn, ItemStack stack, int index, EnumFacing side)
    {
        return !(inventoryIn instanceof ISidedInventory) || ((ISidedInventory)inventoryIn).canExtractItem(index, stack, side);
    }

    private static ItemStack insertStack(IInventory source, IInventory destination, ItemStack stack, int index, EnumFacing direction)
    {
        ItemStack itemstack = destination.getStackInSlot(index);

        if (canInsertItemInSlot(destination, stack, index, direction))
        {
            boolean flag = false;
            boolean flag1 = destination.isEmpty();

            if (itemstack.isEmpty())
            {
                destination.setInventorySlotContents(index, stack);
                stack = ItemStack.EMPTY;
                flag = true;
            }
            else if (canCombine(itemstack, stack))
            {
                int i = stack.getMaxStackSize() - itemstack.getCount();
                int j = Math.min(stack.getCount(), i);
                stack.shrink(j);
                itemstack.grow(j);
                flag = j > 0;
            }

            if (flag)
            {
                if (flag1 && destination instanceof TileEntityHopper)
                {
                    TileEntityHopper tileentityhopper1 = (TileEntityHopper)destination;

                    if (!tileentityhopper1.mayTransfer())
                    {
                        int k = 0;

                        if (source != null && source instanceof TileEntityHopper)
                        {
                            TileEntityHopper tileentityhopper = (TileEntityHopper)source;

                            if (tileentityhopper1.tickedGameTime >= tileentityhopper.tickedGameTime)
                            {
                                k = 1;
                            }
                        }

                        tileentityhopper1.setTransferCooldown(tileentityhopper1.world.spigotConfig.hopperTransfer - k); // Spigot
                    }
                }

                destination.markDirty();
            }
        }

        return stack;
    }

    private IInventory getInventoryForHopperTransfer()
    {
        EnumFacing enumfacing = BlockHopper.getFacing(this.getBlockMetadata());
        return getInventoryAtPosition(this.getWorld(), this.getXPos() + (double)enumfacing.getFrontOffsetX(), this.getYPos() + (double)enumfacing.getFrontOffsetY(), this.getZPos() + (double)enumfacing.getFrontOffsetZ());
    }

    public static IInventory getSourceInventory(IHopper hopper)
    {
        return getInventoryAtPosition(hopper.getWorld(), hopper.getXPos(), hopper.getYPos() + 1.0D, hopper.getZPos());
    }

    public static List<EntityItem> getCaptureItems(World worldIn, double p_184292_1_, double p_184292_3_, double p_184292_5_)
    {
        return worldIn.<EntityItem>getEntitiesWithinAABB(EntityItem.class, new AxisAlignedBB(p_184292_1_ - 0.5D, p_184292_3_, p_184292_5_ - 0.5D, p_184292_1_ + 0.5D, p_184292_3_ + 1.5D, p_184292_5_ + 0.5D), EntitySelectors.IS_ALIVE);
    }

    public static IInventory getInventoryAtPosition(World worldIn, double x, double y, double z)
    {
        IInventory iinventory = null;
        int i = MathHelper.floor(x);
        int j = MathHelper.floor(y);
        int k = MathHelper.floor(z);
        BlockPos blockpos = new BlockPos(i, j, k);
        if (!worldIn.isBlockLoaded(blockpos)) return null; // Spigot
        net.minecraft.block.state.IBlockState state = worldIn.getBlockState(blockpos);
        Block block = state.getBlock();

        if (block.hasTileEntity(state))
        {
            TileEntity tileentity = worldIn.getTileEntity(blockpos);

            if (tileentity instanceof IInventory)
            {
                iinventory = (IInventory)tileentity;

                if (iinventory instanceof TileEntityChest && block instanceof BlockChest)
                {
                    iinventory = ((BlockChest)block).getContainer(worldIn, blockpos, true);
                }
            }
        }

        if (iinventory == null)
        {
            List<Entity> list = worldIn.getEntitiesInAABBexcluding((Entity)null, new AxisAlignedBB(x - 0.5D, y - 0.5D, z - 0.5D, x + 0.5D, y + 0.5D, z + 0.5D), EntitySelectors.HAS_INVENTORY);

            if (!list.isEmpty())
            {
                iinventory = (IInventory)list.get(worldIn.rand.nextInt(list.size()));
            }
        }

        return iinventory;
    }

    private static boolean canCombine(ItemStack stack1, ItemStack stack2)
    {
        if (stack1.getItem() != stack2.getItem())
        {
            return false;
        }
        else if (stack1.getMetadata() != stack2.getMetadata())
        {
            return false;
        }
        else if (stack1.getCount() > stack1.getMaxStackSize())
        {
            return false;
        }
        else
        {
            return ItemStack.areItemStackTagsEqual(stack1, stack2);
        }
    }

    public double getXPos()
    {
        return (double)this.pos.getX() + 0.5D;
    }

    public double getYPos()
    {
        return (double)this.pos.getY() + 0.5D;
    }

    public double getZPos()
    {
        return (double)this.pos.getZ() + 0.5D;
    }

    public void setTransferCooldown(int ticks)
    {
        this.transferCooldown = ticks;
    }

    public boolean isOnTransferCooldown()
    {
        return this.transferCooldown > 0;
    }

    public boolean mayTransfer()
    {
        return this.transferCooldown > 8;
    }

    public String getGuiID()
    {
        return "minecraft:hopper";
    }

    public Container createContainer(InventoryPlayer playerInventory, EntityPlayer playerIn)
    {
        this.fillWithLoot(playerIn);
        return new ContainerHopper(playerInventory, this, playerIn);
    }

    protected NonNullList<ItemStack> getItems()
    {
        return this.inventory;
    }

    public long getLastUpdateTime() { return tickedGameTime; } // Forge
}