package ru.johnlife.lifetools.optional;

public class Iffy<V> {
    public interface ValueProvider<T> {
        T getValue();
    }

    public static <V> Iffy<V> from(V value) {
        return new Iffy<>(value);
    }

    public interface Action<S> {
        void apply(S value);
    }

    private V value;

    public Iffy(V value) {
        this.value = value;
    }

    public Iffy<V> defaultValue(V defaultValue) {
        return value == null ? new Iffy<>(defaultValue) : this;
    }

    public Iffy<V> defaultValue(ValueProvider<V> provider) {
        return value == null ? new Iffy<>(provider.getValue()) : this;
    }

    public MappingResult ifPresent(Action<V> action) {
        if (null != value) {
            action.apply(value);
            return MappingResult.SUCCESS;
        }
        return MappingResult.FAIL;
    }

    public boolean isPresent() {
        return null != value;
    }


    public V get() {
        return value;
    }

    public static <E> Iffy<E> empty() {
        return new Iffy<>(null);
    }

    /*@Override
    public boolean equals(Object obj) {
        Object a = (obj instanceof Iffy) ? ((Iffy) obj).value : obj;
        return (null == value && null == a) || (null != value && null != a && value.equals(a));
    }*/
}
