package catserver.server.threads;

import net.minecraft.tileentity.TileEntityHopper;
import net.minecraft.world.WorldServer;

import java.util.concurrent.ConcurrentLinkedQueue;

public class HopperThread extends Thread {

    private final WorldServer world;
    private final ConcurrentLinkedQueue<TileEntityHopper> queue;

    public HopperThread(WorldServer worldServer, ConcurrentLinkedQueue<TileEntityHopper> queue) {
        this.world = worldServer;
        this.queue = queue;
    }

    @Override
    public void run() {
        while (true) {
            try{
                TileEntityHopper hopper = queue.poll();
                if (hopper == null) {
                    Thread.sleep(2);
                    continue;
                }
                if (world == null || !world.isBlockLoaded(hopper.getPos())) continue;
                if (!this.world.isRemote)
                {
                    --hopper.transferCooldown;
                    hopper.tickedGameTime = this.world.getTotalWorldTime();

                    if (!hopper.isOnTransferCooldown())
                    {
                        hopper.setTransferCooldown(0);
                        hopper.lock.lock();
                        try {
                            hopper.updateHopper();
                        }finally {
                            hopper.lock.unlock();
                        }
                    }
                }
            }catch (Exception e) {
                e.printStackTrace();
            }

        }
    }
}
