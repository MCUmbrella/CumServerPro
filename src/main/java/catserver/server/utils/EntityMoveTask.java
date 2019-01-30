package catserver.server.utils;

import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;

public class EntityMoveTask {
    public final Entity entity;
    public final MoverType moverType;
    public final double x;
    public final double y;
    public final double z;

    public EntityMoveTask(Entity entity, MoverType moverType, double x, double y, double z) {
        this.entity = entity;
        this.moverType = moverType;
        this.x = x;
        this.y = y;
        this.z = z;
    }

}
