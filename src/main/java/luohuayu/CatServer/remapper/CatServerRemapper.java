package luohuayu.CatServer.remapper;

import net.md_5.specialsource.JarMapping;
import net.md_5.specialsource.JarRemapper;
import net.md_5.specialsource.NodeType;

public class CatServerRemapper extends JarRemapper {

    public CatServerRemapper(JarMapping jarMapping) {
        super(jarMapping);
    }

    public String mapSignature(String signature, boolean typeSignature) {
        try {
            return super.mapSignature(signature, typeSignature);
        } catch (Exception e) {
            return signature;
        }
    }

    public String demapFieldName(String owner, String name, int access) {
        String mapped = ReflectionTransformer.jarMapping.trydeClimb(ReflectionTransformer.fieldDeMapping, NodeType.FIELD, owner, name, null, access);
        return mapped == null ? name : mapped;
    }

    public String demapMethodName(String owner, String name, String desc, int access) {
        String mapped = ReflectionTransformer.jarMapping.trydeClimb(ReflectionTransformer.methodDeMapping, NodeType.METHOD, owner, name, desc, access);
        return mapped == null ? name : mapped;
    }

}
