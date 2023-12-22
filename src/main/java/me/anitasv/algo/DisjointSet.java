package me.anitasv.algo;

import java.util.*;

public class DisjointSet<T> {

    private final Map<T, T> map = new HashMap<>();

    public void add(T val) {
        map.put(val, val);
    }

    public T findCanonical(T val) {
        T parent = map.get(val);
        if (!parent.equals(val)) {
            T canonical = findCanonical(parent);
            map.put(val, canonical);
            return canonical;
        }
        return parent;
    }

    public void union(T a, T b) {
        T aCan = findCanonical(a);
        T bCan = findCanonical(b);
        if (aCan == bCan) {
            return;
        }
        // Make b a's parent.
        map.put(a, b);
    }

    public Set<T> ogElements() {
        return map.keySet();
    }
}
