package catserver.server.utils;

import java.util.List;
import java.util.Locale;

import org.bukkit.plugin.InvalidPluginException;

import com.google.common.collect.Lists;

public class PluginBlacklist {
    private static String message = "Plugin %pluginname% is incompatible with Forge server, it will not load!";
    private static String message_zhcn = "插件 %pluginname% 与Forge服务端不兼容(造成严重问题), 该插件将不会加载!";

    private static List<String> blacklist = Lists.newArrayList();

    static {
        blacklist.add("PTweaks");
        blacklist.add("NoSpawnChunks");
        blacklist.add("EscapeLag");
        blacklist.add("LaggRemover");
        blacklist.add("ViaVersion");
        blacklist.add("ProtocolSupport");
        blacklist.add("AntiAttack");
        blacklist.add("AAC");
        blacklist.add("NoCheatPlus");
        blacklist.add("EpicWorldGenerator");
    }

    public static void checkBlacklist(String pluginName) throws InvalidPluginException {
        if (blacklist.contains(pluginName)) {
            throw new InvalidPluginException((Locale.getDefault() == Locale.CHINA ? message_zhcn : message).replace("%pluginname%", pluginName));
        }
    }
}
