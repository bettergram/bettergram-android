package ru.johnlife.lifetools.tools;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.LruCache;

import java.io.ByteArrayOutputStream;

/**
 * Created by yanyu on 4/22/2016.
 */
public class Base64Bitmap {
    private static LruCache<String, Bitmap> cache = new LruCache<>(50);

    public static String encodeToBase64(Bitmap image) {
        ByteArrayOutputStream byteArrayOS = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 75, byteArrayOS);
        return Base64.encodeToString(byteArrayOS.toByteArray(), Base64.DEFAULT);
    }

    public static Bitmap decodeBase64(String input) {
        Bitmap bitmap = cache.get(input);
        if (bitmap != null) return bitmap;
        try {
            byte[] decodedBytes = Base64.decode(input, 0);
            bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
            return bitmap;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
