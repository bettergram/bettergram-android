package ru.johnlife.lifetools.tools;

import android.content.Context;
import android.util.Log;

/**
 * Created by Yan Yurkin
 * 18 November 2017
 */

public class DefaultContext {
    /*package*/ static Context get() {
        try {
            return (Context) Class.forName("android.app.ActivityThread").getMethod("currentApplication").invoke(null, (Object[]) null);
        } catch (Exception e) {
            try {
                Log.w("DefaultContext", "Reflection failed for ActivityThread", e);
                return (Context) Class.forName("android.app.AppGlobals").getMethod("getInitialApplication").invoke(null, (Object[]) null);
            } catch (Exception e1) {
                Log.w("DefaultContext", "Reflection failed for AppGlobals", e);
                return null;
            }
        }
    }
}
