package catserver.server.very;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.net.NetworkInterface;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

import javax.net.ssl.HttpsURLConnection;

import catserver.server.update.Check;
import com.google.common.collect.Lists;
import com.google.gson.Gson;

import catserver.server.CatServer;
import catserver.server.remapper.ReflectionUtils;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.FMLLaunchHandler;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import org.apache.logging.log4j.util.Strings;

public final class VeryClient {
    private static final String warning = "请勿尝试破解,否则CatServer会患上狂猫病,对您造成不良影响!!!";
    public static VeryClient instance;

    // SSLManager.stop() 暗桩校验此处地址
    private String server = "https://pro.catserver.moe:8000/";
    private String server2 = "https://43.248.189.38:8000/";

    private native String auth(int userid, String key, String mac, Object c);

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
        Check check = new Check();
        check.check();
        Timer timer = new Timer();
        timer.schedule(check, 3600, 3600 * 1000);
        instance = new VeryClient();
        VeryConfig.load();

        int code = -1;
        try {
            UserInfo userinfo = new Gson().fromJson(instance.auth(VeryConfig.userid, VeryConfig.key, URLEncoder.encode(instance.getMACAddress()), Thread.currentThread().getContextClassLoader()), UserInfo.class);
            UserInfo.instance = userinfo;
            code = userinfo.code;
        } catch (Exception e) {
            System.out.println("验证过程发生错误: " + e.toString());
        }

        switch(code) {
        case 100:
            if (!Strings.isEmpty(UserInfo.instance.message))
                System.out.println(UserInfo.instance.message);
            Runtime.getRuntime().addShutdownHook(new Thread(()-> VeryClient.instance.logout()));
            new Timer().schedule(new TimerTask() {
                int failCount = 0;
                public void run() {
                    if (!VeryClient.instance.keepAlive()) {
                        failCount++;
                        System.out.println("授权服务器心跳包连接失败,重试次数: %c/15".replace("%c", String.valueOf(failCount)));
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
            return;
        case 101:
            System.out.println("授权已到期或被限制!");
            break;
        case 102:
            System.out.println("该授权已在其他IP使用,更换IP请等待一段时间!");
            break;
        case -2:
            System.out.println("验证被拒绝,请检查验证文件(auth.yml)是否正确!");
            break;
        default:
            System.out.println("验证失败,请检查网络: " + code);
            break;
        }
        FMLCommonHandler.instance().handleExit(0);
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

    private String sendRequest(String parms) throws Exception {
        try {
            return sendRequest0(server, parms);
        } catch (IOException e) {
            return sendRequest0(server2, parms);
        }
    }

    private String sendRequest0(String server, String parms) throws Exception {
        HttpsURLConnection connection = (HttpsURLConnection) new URL(server + "?" + parms).openConnection();
        connection.setSSLSocketFactory(SSLManager.getSocketFactory());
        connection.setHostnameVerifier(new SSLManager());
        connection.setRequestProperty("accept", "*/*");
        connection.setRequestProperty("connection", "Close");
        connection.setRequestProperty("user-agent", "CatServer/VeryClient");
        connection.connect();

        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
        String result = "";
        String line;
        while ((line = in.readLine()) != null) {
            result += line;
        }
        return result;
    }

    private void safeStopServer() {
        try {
            FMLLaunchHandler fmlLaunch = ReflectionHelper.getPrivateValue(FMLLaunchHandler.class, null, "INSTANCE");
            ClassLoader cl = ReflectionHelper.getPrivateValue(FMLLaunchHandler.class, fmlLaunch, "classLoader");
            Class<?> serverClass = Class.forName("net.minecraft.server.MinecraftServer", false, cl);
            Object mcServer = serverClass.getMethod("getServerInst").invoke(null);
            serverClass.getMethod(CatServer.isDev() ? "addScheduledTask" : "func_152344_a", Runnable.class).invoke(mcServer, new Runnable() {
                public void run() {
                    try {
                        serverClass.getMethod(CatServer.isDev() ? "stopServer" : "func_71260_j").invoke(mcServer);
                    } catch (Exception e) {}
                    FMLCommonHandler.instance().handleExit(0);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            FMLCommonHandler.instance().handleExit(0);
        }
    }

    public static native void startThread(Object l, Object c, Class m, Class n);

    public static native void stopThread();

    public static native void initAsync(Object obj);
}
