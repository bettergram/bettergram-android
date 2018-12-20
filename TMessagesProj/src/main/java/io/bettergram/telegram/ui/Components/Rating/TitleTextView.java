package io.bettergram.telegram.ui.Components.Rating;

/**
 * Created by marcos on 13/04/2017.
 */

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

import io.bettergram.telegram.ui.ActionBar.Theme;

public class TitleTextView extends TextView {

    private Context ctx;

    public TitleTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        ctx = context;
        init();
    }

    public TitleTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        ctx = context;
        init();
    }

    public TitleTextView(Context context) {
        super(context);
        ctx = context;
        init();
    }

    private void init() {
        Typeface tf = Typeface.createFromAsset(getContext().getAssets(), "fonts/rmedium.ttf");
        setTypeface(tf);
        setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
    }

}