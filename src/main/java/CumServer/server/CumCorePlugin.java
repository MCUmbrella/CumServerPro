package CumServer.server;

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;

import javax.annotation.Nullable;

import CumServer.server.remapper.NetworkTransformer;
import CumServer.server.remapper.SideTransformer;

import java.util.Map;

public class CumCorePlugin implements IFMLLoadingPlugin {
    @Override
    public String[] getASMTransformerClass() {
        return !CumServer.isDev() ? new String[] {
                NetworkTransformer.class.getCanonicalName(),
                SideTransformer.class.getCanonicalName()
        } : null;
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Nullable
    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {

    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}
