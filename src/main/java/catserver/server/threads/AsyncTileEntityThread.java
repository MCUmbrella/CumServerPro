package catserver.server.threads;

import catserver.server.CatServer;
import net.minecraft.crash.CrashReport;
import net.minecraft.util.ITickable;
import net.minecraft.util.ReportedException;
import net.minecraftforge.fml.common.FMLLog;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class AsyncTileEntityThread {
    private static ExecutorService teThreadPool;
    private static Throwable lastThrowable;
    private static List<Future> futures = new ArrayList<>();

    static {
        if (CatServer.MekTEAsync) {
            teThreadPool = Executors.newSingleThreadExecutor();
            FMLLog.warning("TileEntity异步功能已开启,该功能为测试功能,可能会造成无法预料的问题(包括但不限于刷物品、插件异常、崩溃、数据丢失损坏等)");
            FMLLog.warning("若无特殊情况请在catserver.yml里关闭,开启该功能将不接受任何崩服反馈!");
            FMLLog.warning("服务端将在5秒后继续运行!");
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void submit(ITickable te) {
        if (teThreadPool.isShutdown()) return;
        futures.add(teThreadPool.submit(() -> {
            try {
                te.update();
            } catch (Throwable throwable) {
                if (lastThrowable == null)
                    lastThrowable = throwable;
                teThreadPool.shutdownNow();
            }
        }));
    }

    public static void waitComplete() {
        if (!CatServer.MekTEAsync) return;
        try {
            for(Future future : futures) {
                future.get();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        futures.clear();
        if (lastThrowable != null)
            throw new ReportedException(CrashReport.makeCrashReport(lastThrowable, "异步处理TileEntity时发生错误,请尝试关闭异步选项后再反馈"));
    }
}
