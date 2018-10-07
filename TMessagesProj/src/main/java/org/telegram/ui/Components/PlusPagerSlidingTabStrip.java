package org.telegram.ui.Components;

/**
 * Created by Sergio on 05/10/2016.
 */

/*
 * Copyright (C) 2013 Andreas Stuetz <andreas.stuetz@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.*;
import android.graphics.Paint.Style;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.FileLog;
import org.telegram.ui.ActionBar.Theme;


public class PlusPagerSlidingTabStrip extends HorizontalScrollView {

    private boolean tabTitlesMode = false;
    private boolean tabShouldExpand = true;

    public interface IconTabProvider {
        int getPageIconResId(int position);

        String getPageTitle(int position);
    }

    public interface PlusScrollSlidingTabStripDelegate {
        //void onPageSelected(int page);
        void onTabLongClick(int position);

        void onTabsUpdated();

        void onTabClick();
    }

    private PlusScrollSlidingTabStripDelegate delegate;

    public void setDelegate(PlusScrollSlidingTabStripDelegate scrollSlidingTabStripDelegate) {
        delegate = scrollSlidingTabStripDelegate;
    }

    private static final String TAG = "PlusPagerSlidingTab";
    private LinearLayout.LayoutParams defaultTabLayoutParams;
    private LinearLayout.LayoutParams expandedTabLayoutParams;

    private final PageListener pageListener = new PageListener();
    public OnPageChangeListener delegatePageListener;

    private LinearLayout tabsContainer;
    //private FrameLayout tabsContainerTop;
    private ViewPager pager;

    private int tabCount;

    private int currentPosition = 0;
    private float currentPositionOffset = 0f;

    private Paint rectPaint;
    private Paint dividerPaint;

    private int indicatorColor = 0xFF666666;
    private int underlineColor = 0x1A000000;
    private int dividerColor = 0x1A000000;

    //private boolean shouldExpand = true;
    private boolean textAllCaps = true;
    //private boolean tabCenterAligned;
    private int layoutWidth;

    private int scrollOffset = AndroidUtilities.dp(/*52*/20);
    private int indicatorHeight = AndroidUtilities.dp(8);
    private int underlineHeight = AndroidUtilities.dp(2);
    private int dividerPadding = AndroidUtilities.dp(12);
    private int tabPadding = AndroidUtilities.dp(15);
    private int dividerWidth = AndroidUtilities.dp(1);

    //private int tabTextSize = Theme.plusTabsTextSize;
    private int tabTextColor = 0xFF666666;
    private int tabTextSelectedColor = 0xFF666666;
    private Typeface tabTypeface = null;
    private int tabTypefaceStyle = Typeface.BOLD;
    private int btnBgRes;
    //private int counterBgColor;
    //private int counterBgColorMuted;

    private int lastScrollX = 0;

    //private int tabBackgroundResId = Theme.defColor;// = R.drawable.background_tab;

    private int currentPage = 0;
    //private boolean checkedTabWidths = false;
    private int tabTextIconUnselectedColor;
    private int tabTextIconSelectedColor;

    public PlusPagerSlidingTabStrip(Context context) {
        super(context);

        setFillViewport(true);
        setWillNotDraw(false);
        setHorizontalScrollBarEnabled(false);

        tabsContainer = new LinearLayout(context);
        tabsContainer.setOrientation(LinearLayout.HORIZONTAL);
        tabsContainer.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        tabsContainer.setBackgroundColor(Theme.getColor(Theme.key_actionBarDefault));
        addView(tabsContainer);

        rectPaint = new Paint();
        rectPaint.setAntiAlias(true);
        rectPaint.setStyle(Style.FILL);

        dividerPaint = new Paint();
        dividerPaint.setAntiAlias(true);
        dividerPaint.setStrokeWidth(dividerWidth);

        defaultTabLayoutParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
        //expandedTabLayoutParams = new LinearLayout.LayoutParams(0, LayoutParams.MATCH_PARENT, 1.0f);
        expandedTabLayoutParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT, 1.0F);
        TypedValue outValue = new TypedValue();
        getContext().getTheme().resolveAttribute(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ? android.R.attr.selectableItemBackgroundBorderless : android.R.attr.selectableItemBackground, outValue, true);
        btnBgRes = outValue.resourceId;

        //tabCenterAligned = tabTitlesMode;
        layoutWidth = AndroidUtilities.displaySize.x;
        if (tabTitlesMode) {
            scrollOffset = layoutWidth / 2;
        }
        //SharedPreferences themePrefs = ApplicationLoader.applicationContext.getSharedPreferences(AndroidUtilities.THEME_PREFS, AndroidUtilities.THEME_PREFS_MODE);
        //counterBgColorMuted = themePrefs.getInt("chatsHeaderTabCounterSilentBGColor", 0xffb9b9b9);
        //counterBgColor = themePrefs.getInt("chatsHeaderTabCounterBGColor", 0xffd32f2f);
        //Log.e("PlusPager", "PlusPagerSlidingTabStrip create");
        tabTextIconUnselectedColor = Theme.getColor(Theme.key_actionBarDefaultIcon);
        tabTextIconSelectedColor = Theme.getColor(Theme.key_actionBarDefaultIcon);
    }

    public void setViewPager(ViewPager pager) {
        //Log.e("PlusPager", "setViewPager");
        this.pager = pager;
        if (pager.getAdapter() == null) {
            throw new IllegalStateException("ViewPager does not have adapter instance.");
        }
        pager.setOnPageChangeListener(pageListener);
        notifyDataSetChanged();
        //pager.setOffscreenPageLimit(tabCount); // fixes bug with Nexus 5 6.0.1 and infinite scroll
    }

    public void setOnPageChangeListener(OnPageChangeListener listener) {
        this.delegatePageListener = listener;
    }

    public void notifyDataSetChanged() {
        //Log.e("PlusPager", "Plus Pager notifyDataSetChanged");
        tabsContainer.removeAllViews();
        tabCount = pager.getAdapter().getCount();
        if (tabCount < 2) {
            return;
        }
        for (int i = 0; i < tabCount; i++) {
            if (tabTitlesMode) {
                addTextTabWithCounter(i, ((IconTabProvider) pager.getAdapter()).getPageTitle(i));
            } else {
                addIconTabWithCounter(i, ((IconTabProvider) pager.getAdapter()).getPageIconResId(i));
            }
        }
        updateTabStyles();
        //checkedTabWidths = false;
        getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                    getViewTreeObserver().removeGlobalOnLayoutListener(this);
                } else {
                    getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
                currentPosition = pager.getCurrentItem();
                currentPage = currentPosition;
                //scrollToChild(currentPosition, 0);
                //modified to check if center alignment is true
                if (tabTitlesMode) {
                    scrollToChild2(currentPosition, 0);
                } else {
                    scrollToChild(currentPosition, 0);
                }
            }
        });

    }

    private void addTextTabWithCounter(final int position, String title) {
        TextView tab = new TextView(getContext());
        //tab.setBackgroundResource(btnBgRes);
        tab.setText(title);
        tab.setTypeface(Typeface.DEFAULT_BOLD);
        //tab.setTypeface(position == pager.getCurrentItem() ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
        tab.setTextSize(TypedValue.COMPLEX_UNIT_DIP, getTextSize());
        tab.setGravity(Gravity.CENTER);
        tab.setSingleLine();
        tab.setTextColor(position == pager.getCurrentItem() ? tabTextIconSelectedColor : tabTextIconUnselectedColor);
        if (textAllCaps) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                tab.setAllCaps(true);
            }
        }
        addTabWithCounter(position, tab);
    }

    private void addIconTabWithCounter(final int position, int resId) {
        ImageButton tab = new ImageButton(getContext());
        //tab.setBackgroundResource(btnBgRes);
        tab.setImageResource(resId);
        //Log.e(TAG, "addIconTabWithCounter position " + position + " pager.getCurrentItem() " + pager.getCurrentItem());
        tab.setColorFilter(position == pager.getCurrentItem() ? tabTextIconSelectedColor : tabTextIconUnselectedColor, PorterDuff.Mode.SRC_IN);
        tab.setScaleType(ImageView.ScaleType.CENTER);
        addTabWithCounter(position, tab);
    }

    public void addTabWithCounter(final int position, View view) {
        //Log.e(TAG, "addTabWithCounter " + tabsContainer.getLayoutParams().toString());
        RelativeLayout tab = new RelativeLayout(getContext());
        tab.setFocusable(true);

        tabsContainer.addView(tab, tabShouldExpand ? expandedTabLayoutParams : defaultTabLayoutParams);
        //tab.setPadding(tabPadding, 0, tabPadding, 0);
        //tabsContainer.addView(tab, position, tabShouldExpand ? expandedTabLayoutParams : defaultTabLayoutParams);
        view.setBackgroundResource(btnBgRes);
        //view.setId(1); // changed id from 0 to 1
        view.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //Log.e("PlusPager", "position " + position + " / " + pager.getCurrentItem());
                if (position == pager.getCurrentItem()) {
                    if (delegate != null) {
                        delegate.onTabClick();
                    }
                } else {
                    if (pager != null) {
                        pager.setCurrentItem(position);
                    }
                }
            }
        });
        view.setOnLongClickListener(new View.OnLongClickListener() {
            public boolean onLongClick(View view) {
                if (delegate != null) {
                    delegate.onTabLongClick(position);
                }
                return true;
            }
        });

        tab.addView(view, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
        tab.setSelected(position == currentPosition);

        TextView textView = new TextView(getContext());

        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12);
        textView.setTextColor(0xffffffff);
        textView.setGravity(Gravity.CENTER);
        //textView.setBackgroundResource(R.drawable.sticker_badge);

        GradientDrawable shape = new GradientDrawable();
        shape.setShape(GradientDrawable.RECTANGLE);
        shape.setCornerRadius(AndroidUtilities.dp(32));
        textView.setBackgroundDrawable(shape);

        textView.setMinWidth(AndroidUtilities.dp(18));

        //TODO: WHAAAAT!?!? textView.setPadding(AndroidUtilities.dp(Theme.chatsTabCounterSize > 10 ? Theme.chatsTabCounterSize - 7 : 4), 0, AndroidUtilities.dp(Theme.chatsTabCounterSize > 10 ? Theme.chatsTabCounterSize - 7 : 4), 0);
        textView.setPadding(4, 0, 4, 0);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(AndroidUtilities.dp(3), AndroidUtilities.dp(5), AndroidUtilities.dp(3), AndroidUtilities.dp(5));  // left, top, right, bottom
        //RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) textView.getLayoutParams();
        //params.addRule(tabShouldExpand ? RelativeLayout.ALIGN_PARENT_RIGHT : RelativeLayout.RIGHT_OF, view.getId());
        params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        params.addRule(/*Theme.plusTabsToBottom ? RelativeLayout.ALIGN_PARENT_TOP : */RelativeLayout.ALIGN_PARENT_BOTTOM);
        //textView.setLayoutParams(params);
        //tab.addView(textView, LayoutHelper.createRelative(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, 0, 6, 3, 6, RelativeLayout.ALIGN_PARENT_RIGHT));
        tab.addView(textView, params);

    }

    public void changeTabsColor(int position) {
        //Log.e("PlusPager", "changeTabsColor position " + position + " currentPage " + currentPage);
        RelativeLayout frame = (RelativeLayout) tabsContainer.getChildAt(currentPage);
        if (frame != null) {
            try {
                View view = ((RelativeLayout) tabsContainer.getChildAt(position)).getChildAt(0);
                if (view instanceof ImageButton) {
                    ((ImageButton) frame.getChildAt(0)).setColorFilter(tabTextIconUnselectedColor, PorterDuff.Mode.SRC_IN); // Previous
                    ((ImageButton) view).setColorFilter(tabTextIconSelectedColor, PorterDuff.Mode.SRC_IN); // Selected
                } else if (view instanceof TextView) {
                    ((TextView) frame.getChildAt(0)).setTextColor(tabTextIconUnselectedColor); // Previous
                    ((TextView) view).setTextColor(tabTextIconSelectedColor); // Selected
                }
            } catch (Exception e) {
                FileLog.e(e);
            }
        }
    }

    @SuppressLint("DefaultLocale")
    public void updateCounter(int position, int count, boolean allMuted, boolean force) {
        RelativeLayout frame = (RelativeLayout) tabsContainer.getChildAt(position);
        //Log.e("TabsView", "PlusPager updateCounter position " + position + " unreadCount " + count + " allMuted " + allMuted);
        if (frame != null && frame.getChildCount() > 1) {
            TextView tv = (TextView) frame.getChildAt(1);
            if (tv != null) {
                //Log.e("TabsView", "PlusPager updateCounter NOT NULL");
                //if (count > 0 && !Theme.plusHideTabsCounters) {
                if (count > 0) {
                    tv.setVisibility(VISIBLE);
                    //tv.setText(count >= 10000 && Theme.plusLimitTabsCounters ? "+9999" : String.format("%d", count));
                    tv.setText(String.format("%d", count));
                    //tv.getBackground().setColorFilter(allMuted ? Theme.usePlusTheme ? Theme.chatsTabCounterSilentBGColor : Theme.getColor(Theme.key_chats_unreadCounterMuted) : Theme.usePlusTheme ? Theme.chatsTabCounterBGColor : Theme.getColor(Theme.key_chats_unreadCounter), PorterDuff.Mode.SRC_IN);
                    tv.getBackground().setColorFilter(Color.parseColor("#ff585b"), PorterDuff.Mode.SRC_IN);
                } else {
                    tv.setVisibility(INVISIBLE);
                    //tv.getBackground().setColorFilter(0x00000000, PorterDuff.Mode.SRC_IN);
                }
                if (force) {
                    //tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, Theme.chatsTabCounterSize);
                    //tv.setTextColor(Theme.usePlusTheme ? Theme.chatsTabCounterColor : Theme.getColor(Theme.key_chats_unreadCounterText));
                    //tv.setPadding(AndroidUtilities.dp(Theme.chatsTabCounterSize > 10 ? Theme.chatsTabCounterSize - 7 : 4), 0, AndroidUtilities.dp(Theme.chatsTabCounterSize > 10 ? Theme.chatsTabCounterSize - 7 : 4), 0);
                    tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 11);
                    tv.setTextColor(Theme.getColor(Theme.key_chats_unreadCounterText));
                    tv.setPadding(AndroidUtilities.dp(4), 0, AndroidUtilities.dp(4), 0);
                }
            }
        }
    }

    private void updateTabStyles() {
        //Log.e("TabsView", "PlusPager updateTabStyles tabShouldExpand " + tabShouldExpand);
        //int childWidth = 0;
        //int tabSize = (AndroidUtilities.displaySize.x / tabCount);
        for (int i = 0; i < tabCount; i++) {
            View tab = tabsContainer.getChildAt(i); // RelativeLayout

            // ImageButton or TextView

            //Log.e("PlusPager", i + " updateTabStyles view " + tab.toString());

            //v.setPadding(0, 0, 0, 0);
            tab.setPadding(0, 0, 0, 0);
            if (tabShouldExpand) {
                if (tab.getLayoutParams() != expandedTabLayoutParams) tab.setLayoutParams(expandedTabLayoutParams);
                //v.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, 1.0F));
                //v.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT, 1.0F));
            } else {
                if (tab.getLayoutParams() != defaultTabLayoutParams) tab.setLayoutParams(defaultTabLayoutParams);
                View view = ((RelativeLayout) tabsContainer.getChildAt(i)).getChildAt(0);
                if (view != null) {
                    view.setPadding(tabPadding, 0, tabPadding, 0);
                }

                //v.setPadding(tabPadding, 0, tabPadding, 0);
            }
            //Log.e("PlusPager", i + " updateTabStyles tab " + tab.toString() + " " + tab.getMeasuredWidth() + " " + tab.getWidth());
            //if (v instanceof TextView) {

            //TextView tab = (TextView) v;
            //tab.setTextSize(TypedValue.COMPLEX_UNIT_DIP, tabTextSize);
            //tab.setTypeface(tabTypeface, tabTypefaceStyle);
            //tab.setTypeface(Typeface.DEFAULT_BOLD);
            //tab.setTextColor(i == currentPosition ? tabTextSelectedColor : tabTextColor);

            // setAllCaps() is only available from API 14, so the upper case is made manually if we are on a
            // pre-ICS-build
            //if (textAllCaps) {
            //    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            //        tab.setAllCaps(true);
            //    }
            //}
            //}
            //childWidth += v.getWidth();


        }
        if (delegate != null) {
            delegate.onTabsUpdated();
        }
        //Log.e("PlusPager", AndroidUtilities.displaySize.x + " / " + AndroidUtilities.displaySize.x/tabCount + " / " + tabsContainer.getMeasuredWidth() + " " + pager.getCurrentItem());

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        //Log.e("PlusPager", "onMeasure myWidth " + getMeasuredWidth() + " tabsContainer.getWidth() " + tabsContainer.getWidth() + " getWidth " + tabsContainer.getChildAt(0).getWidth());
        if (!tabShouldExpand || MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.UNSPECIFIED) {
            return;
        }
        int myWidth = getMeasuredWidth();
        tabsContainer.measure(MeasureSpec.EXACTLY | myWidth, heightMeasureSpec);
        /*for (int i = 0; i < tabCount; i++) { Detects if tab title is cutted off
            View tab = tabsContainer.getChildAt(i);
            if(tabTitlesMode){
                TextView tv = (TextView) ((RelativeLayout)tab).getChildAt(0);
                if(tv != null) {
                    android.graphics.Rect bounds = new android.graphics.Rect();
                    String text = String.valueOf(tv.getText());
                    tv.getPaint().getTextBounds(text, 0, text.length(), bounds);
                    CharSequence txt = tv.getText();
                    float width = tv.getPaint().measureText(txt, 0, txt.length());
                    int w = bounds.left + bounds.width();


                    if (w > tv.getWidth()) {

                    }
                }
            }
        }*/

    }

    private void scrollToChild(int position, int offset) {
        if (tabCount == 0) {
            return;
        }
        if (position >= tabsContainer.getChildCount()) {
            return;
        }

        int newScrollX = tabsContainer.getChildAt(position).getLeft() + offset; // NullPointerException tabsContainer.getChildAt(position)
        if (position > 0 || offset > 0) {
            newScrollX -= scrollOffset;
        }

        if (newScrollX != lastScrollX) {
            lastScrollX = newScrollX;
            scrollTo(newScrollX, 0);
        }
    }

    //The method to scroll but still keeping the tab center aligned
    private void scrollToChild2(int position, float offset) {
        if (tabCount == 0) {
            return;
        }
        if (position >= tabsContainer.getChildCount()) {
            return;
        }
        int cellWidth = tabsContainer.getChildAt(position).getWidth();
        //Log.e(TAG, "scrollToChild2 tabCount " + tabCount + " cellWidth " + cellWidth);
        int newScrollX = lastScrollX;

        if (offset < 0.01 && offset > -0.01) {
            if (position + 1 <= tabCount - 1) {
                newScrollX = (int) (tabsContainer.getChildAt(position)
                        .getLeft() + cellWidth / 2 + (float) ((cellWidth + tabsContainer
                        .getChildAt(position + 1).getWidth() / 2) * offset));
            } else {
                newScrollX = (int) (tabsContainer.getChildAt(position)
                        .getLeft() + cellWidth / 2 + (float) ((cellWidth + tabsContainer
                        .getChildAt(position).getWidth()) * offset) / 2);
            }
        } else {
            if (position + 1 <= tabCount - 1) {
                newScrollX = (int) (tabsContainer.getChildAt(position)
                        .getLeft() + (cellWidth * (float) (1 - offset)) / 2 + (float) ((cellWidth + tabsContainer
                        .getChildAt(position + 1).getWidth() / 2) * offset));
            } else {
                newScrollX = (int) (tabsContainer.getChildAt(position)
                        .getLeft() + (cellWidth * (float) (1 - offset)) / 2 + (float) ((cellWidth + tabsContainer
                        .getChildAt(position).getWidth()) * offset) / 2);
            }
        }

        if (position >= 0 || offset > 0.01) {
            newScrollX -= scrollOffset;
        }

        if (newScrollX != lastScrollX) {
            lastScrollX = newScrollX;
            scrollTo(newScrollX, 0);
        }
    }

    private void enableShouldExpand() {
        SharedPreferences plusPreferences = ApplicationLoader.applicationContext.getSharedPreferences("plusconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = plusPreferences.edit();
        tabShouldExpand = true;
        editor.putBoolean("tabsShouldExpand", true);
        editor.apply();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //Log.e("PlusPager", "onDraw");
        if (isInEditMode() || tabCount == 0 || currentPosition >= tabCount) {
            return;
        }
        //Log.e("PlusPager", "onDraw getWidth " + tabsContainer.getWidth() + " " + tabsContainer.getChildAt(0).getWidth());
//        if(!tabShouldExpand && tabTitlesMode){
        if (tabsContainer.getChildAt(0).getWidth() > tabsContainer.getWidth() / 2) {
            enableShouldExpand();
            notifyDataSetChanged();
            return;
        }
//        }
        final int height = getHeight();

        //draw underline
        rectPaint.setColor(Theme.getColor(Theme.key_actionBarDefaultIcon));
        canvas.drawRect(0, height - underlineHeight, tabsContainer.getWidth(), height, rectPaint);

        // default: line below current tab
        View currentTab = tabsContainer.getChildAt(currentPosition);
        float lineLeft = currentTab.getLeft();
        float lineRight = currentTab.getRight();

        // if there is an offset, start interpolating left and right coordinates between current and next tab
        if (currentPositionOffset > 0f && currentPosition < tabCount - 1) {

            View nextTab = tabsContainer.getChildAt(currentPosition + 1);
            final float nextTabLeft = nextTab.getLeft();
            final float nextTabRight = nextTab.getRight();

            lineLeft = (currentPositionOffset * nextTabLeft + (1f - currentPositionOffset) * lineLeft);
            lineRight = (currentPositionOffset * nextTabRight + (1f - currentPositionOffset) * lineRight);
        }

        // draw indicator line
        rectPaint.setColor(Theme.getColor(Theme.key_actionBarDefaultIcon));
        canvas.drawRect(lineLeft, height - indicatorHeight, lineRight, height, rectPaint);

        // draw divider
        dividerPaint.setColor(dividerColor);
        for (int i = 0; i < tabCount - 1; i++) {
            View tab = tabsContainer.getChildAt(i);
            canvas.drawLine(tab.getRight(), dividerPadding, tab.getRight(), height - dividerPadding, dividerPaint);
        }
    }

    private class PageListener implements OnPageChangeListener {

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            //Log.e("PlusPager", "PageListener onPageScrolled position " + position + " positionOffset " + positionOffset + " positionOffsetPixels " + positionOffsetPixels + " / " + (int) (positionOffset * tabsContainer.getChildAt(position).getWidth()));
            //View selectedChild = tabsContainer.getChildAt(position);
            currentPosition = position;
            currentPositionOffset = positionOffset;
            //scrollToChild(position, (int) (positionOffset * selectedChild.getWidth()));
            scroll();
            invalidate();
            if (delegatePageListener != null) {
                delegatePageListener.onPageScrolled(position, positionOffset, positionOffsetPixels);
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            //Log.e("PlusPager", "PageListener onPageScrollStateChanged state " + state + " pager.getCurrentItem() " + pager.getCurrentItem() + " currentPosition " + currentPosition);
            if (state == ViewPager.SCROLL_STATE_IDLE) {
                //scrollToChild(pager.getCurrentItem(), 0);
                scroll();
            }
            if (delegatePageListener != null) {
                delegatePageListener.onPageScrollStateChanged(state);
            }
        }

        private void scroll() {
            if (tabTitlesMode) {
                scrollToChild2(pager.getCurrentItem(), 0);
            } else {
                scrollToChild(pager.getCurrentItem(), 0);
            }
        }

        @Override
        public void onPageSelected(int position) {
            //Log.e("PlusPager", "PageListener onPageSelected position " + position + " currentPosition " + currentPosition);
            if (delegatePageListener != null) {
                delegatePageListener.onPageSelected(position);
            }
            /*for (int a = 0; a < tabsContainer.getChildCount(); a++) {
                tabsContainer.getChildAt(a).setSelected(a == position);
                ((RelativeLayout)tabsContainer.getChildAt(position)).getChildAt(0).setSelected(a == position);
            }*/
            changeTabsColor(position);
            currentPage = position;
        }

    }

    public void onSizeChanged(int paramInt1, int paramInt2, int paramInt3, int paramInt4) {
//        if (!tabShouldExpand) {
        post(new Runnable() {
            public void run() {
                PlusPagerSlidingTabStrip.this.notifyDataSetChanged();
            }
        });
//        }
    }

    /*public void setIndicatorColor(int indicatorColor) {
        this.indicatorColor = indicatorColor;
        invalidate();
    }

    public void setIndicatorColorResource(int resId) {
        this.indicatorColor = getResources().getColor(resId);
        invalidate();
    }

    public int getIndicatorColor() {
        return this.indicatorColor;
    }*/

    public void setIndicatorHeight(int indicatorLineHeightPx) {
        this.indicatorHeight = indicatorLineHeightPx;
        invalidate();
    }

    public int getIndicatorHeight() {
        return indicatorHeight;
    }

    public void setUnderlineColor(int underlineColor) {
        this.underlineColor = underlineColor;
        invalidate();
    }

    public void setUnderlineColorResource(int resId) {
        this.underlineColor = getResources().getColor(resId);
        invalidate();
    }

    public int getUnderlineColor() {
        return underlineColor;
    }

    public void setUnderlineHeight(int underlineHeightPx) {
        this.underlineHeight = underlineHeightPx;
        invalidate();
    }

    public int getUnderlineHeight() {
        return underlineHeight;
    }

    public void setDividerColor(int dividerColor) {
        this.dividerColor = dividerColor;
        invalidate();
    }

    public void setDividerColorResource(int resId) {
        this.dividerColor = getResources().getColor(resId);
        invalidate();
    }

    public int getDividerColor() {
        return dividerColor;
    }

    public void setDividerPadding(int dividerPaddingPx) {
        this.dividerPadding = dividerPaddingPx;
        invalidate();
    }

    public int getDividerPadding() {
        return dividerPadding;
    }

    public void setScrollOffset(int scrollOffsetPx) {
        this.scrollOffset = scrollOffsetPx;
        invalidate();
    }

    public int getScrollOffset() {
        return scrollOffset;
    }

    public void setShouldExpand(boolean shouldExpand) {
//        if(tabShouldExpand != shouldExpand) {
//            tabShouldExpand = shouldExpand;
        //tabsContainer.setLayoutParams(new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        //updateTabStyles();
        requestLayout();
//        }
    }

    public boolean isTextAllCaps() {
        return textAllCaps;
    }

    public void setAllCaps(boolean textAllCaps) {
        this.textAllCaps = textAllCaps;
    }

    public void setTextSize(int textSizePx) {
//        updateTabStyles();
    }

    public int getTextSize() {
        return 14;
    }

    /*public void setTextColor(int textColor) {
        if(this.tabTextColor != textColor) {
            this.tabTextColor = textColor;
            updateTabStyles();
        }
    }

    public void setTextSelectedColor(int textColor) {
        if(this.tabTextSelectedColor != textColor) {
            this.tabTextSelectedColor = textColor;
            updateTabStyles();
        }
    }

    public int getTextColor() {
        return tabTextColor;
    }*/

    public void setTypeface(Typeface typeface, int style) {
        this.tabTypeface = typeface;
        this.tabTypefaceStyle = style;
        updateTabStyles();
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        SavedState savedState = (SavedState) state;
        super.onRestoreInstanceState(savedState.getSuperState());
        currentPosition = savedState.currentPosition;
        requestLayout();
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState savedState = new SavedState(superState);
        savedState.currentPosition = currentPosition;
        return savedState;
    }

    static class SavedState extends BaseSavedState {
        int currentPosition;

        public SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            currentPosition = in.readInt();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(currentPosition);
        }

        public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
            @Override
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }

}
