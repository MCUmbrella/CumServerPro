package catserver.server.threads;

import catserver.server.CatServer;
import net.minecraft.server.MinecraftServer;
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
            Bukkit.getLogger().log(Level.WARNING, "------------------------------");
            Bukkit.getLogger().log(Level.WARNING, "[Cat侦测系统]服务器主线程已陷入停顿" + (curTime - lastTime) + "ms! 你的服务器卡顿了!");
            Bukkit.getLogger().log(Level.WARNING, "当前主线程堆栈追踪:");
            for ( StackTraceElement stack : MinecraftServer.getServerInst().primaryThread.getStackTrace() )
            {
                Bukkit.getLogger().log( Level.WARNING, "\t\t" + stack );
            }
            Bukkit.getLogger().log(Level.WARNING, "------------------------------");
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
