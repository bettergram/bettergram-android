package io.bettergram.telegram.ui.Components.Indicator;

import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.v4.graphics.drawable.DrawableCompat;
import io.bettergram.telegram.messenger.AndroidUtilities;
import io.bettergram.telegram.messenger.MessagesController;
import io.bettergram.telegram.messenger.UserConfig;
import io.bettergram.telegram.tgnet.TLRPC;
import io.bettergram.telegram.ui.ActionBar.Theme;

public class FavoriteIndicator {

    private final int borderWidth = AndroidUtilities.dp(4);
    private int currentAccount = UserConfig.selectedAccount;

    private int offsetX, offsetY;
    private int radius;

    private long dialog_id;

    public FavoriteIndicator dialog(long dialog_id) {
        this.dialog_id = dialog_id;
        return this;
    }

    public FavoriteIndicator offsetX(int offsetX) {
        this.offsetX = offsetX;
        return this;
    }

    public FavoriteIndicator offsetY(int offsetY) {
        this.offsetY = offsetY;
        return this;
    }

    public FavoriteIndicator anchorAvatarRadius(int radius) {
        this.radius = radius;
        return this;
    }

    public void draw(Canvas canvas) {
        if (dialog_id == 0) {
            return;
        }
       
        TLRPC.TL_dialog dialog = MessagesController.getInstance(currentAccount).dialogs_dict.get(dialog_id);
        if (dialog != null && dialog.favorite_date > 0) {
            float x0 = offsetX + radius;
            float y0 = offsetY + radius;
            float dx = (float) (x0 + radius * Math.cos(-40 * Math.PI / 180));
            float dy = (float) (y0 + radius * Math.sin(-40 * Math.PI / 180));
            Drawable d_bg = Theme.dialog_favoriteDrawable;
            DrawableCompat.setTint(d_bg, Theme.getColor(Theme.key_dialog_favoriteBackgroundColor));

            float d_bg_size = radius * 0.75f + borderWidth;
            float pivot_bg = d_bg_size / 2;
            d_bg.setBounds((int) (dx - pivot_bg), (int) dy, (int) ((dx - pivot_bg) + d_bg_size), (int) (dy + d_bg_size));
            d_bg.draw(canvas);

            Drawable d_fg = Theme.dialog_favoriteDrawable;
            DrawableCompat.setTint(d_fg, Theme.getColor(Theme.key_dialog_favoriteForegroundColor));
            float d_fg_size = (radius * 0.75f) - borderWidth;
            float pivot_fg = d_fg_size / 2;
            int top_padding = borderWidth;
            d_bg.setBounds((int) (dx - pivot_fg), (int) (dy + top_padding), (int) ((dx - pivot_fg) + d_fg_size), (int) ((dy + top_padding) + d_fg_size));
            d_bg.draw(canvas);
        }
    }
}
