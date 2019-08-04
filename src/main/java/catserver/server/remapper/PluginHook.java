package catserver.server.remapper;

import com.google.common.collect.Lists;

import java.util.List;

public class PluginHook {
    private static List<String> hookToBlockMaterial = Lists.newArrayList();

    static {
        hookToBlockMaterial.add("com.sk89q.worldedit.bukkit.BukkitWorld;isValidBlockType"); // WorldEdit
    }

    public static boolean isHookToBlockMaterial() {
        return hookToBlockMaterial.contains(getCallFrom());
    }

    public static String getCallFrom() {
        try {
            StackTraceElement[] stackTrace = new Throwable().getStackTrace();
            return stackTrace[3].getClassName() + ";" + stackTrace[3].getMethodName();
        } catch (Throwable ignored) {}
        return "null;null";
    }
}
