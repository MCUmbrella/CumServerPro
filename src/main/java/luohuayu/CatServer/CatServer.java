package luohuayu.CatServer;

import net.minecraft.server.MinecraftServer;

public class CatServer {
	private static final String version = "2.0.0";
	private static final String native_verson = "v1_12_R1";

	public static String getVersion(){
		return version;
	}

    public static String getNativeVersion() {
        return native_verson;
    }

    public static boolean isDev() {
        return System.getProperty("net.minecraftforge.gradle.GradleStart.srg.srg-mcp") != null;
    }

    public static boolean asyncCatch(String reason) {
        if (Thread.currentThread() != MinecraftServer.getServerInst().primaryThread) {
            MinecraftServer.getServerInst().LOGGER.warn("Try to asynchronously " + reason + ", caught!");
            return true;
        }
        return false;
    }
}
