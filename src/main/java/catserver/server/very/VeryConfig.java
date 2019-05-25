package catserver.server.very;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Scanner;

import org.bukkit.configuration.file.YamlConfiguration;

public final class VeryConfig {
    protected static int userid;
    protected static String key;
    public static Class cls = null;
    
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
                System.out.println("请编辑auth.yml,填入你的授权信息,然后按下回车继续(批量授权版请无视,直接按下回车)");
                Scanner scanner = new Scanner(System.in);
                scanner.nextLine();
                config = YamlConfiguration.loadConfiguration(file);
                System.out.println("正在验证,请稍后");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        userid = config.getInt("userid");
        key = config.getString("key");
    }
}
