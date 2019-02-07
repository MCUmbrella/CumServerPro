package catserver.server.utils;

import net.minecraft.entity.EntityLivingBase;

public class EntityAITask implements Runnable {
    private final EntityLivingBase entityLivingBase;
    private final long time;
    public EntityAITask(EntityLivingBase entityLivingBase, long time) {
        this.entityLivingBase = entityLivingBase;
        this.time = time;
    }

    @Override
    public void run() {
        if (System.currentTimeMillis() - this.time > 100)
            return;
        if (this.entityLivingBase.world.unloadedEntitySet.contains(this.entityLivingBase))
            return;
        entityLivingBase.updateEntityActionStateAsync();
    }
}
