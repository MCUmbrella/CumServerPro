package catserver.server.threads;

import catserver.server.utils.EntityMoveTask;
import net.minecraft.entity.Entity;
import net.minecraft.world.WorldServer;
import java.util.concurrent.LinkedBlockingQueue;

public class EntityMoveThread extends Thread {

    private final WorldServer world;
    public final LinkedBlockingQueue<EntityMoveTask> queue;

    public EntityMoveThread(WorldServer worldServer, LinkedBlockingQueue<EntityMoveTask> queue, String name) {
        this.world = worldServer;
        this.queue = queue;
        setName(name);
    }


    @Override
    public void run() {
        while (world != null) {
            try {
                EntityMoveTask task = queue.take();
                Entity entity = task.entity;

                if (world == null || entity.isDead || !world.isChunkLoaded((int)entity.posX >> 4, (int)entity.posZ >> 4, true)) continue;

                //Start
                entity.move0(task.moverType, task.x, task.y, task.z, true);
            }catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
