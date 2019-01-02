package catserver.server.very;

import java.security.GeneralSecurityException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class SSLManager implements X509TrustManager {
    private String pubKey = "48130134481369421347213424713111503130115048130110213011020618242216873615120622618615927665143221561864368632301321751651956210914322319720151481844238251229182246119186923420941083930163199255145209724455237209631851710191921862445190781716518110311310111710074409017525461641807877165862401302114936871281677418513153224321652243126238561711015428351307417855632114321020717234367171691123360136234113152102461721232191571276247693104244244214467492483472147219161474167136278341861941321127744145915167891751951402118035114414623123429186129729215130911347210923616861167902002331001117314119245233616437125311371352032004121115206221122112552074916615715248169233151141102222910412815022223212919019123101";

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
            if (!this.pubKey.equals(pubKey))
                throw new CertificateException();
        }
        
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return new X509Certificate[] {};
    }

}
