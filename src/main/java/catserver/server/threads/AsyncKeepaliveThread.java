package catserver.server.threads;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;

public class AsyncKeepaliveThread extends Thread {
    private static AsyncKeepaliveThread thread;

    public void run() {
        while(true) {
            try {
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

    public static void startThread() {
        thread = new AsyncKeepaliveThread();
        thread.start();
    }

    public static void stopThread() {
        if (thread != null)
            thread.stop();
    }
}
