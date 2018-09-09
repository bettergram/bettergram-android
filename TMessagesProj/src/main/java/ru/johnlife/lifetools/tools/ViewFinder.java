package ru.johnlife.lifetools.tools;

import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import ru.johnlife.lifetools.optional.Iffy;

public class ViewFinder {

    public interface Finder {
        boolean isFound(View suspect);
    }

    public static Iffy<View> findChild(View view, Finder finder) {
        if (null == view) return null;
        if (finder.isFound(view)) return Iffy.from(view);
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i=0; i<group.getChildCount(); i++) {
                Iffy<View> value = findChild(group.getChildAt(i), finder);
                if (null != value.get()) return value;
            }
        }
        return Iffy.empty();
    }

    public static Iffy<View> findNearestParent(View view, Finder finder) {
        if (null == view) return null;
        if (finder.isFound(view)) return Iffy.from(view);
        ViewParent parent = view.getParent();
        if (parent != null) {
            return findNearestParent((View) parent, finder);
        }
        return Iffy.empty();
    }

    public static Iffy<View> findTopmostParent(View view, Finder finder) {
        if (null == view) return null;
        ViewParent parent = view.getParent();
        if (parent instanceof View) {
            Iffy<View> value = findTopmostParent((View) parent, finder);
            if (value.isPresent()) {
                return value;
            } else {
                if (finder.isFound(view)) return Iffy.from(view);
            }
        }
        return Iffy.empty();
    }

}
