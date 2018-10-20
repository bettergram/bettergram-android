package org.telegram.ui.Components;

import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.DataSetObserver;
import android.os.Build;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import io.bettergram.messenger.R;
import org.telegram.messenger.*;
import org.telegram.tgnet.TLRPC;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static io.bettergram.tools.DialogsObject.*;

/**
 * Created by Yan on 09/07/2018.
 */

public class TabsView extends FrameLayout implements NotificationCenter.NotificationCenterDelegate {
    private static final String TAG = "TabsView";
    //TODO: move to controller
    private static int selectedTab = 0;
    private static final List<Tab> tabs = Arrays.asList(
            new Tab(LocaleController.getString("tabsAll", R.string.tabsAll), R.drawable.tab_all, 0),
            new Tab(LocaleController.getString("tabsDirect", R.string.tabsDirect), R.drawable.tab_user, 101),
            new Tab(LocaleController.getString("tabsGroups", R.string.tabsGroups), R.drawable.tab_group, 102),
            new Tab(LocaleController.getString("tabsAnnouncements", R.string.tabsAnnouncements), R.drawable.tab_channel, 103),
            new Tab(LocaleController.getString("tabsFavorites", R.string.tabsFavorites), R.drawable.tab_favs, 104)
            //new Tab(LocaleController.getString("tabsCrypto", R.string.tabsCrypto), R.drawable.tab_bot, 105)
    );

    private final AccelerateDecelerateInterpolator interpolator = new AccelerateDecelerateInterpolator();
    private int viewWidth;
    private int viewHeight;
    private boolean hidden;
    private int lastParentTopMargin, lastTabTopMargin;

    private boolean counting = false;

    private static class Tab {
        private final String title;
        private final int res;
        private final int type;
        private int position; //0:All 1:Users 2:Groups 3:SuperGroups 4:Channels 5:Bots 6:Favs(8) 7:Groups+SuperGroups(9) 8:Creator(10) 9:Unread(11)
        private int unread;

        Tab(String title, int res, int type) {
            this(title, res, type, -1);
        }

        Tab(String title, int res, int type, int position) {
            this.title = title;
            this.res = res;
            this.type = type;
            this.position = position;
            this.unread = 0;
        }

        public String getTitle() {
            return this.title;
        }

        public int getRes() {
            return this.res;
        }

        public int getType() {
            return this.type;
        }

        public int getPosition() {
            return this.position;
        }

        public int getUnread() {
            return this.unread;
        }

        public void setUnread(int unread) {
            this.unread = unread;
        }
    }

    public interface RefreshAction {
        void onNewTypeSelected(int type, boolean scroll);
    }

    private LinearLayout tabsContainer;
    private int currentAccount = UserConfig.selectedAccount;
    private PlusPagerSlidingTabStrip pagerSlidingTabStrip;
    private ViewPager pager;
    private int currentPage;
    private boolean force;
    private RefreshAction refreshAction;

    public TabsView refreshAction(RefreshAction action) {
        this.refreshAction = action;
        return this;
    }

    private void loadArray() {
        pager.setAdapter(null);
        pager.setOffscreenPageLimit(tabs.size()); // fixes bug with Nexus 5 6.0.1 and infinite scroll
        pager.setAdapter(new TabsAdapter());
        updatePagerItem();
    }

    public void reloadTabs() {
        loadArray();
        pager.getAdapter().notifyDataSetChanged();
    }

    private void updatePagerItem() {
        currentPage = selectedTab;
        pager.setCurrentItem(currentPage);
    }

    public TabsView(Context context) {
        super(context);
        init(context);
    }

    public TabsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public TabsView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public TabsView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    @Override
    protected void onSizeChanged(int xNew, int yNew, int xOld, int yOld) {
        super.onSizeChanged(xNew, yNew, xOld, yOld);
        viewWidth = xNew;
        viewHeight = yNew;
    }

