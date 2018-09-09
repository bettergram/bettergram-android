package ru.johnlife.lifetools.tools;

import android.graphics.Color;

public class ColorUtil {
    public static int parse(String color) {
        String value = color;
        if (color.startsWith("#") && color.length() < 7) {
            String valuePart = color.substring(1);
            char[] chars = valuePart.toCharArray();
            char[] result = new char[chars.length * 2];
            for (int i = 0; i < chars.length; i++) {
                result[2*i] = chars[i];
                result[2*i+1] = chars[i];
            }
            value = "#" + new String(result);
        }
        return Color.parseColor(value);
    }

    public static int alpha(int color, int alpha) {
        return (0x00ffffff & color) | (alpha << 24);
    }
}
