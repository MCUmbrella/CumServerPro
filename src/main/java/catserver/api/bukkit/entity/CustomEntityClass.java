package catserver.api.bukkit.entity;

import catserver.api.bukkit.NMSUtils;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

public class CustomEntityClass {
    private final String entityName;
    private final Class<? extends Entity> entityClass;

    public CustomEntityClass(String entityName, Class<? extends Entity> entityClass) {
        this.entityName = entityName;
        this.entityClass = entityClass;
    }

    public String getEntityName() {
        return this.entityName;
    }

    public org.bukkit.entity.Entity spawn(org.bukkit.World world, int x, int y, int z) {
        WorldServer worldserver = NMSUtils.toNMS(world);
        Entity entity = null;
        try {
            entity = entityClass.getConstructor(World.class).newInstance(worldserver);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        if (entity == null) return null;
        entity.setPosition(x, y, z);
        worldserver.spawnEntity(entity);
        return entity.getBukkitEntity();
        
    }
}
