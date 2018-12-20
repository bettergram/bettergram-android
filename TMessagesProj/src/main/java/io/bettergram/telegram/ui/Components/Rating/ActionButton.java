package io.bettergram.telegram.ui.Components.Rating;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.Button;

/**
 * Created by marcos on 13/04/2017.
 */

public class ActionButton extends Button {

    public ActionButton(Context context) {
        super(context);
        init();
    }

    public ActionButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ActionButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    private void init() {
        Typeface tf = Typeface.createFromAsset(getContext().getAssets(), "fonts/rmedium.ttf");
        setTypeface(tf);
    }
}