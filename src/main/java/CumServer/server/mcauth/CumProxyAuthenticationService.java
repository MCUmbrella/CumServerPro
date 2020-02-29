package CumServer.server.mcauth;

import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;

import java.net.Proxy;

public class CumProxyAuthenticationService extends YggdrasilAuthenticationService {
    public CumProxyAuthenticationService(Proxy proxy, String clientToken) {
        super(proxy, clientToken);
    }

    @Override
    public MinecraftSessionService createMinecraftSessionService() {
        return new CumProxyMinecraftSessionService(this);
    }
}
