package ru.johnlife.lifetools.tools;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.RectF;

import ru.johnlife.lifetools.data.Size;

/**
 * Created by Yan Yurkin
 * 19 November 2017
 */

public class ScaleBitmap {

    public static Bitmap maintainAspect(Bitmap source, int width, int height) {
        Matrix m = new Matrix();
        m.setRectToRect(new RectF(0, 0, source.getWidth(), source.getHeight()), new RectF(0, 0, width, height), Matrix.ScaleToFit.CENTER);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), m, true);
    }

    public static Bitmap toLongest(Bitmap source, int longest) {
        return maintainAspect(source, longest, longest);
    }

    public static Bitmap toWidth(Bitmap source, int width) {
        return maintainAspect(source, width, Integer.MAX_VALUE);
    }

    public static Bitmap toHeight(Bitmap source, int height) {
        return maintainAspect(source, Integer.MAX_VALUE, height);
    }

    public static Size maintainAspect(Size source, int width, int height) {
        Size target = new Size(width, height);
        double minRatio = Math.min(source.getRatio(), target.getRatio());
        double maxRatio = Math.max(source.getRatio(), target.getRatio());
        return new Size((int)(height * minRatio), (int)(width/maxRatio));
    }
}
