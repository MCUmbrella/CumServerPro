package catserver.server.async;

import net.minecraft.entity.EntityLivingBase;

public class EntityAIMoveTask extends AbstractEntityTask{
    private final EntityLivingBase livingBase;
    private final float strafe, vertical, forward;

    public EntityAIMoveTask(EntityLivingBase livingBase, float strafe, float vertical, float forward) {
        this.livingBase = livingBase;
        this.strafe = strafe;
        this.vertical = vertical;
        this.forward = forward;
    }

    @Override
    public void run() {
        livingBase.travel0(strafe, vertical, forward, true);
    }
}
