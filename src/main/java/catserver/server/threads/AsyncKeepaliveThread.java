package catserver.server.threads;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;

public class AsyncKeepaliveThread {
    private static boolean isRunning = true;

    public static void startAsync() {
        while(true) {
            try {
                if (! isRunning)
                    return;
                for (EntityPlayerMP player : MinecraftServer.getServerInst().getPlayerList().playerEntityList) {
                    if (player.connection != null)
                        player.connection.asyncKeepalive();
                }
                Thread.sleep(100);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static native void startThread();

    public static void stopThread() {
        isRunning = false;
    }
}
