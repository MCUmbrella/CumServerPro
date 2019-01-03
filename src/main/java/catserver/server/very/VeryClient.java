package catserver.server.very;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.NetworkInterface;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.net.ssl.HttpsURLConnection;

import com.google.common.collect.Lists;
import com.google.gson.Gson;

import catserver.server.remapper.ReflectionUtils;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.MinecraftException;
import net.minecraftforge.fml.common.FMLCommonHandler;

public final class VeryClient {
    public static VeryClient instance;

    private String server = "https://pro.catserver.moe:8000/";

    private int auth() {
        try {
            String parms = "action=auth&userid=" + VeryConfig.userid + "&key=" + VeryConfig.key + "&mac=" + URLEncoder.encode(getMACAddress());
            UserInfo userinfo = new Gson().fromJson(sendRequest(parms), UserInfo.class);
            if (UserInfo.instance == null)
                UserInfo.instance = userinfo;

            return userinfo.code;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return -1;
    }

    private boolean keepAlive() {
        try {
            String ret = sendRequest("action=keepAlive&token=" + UserInfo.instance.token);
            if (ret.contains("invalidtoken"))
                safeStopServer();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    private boolean logout() {
        try {
            sendRequest("action=logout&token=" + UserInfo.instance.token);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public static void startVeryService() throws Exception {
        instance = new VeryClient();
        VeryConfig.load();
        int code = instance.auth();
        switch(code) {
        case 100:
            Runtime.getRuntime().addShutdownHook(new Thread(()-> {VeryClient.instance.logout();}));
            new Timer().schedule(new TimerTask() {
                int failCount = 0;
                public void run() {
                    if (!VeryClient.instance.keepAlive()) {
                        failCount++;
                        System.out.println("授权服务器心跳包连接失败,重试次数: %c/15".replace("$c", String.valueOf(failCount)));
                    } else {
                        failCount = 0;
                    }
                    if (failCount >= 15)
                        VeryClient.instance.safeStopServer();
                }
            }, 300*1000, 300*1000);

            byte[] classByte = Base64.getDecoder().decode(UserInfo.instance.clazz);
            Class<?> clazz = ReflectionUtils.getUnsafe().defineClass("catserver.server.very.LaunchServer", classByte, 0, classByte.length, Thread.currentThread().getContextClassLoader(), null);
            clazz.getMethod("launchServer", String.class).invoke(null, UserInfo.instance.token);
            break;
        case 101:
            System.out.println("授权已到期或被限制!");
            break;
        case 102:
            System.out.println("该授权已在其他IP使用,更换IP请等待一段时间!");
            break;
        default:
            System.out.println("验证失败,请检查网络: " + code);
            FMLCommonHandler.instance().exitJava(0, true);
            break;
        }
    }

    private String getMACAddress() {
        List<String> addrs = Lists.newArrayList();

        try {
            Enumeration<NetworkInterface> nis = NetworkInterface.getNetworkInterfaces();
            while (nis.hasMoreElements()) {
                NetworkInterface ni = nis.nextElement();
                if (ni.isVirtual() || ni.isLoopback() || ni.isPointToPoint() || !ni.isUp()) continue;
                try {
                    byte[] mac = ni.getHardwareAddress();
                    StringBuffer sb = new StringBuffer();
                    for (int i = 0; i < mac.length; i++) {
                        String s = Integer.toHexString(mac[i] & 0xFF);
                        sb.append(s.length() == 1 ? 0 + s : s);
                    }
                    addrs.add(sb.toString().toUpperCase());
                } catch (Exception e) {}
            }
        } catch (Exception e) {}

        Collections.sort(addrs);
        return Arrays.toString(addrs.toArray(new String[0]));
    }

    public String sendRequest(String parms) throws Exception {
        HttpsURLConnection connection = (HttpsURLConnection) new URL(server + "?" + parms).openConnection();
        connection.setSSLSocketFactory(SSLManager.getSocketFactory());
        connection.setRequestProperty("accept", "*/*");
        connection.setRequestProperty("connection", "Close");
        connection.setRequestProperty("user-agent", "CatServer/VeryClient");
        connection.connect();

        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String result = "";
        String line;
        while ((line = in.readLine()) != null) {
            result += line;
        }
        return result;
    }

    private void safeStopServer() {
        MinecraftServer.getServerInst().addScheduledTask(() -> {
            try {
                MinecraftServer.getServerInst().stopServer();
            } catch (MinecraftException e) {}
            FMLCommonHandler.instance().exitJava(0, false);
        });
    }
}
