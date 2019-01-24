package catserver.server;

import catserver.server.very.VeryConfig;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.FMLLog;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class CatServer {
	private static final String version = "2.0.0";
	private static final String native_verson = "v1_12_R1";
    public static YamlConfiguration config;
    public static boolean hopperAsync = true;
    public static boolean entityMoveAsync = true;

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
            FMLLog.warning("Try to asynchronously " + reason + ", caught!");
            return true;
        }
        return false;
    }

    public static void loadConfig() {
        File file = new File("catserver.yml");
        if (file.exists()) {
            config = YamlConfiguration.loadConfiguration(file);
        } else {
            config = YamlConfiguration.loadConfiguration(new InputStreamReader(VeryConfig.class.getClassLoader().getResourceAsStream("configurations/catserver.yml")));
            try {
                file.createNewFile();
                config.save(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        hopperAsync = config.getBoolean("async.hopper");
        entityMoveAsync = config.getBoolean("async.entityMove");
    }
}
