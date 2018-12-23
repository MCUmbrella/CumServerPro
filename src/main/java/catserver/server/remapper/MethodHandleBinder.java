package catserver.server.remapper;

import java.lang.invoke.MethodHandle;
import java.util.ArrayList;
import java.util.List;

public class MethodHandleBinder {

    public static List<MethodHandle> handles = new ArrayList<>();

    public static MethodHandle bindTo(MethodHandle methodHandle, Object obj) {
        if (obj instanceof Class && handles.contains(methodHandle)) {
            handles.remove(methodHandle);
            return methodHandle.bindTo(new ReflectionVirtualMethod((Class) obj));
        }
        return methodHandle.bindTo(obj);
    }
}
