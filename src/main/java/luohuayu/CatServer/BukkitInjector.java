package luohuayu.CatServer;

import java.util.Map;

import org.apache.logging.log4j.Level;
import org.bukkit.Material;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.fml.common.FMLLog;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public class BukkitInjector {
    public static boolean initializedBukkit = false;

    public static void injectItemBukkitMaterials()
    {
        for (Map.Entry<ResourceLocation, Item> entry : ForgeRegistries.ITEMS.getEntries()) {
            ResourceLocation key = entry.getKey();
            Item item = entry.getValue();
            if(!key.getResourceDomain().equals("minecraft")) {
                String materialName = key.toString().toUpperCase().replaceAll("(:|\\s)", "_").replaceAll("\\W", "");
                Material material = Material.addMaterial(EnumHelper.addEnum(Material.class, materialName, new Class[]{Integer.TYPE}, new Object[]{Integer.valueOf(Item.getIdFromItem(item))}));
                if (material != null) {
                    FMLLog.log(Level.DEBUG, "Injected new Forge item material %s with ID %d.", material.name(), material.getId());
                }
            }
        }
    }

    public static void injectBlockBukkitMaterials()
    {
        for (Map.Entry<ResourceLocation, Block> entry : ForgeRegistries.BLOCKS.getEntries()) {
            ResourceLocation key = entry.getKey();
            Block block = entry.getValue();
            if(!key.getResourceDomain().equals("minecraft")) {
                String materialName = key.toString().toUpperCase().replaceAll("(:|\\s)", "_").replaceAll("\\W", "");
                Material material = Material.addMaterial(EnumHelper.addEnum(Material.class, materialName, new Class[]{Integer.TYPE}, new Object[]{Integer.valueOf(Block.getIdFromBlock(block))}));
                if (material != null) {
                    FMLLog.log(Level.DEBUG, "Injected new Forge block material %s with ID %d.", material.name(), material.getId());
                }
            }
        }
    }
}
