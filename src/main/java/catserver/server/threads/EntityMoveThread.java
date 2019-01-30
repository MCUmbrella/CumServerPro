package catserver.server.threads;

import catserver.server.utils.EntityTask;
import net.minecraft.world.WorldServer;
import java.util.concurrent.LinkedBlockingQueue;

public class EntityMoveThread extends Thread {

    private final WorldServer world;
    public final LinkedBlockingQueue<EntityTask> queue;

    public EntityMoveThread(WorldServer worldServer, LinkedBlockingQueue<EntityTask> queue, String name) {
        this.world = worldServer;
        this.queue = queue;
        setName(name);
    }


    @Override
    public void run() {
        while (world != null) {
            try {
                EntityTask task = queue.take();
                //Start
                task.run();
            }catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
