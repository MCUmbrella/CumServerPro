package catserver.server.utils.container;

import it.unimi.dsi.fastutil.longs.*;
import it.unimi.dsi.fastutil.objects.*;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

public class ConcurrentLong2ObjectCHashMap<V> implements Long2ObjectMap<V> {
    ConcurrentHashMap<Long, V> coreMap = new ConcurrentHashMap<>(8192);
    ConcurrentSkipListSet keys = new ConcurrentSkipListSet();
    private volatile V def = null;

    @Override
    public V put(long key, V value) {
        keys.add(key);
        return coreMap.put(key, value);
    }

    @Override
    public V get(long key) {
        return coreMap.get(key);
    }

    @Override
    public V remove(long key) {
        keys.remove(key);
        return coreMap.remove(key);
    }

    @Override
    public boolean containsKey(long key) {
        return keys.contains(key);
    }

    @Override
    public void defaultReturnValue(V rv) {
        def = rv;
    }

    @Override
    public V defaultReturnValue() {
        return def;
    }

    @Override
    public V put(Long key, V value) {
        keys.add(key);
        return coreMap.put(key, value);
    }

    @Override
    public V get(Object key) {
        return coreMap.get(key);
    }

    @Override
    public boolean containsKey(Object key) {
        return keys.contains(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return coreMap.containsValue(value);
    }

    @Override
    public V remove(Object key) {
        keys.remove(key);
        return coreMap.remove(key);
    }

    @Override
    public void putAll(Map<? extends Long, ? extends V> m) {
        keys.addAll(m.keySet());
        coreMap.putAll(m);
    }

    @Override
    public int size() {
        return coreMap.size();
    }

    @Override
    public boolean isEmpty() {
        return coreMap.isEmpty();
    }

    @Override
    public void clear() {
        keys.clear();
        coreMap.clear();
    }

    @Override
    public ObjectSet<Map.Entry<Long, V>> entrySet() {
        return new AtomEntrySet();
    }

    @Override
    public ObjectSet<Entry<V>> long2ObjectEntrySet() {
        return new CatObjectEntrySet();
    }

    @Override
    public LongSet keySet() {
        final Set<Long> sets = coreMap.keySet();
        return new AbstractLongSet() {
            @Override
            public LongIterator iterator() {
                final Iterator<Long> iterator = sets.iterator();
                return new AbstractLongIterator() {
                    long thisLong = 0;

                    @Override
                    public boolean hasNext() {
                        return iterator.hasNext();
                    }

                    @Override
                    public long nextLong() {
                        return thisLong = iterator.next();
                    }

                    @Override
                    public Long next() {
                        return thisLong = iterator.next();
                    }

                    @Override
                    public void remove() {
                        keys.remove(thisLong);
                        iterator.remove();
                    }
                };
            }

            @Override
            public int size() {
                return sets.size();
            }
        };
    }

    @Override
    public ObjectCollection<V> values() {
        return new CatObjectSet();
    }

    public class CatObjectEntrySet extends AbstractObjectSet<Entry<V>> {
        @Override
        public ObjectIterator<Entry<V>> iterator() {
            final Iterator<Map.Entry<Long, V>> iterator = coreMap.entrySet().iterator();
            return new AbstractObjectIterator<Entry<V>>() {
                long thisLong = 0;
                @Override
                public boolean hasNext() {
                    return iterator.hasNext();
                }

                @Override
                public Entry<V> next() {
                    final Map.Entry<Long, V> entry = iterator.next();
                    this.thisLong = entry.getKey();
                    return new Entry<V>() {
                        @Override
                        public Long getKey() {
                            return entry.getKey();
                        }

                        @Override
                        public V getValue() {
                            return entry.getValue();
                        }

                        @Override
                        public V setValue(V value) {
                            return entry.setValue(value);
                        }

                        @Override
                        public boolean equals(Object o) {
                            return entry.equals(o);
                        }

                        @Override
                        public int hashCode() {
                            return entry.hashCode();
                        }

                        @Override
                        public long getLongKey() {
                            return entry.getKey();
                        }
                    };
                }

                @Override
                public void remove() {
                    keys.remove(thisLong);
                    iterator.remove();
                }
            };
        }

        @Override
        public int size() {
            return coreMap.size();
        }
    }

    public class CatObjectSet extends AbstractObjectSet<V> {
        @Override
        public ObjectIterator<V> iterator() {
            final Iterator<Map.Entry<Long, V>> iterator = new AtomEntrySet().iterator();
            return new AbstractObjectIterator<V>() {
                long thisLong = 0;
                @Override
                public boolean hasNext() {
                    return iterator.hasNext();
                }

                @Override
                public V next() {
                    Map.Entry<Long, V> entry = iterator.next();
                    thisLong = entry.getKey();
                    return entry.getValue();
                }

                @Override
                public void remove() {
                    keys.remove(thisLong);
                    iterator.remove();
                }
            };
        }

        @Override
        public int size() {
            return 0;
        }
    }

    public class AtomEntrySet extends AbstractObjectSet<Map.Entry<Long, V>> {

        @Override
        public ObjectIterator<Map.Entry<Long, V>> iterator() {
            final Iterator<Map.Entry<Long, V>> iterator = coreMap.entrySet().iterator();
            return new AbstractObjectIterator<Map.Entry<Long, V>>() {
                private long thisLong = 0;

                @Override
                public boolean hasNext() {
                    return iterator.hasNext();
                }

                @Override
                public Map.Entry<Long, V> next() {
                    Map.Entry<Long, V> entry = iterator.next();
                    thisLong = entry.getKey();
                    return entry;
                }

                @Override
                public void remove() {
                    keys.remove(thisLong);
                    iterator.remove();
                }
            };
        }

        @Override
        public int size() {
            return coreMap.entrySet().size();
        }
    }
}
