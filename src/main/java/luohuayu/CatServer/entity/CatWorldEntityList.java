package luohuayu.CatServer.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.passive.IAnimals;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import java.util.ArrayList;
import java.util.Collection;

//Paper Class
public class CatWorldEntityList extends ArrayList<Entity> {

    private final WorldServer world;
    private final int[] entityCounts = new int[EnumCreatureType.values().length];


    public CatWorldEntityList(World world) {
        this.world = (WorldServer) world;
    }

    @Override
    public boolean addAll(Collection<? extends Entity> c) {
        for (Entity e : c) {
            updateEntityCount(e, 1);
        }

        return super.addAll(c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        for (Object e : c) {
            if (e instanceof Entity && ((Entity) e).getEntityWorld() == world) {
                updateEntityCount((Entity) e, -1);
            }
        }

        return super.removeAll(c);
    }

    @Override
    public boolean add(Entity e) {
        updateEntityCount(e, 1);

        return super.add(e);
    }

    @Override
    public Entity remove(int index) {
        guard();
        Entity entity = super.remove(index);
        if (entity != null) updateEntityCount(entity, -1);
        return entity;
    }

    @Override
    public boolean remove(Object o) {
        guard();
        if (super.remove(o)) {
            updateEntityCount((Entity) o, -1);
            return true;
        }
        return false;
    }

    private void guard() {
        if (world.guardEntityList) {
            throw new java.util.ConcurrentModificationException();
        }
    }

    public int getCreatureCount(EnumCreatureType type) {
        return entityCounts[type.ordinal()];
    }

    private void updateEntityCount(EnumCreatureType type, int amt) {
        int count = entityCounts[type.ordinal()];

        count += amt;

        if (count < 0) {
            MinecraftServer.LOGGER.error("Paper - Entity count cache has gone negative");
            count = 0;
        }

        entityCounts[type.ordinal()] = count;
    }

    public void updateEntityCount(Entity entity, int amt) {
        if (!(entity instanceof IAnimals)) return;

        if (entity instanceof EntityLiving) {
            EntityLiving EntityLiving = (EntityLiving) entity;
            if (amt > 0 && EntityLiving.isDespawnableByDefault() && EntityLiving.isNoDespawnRequired()) {
                return;
            }
        }
        if (amt < 0) {
            if (!entity.hasBeenCounted) {
                return;
            }
            // Only remove once, we remove from if the entity list is guarded, but may be called later
            entity.hasBeenCounted = false;
        } else {
            if (entity.hasBeenCounted) {
                return;
            }
            entity.hasBeenCounted = true;
        }

        for (EnumCreatureType type : EnumCreatureType.values()) {
            if (type.matches(entity)) {
                updateEntityCount(type, amt);
                break;
            }
        }
    }
}