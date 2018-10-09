package org.telegram.ui.Components;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.View;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.ui.ActionBar.Theme;

public class OnlineIndicator {

    private final int strokeColor = Theme.getColor(Theme.key_dialog_activeStateBorderColor);
    private final int borderWidth = AndroidUtilities.dp(2);

    private Paint paint = Theme.dialog_activeStatePaint;

    private View parent;

    private int offsetX, offsetY;
    private int radius;

    private long dialog_id;

    private int active = -1;

    public OnlineIndicator(View parent) {
        this.parent = parent;
    }

    public OnlineIndicator dialog(long dialog_id) {
        this.dialog_id = dialog_id;
        return this;
    }

    public OnlineIndicator offsetX(int offsetX) {
        this.offsetX = offsetX;
        return this;
    }

    public OnlineIndicator offsetY(int offsetY) {
        this.offsetY = offsetY;
        return this;
    }

    public OnlineIndicator radius(int radius) {
        this.radius = radius;
        return this;
    }

    public OnlineIndicator active(int active) {
        this.active = active;
        return this;
    }

    public void draw(Canvas canvas) {
        if (dialog_id == 0) {
            return;
        }

        if ((dialog_id >> 32) == 0 && active != -1) {
            float x0 = radius;
            float y0 = radius;
            float dx = (float) (x0 + radius * Math.cos(40 * Math.PI / 180));
            float dy = (float) (y0 + radius * Math.sin(40 * Math.PI / 180));
            float circleSize = radius * 0.25f;
            paint.setColor(strokeColor);
            canvas.drawCircle(offsetX + dx, offsetY + dy, circleSize + borderWidth, paint);
            paint.setColor(Theme.getColor(active == 1 ? Theme.key_dialog_activeStateOnlineColor : Theme.key_dialog_activeStateOfflineColor));
            canvas.drawCircle(offsetX + dx, offsetY + dy, circleSize, paint);

            if (parent == null) {
                throw new NullPointerException("\"parent\" cannot be null.");
            }
            parent.invalidate();
        }
    }
}
