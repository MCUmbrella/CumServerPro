package catserver.server.utils;

import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;

public class Int2ObjCached {
    public static boolean b = false;
    private Int2ObjectMap<Object> a;

    public void ininint() {
        if (!b)
            return;
         a = new Int2ObjectLinkedOpenHashMap<>();
    }

    public void addCache(int index, Object o) {
        a.put(index, o);
    }

    public void removeCache(int index) {
        a.remove(index);
    }

    public Object getCache(int index) {
        return a.get(index);
    }
}
