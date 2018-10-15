package luohuayu.CatServer.remapper;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.objectweb.asm.Type;

import luohuayu.CatServer.CatServer;

public class ReflectionMethods {

    public static Class<?> forName(String className) throws ClassNotFoundException {
        return forName(className, true, ReflectionUtils.getCallerClassloader());
    }

    public static Class<?> forName(String className, boolean initialize, ClassLoader classLoader) throws ClassNotFoundException {
        if (!className.startsWith("net.minecraft.server." + CatServer.getNativeVersion())) return Class.forName(className, initialize, classLoader);
        className = ReflectionTransformer.jarMapping.classes.get(className.replace('.', '/')).replace('/', '.');
        return Class.forName(className, initialize, classLoader);
    }

    // Get Fields
    public static Field getField(Class<?> inst, String name) throws NoSuchFieldException, SecurityException {
        return inst.getField(ReflectionTransformer.remapper.mapFieldName(RemapUtils.reverseMap(inst), name, null));
    }

    public static Field getDeclaredField(Class<?> inst, String name) throws NoSuchFieldException, SecurityException {
        return inst.getDeclaredField(ReflectionTransformer.remapper.mapFieldName(RemapUtils.reverseMap(inst), name, null));
    }

    // Get Methods
    public static Method getMethod(Class<?> inst, String name, Class<?>...parameterTypes) throws NoSuchMethodException, SecurityException {
        return inst.getMethod(RemapUtils.mapMethod(inst, name, parameterTypes), parameterTypes);
    }

    public static Method getDeclaredMethod(Class<?> inst, String name, Class<?>...parameterTypes) throws NoSuchMethodException, SecurityException {
        return inst.getDeclaredMethod(RemapUtils.mapMethod(inst, name, parameterTypes), parameterTypes);
    }

    public static String demapField(Field pField) {
        if (!pField.getDeclaringClass().getName().startsWith("net/minecraft")) return pField.getName();

        return ReflectionTransformer.remapper.demapFieldName(Type.getInternalName(pField.getDeclaringClass()),
                pField.getName(),
                pField.getModifiers());
    }

    public static String demapMethod(Method pMethod) {
        if (!pMethod.getDeclaringClass().getName().startsWith("net.minecraft")) return pMethod.getName();
        try {
            return ReflectionTransformer.remapper.demapMethodName(Type.getInternalName(pMethod.getDeclaringClass()),
                    pMethod.getName(),
                    Type.getMethodDescriptor(pMethod),
                    pMethod.getModifiers());
        } catch (Throwable exp) {
            exp.printStackTrace();
        }
        return pMethod.getName();
    }

    public static Class<?> getClass(String pClazzName) throws ClassNotFoundException {
        return getClass((ClassLoader)null, pClazzName);
    }
    
    public static Class<?> getClass(ClassLoader pLoader, String pClazzName) throws ClassNotFoundException {
        String tMappedClass = ReflectionTransformer.jarMapping.mapClass(pClazzName.replace('.', '/'));
        return pLoader == null ? Class.forName(tMappedClass) : pLoader.loadClass(tMappedClass);
    }
}
