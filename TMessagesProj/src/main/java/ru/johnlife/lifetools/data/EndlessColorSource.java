package ru.johnlife.lifetools.data;

import android.graphics.Color;

/**
 * Created by yanyu on 4/29/2016.
 */
public class EndlessColorSource {
    private int colors[];
    private int idx;

    public EndlessColorSource(String[] colors) {
        this.colors = new int[colors.length];
        for (int i=0; i<colors.length; i++) {
            String color = colors[i];
            this.colors[i] = Color.parseColor(color);
        }
        idx = 0;
    }

    public int getNext() {
        int value = colors[idx++];
        if (idx >= colors.length) idx = 0;
        return value;
    }
}
