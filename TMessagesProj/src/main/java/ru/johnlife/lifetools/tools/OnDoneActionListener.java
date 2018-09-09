package ru.johnlife.lifetools.tools;

import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;

/**
 * Created by Yan Yurkin
 * 03 May 2017
 */

public abstract class OnDoneActionListener implements TextView.OnEditorActionListener {
    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_SEARCH
            || actionId == EditorInfo.IME_ACTION_DONE
            || actionId == EditorInfo.IME_ACTION_GO
            || (null != event &&
            event.getAction() == KeyEvent.ACTION_DOWN &&
            event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
            HideKeyboard.forView(v);
            act(v);
            return true;
        }
        return false;
    }

    protected abstract void act(TextView v);
}
