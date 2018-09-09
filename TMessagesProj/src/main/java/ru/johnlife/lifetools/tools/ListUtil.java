package ru.johnlife.lifetools.tools;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import ru.johnlife.lifetools.data.Identifiable;
import ru.johnlife.lifetools.optional.Iffy;

public class ListUtil {
    public interface Collector<T, E> {
        T collect(E item);
    }
    public static  <T, E> List<T> collect(List<E> source, Collector<T, E> collector) {
        final List<T> value = new ArrayList<>(source.size());
        for (E e : source) {
            value.add(collector.collect(e));
        }
        return value;
    }
    public static  <T, E> List<T> collect(Iterable<E> source, Collector<T, E> collector) {
        final List<T> value = new ArrayList<T>();
        for (E e : source) {
            value.add(collector.collect(e));
        }
        return value;
    }



    public interface Filter<T> {
        boolean take(T item);
    }
    public static  <T> List<T> filter(List<T> source, Filter<T> filter) {
        final List<T> value = new ArrayList<>(source.size());
        for (T t : source) {
            if (filter.take(t)) value.add(t);
        }
        return value;
    }

    public static <T> Iffy<T> find(Iterable<T> where, Filter<T> filter) {
        for (T t : where) {
            if (filter.take(t)) return Iffy.from(t);
        }
        return Iffy.empty();
    }

    public interface IdExtractor<ID, T> {
        Identifiable<ID> getIdentifiable(T item);
    }

    public static <T, ID> Iffy<T> find(Iterable<T> where, ID id, IdExtractor<ID, T> extractor) {
        if (id == null) {
            return Iffy.empty();
        }
        return find(where, t-> id.equals(extractor.getIdentifiable(t).getId()));
    }

    public static <T> void shift(List<T> list, int from, int to) {
        if (from == to || from >= list.size() || from < 0) return;
        int d = from > to ? -1 : 1;
        T target = list.get(from);
        try {
            for (int i = from; i != to; i += d) {
                list.set(i, list.get(i+d));
            }
            list.set(to, target);
        } catch (ArrayIndexOutOfBoundsException e) {
            Log.w("ListUtils", "shift: ", e);
        }
    }
}
