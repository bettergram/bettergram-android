package ru.johnlife.lifetools.optional;

import android.util.SparseIntArray;

import static ru.johnlife.lifetools.essentials.Constants.NO_VALUE;

public class MapperInt {
    private SparseIntArray map = new SparseIntArray();
    private int defaultValue = NO_VALUE;

    public MapperInt add(String key, int value) {
        return add(key.hashCode(), value);
    }

    public MapperInt add(int key, int value) {
        map.put(key, value);
        return this;
    }

    public MapperInt defaultValue(int value) {
        defaultValue = value;
        return this;
    }

    public MappingResult performOn(String key, ActionInt action) {
        return performOn(key.hashCode(), action);
    }

    public MappingResult performOn(int key, ActionInt action) {
        int value = map.get(key, NO_VALUE);
        if (value == NO_VALUE) {
            value = defaultValue;
        }
        if (value != NO_VALUE) {
            action.applyTo(value);
            return MappingResult.SUCCESS;
        }
        return MappingResult.FAIL;
    }

    /**
     * DON'T USE THIS UNLESS YOU REALLY HAVE TO
     * It does autoboxing int to Integer, so in intense usage it
     * will drain all your memory in couple of seconds
     * */
    public Iffy<Integer> get(String key) {
        return get(key.hashCode());
    }
    /**
     * DON'T USE THIS UNLESS YOU REALLY HAVE TO
     * It does autoboxing int to Integer, so in intense usage it
     * will drain all your memory in couple of seconds
     * */
    public Iffy<Integer> get(int key) {
        int value = map.get(key);
        if (value == NO_VALUE) {
            value = defaultValue;
        }
        return Iffy.from(value);
    }


    public Iffy<Integer> findKeyFor(int value) {
        if (value == NO_VALUE) return Iffy.empty();
        for (int i = 0; i < map.size(); i++) {
            if (value == map.valueAt(i)) {
                return Iffy.from(map.keyAt(i));
            }
        }
        return Iffy.empty();
    }

    public boolean isEmpty() {
        return map.size() == 0 || defaultValue != NO_VALUE;
    }

}
