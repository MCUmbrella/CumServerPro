package catserver.server.update;

import catserver.server.very.SSLManager;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.TimerTask;

public class Check extends TimerTask {
    private String server = "https://pro.catserver.moe:8000/";

    @Override
    public void run() {
        String v = Check.class.getPackage().getImplementationVersion();
        try {
            String n = sendRequest("action=version");
            if (!n.equals(v)) {
                System.out.println("检测到CatServer版本更新: 最新版: " + n);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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
        String v = Check.class.getPackage().getImplementationVersion();
        try {
            String n = sendRequest("action=version");
            if (!n.equals(v)) {
                System.out.println("检测到CatServer版本更新, 请更新. 最新版: " + n);
                System.exit(1);
            }else {
                System.out.println("CatServer更新检测完毕, 已经是最新版本: " + n);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