    private void init(Context context) {
        pager = new ViewPager(context) {
            @Override
            public boolean onInterceptTouchEvent(MotionEvent ev) {
                if (getParent() != null) {
                    getParent().requestDisallowInterceptTouchEvent(true);
                }
                return super.onInterceptTouchEvent(ev);
            }
        };

        loadArray();
        tabsContainer = new LinearLayout(context) {
            @Override
            public boolean onInterceptTouchEvent(MotionEvent ev) {
                if (getParent() != null) {
                    getParent().requestDisallowInterceptTouchEvent(true);
                }
                return super.onInterceptTouchEvent(ev);
            }
        };
        tabsContainer.setOrientation(LinearLayout.HORIZONTAL);
        addView(tabsContainer, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
        pagerSlidingTabStrip = new PlusPagerSlidingTabStrip(context);
        pagerSlidingTabStrip.setShouldExpand(false);
        pagerSlidingTabStrip.setViewPager(pager);
        pagerSlidingTabStrip.setIndicatorHeight(AndroidUtilities.dp(3));

        pagerSlidingTabStrip.setDividerColor(0x00000000);
        pagerSlidingTabStrip.setUnderlineHeight(0);
        pagerSlidingTabStrip.setUnderlineColor(/*0xffe2e5e7*/0x00000000);

        tabsContainer.addView(pagerSlidingTabStrip, LayoutHelper.createLinear(0, LayoutHelper.MATCH_PARENT, 1.0f));

        pagerSlidingTabStrip.setDelegate(new PlusPagerSlidingTabStrip.PlusScrollSlidingTabStripDelegate() {
            @Override
            public void onTabLongClick(int position) {
                if (selectedTab == position) {
                    //TODO
                }
            }

            @Override
            public void onTabsUpdated() {
                forceUpdateTabCounters();
                unreadCount();
            }

            @Override
            public void onTabClick() {
                //gone into RefreshAction
            }
        });

        pagerSlidingTabStrip.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            private boolean loop;

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                if (refreshAction != null) {
                    refreshAction.onNewTypeSelected(tabs.get(position).getType(), true);
                }
                currentPage = position;
                saveNewPage();
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                if (state == ViewPager.SCROLL_STATE_IDLE) {
                    if (loop) {
                        AndroidUtilities.runOnUIThread(() -> pager.setCurrentItem(currentPage == 0 ? pager.getAdapter().getCount() - 1 : 0), 100);
                        loop = false;
                    }
                } else if (state == 1) {
                    loop = false;
                } else if (state == 2) {
                    loop = false;
                }
            }
        });

        addView(pager, 0, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));
        forceUpdateTabCounters();
        unreadCount();
    }

    private void saveNewPage() {
        selectedTab = currentPage;
        SharedPreferences plusPreferences = ApplicationLoader.applicationContext.getSharedPreferences("plusconfig", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = plusPreferences.edit();
        editor.putInt("selectedTab", selectedTab);
        //TODO: important
//        Theme.plusDialogType = tabsArray.get(selectedTab).getType();
//        editor.putInt("dialogType", Theme.plusDialogType);
        editor.apply();
    }

    public ViewPager getPager() {
        return pager;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        //TODO: important
        //NotificationCenter.getInstance(currentAccount).removeObserver(this, NotificationCenter.updateInterfaces);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        //TODO: important
        NotificationCenter.getInstance(currentAccount).addObserver(this, NotificationCenter.updateInterfaces);
        NotificationCenter.getInstance(currentAccount).addObserver(this, NotificationCenter.dialogsNeedReload);
    }

    @Override
    public void didReceivedNotification(int id, int account, Object... args) {
        //TODO: important
        if (!counting && (id == NotificationCenter.updateInterfaces || id == NotificationCenter.dialogsNeedReload)) {
            counting = true;
            unreadCount();
        }
    }

    public void forceUpdateTabCounters() {
        force = true;
    }

    private void unreadCount() {
        MessagesController messagesController = MessagesController.getInstance(currentAccount);
        //unreadCount(messagesController.dialogsUnread, positions[8]);
        //unreadCount(messagesController.dialogsAdmin, positions[7]);
        //unreadCount(messagesController.dialogsFavs, positions[6]);
        //unreadCount(messagesController.dialogsBots, positions[5]);
        //unreadCount(messagesController.dialogsChannels, positions[4]);
        //unreadCountGroups();
        //unreadCount(messagesController.dialogsUsers, positions[1]);
        ArrayList<TLRPC.TL_dialog> dialogs = messagesController.dialogs;
        for (int i = 0; i < tabs.size(); i++) {
            force = true;
            unreadCount(dialogs, i);
        }
    }

//    private void unreadCountGroups() {
//        MessagesController messagesController = MessagesController.getInstance(currentAccount);
//        if(!Theme.plusHideGroupsTab)unreadCount(!Theme.plusHideSuperGroupsTab ? messagesController.dialogsGroups : messagesController.dialogsGroupsAll, positions[2]);
//        if(!Theme.plusHideSuperGroupsTab)unreadCount(messagesController.dialogsMegaGroups, positions[3]);
//    }

    private void unreadCount(final ArrayList<TLRPC.TL_dialog> dialogs, int position) {
        if (position == -1) {
            counting = false;
            return;
        }
        SharedPreferences plusPreferences = ApplicationLoader.applicationContext.getSharedPreferences("plusconfig", Activity.MODE_PRIVATE);
        boolean allMuted = true;
        int unreadCount = 0;
        int i;
        if (dialogs != null && !dialogs.isEmpty()) {
            for (int a = 0; a < dialogs.size(); a++) {
                TLRPC.TL_dialog dialg = dialogs.get(a);
                if (dialg != null && dialg.unread_count > 0) {
                    //boolean isMuted = MessagesController.getInstance(currentAccount).isDialogMuted(dialg.id);
                    //if (!isMuted) {
                    i = dialg.unread_count;
                    //if (i == 0 && plusPreferences.getInt("unread_" + dialg.id, 0) == 1) i = 1;
                    //if (i > 0) {
                    if (position == 0 || (position == 1 && isDirect(dialg)) || (position == 2 && isGroup(dialg)) || (position == 3 && isAnnouncement(dialg)) || (position == 4 & isFavorite(dialg))) {
                        unreadCount++;
                    }
                    allMuted = false;
                    //}
                    //}
                }
            }
        }
        if (unreadCount != tabs.get(position).getUnread() || force) {
            tabs.get(position).setUnread(unreadCount);
            pagerSlidingTabStrip.updateCounter(position, unreadCount, allMuted, force);
        }
        counting = false;
    }

    private void unreadCountAll(ArrayList<TLRPC.TL_dialog> dialogs, int position) {
        unreadCount(dialogs, position);
        force = false;
    }

    private class TabsAdapter extends PagerAdapter implements PlusPagerSlidingTabStrip.IconTabProvider {
        @Override
        public int getCount() {
            return tabs.size();
        }

        @Override
        public void notifyDataSetChanged() {
            super.notifyDataSetChanged();
            if (pagerSlidingTabStrip != null) {
                pagerSlidingTabStrip.notifyDataSetChanged();
            }
        }

        @Override
        public Object instantiateItem(ViewGroup viewGroup, int position) {
            View view = new View(viewGroup.getContext());
            viewGroup.addView(view);
            return view;
        }

        @Override
        public void destroyItem(ViewGroup viewGroup, int position, Object object) {
            viewGroup.removeView((View) object);
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public void unregisterDataSetObserver(DataSetObserver observer) {
            if (observer != null) {
                super.unregisterDataSetObserver(observer);
            }
        }

        @Override
        public int getPageIconResId(int position) {
            return tabs.get(position).getRes();
        }

        @Override
        public String getPageTitle(int position) {
            return tabs.get(position).getTitle();
        }
    }

    // hack
    public void forceRefreshAction() {
        if (refreshAction != null) {
            int type = tabs.get(currentPage).getType();
            refreshAction.onNewTypeSelected(type, false);
        }
    }

    public void hide(boolean hide) {
        if (hidden == hide) {
            return;
        }
        hidden = hide;

        ViewGroup parent = (ViewGroup) getParent();
        MarginLayoutParams parentMargins = (MarginLayoutParams) parent.getLayoutParams();

        lastParentTopMargin = hide ? parentMargins.topMargin : lastParentTopMargin;

        ValueAnimator marginAnimator = ValueAnimator.ofInt(lastParentTopMargin, lastParentTopMargin);
        marginAnimator.addUpdateListener((ValueAnimator valueAnimator) -> {
            parentMargins.topMargin = (int) valueAnimator.getAnimatedValue();
            parent.setLayoutParams(parentMargins);
        });

        MarginLayoutParams tabMargins = (MarginLayoutParams) getLayoutParams();

        lastTabTopMargin = hide ? tabMargins.topMargin : lastTabTopMargin;

        ValueAnimator tabMarginAnimator = ValueAnimator.ofInt(hide ? lastTabTopMargin : -viewHeight, hide ? -viewHeight : lastTabTopMargin);
        tabMarginAnimator.addUpdateListener(valueAnimator -> {
            tabMargins.topMargin = (int) valueAnimator.getAnimatedValue();
            setLayoutParams(tabMargins);
        });

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(marginAnimator, tabMarginAnimator);
        animatorSet.setDuration(100);
        animatorSet.setInterpolator(interpolator);
        animatorSet.start();
    }
}
