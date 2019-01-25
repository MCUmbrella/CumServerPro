package catserver.server.utils;

import net.minecraft.server.MinecraftServer;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;
import java.util.function.Predicate;

public class ThreadSafeList<E> extends Vector<E> {
    private static final String message = "插件/MOD尝试异步操作List已拦截,请与插件/MOD作者反馈!";
    private final boolean print;

    public ThreadSafeList(boolean print) {
        this.print = print;
    }

    @Override
    public boolean add(E e) {
        if (!Bukkit.isPrimaryThread()) {
            switchPrimaryThread(() -> super.add(e));
            if (print)
                new UnsupportedOperationException(message).printStackTrace();
            return true;
        }
        return super.add(e);
    }

    @Override
    public void add(int index, E element) {
        if (!Bukkit.isPrimaryThread()) {
            switchPrimaryThread(() -> super.add(index, element));
            if (print)
                new UnsupportedOperationException(message).printStackTrace();
            return;
        }
        super.add(index, element);
    }

    @Override
    public boolean remove(Object o) {
        if (!Bukkit.isPrimaryThread()) {
            switchPrimaryThread(() -> super.remove(o));
            if (print)
                new UnsupportedOperationException(message).printStackTrace();
            return super.contains(o);
        }
        return super.remove(o);
    }

    @Override
    public synchronized E remove(int index) {
        if (!Bukkit.isPrimaryThread()) {
            switchPrimaryThread(() -> super.remove(index));
            if (print)
                new UnsupportedOperationException(message).printStackTrace();
            return get(index);
        }
        return super.remove(index);
    }

    @Override
    public void clear() {
        if (!Bukkit.isPrimaryThread()) {
            switchPrimaryThread(() -> super.clear());
            if (print)
                new UnsupportedOperationException(message).printStackTrace();
            return;
        }
        super.clear();
    }

    @Override
    public synchronized boolean addAll(Collection<? extends E> c) {
        if (!Bukkit.isPrimaryThread()) {
            switchPrimaryThread(() -> super.addAll(c));
            if (print)
                new UnsupportedOperationException(message).printStackTrace();
            return true;
        }
        return super.addAll(c);
    }

    @Override
    public synchronized boolean addAll(int index, Collection<? extends E> c) {
        if (!Bukkit.isPrimaryThread()) {
            switchPrimaryThread(() -> super.addAll(index, c));
            if (print)
                new UnsupportedOperationException(message).printStackTrace();
            return true;
        }
        return super.addAll(index, c);
    }

    @Override
    public synchronized void addElement(E obj) {
        if (!Bukkit.isPrimaryThread()) {
            switchPrimaryThread(() -> super.addElement(obj));
            if (print)
                new UnsupportedOperationException(message).printStackTrace();
            return;
        }
        super.addElement(obj);
    }

    @Override
    public synchronized void removeElementAt(int index) {
        if (!Bukkit.isPrimaryThread()) {
            if (print)
                new UnsupportedOperationException(message).printStackTrace();
            return;
        }
        super.removeElementAt(index);
    }

    @Override
    public synchronized boolean removeAll(Collection<?> c) {
        if (!Bukkit.isPrimaryThread()) {
            if (print)
                new UnsupportedOperationException(message).printStackTrace();
            return false;
        }
        return super.removeAll(c);
    }

    @Override
    public synchronized void removeAllElements() {
        if (!Bukkit.isPrimaryThread()) {
            if (print)
                new UnsupportedOperationException(message).printStackTrace();
            return;
        }
        super.removeAllElements();
    }

    @Override
    public synchronized boolean removeElement(Object obj) {
        if (!Bukkit.isPrimaryThread()) {
            if (print)
                new UnsupportedOperationException(message).printStackTrace();
            return false;
        }
        return super.removeElement(obj);
    }

    @Override
    public synchronized boolean removeIf(Predicate<? super E> filter) {
        if (!Bukkit.isPrimaryThread()) {
            if (print)
                new UnsupportedOperationException(message).printStackTrace();
            return false;
        }
        return super.removeIf(filter);
    }

    @Override
    public synchronized Iterator<E> iterator() {
        if (!Bukkit.isPrimaryThread()) {
            if (print)
                new UnsupportedOperationException(message).printStackTrace();
            return new ArrayList<E>(this).iterator();
        }
        return super.iterator();
    }

    private void switchPrimaryThread(Runnable runnable) {
        MinecraftServer.getServerInst().addScheduledTask(runnable);
    }
}
