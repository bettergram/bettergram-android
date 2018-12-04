package io.bettergram.telegram.ui.Components.BottomBar;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.DimenRes;
import android.support.annotation.NonNull;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import io.bettergram.telegram.ui.ActionBar.Theme;
import io.bettergram.telegram.ui.Components.CounterImage;
import io.bettergram.telegram.ui.Components.WrappedDrawable;

import static android.view.View.GONE;
import static io.bettergram.telegram.messenger.AndroidUtilities.dp;
import static io.bettergram.telegram.messenger.AndroidUtilities.findViewsByType;
import static io.bettergram.telegram.ui.Components.BottomBar.TabAnimator.animateTranslationY;

public class Tab {
    private final BottomBarItem item;
    private final View root;
    private final TextView title;
    public final CounterImage icon;
    private final Context context;

    private final int activeTopMargin;
    private final int inactiveTopMargin;
    @ColorInt
    private final int activeColor;
    @ColorInt
    private final int inactiveColor;
    private final Drawable iconDrawable;

    Tab(@NonNull BottomBarItem item, @NonNull View root, @ColorInt int activeColor, @ColorInt int inactiveColor) {
        this.item = item;
        this.root = root;
        context = root.getContext();

        title = findViewsByType(root, TextView.class).get(0);
        icon = findViewsByType(root, CounterImage.class).get(0);
        icon.textSize(9).borderColor(Theme.getColor(Theme.key_bottombar_backgroundColor)).update();

        activeTopMargin = dp(0);
        inactiveTopMargin = dp(3);
        this.activeColor = activeColor;
        this.inactiveColor = inactiveColor;
        iconDrawable = item.getIconDrawable(context);

        setupIcon(icon);
        setupTitle();
    }

    private void setupIcon(@NonNull ImageView icon) {
        DrawableCompat.setTint(iconDrawable, inactiveColor);
        WrappedDrawable wrappedDrawable = new WrappedDrawable(iconDrawable);
        wrappedDrawable.setBounds(0, 0, dp(24), dp(24));
        icon.setImageDrawable(wrappedDrawable);
    }

    private int getSizeInPx(@DimenRes int res) {
        return context.getResources().getDimensionPixelSize(res);
    }

    void select(boolean animate) {
        title.setTextColor(activeColor);
        DrawableCompat.setTint(iconDrawable, activeColor);
        WrappedDrawable wrappedDrawable = new WrappedDrawable(iconDrawable);
        wrappedDrawable.setBounds(0, 0, dp(24), dp(24));
        icon.setImageDrawable(wrappedDrawable);

        if (animate) {
            animateTranslationY(root, activeTopMargin);
        } else {
            root.setTranslationY(activeTopMargin);
        }
    }

    void deselect(boolean animate) {
        title.setTextColor(inactiveColor);
        DrawableCompat.setTint(iconDrawable, inactiveColor);
        WrappedDrawable wrappedDrawable = new WrappedDrawable(iconDrawable);
        wrappedDrawable.setBounds(0, 0, dp(24), dp(24));
        icon.setImageDrawable(wrappedDrawable);

        if (animate) {
            animateTranslationY(root, inactiveTopMargin);
        } else {
            root.setTranslationY(inactiveTopMargin);
        }
    }

    private void setupTitle() {
        if (item.getTitle() == 0) {
            title.setVisibility(GONE);
        } else {
            title.setText(item.getTitle());
        }
        title.setTextColor(inactiveColor);
    }

    public void refresh() {
        root.performClick();
    }
}