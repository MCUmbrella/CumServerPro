package catserver.server.threads;

import catserver.server.CatServer;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.FMLLog;
import org.bukkit.Bukkit;

import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;

public class WatchCatThread extends TimerTask {
    private static Timer timer = new Timer();
    private static long lastTime = 0;
    private static long lastWarnTime = 0;

    @Override
    public void run() {
        long curTime = System.currentTimeMillis();
        if (lastTime > 0 && curTime - lastTime > 2000 && curTime - lastWarnTime > 30000) {
            lastWarnTime = curTime;
            FMLLog.log.debug("------------------------------");
            FMLLog.log.debug("[Cat侦测系统]服务器主线程已陷入停顿" + (curTime - lastTime) + "ms! 你的服务器卡顿了!");
            FMLLog.log.debug("当前主线程堆栈追踪:");
            for ( StackTraceElement stack : MinecraftServer.getServerInst().primaryThread.getStackTrace() )
            {
                FMLLog.log.debug("\t\t" + stack);
            }
            FMLLog.log.debug("--------------请注意,这不是报错!请勿反馈!可在catserver.yml中check.threadLag关闭----------------");
        }
    }

    public static void update() {
        lastTime = System.currentTimeMillis();
    }

    public static void startThread() {
        if (CatServer.threadLag)
        timer.schedule(new WatchCatThread(), 30 * 1000, 500);
    }

    public static void stopThread() {
        timer.cancel();
    }
}
