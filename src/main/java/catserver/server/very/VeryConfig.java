package catserver.server.very;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import org.bukkit.configuration.file.YamlConfiguration;

public final class VeryConfig {
    protected static int userid;
    protected static String key;
    
    public static void load() {
        File file = new File("auth.yml");
        YamlConfiguration config;
        if (file.exists()) {
            config = YamlConfiguration.loadConfiguration(file);
        } else {
            config = YamlConfiguration.loadConfiguration(new InputStreamReader(VeryConfig.class.getClassLoader().getResourceAsStream("auth.yml")));
            try {
                file.createNewFile();
                config.save(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        userid = config.getInt("userid");
        key = config.getString("key");
    }
}
