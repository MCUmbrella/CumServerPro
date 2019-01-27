package catserver.server.async;

import net.minecraft.entity.EntityLivingBase;

public class EntityAICollisionTask extends AbstractEntityTask{
    public final EntityLivingBase entityLivingBase;

    public EntityAICollisionTask(EntityLivingBase entityLivingBase) {
        this.entityLivingBase = entityLivingBase;
    }

    @Override
    public void run() {
        entityLivingBase.collideWithNearbyEntities0();
    }
}
