package ru.johnlife.lifetools.tools;

import android.content.Context;
import android.util.TypedValue;

/**
 * Created by Yan Yurkin
 * 18 November 2017
 */

public class Dp2Px {
    public static int convert(int dp, Context context) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
    }

    public static int convert(int dp) {
        return convert(dp, DefaultContext.get());
    }

}
