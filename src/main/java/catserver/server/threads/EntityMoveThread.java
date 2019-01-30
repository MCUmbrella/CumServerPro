package catserver.server.threads;

import catserver.server.utils.EntityMoveTask;
import net.minecraft.entity.Entity;
import net.minecraft.world.WorldServer;

import java.util.concurrent.LinkedBlockingQueue;

public class EntityMoveThread extends Thread {

    private final WorldServer world;
    private final LinkedBlockingQueue<EntityMoveTask> queue;

    public EntityMoveThread(WorldServer worldServer, LinkedBlockingQueue<EntityMoveTask> queue) {
        this.world = worldServer;
        this.queue = queue;
    }


    @Override
    public void run() {
        long nowTime = System.currentTimeMillis();
        short count = 0;
        while (world != null) {
            try {
                if ((count++ % 10) == 0) {
                    nowTime = System.currentTimeMillis();
                    count = 0;
                }
                EntityMoveTask task = queue.take();
                if (nowTime - task.time > 200) continue;
                Entity entity = task.entity;

                if (entity.isDead || !world.isChunkLoaded((int) entity.posX >> 4, (int) entity.posZ >> 4, true)) continue;

                //Start
                entity.move0(task.moverType, task.x, task.y, task.z, true);
            }catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
