package catserver.server.remapper;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.Iterator;

public class QueueTrans implements IClassTransformer {
    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (! transformedName.equals("com.conversantmedia.util.concurrent.AbstractWaitingCondition"))
            return basicClass;
        ClassNode classNode = new ClassNode();
        new ClassReader(basicClass).accept(classNode, 0);
        for (MethodNode method : classNode.methods) {
            if (! method.name.equals("signal"))
                continue;
            Iterator<AbstractInsnNode> iterator = method.instructions.iterator();
            while (iterator.hasNext()) {
                AbstractInsnNode abNode = iterator.next();
                if (abNode instanceof MethodInsnNode && ((MethodInsnNode) abNode).owner.equals("java/util/concurrent/locks/LockSupport") && ((MethodInsnNode) abNode).name.equals("unpark")) {
                    method.instructions.set(abNode, new InsnNode(Opcodes.POP));
                }
            }
        }

        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        classNode.accept(classWriter);
        return classWriter.toByteArray();
    }
}
