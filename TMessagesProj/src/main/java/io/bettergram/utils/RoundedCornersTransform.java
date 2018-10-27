package io.bettergram.utils;

import android.graphics.*;
import com.squareup.picasso.Transformation;

public class RoundedCornersTransform implements Transformation {

    private static RoundedCornersTransform instance = null;

    private RoundedCornersTransform() {
        // Exists only to defeat instantiation.
    }

    public static RoundedCornersTransform getInstance() {
        if (instance == null) {
            instance = new RoundedCornersTransform();
        }
        return instance;
    }

    @Override
    public Bitmap transform(Bitmap source) {
        int width = source.getWidth();
        int height = source.getHeight();

        Bitmap squaredBitmap = Bitmap.createBitmap(source, 0, 0, width, height);
        if (squaredBitmap != source) {
            source.recycle();
        }

        Bitmap bitmap = Bitmap.createBitmap(width, height, source.getConfig());

        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        BitmapShader shader = new BitmapShader(squaredBitmap, BitmapShader.TileMode.CLAMP, BitmapShader.TileMode.CLAMP);
        paint.setShader(shader);
        paint.setAntiAlias(true);

        float r = Math.min(width, height) / 28f;
        canvas.drawRoundRect(new RectF(0, 0, source.getWidth(), source.getHeight()), r, r, paint);
        squaredBitmap.recycle();
        return bitmap;
    }

    @Override
    public String key() {
        return "rounded_corners";
    }
}