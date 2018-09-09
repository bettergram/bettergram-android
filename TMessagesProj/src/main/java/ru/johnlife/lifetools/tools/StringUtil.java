package ru.johnlife.lifetools.tools;

import android.util.SparseArray;

import java.util.List;

import ru.johnlife.lifetools.data.SparseList;

/**
 * Created by yanyu on 5/21/2016.
 */
public class StringUtil {
    public interface Transformer<T> {
        String transform(T object);
    }

    public interface SparseArrayTransformer {
        String transform(int idx);
    }

    public static <T> String implode(SparseArray<T> what, String delimeter, SparseArrayTransformer transformer) {
        if (what == null) return "";
        String[] array = new String[what.size()];
        for (int i = 0; i < what.size(); i++) {
            array[i] = transformer.transform(i);
        }
        return implode(array, delimeter);
    }

    public static <T> String implode(List<T> what, String delimeter, Transformer<T> transformer) {
        if (what == null) return "";
        String[] array = new String[what.size()];
        int i = 0;
        for (T object : what) {
            array[i++] = transformer.transform(object);
        }
        return implode(array, delimeter);
    }

    public static <T> String implode(Iterable<T> what, String delimeter, Transformer<T> transformer) {
        return implode(ListUtil.collect(what, v->v), delimeter, transformer);
    }

    public static String implode(List<String> what, String delimeter) {
        if (what == null) return "";
        return implode(what.toArray(new String[]{}), delimeter);
    }

    public static String implode(String[] what, String delimeter) {
        if (what == null || what.length == 0) return "";
        StringBuilder b = new StringBuilder();
        for (String s : what) {
            b.append(s).append(delimeter);
        }
        b.delete(b.length()-delimeter.length(),b.length());
        return b.toString();
    }
}
