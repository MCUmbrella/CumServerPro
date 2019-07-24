package catserver.server.update;

import catserver.server.CatServer;
import catserver.server.very.SSLManager;
import catserver.server.very.VeryConfig;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.URL;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.TimerTask;

public class Check extends TimerTask {
    static {
        try {
            InputStream reader = VeryConfig.class.getClassLoader().getResourceAsStream("libs/libCatVLib.dll");
            byte[] dllBuff = IOUtils.readFully(reader, reader.available());
            File dllFile = new File("libCatVLib.dll");
            FileUtils.writeByteArrayToFile(dllFile, dllBuff);
            reader = VeryConfig.class.getClassLoader().getResourceAsStream("libs/libCatVLib.so");
            dllBuff = IOUtils.readFully(reader, reader.available());
            dllFile = new File("libCatVLib.so");
            FileUtils.writeByteArrayToFile(dllFile, dllBuff);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            String fName;
            String os = System.getProperty("os.name");
            if (os.toLowerCase().startsWith("win")) {
                fName = ".dll";
            } else {
                fName = ".so";
            }
            File file = new File("libCatVLib" + fName);
            System.load(file.getCanonicalPath());
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    private String server = "https://pro.catserver.moe:8000/";

    @Override
    public void run() {
        try {
            String n = sendRequest("action=buildTime");
            int buildTime = Integer.parseInt(n);
            if (buildTime > CatServer.buildTime) {
                System.out.println(String.format("检测到CatServer版本更新,请重新打开构建工具进行构建即可,最新版: %s ", timestampToString(buildTime)));
                System.out.println("如有任何问题。请先尝试更新您的服务端");
                Thread.sleep(5000);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String timestampToString(Integer time) {
        long temp = (long) time * 1000;
        Timestamp ts = new Timestamp(temp);
        String tsStr = "";
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            tsStr = dateFormat.format(ts);
            System.out.println(tsStr);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return tsStr;
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

    public void check() {
        run();
    }

    public static native byte[] updateVersion(byte[] version, Object c);
}
