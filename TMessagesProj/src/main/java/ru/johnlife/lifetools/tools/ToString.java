package ru.johnlife.lifetools.tools;

import android.view.View;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ToString {
    private static final Pattern ivPattern = Pattern.compile("[^{]+\\{(\\S+).+");

    public static String view(View v) {
        if (null == v) return "null";
        String src = v.toString();
        Matcher matcher = ivPattern.matcher(src);
        return matcher.find() ? matcher.group(1) : src;
    }

}
