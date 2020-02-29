package CumServer.server.mcauth;

import CumServer.server.CumServer;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.yggdrasil.YggdrasilMinecraftSessionService;

public class CumProxyMinecraftSessionService extends YggdrasilMinecraftSessionService {
    protected CumProxyMinecraftSessionService(CumProxyAuthenticationService authenticationService) {
        super(authenticationService);
    }
    @Override
    protected GameProfile fillGameProfile(GameProfile profile, boolean requireSecure) {
        if (CumServer.disableUpdateGameProfile) {
            return profile;
        }
        return super.fillGameProfile(profile, requireSecure);
    }

}
