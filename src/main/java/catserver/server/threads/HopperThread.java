package catserver.server.threads;

import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.tileentity.TileEntityHopper;
import net.minecraft.util.ReportedException;
import net.minecraft.world.WorldServer;

import java.util.concurrent.LinkedBlockingQueue;

public class HopperThread extends Thread {

    private final WorldServer world;
    private final LinkedBlockingQueue<TileEntityHopper> queue;

    public HopperThread(WorldServer worldServer, LinkedBlockingQueue<TileEntityHopper> queue) {
        this.world = worldServer;
        this.queue = queue;
    }

    @Override
    public void run() {
        TileEntityHopper thisHopper = null;
        while (world != null) {
            try{
                TileEntityHopper hopper = queue.take();
                thisHopper = hopper;
                if (!world.isBlockLoaded(hopper.getPos())) continue;
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
            }catch (Throwable throwable) {
                CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Exception update Hopper");
                CrashReportCategory crashreportcategory = crashreport.makeCategory("Hopper to be update");
                crashreportcategory.addCrashSection("Location", String.format("%d,%d,%d", thisHopper.getXPos(), thisHopper.getYPos(), thisHopper.getZPos()));
                throw new ReportedException(crashreport);
            }

        }
    }
}
