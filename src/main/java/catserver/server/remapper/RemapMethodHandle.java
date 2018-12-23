package catserver.server.remapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.util.HashMap;

public class RemapMethodHandle {
    private static HashMap<String, String> map = new HashMap<>();

    public static MethodHandle findSpecial(MethodHandles.Lookup lookup, Class<?> refc, String name, MethodType type, Class<?> specialCaller) throws NoSuchMethodException, IllegalAccessException {
        if (refc.getName().startsWith("net.minecraft.")) {
            name = RemapUtils.mapMethod(refc, name, type.parameterArray());
        }
        return lookup.findSpecial(refc, name, type, specialCaller);
    }

    public static MethodHandle findVirtual(MethodHandles.Lookup lookup, Class<?> refc, String name, MethodType type) throws NoSuchMethodException, IllegalAccessException {
        if (refc.getName().startsWith("net.minecraft.")) {
            name = RemapUtils.mapMethod(refc, name, type.parameterArray());
        }else if (refc.getName().equals("java.lang.Class")) {
            switch (name) {
                case "getField":
                case "getDeclaredField":
                case "getMethod":
                case "getDeclaredMethod":
                case "getSimpleName":
                    MethodHandle handle = lookup.findStatic(ReflectionVirtualMethod.class, name, type);
                    MethodHandleBinder.handles.add(handle);
                    return handle;
            }
        }
        return lookup.findVirtual(refc, name, type);
    }

    public static MethodHandle findStatic(MethodHandles.Lookup lookup, Class<?> refc, String name, MethodType type) throws NoSuchMethodException, IllegalAccessException {
        if (refc.getName().startsWith("net.minecraft.")) {
            name = RemapUtils.mapMethod(refc, name, type.parameterArray());
        } else if (refc.getName().equals("java.lang.Class") && name.equals("forName")) {
            refc = ReflectionMethods.class;
        }
        return lookup.findStatic(refc, name, type);
    }

    public static MethodType fromMethodDescriptorString(String descriptor, ClassLoader loader) {
        String remapDesc = map.getOrDefault(descriptor, descriptor);
        return MethodType.fromMethodDescriptorString(remapDesc, loader);
    }

    public static MethodHandle unreflect(MethodHandles.Lookup lookup, Method m) throws IllegalAccessException {
        if (m.getDeclaringClass().getName().equals("java.lang.Class")) {
            switch (m.getName()) {
                case "forName":
                    return getClassReflectionMethod(lookup, m.getName(), String.class);
                case "getField":
                case "getDeclaredField":
                    return getClassReflectionMethod(lookup, m.getName(), Class.class, String.class);
                case "getMethod":
                case "getDeclaredMethod":
                    return getClassReflectionMethod(lookup, m.getName(), Class.class, String.class, Class[].class);
            }
        }

        return lookup.unreflect(m);
    }

    private static MethodHandle getClassReflectionMethod(MethodHandles.Lookup lookup, String name, Class<?>... p) {
        try {
            return lookup.unreflect(ReflectionMethods.class.getMethod(name, p));
        } catch (NoSuchMethodException | IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }


    public static void loadMappings(BufferedReader reader) throws IOException {

        String line;
        while ((line = reader.readLine()) != null) {
            int commentIndex = line.indexOf('#');
            if (commentIndex != -1) {
                line = line.substring(0, commentIndex);
            }
            if (line.isEmpty() || !line.startsWith("MD: ")) {
                continue;
            }
            String[] sp = line.split("\\s+");
            String firDesc = sp[2];
            String secDesc = sp[4];
            map.put(firDesc, secDesc);
        }
    }

}
