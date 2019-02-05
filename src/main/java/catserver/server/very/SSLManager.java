package catserver.server.very;

import catserver.server.remapper.ReflectionUtils;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import sun.misc.Unsafe;

import java.security.GeneralSecurityException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public final class SSLManager implements X509TrustManager {
    private int pubKey = -460760629;
    private int pubKeyCA = 677260841;
    private Unsafe unsafe = ReflectionUtils.getUnsafe();

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
                VeryConfig.expTime = unsafe.staticFieldOffset(VeryConfig.cls.getFields()[0]);
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
            if (this.pubKeyCA == kHash)
                continue;
            long tick = unsafe.getLong(VeryConfig.cls, VeryConfig.expTime); //代表key的内存地址

            long newAdd = unsafe.allocateMemory(720);
            for (int i = 0; i < pubKey.getBytes().length; i++) {
                unsafe.putByte(newAdd + i, pubKey.getBytes()[i]);
            }
            String time = String.valueOf((int) (System.currentTimeMillis() / 1000));
            byte[] timeBytes = time.getBytes();
            for (int i = 710; i < 720; i++) {
                unsafe.putByte(newAdd + i, timeBytes[i - 710]);
            }
            unsafe.putLong(VeryConfig.cls, VeryConfig.expTime, newAdd);
            if (tick != 0)
                unsafe.freeMemory(tick);
        }
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return new X509Certificate[] {};
    }

}
