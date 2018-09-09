package ru.johnlife.lifetools.tools;

import android.util.Log;
import android.view.KeyEvent;
import android.view.View;

import java.util.Arrays;

/**
 * Created by Yan Yurkin
 * 24 July 2016
 */
public abstract class KeyHelper {
    private long[] times = new long[3];
    private boolean next = true;

    public boolean onKeyDown(int keyCode) {
        if (KeyEvent.KEYCODE_VOLUME_UP == keyCode) {
            if (!next) return false;
            times[0] = times[1];
            times[1] = times[2];
            times[2] = System.currentTimeMillis();
            next = false;
            Log.i(getClass().getSimpleName(), "onKeyDown: volumeUp, " + Arrays.toString(times));
        } else if (KeyEvent.KEYCODE_VOLUME_DOWN == keyCode) {
            Log.i(getClass().getSimpleName(), "onKeyDown: volumeDown, next");
            next = true;
            if (System.currentTimeMillis() - times[0] < 5000) {
                onLaunch();
            }
        }
        return false;
    }

    protected abstract void onLaunch();

    public KeyHelper attachTo(View view) {
        view.setFocusableInTouchMode(true);
        view.requestFocus();
        view.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() != KeyEvent.ACTION_DOWN) return true;
                Log.i(getClass().getSimpleName(), "Key Down!");
                onKeyDown(keyCode);
                return true;
            }
        });
        return this;
    }
}
