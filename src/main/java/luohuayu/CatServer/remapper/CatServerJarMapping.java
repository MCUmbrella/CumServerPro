package luohuayu.CatServer.remapper;

import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import net.md_5.specialsource.InheritanceMap;
import net.md_5.specialsource.JarMapping;
import net.md_5.specialsource.NodeType;
import net.md_5.specialsource.provider.InheritanceProvider;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

public class CatServerJarMapping extends JarMapping {

    private InheritanceMap _inheritanceMap = null;
    private InheritanceProvider _fallbackInheritanceProvider = null;
    private Set<String> _excludedPackages = null;

    public CatServerJarMapping() {
        super();

        this._inheritanceMap = ReflectionHelper.getPrivateValue(JarMapping.class, this, "inheritanceMap");
        this._excludedPackages = ReflectionHelper.getPrivateValue(JarMapping.class, this, "excludedPackages");
    }

    @Override
    public void setInheritanceMap(InheritanceMap inheritanceMap) {
        super.setInheritanceMap(inheritanceMap);
        this._inheritanceMap = inheritanceMap;
    }

    @Override
    public void setFallbackInheritanceProvider(InheritanceProvider fallbackInheritanceProvider) {
        super.setFallbackInheritanceProvider(fallbackInheritanceProvider);
        this._fallbackInheritanceProvider = fallbackInheritanceProvider;
    }

    public String trydeClimb(Map<String, String> map, NodeType type, String owner, String name, int access) {
        for(Map.Entry<String, String> sEntry : map.entrySet()) {
            if(sEntry.getValue().equals(name)) {
                int tIndex = sEntry.getKey().lastIndexOf('/');
                String tOwner = tIndex == -1 ? "" : sEntry.getKey().substring(0, tIndex);
                if(tOwner.equals(owner)) {
                    String tName = sEntry.getKey().substring(tIndex == -1 ? 0 : tIndex + 1);
                    if(type == NodeType.METHOD) {
                        tName = tName.split(" ", 2)[0];
                    }
                    return tName;
                }
            }
        }

        if((access == -1 || (!Modifier.isPrivate(access) && !Modifier.isStatic(access)))) {
            Collection<String> parents = null;

            if(_inheritanceMap.hasParents(owner)) {
                parents = _inheritanceMap.getParents(owner);
            }else if(_fallbackInheritanceProvider != null) {
                parents = _fallbackInheritanceProvider.getParents(owner);
                _inheritanceMap.setParents(owner, parents);
            }

            if(parents != null) {
                // declimb the inheritance tree
                for(String parent : parents) {
                    String demapped = trydeClimb(map, type, parent, name, access);
                    if(demapped != null) {
                        return demapped;
                    }
                }
            }
        }
        return null;
    }

}
