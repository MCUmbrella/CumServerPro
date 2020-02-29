package CumServer.server.threads;

import CumServer.server.CumServer;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.FMLLog;
import org.bukkit.Bukkit;

import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;

public class CumWatchdogThread extends TimerTask {
    private static Timer timer = new Timer();
    private static long lastTime = 0;
    private static long lastWarnTime = 0;

    @Override
    public void run() {
        long curTime = System.currentTimeMillis();
        if (lastTime > 0 && curTime - lastTime > 2000 && curTime - lastWarnTime > 30000) {
            lastWarnTime = curTime;
            FMLLog.log.debug("------------------------------");
            FMLLog.log.debug("[CumServer]服务器主线程卡死" + (curTime - lastTime) + "ms!");
            FMLLog.log.debug("当前主线程堆栈追踪:");
            for ( StackTraceElement stack : MinecraftServer.getServerInst().primaryThread.getStackTrace() )
            {
                FMLLog.log.debug("\t\t" + stack);
            }
            FMLLog.log.debug("--------------在CumServer.yml中check.threadLag关闭----------------");
        }
    }

    public static void update() {
        lastTime = System.currentTimeMillis();
    }

    public static void startThread() {
        if (CumServer.threadLag)
        timer.schedule(new CumWatchdogThread(), 30 * 1000, 500);
    }

    public static void stopThread() {
        timer.cancel();
    }
}
