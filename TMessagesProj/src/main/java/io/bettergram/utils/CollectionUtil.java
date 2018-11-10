package io.bettergram.utils;

import java.util.Collection;

public final class CollectionUtil {

    public static <T> T find(final Collection<T> collection, final Predicate<T> predicate) {
        for (T item : collection) {
            if (predicate.contains(item)) {
                return item;
            }
        }
        return null;
    }

    public interface Predicate<T> {

        boolean contains(T item);
    }
}
