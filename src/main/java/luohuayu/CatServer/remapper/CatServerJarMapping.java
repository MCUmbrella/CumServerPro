package luohuayu.CatServer.remapper;

import java.util.Map;
import java.util.Set;

import org.objectweb.asm.Type;

import com.google.common.base.Objects;

import net.md_5.specialsource.InheritanceMap;
import net.md_5.specialsource.JarMapping;
import net.md_5.specialsource.JarRemapper;
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

    public String trydeClimb(Map<String, String> map, NodeType type, String owner, String name, String desc, int access) {
        for (Map.Entry<String, String> sEntry : map.entrySet()) {
            if (sEntry.getValue().equals(name)) {
                String tSign = sEntry.getKey(), tDesc = null;
                if (type == NodeType.METHOD) {
                    String[] tInfo = tSign.split(" ");
                    tSign = tInfo[0];
                    tDesc = tInfo.length > 1 ? remapDesc(tInfo[1]) : tDesc;
                }

                int tIndex = tSign.lastIndexOf('/');
                String tOwner = this.mapClass(tSign.substring(0, tIndex == -1 ? tSign.length() : tIndex));
                if (tOwner.equals(owner) && (Objects.equal(desc, tDesc))) {
                    return tSign.substring(tIndex == -1 ? 0 : tIndex + 1);
                }
            }
        }

        return null;
    }

    /**
     * remap Bukkit format to Forge
     * 
     * @param pMethodDesc
     *            Bukkit Method Desc
     * @return Forge Method Desc
     */
    public String remapDesc(String pMethodDesc) {
        Type[] tTypes = Type.getArgumentTypes(pMethodDesc);
        for (int i = tTypes.length - 1; i >= 0; i--) {
            String tTypeDesc = tTypes[i].getDescriptor();
            if (tTypeDesc.endsWith(";")) {
                int tIndex = tTypeDesc.indexOf("L");
                String tMappedName = this.mapClass(tTypeDesc.substring(tIndex + 1, tTypeDesc.length() - 1));
                tMappedName = "L" + tMappedName + ";";
                if (tIndex > 0 && tIndex != 0) {
                    tMappedName = tTypeDesc.substring(0, tIndex);
                }

                tTypes[i] = Type.getType(tMappedName);
            }

        }
        return Type.getMethodDescriptor(Type.getType(mapClass(getTypeDesc(Type.getReturnType(pMethodDesc)))), tTypes);
    }

    public static final String NMS_PREFIX = "net/minecraft/server/";
    public static final String NMS_VERSION = "v1_12_R1";

    public String mapClass(String pBukkitClass) {
        String tRemapped = JarRemapper.mapTypeName(pBukkitClass, this.packages, this.classes, pBukkitClass);
        if (tRemapped.equals(pBukkitClass) && pBukkitClass.startsWith(NMS_PREFIX) && !pBukkitClass.contains(NMS_VERSION)) {
            String tNewClassStr = NMS_PREFIX + NMS_VERSION + "/" + pBukkitClass.substring(NMS_PREFIX.length());
            return JarRemapper.mapTypeName(tNewClassStr, this.packages, this.classes, pBukkitClass);
        }
        return tRemapped;
    }

    public String getTypeDesc(Type pType) {
        try {
            return pType.getInternalName();
        } catch (NullPointerException ignore) {
            return pType.toString();
            // TODO: handle exception
        }
    }

}
