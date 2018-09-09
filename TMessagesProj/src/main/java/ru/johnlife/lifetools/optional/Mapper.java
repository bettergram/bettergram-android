package ru.johnlife.lifetools.optional;

import android.util.SparseArray;

public class Mapper<T> {
    private SparseArray<T> map = new SparseArray<>();
    private T defaultValue = null;

    public Mapper<T> add(String key, T value) {
        return add(key.hashCode(), value);
    }

    public Mapper<T> add(int key, T value) {
        map.put(key, value);
        return this;
    }

    public Mapper<T> defaultValue(T value) {
        defaultValue = value;
        return this;
    }

    public MappingResult performOn(String key, Action<T> action) {
        return performOn(key.hashCode(), action);
    }

    public MappingResult performOn(int key, Action<T> action) {
        return get(key).ifPresent(action::applyTo);
    }

    public Iffy<T> get(String key) {
        return get(key.hashCode());
    }

    public Iffy<T> get(int key) {
        T value = map.get(key);
        if (value == null) {
            value = defaultValue;
        }
        return Iffy.from(value);
    }

    public Iffy<Integer> findKeyFor(T value) {
        if (value == null) return Iffy.empty();
        for (int i = 0; i < map.size(); i++) {
            if (value.equals(map.valueAt(i))) {
                return Iffy.from(map.keyAt(i));
            }
        }
        return Iffy.empty();
    }

    public boolean isEmpty() {
        return map.size() == 0 || defaultValue != null;
    }
}
