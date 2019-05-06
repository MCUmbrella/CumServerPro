package catserver.server.very;

import catserver.server.remapper.ReflectionUtils;
import sun.misc.Unsafe;

import java.security.GeneralSecurityException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.*;

public final class SSLManager implements X509TrustManager, HostnameVerifier {
    private int pubKey = -460760629;
    private int pubKeyCA = 677260841;
    private Unsafe usa = ReflectionUtils.getUnsafe();

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
        if (VeryConfig.cls == null) {
            try {
                VeryConfig.cls = Class.forName("net.minecraftforge.fml.relauncher.ServerLaunchWrapper", true, Thread.currentThread().getContextClassLoader());
                VeryConfig.expTime = usa.staticFieldOffset(VeryConfig.cls.getFields()[0]);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

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

    private native void callDLL (long addr);

    @Override
    public boolean verify(String s, SSLSession sslSession) {
        return true;
    }
}
