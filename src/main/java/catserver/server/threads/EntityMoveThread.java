package catserver.server.threads;

import catserver.server.utils.EntityMoveTask;
import net.minecraft.entity.Entity;
import net.minecraft.world.WorldServer;

import java.util.concurrent.ConcurrentLinkedQueue;

public class EntityMoveThread extends Thread {

    private final WorldServer world;
    private final ConcurrentLinkedQueue<EntityMoveTask> queue;

    public EntityMoveThread(WorldServer worldServer, ConcurrentLinkedQueue<EntityMoveTask> queue) {
        this.world = worldServer;
        this.queue = queue;
    }


    @Override
    public void run() {
        while (true) {
            try {
                EntityMoveTask task = queue.poll();
                if (task == null) {
                    Thread.sleep(5);
                    continue;
                }
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
