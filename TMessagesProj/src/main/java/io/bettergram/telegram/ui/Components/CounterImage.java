package io.bettergram.telegram.ui.Components;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.widget.ImageView;

import io.bettergram.telegram.messenger.AndroidUtilities;
import io.bettergram.telegram.messenger.Strings;
import io.bettergram.telegram.ui.ActionBar.Theme;

import static android.text.TextUtils.isEmpty;
import static io.bettergram.telegram.messenger.AndroidUtilities.dp;

public class CounterImage extends ImageView {

    TextPaint countTextPaint;
    Paint countPaint;
    Paint countPaintStroke;
    int borderColor;
    StaticLayout countLayout;
    RectF rect = new RectF();
    int countWidth;
    float centreX, centreY;
    String countString = Strings.EMPTY;
    boolean draw = false;
    private int textSize;

    public CounterImage textSize(int textSize) {
        this.textSize = textSize;
        return this;
    }

    public CounterImage borderColor(int borderColor) {
        this.borderColor = borderColor;
        return this;
    }

    public CounterImage countString(String countString) {
        this.countString = countString;
        return this;
    }

    public void update() {
        warmUpEverything();
        post(this::invalidate);
    }

    public CounterImage(Context context) {
        super(context);
        init();
    }

    public CounterImage(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CounterImage(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        countTextPaint = new TextPaint(TextPaint.ANTI_ALIAS_FLAG);
        countTextPaint.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        countTextPaint.setColor(Theme.getColor(Theme.key_chats_unreadCounterText));
        countPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        countPaint.setColor(Color.RED);
        countPaintStroke = new Paint(Paint.ANTI_ALIAS_FLAG);
        countPaintStroke.setStyle(Paint.Style.STROKE);
        countPaintStroke.setStrokeWidth(2);
        warmUpEverything();
    }

    private void warmUpEverything() {
        countTextPaint.setTextSize(dp(textSize));
        countPaintStroke.setColor(borderColor);
        countWidth = Math.max(dp(9), (int) Math.ceil(countTextPaint.measureText(countString)));
        countLayout = new StaticLayout(countString, countTextPaint, countWidth, Layout.Alignment.ALIGN_CENTER, 1.0f, 0.0f, false);
        draw = !isEmpty(countString);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (draw) {
            centreX = getX() + getWidth() * 0.5f;
            centreY = getY() + getHeight() * 0.5f;

            Drawable drawable = getDrawable();
            int countLeft = Math.round(centreX + (drawable.getIntrinsicWidth() * 0.45f));// + AndroidUtilities.dp(len > 2 ? 2 : 4));
            int countTop = Math.round(centreY - dp(3.25f));
            int x = countLeft - dp(3.25f);
            rect.set(x, countTop, x + countWidth + dp(8), countTop + dp(17));
            canvas.drawRoundRect(rect, 8.5f * AndroidUtilities.density, 8.5f * AndroidUtilities.density, countPaint);
            canvas.drawRoundRect(rect, 8.5f * AndroidUtilities.density, 8.5f * AndroidUtilities.density, countPaintStroke);
            canvas.save();
            int len = countString.length();
            //canvas.translate(countLeft + dp(len > 2 ? 0.125f : 0.5f), countTop + dp(1.90f));
            canvas.translate(countLeft + dp(len > 2 ? 0.125f : 0.5f), countTop + ((rect.height() - dp(textSize)) * 0.5f) - dp(0.25f));
            countLayout.draw(canvas);
        }
    }
}
