package catserver.server.remapper;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ReflectionVirtualMethod {
    private final Class cls;

    public ReflectionVirtualMethod(Class cls) {
        this.cls = cls;
    }

    public Field getField(String name) throws NoSuchFieldException, SecurityException {
        return ReflectionMethods.getField(cls, name);
    }

    public Field getDeclaredField(String name) throws NoSuchFieldException, SecurityException {
        return ReflectionMethods.getDeclaredField(cls, name);
    }

    public Method getMethod(String name, Class<?>... parameterTypes) throws NoSuchMethodException, SecurityException {
        return ReflectionMethods.getMethod(cls, name, parameterTypes);
    }

    public Method getDeclaredMethod(String name, Class<?>... parameterTypes) throws NoSuchMethodException, SecurityException {
        return ReflectionMethods.getDeclaredMethod(cls, name, parameterTypes);
    }

    public String getSimpleName() {
        return ReflectionMethods.getSimpleName(cls);
    }

}
