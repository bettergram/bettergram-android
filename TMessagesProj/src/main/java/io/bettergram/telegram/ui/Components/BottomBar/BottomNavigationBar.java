package io.bettergram.telegram.ui.Components.BottomBar;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Parcelable;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Pair;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import io.bettergram.messenger.R;
import io.bettergram.telegram.messenger.AndroidUtilities;
import io.bettergram.telegram.messenger.NotificationCenter;
import io.bettergram.telegram.ui.ActionBar.Theme;
import io.bettergram.telegram.ui.Components.LayoutHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BottomNavigationBar extends LinearLayout implements NotificationCenter.NotificationCenterDelegate {

    private static final String TAG = BottomNavigationBar.class.getName();

    private static final List<Pair<Integer, Integer>> ITEMS = Arrays.asList(
            new Pair<>(R.drawable.ic_tab_chats, R.string.barItemChats),
            new Pair<>(R.drawable.ic_tab_crypto, R.string.barItemPrices),
            new Pair<>(R.drawable.ic_tab_news, R.string.barItemNews),
            new Pair<>(R.drawable.ic_tab_videos, R.string.barItemVideos),
            new Pair<>(R.drawable.ic_tab_resources, R.string.barItemResources)
    );

    private final List<Tab> tabs = new ArrayList<>(5);
    @ColorInt
    private int inactiveColorId;
    @ColorInt
    private int activeColorId;
    private int selectedPosition;
    private boolean shouldTriggerListenerOnLayout;
    private int tabItemBgRes;

    private OnSelectListener onSelectListener = (position, title) -> {

    };
    private OnReselectListener onReselectListener = (position, title) -> {

    };

    public BottomNavigationBar(Context context) {
        this(context, null);
    }

    public BottomNavigationBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BottomNavigationBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setUpElevation(context, attrs);
        initTabActivityColors();
        init();
        createStubForEditMode();
    }

    private void createStubForEditMode() {
        Thread t = new Thread(() -> {
            int size = ITEMS.size();
            for (int i = 0; i < size; i++) {
                Pair<Integer, Integer> item = ITEMS.get(i);
                addTab(new BottomBarItem(item.first, item.second));
            }
        });
        t.start();
    }

    private void initTabActivityColors() {
        inactiveColorId = Theme.getColor(Theme.key_bottombar_inactiveColor);
        activeColorId = Theme.getColor(Theme.key_bottombar_activeColor);
    }

    @ColorInt
    private int colorToInt(@ColorRes int color) {
        return ContextCompat.getColor(getContext(), color);
    }

    @NonNull
    private Tab getCurrent() {
        return tabs.get(selectedPosition);
    }

    /**
     * Selects tab, not triggering listener
     *
     * @param position position to select
     * @param animate  indicates whether selection should  be animated
     */
    public void selectTab(int position, boolean animate) {
        if (position != selectedPosition) {
            getCurrent().deselect(animate);
            selectedPosition = position;
            getCurrent().select(animate);
        }
    }

    /**
     * @return current selected position
     */
    public int getSelectedPosition() {
        return selectedPosition;
    }

    /**
     * Selects tab, triggering listener
     *
     * @param position position to select
     * @param animate  indicates wheter selection should  be animated
     */
    public BottomNavigationBar selectTabAndTriggerListener(int position, boolean animate) {
        if (position != selectedPosition) {
            onSelectListener.onSelect(position, getContext().getString(ITEMS.get(position).second));
        } else {
            onReselectListener.onReselect(position, getContext().getString(ITEMS.get(position).second));
        }
        selectTab(position, animate);
        return this;
    }

    /**
     * Enables or disables automatic invocation of click listener during layout.
     * Disabled by default.
     *
     * @param shouldTrigger indicates whether selection listener should be triggered
     */
    public void setTriggerListenerOnLayout(boolean shouldTrigger) {
        shouldTriggerListenerOnLayout = shouldTrigger;
    }

    private void setUpElevation(@NonNull Context context, @Nullable AttributeSet attrs) {
        if (!atLeastLollipop()) {
            return;
        }

        int[] set = {android.R.attr.elevation};
        TypedArray a = context.obtainStyledAttributes(attrs, set);

        int defaultElevation = AndroidUtilities.dp(8);
        float elevation = a.getDimensionPixelSize(0, defaultElevation);
        ViewCompat.setElevation(this, elevation);

        a.recycle();
    }

    public BottomNavigationBar addTab(@NonNull BottomBarItem item) {
        final Context context = getContext();
        LinearLayout tabView = new LinearLayout(context);
        tabView.setClickable(true);
        tabView.setOrientation(LinearLayout.VERTICAL);
        tabView.setTranslationY(AndroidUtilities.dp(2));
        tabView.setBackgroundResource(tabItemBgRes);

        ImageView tabIcon = new ImageView(context);

        addViewOnUIThread(tabView, tabIcon, LayoutHelper.createLinear(24, 24, Gravity.CENTER));

        TextView tabTitle = new TextView(context);
        tabTitle.setMaxLines(1);
        tabTitle.setIncludeFontPadding(false);
        tabTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);

        addViewOnUIThread(tabView, tabTitle, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER));


        tabView.requestDisallowInterceptTouchEvent(true);
        addViewOnUIThread(this, tabView, LayoutHelper.createLinear(0, LayoutHelper.WRAP_CONTENT, 1.0f, Gravity.CENTER));

        AndroidUtilities.runOnUIThread(() -> {
            int position = tabs.size();
            Tab tab = createTab(item, tabView, position);
            tabs.add(tab);
        });
        return this;
    }

    private void addViewOnUIThread(ViewGroup parent, View child, LayoutParams params) {
        AndroidUtilities.runOnUIThread(() -> parent.addView(child, params));
    }

    public BottomNavigationBar setOnSelectListener(@NonNull OnSelectListener listener) {
        onSelectListener = listener;
        return this;
    }

    public BottomNavigationBar setOnReselectListener(@NonNull OnReselectListener listener) {
        onReselectListener = listener;
        return this;
    }

    @NonNull
    private Tab createTab(@NonNull BottomBarItem item, @NonNull View tabView, final int position) {
        Tab tab = new Tab(item, tabView, activeColorId, inactiveColorId);
        tabView.setOnClickListener(v -> {
            if (position == selectedPosition) {
                onReselectListener.onReselect(position, getContext().getString(ITEMS.get(position).second));
                return;
            }
            selectTab(position, true);
            onSelectListener.onSelect(position, getContext().getString(ITEMS.get(position).second));
        });
        return tab;
    }

    private void init() {
        int minHeight = AndroidUtilities.dp(50);
        setMinimumHeight(minHeight);
        setOrientation(HORIZONTAL);

        setBackgroundColor(Theme.getColor(Theme.key_bottombar_backgroundColor));

        if (atLeastLollipop()) {
            setOutlineProvider(ViewOutlineProvider.BOUNDS);
        }

        TypedValue outValue = new TypedValue();
        getContext().getTheme().resolveAttribute(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ? android.R.attr.selectableItemBackgroundBorderless : android.R.attr.selectableItemBackground, outValue, true);
        tabItemBgRes = outValue.resourceId;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (tabs.size() == 0) {
            return;
        }
        getCurrent().select(false);
        if (shouldTriggerListenerOnLayout) {
            onSelectListener.onSelect(selectedPosition, getContext().getString(ITEMS.get(selectedPosition).second));
        }
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (!(state instanceof SavedState)) {
            super.onRestoreInstanceState(state);
            return;
        }

        SavedState ss = (SavedState) state;
        selectedPosition = ss.selectedPosition;
        super.onRestoreInstanceState(ss.getSuperState());
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState ss = new SavedState(superState);
        ss.selectedPosition = selectedPosition;
        return ss;
    }

    @Override
    public void didReceivedNotification(int id, int account, Object... args) {
        for (Object object : args) {
            Log.i(TAG, "object: " + object.toString());
        }
    }

    private boolean atLeastLollipop() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }

    public interface OnSelectListener {
        void onSelect(int position, String title);
    }

    public interface OnReselectListener {
        void onReselect(int position, String title);
    }
}