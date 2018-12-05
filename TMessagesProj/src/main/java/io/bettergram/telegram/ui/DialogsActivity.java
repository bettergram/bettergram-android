/*
 * This is the source code of Telegram for Android v. 3.x.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2017.
 */

package io.bettergram.telegram.ui;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.StateListAnimator;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Outline;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.ViewCompat;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import io.bettergram.Constants;
import io.bettergram.adapters.BetterDialogsAdapter;
import io.bettergram.adapters.CryptoAdapter;
import io.bettergram.adapters.NewsAdapter;
import io.bettergram.adapters.ResourcesAdapter;
import io.bettergram.adapters.YouTubePlayerAdapter;
import io.bettergram.messenger.R;
import io.bettergram.service.api.CurrencyApi;
import io.bettergram.telegram.messenger.AndroidUtilities;
import io.bettergram.telegram.messenger.AppVersionTask;
import io.bettergram.telegram.messenger.ApplicationLoader;
import io.bettergram.telegram.messenger.BuildVars;
import io.bettergram.telegram.messenger.ChatObject;
import io.bettergram.telegram.messenger.ContactsController;
import io.bettergram.telegram.messenger.DataQuery;
import io.bettergram.telegram.messenger.DialogObject;
import io.bettergram.telegram.messenger.FileLog;
import io.bettergram.telegram.messenger.ImageLoader;
import io.bettergram.telegram.messenger.LocaleController;
import io.bettergram.telegram.messenger.MessageObject;
import io.bettergram.telegram.messenger.MessagesController;
import io.bettergram.telegram.messenger.NotificationCenter;
import io.bettergram.telegram.messenger.SharedConfig;
import io.bettergram.telegram.messenger.UserConfig;
import io.bettergram.telegram.messenger.UserObject;
import io.bettergram.telegram.messenger.support.widget.LinearLayoutManager;
import io.bettergram.telegram.messenger.support.widget.LinearSmoothScrollerMiddle;
import io.bettergram.telegram.messenger.support.widget.RecyclerView;
import io.bettergram.telegram.messenger.support.widget.helper.ItemTouchHelper;
import io.bettergram.telegram.tgnet.ConnectionsManager;
import io.bettergram.telegram.tgnet.TLObject;
import io.bettergram.telegram.tgnet.TLRPC;
import io.bettergram.telegram.ui.ActionBar.ActionBar;
import io.bettergram.telegram.ui.ActionBar.ActionBarMenu;
import io.bettergram.telegram.ui.ActionBar.ActionBarMenuItem;
import io.bettergram.telegram.ui.ActionBar.AlertDialog;
import io.bettergram.telegram.ui.ActionBar.BaseFragment;
import io.bettergram.telegram.ui.ActionBar.BottomSheet;
import io.bettergram.telegram.ui.ActionBar.MenuDrawable;
import io.bettergram.telegram.ui.ActionBar.Theme;
import io.bettergram.telegram.ui.ActionBar.ThemeDescription;
import io.bettergram.telegram.ui.Adapters.DialogsSearchAdapter;
import io.bettergram.telegram.ui.Cells.AccountSelectCell;
import io.bettergram.telegram.ui.Cells.DialogCell;
import io.bettergram.telegram.ui.Cells.DialogsEmptyCell;
import io.bettergram.telegram.ui.Cells.DividerCell;
import io.bettergram.telegram.ui.Cells.DrawerActionCell;
import io.bettergram.telegram.ui.Cells.DrawerAddCell;
import io.bettergram.telegram.ui.Cells.DrawerProfileCell;
import io.bettergram.telegram.ui.Cells.DrawerUserCell;
import io.bettergram.telegram.ui.Cells.GraySectionCell;
import io.bettergram.telegram.ui.Cells.HashtagSearchCell;
import io.bettergram.telegram.ui.Cells.HintDialogCell;
import io.bettergram.telegram.ui.Cells.LoadingCell;
import io.bettergram.telegram.ui.Cells.ProfileSearchCell;
import io.bettergram.telegram.ui.Cells.UserCell;
import io.bettergram.telegram.ui.Components.AlertsCreator;
import io.bettergram.telegram.ui.Components.AnimatedArrowDrawable;
import io.bettergram.telegram.ui.Components.AvatarDrawable;
import io.bettergram.telegram.ui.Components.BackupImageView;
import io.bettergram.telegram.ui.Components.BottomBar.BottomNavigationBar;
import io.bettergram.telegram.ui.Components.ChatActivityEnterView;
import io.bettergram.telegram.ui.Components.CombinedDrawable;
import io.bettergram.telegram.ui.Components.EmptyTextProgressView;
import io.bettergram.telegram.ui.Components.FragmentContextView;
import io.bettergram.telegram.ui.Components.JoinGroupAlert;
import io.bettergram.telegram.ui.Components.LayoutHelper;
import io.bettergram.telegram.ui.Components.ProxyDrawable;
import io.bettergram.telegram.ui.Components.PullToRefresh.PullRefreshLayout;
import io.bettergram.telegram.ui.Components.RadialProgressView;
import io.bettergram.telegram.ui.Components.RecyclerListView;
import io.bettergram.telegram.ui.Components.SizeNotifierFrameLayout;
import io.bettergram.telegram.ui.Components.StickersAlert;
import io.bettergram.telegram.ui.Components.TabsView;

public class DialogsActivity extends BaseFragment implements NotificationCenter.NotificationCenterDelegate {

    private RecyclerListView listView;
    private LinearLayoutManager layoutManager;
    private BetterDialogsAdapter dialogsAdapter;
    private NewsAdapter newsAdapter;
    private CryptoAdapter cryptoAdapter;
    private YouTubePlayerAdapter videoAdapter;
    private ResourcesAdapter resourcesAdapter = new ResourcesAdapter();
    private DialogsSearchAdapter dialogsSearchAdapter;
    private EmptyTextProgressView searchEmptyView;
    private RadialProgressView progressView;
    private ActionBarMenuItem passcodeItem;
    private ActionBarMenuItem proxyItem;
    private ProxyDrawable proxyDrawable;
    private ImageView floatingButton;
    private TabsView newTabsView;
    private Dialog bottomSheetDialog;

    private ImageView unreadFloatingButton;
    private FrameLayout unreadFloatingButtonContainer;
    private TextView unreadFloatingButtonCounter;
    private int currentUnreadCount;

    private AnimatedArrowDrawable arrowDrawable;
    private RecyclerView sideMenu;
    private ChatActivityEnterView commentView;
    private ActionBarMenuItem switchItem;

    private PullRefreshLayout ptrLayout;
    private BottomNavigationBar bottomBar;

    private AlertDialog permissionDialog;
    private boolean askAboutContacts = true;

    private boolean proxyItemVisisble;

    private int prevPosition;
    private int prevTop;
    private boolean scrollUpdated;
    private boolean floatingHidden;
    private final AccelerateDecelerateInterpolator floatingInterpolator = new AccelerateDecelerateInterpolator();

    private boolean checkPermission = true;

    private int currentConnectionState;

    private String selectAlertString;
    private String selectAlertStringGroup;
    private String addToGroupAlertString;
    private int dialogsType;

    public static boolean dialogsLoaded[] = new boolean[UserConfig.MAX_ACCOUNT_COUNT];
    private boolean searching;
    private boolean searchWas;
    private boolean onlySelect;
    private long selectedDialog;
    private String searchString;
    private long openedDialogId;
    private boolean cantSendToChannels;
    private boolean allowSwitchAccount;

    private boolean backPressed = false;

    private int currentBottomTabPosition = 0;

    private DialogsActivityDelegate delegate;

    private AppVersionTask appVersionTask;

    private AlertDialog outdatedDialog;

    public interface DialogsActivityDelegate {
        void didSelectDialogs(DialogsActivity fragment, ArrayList<Long> dids, CharSequence message, boolean param);
    }

    public DialogsActivity(Bundle args) {
        super(args);
    }

    @Override
    public boolean onFragmentCreate() {
        super.onFragmentCreate();

        if (getArguments() != null) {
            onlySelect = arguments.getBoolean("onlySelect", false);
            cantSendToChannels = arguments.getBoolean("cantSendToChannels", false);
            dialogsType = arguments.getInt("dialogsType", 0);
            selectAlertString = arguments.getString("selectAlertString");
            selectAlertStringGroup = arguments.getString("selectAlertStringGroup");
            addToGroupAlertString = arguments.getString("addToGroupAlertString");
            allowSwitchAccount = arguments.getBoolean("allowSwitchAccount");
        }

        if (dialogsType == 0) {
            askAboutContacts = MessagesController.getGlobalNotificationsSettings().getBoolean("askAboutContacts", true);
            SharedConfig.loadProxyList();
        }

        if (searchString == null) {
            currentConnectionState = ConnectionsManager.getInstance(currentAccount).getConnectionState();

            NotificationCenter.getInstance(currentAccount).addObserver(this, NotificationCenter.dialogsNeedReload);
            NotificationCenter.getGlobalInstance().addObserver(this, NotificationCenter.emojiDidLoaded);
            if (!onlySelect) {
                NotificationCenter.getGlobalInstance().addObserver(this, NotificationCenter.closeSearchByActiveAction);
                NotificationCenter.getGlobalInstance().addObserver(this, NotificationCenter.proxySettingsChanged);
            }
            NotificationCenter.getInstance(currentAccount).addObserver(this, NotificationCenter.updateInterfaces);
            NotificationCenter.getInstance(currentAccount).addObserver(this, NotificationCenter.encryptedChatUpdated);
            NotificationCenter.getInstance(currentAccount).addObserver(this, NotificationCenter.contactsDidLoaded);
            NotificationCenter.getInstance(currentAccount).addObserver(this, NotificationCenter.appDidLogout);
            NotificationCenter.getInstance(currentAccount).addObserver(this, NotificationCenter.openedChatChanged);
            NotificationCenter.getInstance(currentAccount).addObserver(this, NotificationCenter.notificationsSettingsUpdated);
            NotificationCenter.getInstance(currentAccount).addObserver(this, NotificationCenter.messageReceivedByAck);
            NotificationCenter.getInstance(currentAccount).addObserver(this, NotificationCenter.messageReceivedByServer);
            NotificationCenter.getInstance(currentAccount).addObserver(this, NotificationCenter.messageSendError);
            NotificationCenter.getInstance(currentAccount).addObserver(this, NotificationCenter.needReloadRecentDialogsSearch);
            NotificationCenter.getInstance(currentAccount).addObserver(this, NotificationCenter.didLoadedReplyMessages);
            NotificationCenter.getInstance(currentAccount).addObserver(this, NotificationCenter.reloadHints);
            NotificationCenter.getInstance(currentAccount).addObserver(this, NotificationCenter.didUpdatedConnectionState);
            NotificationCenter.getInstance(currentAccount).addObserver(this, NotificationCenter.dialogsUnreadCounterChanged);

            NotificationCenter.getGlobalInstance().addObserver(this, NotificationCenter.didSetPasscode);

            NotificationCenter.getGlobalInstance().addObserver(this, NotificationCenter.updateToLatestApiVersion);
            NotificationCenter.getGlobalInstance().addObserver(this, NotificationCenter.currencySearchResultsUpdate);
            NotificationCenter.getGlobalInstance().addObserver(this, NotificationCenter.updateCurrencyDataToBackup);
        }

        if (!dialogsLoaded[currentAccount]) {
            MessagesController.getInstance(currentAccount).loadGlobalNotificationsSettings();
            MessagesController.getInstance(currentAccount).loadDialogs(0, 100, true);
            MessagesController.getInstance(currentAccount).loadHintDialogs();
            ContactsController.getInstance(currentAccount).checkInviteText();
            MessagesController.getInstance(currentAccount).loadPinnedDialogs(0, null);
            DataQuery.getInstance(currentAccount).loadRecents(DataQuery.TYPE_FAVE, false, true, false);
            DataQuery.getInstance(currentAccount).checkFeaturedStickers();
            dialogsLoaded[currentAccount] = true;
        }

        return true;
    }

    @Override
    public void onFragmentDestroy() {
        super.onFragmentDestroy();
        if (searchString == null) {
            NotificationCenter.getInstance(currentAccount).removeObserver(this, NotificationCenter.dialogsNeedReload);
            NotificationCenter.getGlobalInstance().removeObserver(this, NotificationCenter.emojiDidLoaded);
            if (!onlySelect) {
                NotificationCenter.getGlobalInstance().removeObserver(this, NotificationCenter.closeSearchByActiveAction);
                NotificationCenter.getGlobalInstance().removeObserver(this, NotificationCenter.proxySettingsChanged);
            }
            NotificationCenter.getInstance(currentAccount).removeObserver(this, NotificationCenter.updateInterfaces);
            NotificationCenter.getInstance(currentAccount).removeObserver(this, NotificationCenter.encryptedChatUpdated);
            NotificationCenter.getInstance(currentAccount).removeObserver(this, NotificationCenter.contactsDidLoaded);
            NotificationCenter.getInstance(currentAccount).removeObserver(this, NotificationCenter.appDidLogout);
            NotificationCenter.getInstance(currentAccount).removeObserver(this, NotificationCenter.openedChatChanged);
            NotificationCenter.getInstance(currentAccount).removeObserver(this, NotificationCenter.notificationsSettingsUpdated);
            NotificationCenter.getInstance(currentAccount).removeObserver(this, NotificationCenter.messageReceivedByAck);
            NotificationCenter.getInstance(currentAccount).removeObserver(this, NotificationCenter.messageReceivedByServer);
            NotificationCenter.getInstance(currentAccount).removeObserver(this, NotificationCenter.messageSendError);
            NotificationCenter.getInstance(currentAccount).removeObserver(this, NotificationCenter.needReloadRecentDialogsSearch);
            NotificationCenter.getInstance(currentAccount).removeObserver(this, NotificationCenter.didLoadedReplyMessages);
            NotificationCenter.getInstance(currentAccount).removeObserver(this, NotificationCenter.reloadHints);
            NotificationCenter.getInstance(currentAccount).removeObserver(this, NotificationCenter.didUpdatedConnectionState);
            NotificationCenter.getInstance(currentAccount).removeObserver(this, NotificationCenter.dialogsUnreadCounterChanged);

            NotificationCenter.getGlobalInstance().removeObserver(this, NotificationCenter.didSetPasscode);

            NotificationCenter.getGlobalInstance().removeObserver(this, NotificationCenter.updateToLatestApiVersion);
            NotificationCenter.getGlobalInstance().removeObserver(this, NotificationCenter.currencySearchResultsUpdate);
            NotificationCenter.getGlobalInstance().removeObserver(this, NotificationCenter.updateCurrencyDataToBackup);
        }
        if (commentView != null) {
            commentView.onDestroy();
        }
        delegate = null;
    }

    @SuppressLint("DefaultLocale")
    @Override
    public View createView(final Context context) {
        searching = false;
        searchWas = false;

        AndroidUtilities.runOnUIThread(() -> Theme.createChatResources(context, false));

        ActionBarMenu menu = actionBar.createMenu();
        if (!onlySelect && searchString == null) {
            proxyDrawable = new ProxyDrawable(context);
            proxyItem = menu.addItem(2, proxyDrawable);
            passcodeItem = menu.addItem(1, R.drawable.lock_close);
            updatePasscodeButton();
            updateProxyButton(false);
        }
        final ActionBarMenuItem item = menu.addItem(0, R.drawable.ic_ab_search).setIsSearchField(true).setActionBarMenuItemSearchListener(new ActionBarMenuItem.ActionBarMenuItemSearchListener() {
            @Override
            public void onSearchExpand() {
                newTabsView.hide(true);
                searching = true;
                if (switchItem != null) {
                    switchItem.setVisibility(View.GONE);
                }
                if (proxyItem != null && proxyItemVisisble) {
                    proxyItem.setVisibility(View.GONE);
                }
                if (listView != null) {
                    if (searchString != null) {
                        listView.setEmptyView(searchEmptyView);
                        progressView.setVisibility(View.GONE);
                    }
                    if (!onlySelect) {
                        floatingButton.setVisibility(View.GONE);
                        unreadFloatingButtonContainer.setVisibility(View.GONE);
                    }
                }
                updatePasscodeButton();
            }

            @Override
            public boolean canCollapseSearch() {
                if (currentBottomTabPosition == 1) {
                    cryptoAdapter.setSearchMode(false);
                    NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.updateCurrencyDataToBackup);
                }
                if (switchItem != null) {
                    switchItem.setVisibility(View.VISIBLE);
                }
                if (proxyItem != null && proxyItemVisisble) {
                    proxyItem.setVisibility(View.VISIBLE);
                }
                if (searchString != null) {
                    finishFragment();
                    return false;
                }
                return true;
            }

            @Override
            public void onSearchCollapse() {
                if (currentBottomTabPosition == 0) {
                    newTabsView.hide(false);
                } else {
                    cryptoAdapter.setSearchMode(false);
                }
                searching = false;
                searchWas = false;
                if (listView != null) {
                    listView.setEmptyView(progressView);
                    searchEmptyView.setVisibility(View.GONE);
                    if (!onlySelect) {
                        floatingButton.setVisibility(View.VISIBLE);
                        if (currentUnreadCount != 0) {
                            unreadFloatingButtonContainer.setVisibility(View.VISIBLE);
                            unreadFloatingButtonContainer.setTranslationY(AndroidUtilities.dp(74));
                        }
                        floatingHidden = true;
                        floatingButton.setTranslationY(AndroidUtilities.dp(100));
                        hideFloatingButton(false, false);
                    }
                    if (listView.getAdapter() != dialogsAdapter && currentBottomTabPosition == 0) {
                        listView.setAdapter(dialogsAdapter);
                        dialogsAdapter.notifyDataSetChanged();
                    }
                }
                if (currentBottomTabPosition == 0) {
                    if (dialogsSearchAdapter != null) {
                        dialogsSearchAdapter.searchDialogs(null);
                    }
                    updatePasscodeButton();
                }
            }

            @Override
            public void onTextChanged(EditText editText) {
                String text = editText.getText().toString();
                if (currentBottomTabPosition == 0) {
                    if (text.length() != 0 || dialogsSearchAdapter != null && dialogsSearchAdapter.hasRecentRearch()) {
                        searchWas = true;
                        if (dialogsSearchAdapter != null && listView.getAdapter() != dialogsSearchAdapter) {
                            listView.setAdapter(dialogsSearchAdapter);
                            dialogsSearchAdapter.notifyDataSetChanged();
                        }
                        if (searchEmptyView != null && listView.getEmptyView() != searchEmptyView) {
                            progressView.setVisibility(View.GONE);
                            searchEmptyView.showTextView();
                            listView.setEmptyView(searchEmptyView);
                        }
                    }
                    if (dialogsSearchAdapter != null) {
                        dialogsSearchAdapter.searchDialogs(text);
                    }
                } else {
                    ptrLayout.setRefreshing(text.length() > 0);
                    cryptoAdapter.setSearchMode(text.length() > 0);
                    CurrencyApi.search(text);
                }
            }
        });
        item.getSearchField().setHint(LocaleController.getString("Search", R.string.Search));
        if (onlySelect) {
            actionBar.setBackButtonImage(R.drawable.ic_ab_back);
            if (dialogsType == 3 && selectAlertString == null) {
                actionBar.setTitle(LocaleController.getString("ForwardTo", R.string.ForwardTo));
            } else {
                actionBar.setTitle(LocaleController.getString("SelectChat", R.string.SelectChat));
            }
        } else {
            if (searchString != null) {
                actionBar.setBackButtonImage(R.drawable.ic_ab_back);
            } else {
                actionBar.setBackButtonDrawable(new MenuDrawable());
            }
            if (BuildVars.DEBUG_VERSION) {
                actionBar.setTitle(context.getString(R.string.AppNameBeta));
            } else {
                actionBar.setTitle(context.getString(R.string.AppName));
            }
            actionBar.setSupportsHolidayImage(true);
        }
        actionBar.setTitleActionRunnable(() -> {
            hideFloatingButton(false, false);
            listView.smoothScrollToPosition(0);
        });

        if (allowSwitchAccount && UserConfig.getActivatedAccountsCount() > 1) {
            switchItem = menu.addItemWithWidth(1, 0, AndroidUtilities.dp(56));
            AvatarDrawable avatarDrawable = new AvatarDrawable();
            avatarDrawable.setTextSize(AndroidUtilities.dp(12));

            BackupImageView imageView = new BackupImageView(context);
            imageView.setRoundRadius(AndroidUtilities.dp(18));
            switchItem.addView(imageView, LayoutHelper.createFrame(36, 36, Gravity.CENTER));

            TLRPC.User user = UserConfig.getInstance(currentAccount).getCurrentUser();
            avatarDrawable.setInfo(user);
            TLRPC.FileLocation avatar;
            if (user.photo != null && user.photo.photo_small != null && user.photo.photo_small.volume_id != 0 && user.photo.photo_small.local_id != 0) {
                avatar = user.photo.photo_small;
            } else {
                avatar = null;
            }
            imageView.getImageReceiver().setCurrentAccount(currentAccount);
            imageView.setImage(avatar, "50_50", avatarDrawable);

            for (int a = 0; a < UserConfig.MAX_ACCOUNT_COUNT; a++) {
                TLRPC.User u = UserConfig.getInstance(a).getCurrentUser();
                if (u != null) {
                    AccountSelectCell cell = new AccountSelectCell(context);
                    cell.setAccount(a, true);
                    switchItem.addSubItem(10 + a, cell, AndroidUtilities.dp(230), AndroidUtilities.dp(48));
                }
            }
        }
        actionBar.setAllowOverlayTitle(true);

        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    if (onlySelect) {
                        finishFragment();
                    } else if (parentLayout != null) {
                        parentLayout.getDrawerLayoutContainer().openDrawer(false);
                    }
                } else if (id == 1) {
                    SharedConfig.appLocked = !SharedConfig.appLocked;
                    SharedConfig.saveConfig();
                    updatePasscodeButton();
                } else if (id == 2) {
                    presentFragment(new ProxyListActivity());
                } else if (id >= 10 && id < 10 + UserConfig.MAX_ACCOUNT_COUNT) {
                    if (getParentActivity() == null) {
                        return;
                    }
                    DialogsActivityDelegate oldDelegate = delegate;
                    LaunchActivity launchActivity = (LaunchActivity) getParentActivity();
                    launchActivity.switchToAccount(id - 10, true);

                    DialogsActivity dialogsActivity = new DialogsActivity(arguments);
                    dialogsActivity.setDelegate(oldDelegate);
                    launchActivity.presentFragment(dialogsActivity, false, true);
                }
            }
        });

        if (sideMenu != null) {
            sideMenu.setBackgroundColor(Theme.getColor(Theme.key_chats_menuBackground));
            sideMenu.setGlowColor(Theme.getColor(Theme.key_chats_menuBackground));
            sideMenu.getAdapter().notifyDataSetChanged();
        }

        SizeNotifierFrameLayout contentView = new SizeNotifierFrameLayout(context) {

            int inputFieldHeight = 0;

            @Override
            protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                int widthSize = MeasureSpec.getSize(widthMeasureSpec);
                int heightSize = MeasureSpec.getSize(heightMeasureSpec);

                setMeasuredDimension(widthSize, heightSize);
                heightSize -= getPaddingTop();

                measureChildWithMargins(actionBar, widthMeasureSpec, 0, heightMeasureSpec, 0);

                int keyboardSize = getKeyboardHeight();
                int childCount = getChildCount();

                if (commentView != null) {
                    measureChildWithMargins(commentView, widthMeasureSpec, 0, heightMeasureSpec, 0);
                    Object tag = commentView.getTag();
                    if (tag != null && tag.equals(2)) {
                        if (keyboardSize <= AndroidUtilities.dp(20) && !AndroidUtilities.isInMultiwindow) {
                            heightSize -= commentView.getEmojiPadding();
                        }
                        inputFieldHeight = commentView.getMeasuredHeight();
                    } else {
                        inputFieldHeight = 0;
                    }
                }

                for (int i = 0; i < childCount; i++) {
                    View child = getChildAt(i);
                    if (child == null || child.getVisibility() == GONE || child == commentView || child == actionBar) {
                        continue;
                    }
                    if (child == listView || child == progressView || child == searchEmptyView) {
                        int contentWidthSpec = MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.EXACTLY);
                        int contentHeightSpec = MeasureSpec.makeMeasureSpec(Math.max(AndroidUtilities.dp(10), heightSize - inputFieldHeight + AndroidUtilities.dp(2)), MeasureSpec.EXACTLY);
                        child.measure(contentWidthSpec, contentHeightSpec);
                    } else if (commentView != null && commentView.isPopupView(child)) {
                        if (AndroidUtilities.isInMultiwindow) {
                            if (AndroidUtilities.isTablet()) {
                                child.measure(MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(Math.min(AndroidUtilities.dp(320), heightSize - inputFieldHeight - AndroidUtilities.statusBarHeight + getPaddingTop()), MeasureSpec.EXACTLY));
                            } else {
                                child.measure(MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(heightSize - inputFieldHeight - AndroidUtilities.statusBarHeight + getPaddingTop(), MeasureSpec.EXACTLY));
                            }
                        } else {
                            child.measure(MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(child.getLayoutParams().height, MeasureSpec.EXACTLY));
                        }
                    } else {
                        measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, 0);
                    }
                }
            }

            @Override
            protected void onLayout(boolean changed, int l, int t, int r, int b) {
                final int count = getChildCount();

                int paddingBottom;
                Object tag = commentView != null ? commentView.getTag() : null;
                if (tag != null && tag.equals(2)) {
                    paddingBottom = getKeyboardHeight() <= AndroidUtilities.dp(20) && !AndroidUtilities.isInMultiwindow ? commentView.getEmojiPadding() : 0;
                } else {
                    paddingBottom = 0;
                }
                setBottomClip(paddingBottom);

                for (int i = 0; i < count; i++) {
                    final View child = getChildAt(i);
                    if (child.getVisibility() == GONE) {
                        continue;
                    }
                    final LayoutParams lp = (LayoutParams) child.getLayoutParams();

                    final int width = child.getMeasuredWidth();
                    final int height = child.getMeasuredHeight();

                    int childLeft;
                    int childTop;

                    int gravity = lp.gravity;
                    if (gravity == -1) {
                        gravity = Gravity.TOP | Gravity.LEFT;
                    }

                    final int absoluteGravity = gravity & Gravity.HORIZONTAL_GRAVITY_MASK;
                    final int verticalGravity = gravity & Gravity.VERTICAL_GRAVITY_MASK;

                    switch (absoluteGravity & Gravity.HORIZONTAL_GRAVITY_MASK) {
                        case Gravity.CENTER_HORIZONTAL:
                            childLeft = (r - l - width) / 2 + lp.leftMargin - lp.rightMargin;
                            break;
                        case Gravity.RIGHT:
                            childLeft = r - width - lp.rightMargin;
                            break;
                        case Gravity.LEFT:
                        default:
                            childLeft = lp.leftMargin;
                    }

                    switch (verticalGravity) {
                        case Gravity.TOP:
                            childTop = lp.topMargin + getPaddingTop();
                            break;
                        case Gravity.CENTER_VERTICAL:
                            childTop = ((b - paddingBottom) - t - height) / 2 + lp.topMargin - lp.bottomMargin;
                            break;
                        case Gravity.BOTTOM:
                            childTop = ((b - paddingBottom) - t) - height - lp.bottomMargin;
                            break;
                        default:
                            childTop = lp.topMargin;
                    }

                    if (commentView != null && commentView.isPopupView(child)) {
                        if (AndroidUtilities.isInMultiwindow) {
                            childTop = commentView.getTop() - child.getMeasuredHeight() + AndroidUtilities.dp(1);
                        } else {
                            childTop = commentView.getBottom();
                        }
                    }
                    child.layout(childLeft, childTop, childLeft + width, childTop + height);
                }

                notifyHeightChanged();
            }
        };
        fragmentView = contentView;

        listView = new RecyclerListView(context);
        listView.setVerticalScrollBarEnabled(true);
        listView.setItemAnimator(null);
        listView.setInstantClick(true);
        listView.setLayoutAnimation(null);
        listView.setBackgroundColor(Theme.getColor(Theme.key_dialog_backgroundColor));
        listView.setTag(4);
        layoutManager = new LinearLayoutManager(context) {
            @Override
            public boolean supportsPredictiveItemAnimations() {
                return false;
            }

            @Override
            public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state, int position) {
                LinearSmoothScrollerMiddle linearSmoothScroller = new LinearSmoothScrollerMiddle(recyclerView.getContext());
                linearSmoothScroller.setTargetPosition(position);
                startSmoothScroll(linearSmoothScroller);
            }
        };
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        listView.setLayoutManager(layoutManager);
        listView.setVerticalScrollbarPosition(LocaleController.isRTL ? RecyclerListView.SCROLLBAR_POSITION_LEFT : RecyclerListView.SCROLLBAR_POSITION_RIGHT);

        newTabsView = new TabsView(context)
                .refreshAction((type, scroll) -> {
                    //todo: check for the same type?
                    int firstVisibleItem = layoutManager.findFirstVisibleItemPosition();
                    //change data set
                    if (dialogsAdapter != null) {
                        dialogsAdapter.setDialogsType(type);
                    }
                    if (scroll) {
                        //scroll to top
                        if (firstVisibleItem < 20) {
                            listView.smoothScrollToPosition(0);
                        } else {
                            listView.scrollToPosition(0);
                        }
                    }
                    actionBar.setTitle(newTabsView.getCurrentTab().getTitle());
                });
        LinearLayout tabsContainer = new LinearLayout(context);
        tabsContainer.setOrientation(LinearLayout.VERTICAL);

        tabsContainer.addView(newTabsView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, AndroidUtilities.isTablet() ? 44 : 42, Gravity.TOP));

        ptrLayout = new PullRefreshLayout(context);
        ptrLayout.setEnabled(false); /* setting as disabled by default*/
        ptrLayout.addView(listView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        tabsContainer.addView(ptrLayout, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 0, 1f));

        contentView.addView(tabsContainer, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.CENTER, 0, AndroidUtilities.isTablet() ? 16 : 14, 0, AndroidUtilities.isTablet() ? 40 : 42));// added bottom margin for the bottom navigation view
        listView.setOnItemClickListener((view, position) -> {
            if (listView == null || listView.getAdapter() == null || getParentActivity() == null) {
                return;
            }

            long dialog_id = 0;
            int message_id = 0;
            boolean isGlobalSearch = false;
            RecyclerView.Adapter adapter = listView.getAdapter();
            if (adapter == dialogsAdapter) {
                TLObject object = dialogsAdapter.getItem(position);
                if (object instanceof TLRPC.User) {
                    dialog_id = ((TLRPC.User) object).id;
                } else if (object instanceof TLRPC.TL_dialog) {
                    dialog_id = ((TLRPC.TL_dialog) object).id;
                } else if (object instanceof TLRPC.TL_recentMeUrlChat) {
                    dialog_id = -((TLRPC.TL_recentMeUrlChat) object).chat_id;
                } else if (object instanceof TLRPC.TL_recentMeUrlUser) {
                    dialog_id = ((TLRPC.TL_recentMeUrlUser) object).user_id;
                } else if (object instanceof TLRPC.TL_recentMeUrlChatInvite) {
                    TLRPC.TL_recentMeUrlChatInvite chatInvite = (TLRPC.TL_recentMeUrlChatInvite) object;
                    TLRPC.ChatInvite invite = chatInvite.chat_invite;
                    if (invite.chat == null && (!invite.channel || invite.megagroup) || invite.chat != null && (!ChatObject.isChannel(invite.chat) || invite.chat.megagroup)) {
                        String hash = chatInvite.url;
                        int index = hash.indexOf('/');
                        if (index > 0) {
                            hash = hash.substring(index + 1);
                        }
                        showDialog(new JoinGroupAlert(getParentActivity(), invite, hash, DialogsActivity.this));
                        return;
                    } else {
                        if (invite.chat != null) {
                            dialog_id = -invite.chat.id;
                        } else {
                            return;
                        }
                    }
                } else if (object instanceof TLRPC.TL_recentMeUrlStickerSet) {
                    TLRPC.StickerSet stickerSet = ((TLRPC.TL_recentMeUrlStickerSet) object).set.set;
                    TLRPC.TL_inputStickerSetID set = new TLRPC.TL_inputStickerSetID();
                    set.id = stickerSet.id;
                    set.access_hash = stickerSet.access_hash;
                    showDialog(new StickersAlert(getParentActivity(), DialogsActivity.this, set, null, null));
                    return;
                } else if (object instanceof TLRPC.TL_recentMeUrlUnknown) {
                    return;
                } else {
                    return;
                }
            } else if (adapter == dialogsSearchAdapter) {
                Object obj = dialogsSearchAdapter.getItem(position);
                isGlobalSearch = dialogsSearchAdapter.isGlobalSearch(position);
                if (obj instanceof TLRPC.User) {
                    dialog_id = ((TLRPC.User) obj).id;
                    if (!onlySelect) {
                        dialogsSearchAdapter.putRecentSearch(dialog_id, (TLRPC.User) obj);
                    }
                } else if (obj instanceof TLRPC.Chat) {
                    if (((TLRPC.Chat) obj).id > 0) {
                        dialog_id = -((TLRPC.Chat) obj).id;
                    } else {
                        dialog_id = AndroidUtilities.makeBroadcastId(((TLRPC.Chat) obj).id);
                    }
                    if (!onlySelect) {
                        dialogsSearchAdapter.putRecentSearch(dialog_id, (TLRPC.Chat) obj);
                    }
                } else if (obj instanceof TLRPC.EncryptedChat) {
                    dialog_id = ((long) ((TLRPC.EncryptedChat) obj).id) << 32;
                    if (!onlySelect) {
                        dialogsSearchAdapter.putRecentSearch(dialog_id, (TLRPC.EncryptedChat) obj);
                    }
                } else if (obj instanceof MessageObject) {
                    MessageObject messageObject = (MessageObject) obj;
                    dialog_id = messageObject.getDialogId();
                    message_id = messageObject.getId();
                    dialogsSearchAdapter.addHashtagsFromMessage(dialogsSearchAdapter.getLastSearchString());
                } else if (obj instanceof String) {
                    actionBar.openSearchField((String) obj);
                }
            }

            if (dialog_id == 0) {
                return;
            }

            if (onlySelect) {
                if (dialogsAdapter.hasSelectedDialogs()) {
                    dialogsAdapter.addOrRemoveSelectedDialog(dialog_id, view);
                    updateSelectedCount();
                } else {
                    didSelectResult(dialog_id, true, false);
                }
            } else {
                Bundle args = new Bundle();
                int lower_part = (int) dialog_id;
                int high_id = (int) (dialog_id >> 32);
                if (lower_part != 0) {
                    if (high_id == 1) {
                        args.putInt("chat_id", lower_part);
                    } else {
                        if (lower_part > 0) {
                            args.putInt("user_id", lower_part);
                        } else if (lower_part < 0) {
                            if (message_id != 0) {
                                TLRPC.Chat chat = MessagesController.getInstance(currentAccount).getChat(-lower_part);
                                if (chat != null && chat.migrated_to != null) {
                                    args.putInt("migrated_to", lower_part);
                                    lower_part = -chat.migrated_to.channel_id;
                                }
                            }
                            args.putInt("chat_id", -lower_part);
                        }
                    }
                } else {
                    args.putInt("enc_id", high_id);
                }
                if (message_id != 0) {
                    args.putInt("message_id", message_id);
                } else if (!isGlobalSearch) {
                    if (actionBar != null) {
                        actionBar.closeSearchField();
                    }
                }
                if (AndroidUtilities.isTablet()) {
                    if (openedDialogId == dialog_id && adapter != dialogsSearchAdapter) {
                        return;
                    }
                    if (dialogsAdapter != null) {
                        dialogsAdapter.setOpenedDialogId(openedDialogId = dialog_id);
                        updateVisibleRows(MessagesController.UPDATE_MASK_SELECT_DIALOG);
                    }
                }
                if (searchString != null) {
                    if (MessagesController.getInstance(currentAccount).checkCanOpenChat(args, DialogsActivity.this)) {
                        NotificationCenter.getInstance(currentAccount).postNotificationName(NotificationCenter.closeChats);
                        presentFragment(new ChatActivity(args));
                    }
                } else {
                    if (MessagesController.getInstance(currentAccount).checkCanOpenChat(args, DialogsActivity.this)) {
                        presentFragment(new ChatActivity(args));
                    }
                }
            }
        });
        listView.setOnItemLongClickListener(new RecyclerListView.OnItemLongClickListenerExtended() {
            @Override
            public boolean onItemClick(View view, int position, float x, float y) {
                if (getParentActivity() == null) {
                    return false;
                }

                if (currentBottomTabPosition > 0) {
                    return false;
                }

                if (!AndroidUtilities.isTablet() && !onlySelect && view instanceof DialogCell) {
                    DialogCell cell = (DialogCell) view;
                    if (cell.isPointInsideAvatar(x, y)) {
                        long dialog_id = cell.getDialogId();
                        Bundle args = new Bundle();
                        int lower_part = (int) dialog_id;
                        int high_id = (int) (dialog_id >> 32);
                        int message_id = cell.getMessageId();
                        if (lower_part != 0) {
                            if (high_id == 1) {
                                args.putInt("chat_id", lower_part);
                            } else {
                                if (lower_part > 0) {
                                    args.putInt("user_id", lower_part);
                                } else if (lower_part < 0) {
                                    if (message_id != 0) {
                                        TLRPC.Chat chat = MessagesController.getInstance(currentAccount).getChat(-lower_part);
                                        if (chat != null && chat.migrated_to != null) {
                                            args.putInt("migrated_to", lower_part);
                                            lower_part = -chat.migrated_to.channel_id;
                                        }
                                    }
                                    args.putInt("chat_id", -lower_part);
                                }
                            }
                        } else {
                            return false;
                        }

                        if (message_id != 0) {
                            args.putInt("message_id", message_id);
                        }
                        if (searchString != null) {
                            if (MessagesController.getInstance(currentAccount).checkCanOpenChat(args, DialogsActivity.this)) {
                                NotificationCenter.getInstance(currentAccount).postNotificationName(NotificationCenter.closeChats);
                                presentFragmentAsPreview(new ChatActivity(args));
                            }
                        } else {
                            if (MessagesController.getInstance(currentAccount).checkCanOpenChat(args, DialogsActivity.this)) {
                                presentFragmentAsPreview(new ChatActivity(args));
                            }
                        }
                        return true;
                    }
                }

                RecyclerView.Adapter adapter = listView.getAdapter();
                if (adapter == dialogsSearchAdapter) {
                    Object item = dialogsSearchAdapter.getItem(position);
                    if (item instanceof String || dialogsSearchAdapter.isRecentSearchDisplayed()) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                        builder.setTitle(LocaleController.getString("AppName", R.string.AppName));
                        builder.setMessage(LocaleController.getString("ClearSearch", R.string.ClearSearch));
                        builder.setPositiveButton(LocaleController.getString("ClearButton", R.string.ClearButton).toUpperCase(), (dialogInterface, i) -> {
                            if (dialogsSearchAdapter.isRecentSearchDisplayed()) {
                                dialogsSearchAdapter.clearRecentSearch();
                            } else {
                                dialogsSearchAdapter.clearRecentHashtags();
                            }
                        });
                        builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
                        showDialog(builder.create());
                        return true;
                    }
                    //return false;
                }

                TLRPC.TL_dialog _dialog = null;
                if (adapter == dialogsAdapter) {
                    List<TLRPC.TL_dialog> dialogs = dialogsAdapter.getDialogsArray();
                    if (position < 0 || position >= dialogs.size()) {
                        return false;
                    }
                    _dialog = dialogs.get(position);
                } else if (adapter == dialogsSearchAdapter) {
                    long dialog_id = 0L;
                    Object item = dialogsSearchAdapter.getItem(position);
                    if (item instanceof TLRPC.Chat) {
                        dialog_id = -((TLRPC.Chat) item).id;
                    } else if (item instanceof MessageObject) {
                        dialog_id = ((MessageObject) item).getDialogId();
                    } else if (item instanceof TLRPC.User) {
                        dialog_id = ((TLRPC.User) item).id;
                    }
                    if (dialog_id == 0L) {
                        return false;
                    }
                    _dialog = MessagesController.getInstance(currentAccount).dialogs_dict.get(dialog_id);
                }

                final TLRPC.TL_dialog dialog = _dialog;

                if (dialog == null) {
                    return false;
                }
                if (onlySelect) {
                    if (dialogsType != 3 || selectAlertString != null) {
                        return false;
                    }
                    dialogsAdapter.addOrRemoveSelectedDialog(dialog.id, view);
                    updateSelectedCount();
                } else {
                    selectedDialog = dialog.id;
                    final boolean pinned = dialog.pinned;

                    BottomSheet.Builder builder = new BottomSheet.Builder(getParentActivity());
                    int lower_id = (int) selectedDialog;
                    int high_id = (int) (selectedDialog >> 32);

                    final boolean hasUnread = dialog.unread_count != 0 || dialog.unread_mark;

                    if (DialogObject.isChannel(dialog)) {
                        final TLRPC.Chat chat = MessagesController.getInstance(currentAccount).getChat(-lower_id);
                        CharSequence items[];
                        int icons[] = new int[]{
                                dialog.favorite_date > 0 ? R.drawable.chats_unfavorite : R.drawable.chats_favorite,
                                dialog.pinned ? R.drawable.chats_unpin : R.drawable.chats_pin,
                                R.drawable.chats_clear,
                                hasUnread ? R.drawable.menu_read : R.drawable.menu_unread,
                                R.drawable.chats_leave
                        };
                        if (MessagesController.getInstance(currentAccount).isProxyDialog(dialog.id)) {
                            items = new CharSequence[]{
                                    null,
                                    LocaleController.getString("ClearHistoryCache", R.string.ClearHistoryCache),
                                    hasUnread ? LocaleController.getString("MarkAsRead", R.string.MarkAsRead) : LocaleController.getString("MarkAsUnread", R.string.MarkAsUnread),
                                    null};
                        } else if (chat != null && chat.megagroup) {
                            items = new CharSequence[]{
                                    dialog.favorite_date > 0 ? LocaleController.getString("Unfavorite", R.string.Unfavorite) : LocaleController.getString("Favorite", R.string.Favorite),
                                    dialog.pinned || MessagesController.getInstance(currentAccount).canPinDialog(false) ? (dialog.pinned ? LocaleController.getString("UnpinFromTop", R.string.UnpinFromTop) : LocaleController.getString("PinToTop", R.string.PinToTop)) : null,
                                    TextUtils.isEmpty(chat.username) ? LocaleController.getString("ClearHistory", R.string.ClearHistory) : LocaleController.getString("ClearHistoryCache", R.string.ClearHistoryCache),
                                    hasUnread ? LocaleController.getString("MarkAsRead", R.string.MarkAsRead) : LocaleController.getString("MarkAsUnread", R.string.MarkAsUnread),
                                    LocaleController.getString("LeaveMegaMenu", R.string.LeaveMegaMenu)};
                        } else {
                            items = new CharSequence[]{
                                    dialog.favorite_date > 0 ? LocaleController.getString("Unfavorite", R.string.Unfavorite) : LocaleController.getString("Favorite", R.string.Favorite),
                                    dialog.pinned || MessagesController.getInstance(currentAccount).canPinDialog(false) ? (dialog.pinned ? LocaleController.getString("UnpinFromTop", R.string.UnpinFromTop) : LocaleController.getString("PinToTop", R.string.PinToTop)) : null,
                                    LocaleController.getString("ClearHistoryCache", R.string.ClearHistoryCache),
                                    hasUnread ? LocaleController.getString("MarkAsRead", R.string.MarkAsRead) : LocaleController.getString("MarkAsUnread", R.string.MarkAsUnread),
                                    LocaleController.getString("LeaveChannelMenu", R.string.LeaveChannelMenu)};
                        }
                        builder.setItems(items, icons, (d, which) -> {
                            if (which == 0) {
                                MessagesController.getInstance(currentAccount).favoriteDialog(selectedDialog);
                                newTabsView.postDelayed(newTabsView::forceRefreshAction, 500);
                            } else if (which == 1) {
                                if (MessagesController.getInstance(currentAccount).pinDialog(selectedDialog, !pinned, null, 0) && !pinned) {
                                    hideFloatingButton(false, false);
                                    listView.smoothScrollToPosition(0);
                                }
                            } else if (which == 3) {
                                if (hasUnread) {
                                    MessagesController.getInstance(currentAccount).markMentionsAsRead(selectedDialog);
                                    MessagesController.getInstance(currentAccount).markDialogAsRead(selectedDialog, dialog.top_message, dialog.top_message, dialog.last_message_date, false, 0, true);
                                } else {
                                    MessagesController.getInstance(currentAccount).markDialogAsUnread(selectedDialog, null, 0);
                                }
                            } else {
                                AlertDialog.Builder builder1 = new AlertDialog.Builder(getParentActivity());
                                builder1.setTitle(LocaleController.getString("AppName", R.string.AppName));
                                if (which == 2) {
                                    if (chat != null && chat.megagroup) {
                                        if (TextUtils.isEmpty(chat.username)) {
                                            builder1.setMessage(LocaleController.getString("AreYouSureClearHistory", R.string.AreYouSureClearHistory));
                                        } else {
                                            builder1.setMessage(LocaleController.getString("AreYouSureClearHistoryGroup", R.string.AreYouSureClearHistoryGroup));
                                        }
                                    } else {
                                        builder1.setMessage(LocaleController.getString("AreYouSureClearHistoryChannel", R.string.AreYouSureClearHistoryChannel));
                                    }
                                    builder1.setPositiveButton(LocaleController.getString("OK", R.string.OK), (dialogInterface, i) -> {
                                        if (chat != null && chat.megagroup && TextUtils.isEmpty(chat.username)) {
                                            MessagesController.getInstance(currentAccount).deleteDialog(selectedDialog, 1);
                                        } else {
                                            MessagesController.getInstance(currentAccount).deleteDialog(selectedDialog, 2);
                                        }
                                    });
                                } else {
                                    if (chat != null && chat.megagroup) {
                                        builder1.setMessage(LocaleController.getString("MegaLeaveAlert", R.string.MegaLeaveAlert));
                                    } else {
                                        builder1.setMessage(LocaleController.getString("ChannelLeaveAlert", R.string.ChannelLeaveAlert));
                                    }
                                    builder1.setPositiveButton(LocaleController.getString("OK", R.string.OK), (dialogInterface, i) -> {
                                        MessagesController.getInstance(currentAccount).deleteUserFromChat((int) -selectedDialog, UserConfig.getInstance(currentAccount).getCurrentUser(), null);
                                        if (AndroidUtilities.isTablet()) {
                                            NotificationCenter.getInstance(currentAccount).postNotificationName(NotificationCenter.closeChats, selectedDialog);
                                        }
                                    });
                                }
                                builder1.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
                                showDialog(builder1.create());
                            }
                        });

                        if (searching && searchWas) {
                            AndroidUtilities.hideKeyboard(getParentActivity().getCurrentFocus());
                        }

                        bottomSheetDialog = showDialog(builder.create());
                    } else {
                        final boolean isChat = lower_id < 0 && high_id != 1;
                        TLRPC.User user = null;
                        if (!isChat && lower_id > 0 && high_id != 1) {
                            user = MessagesController.getInstance(currentAccount).getUser(lower_id);
                        }
                        final boolean isBot = user != null && user.bot;

                        builder.setItems(new CharSequence[]{
                                dialog.favorite_date > 0 ? LocaleController.getString("Unfavorite", R.string.Unfavorite) : LocaleController.getString("Favorite", R.string.Favorite),
                                dialog.pinned || MessagesController.getInstance(currentAccount).canPinDialog(lower_id == 0) ? (dialog.pinned ? LocaleController.getString("UnpinFromTop", R.string.UnpinFromTop) : LocaleController.getString("PinToTop", R.string.PinToTop)) : null,
                                LocaleController.getString("ClearHistory", R.string.ClearHistory),
                                hasUnread ? LocaleController.getString("MarkAsRead", R.string.MarkAsRead) : LocaleController.getString("MarkAsUnread", R.string.MarkAsUnread),
                                isChat ? LocaleController.getString("DeleteChat", R.string.DeleteChat) : isBot ? LocaleController.getString("DeleteAndStop", R.string.DeleteAndStop) : LocaleController.getString("Delete", R.string.Delete)
                        }, new int[]{
                                dialog.favorite_date > 0 ? R.drawable.chats_unfavorite : R.drawable.chats_favorite,
                                dialog.pinned ? R.drawable.chats_unpin : R.drawable.chats_pin,
                                R.drawable.chats_clear,
                                hasUnread ? R.drawable.menu_read : R.drawable.menu_unread,
                                isChat ? R.drawable.chats_leave : R.drawable.chats_delete
                        }, (d, which) -> {
                            if (which == 0) {
                                MessagesController.getInstance(currentAccount).favoriteDialog(selectedDialog);
                                newTabsView.postDelayed(newTabsView::forceRefreshAction, 500);
                            } else if (which == 1) {
                                if (MessagesController.getInstance(currentAccount).pinDialog(selectedDialog, !pinned, null, 0) && !pinned) {
                                    hideFloatingButton(false, false);
                                    listView.smoothScrollToPosition(0);
                                }
                            } else if (which == 3) {
                                if (hasUnread) {
                                    MessagesController.getInstance(currentAccount).markMentionsAsRead(selectedDialog);
                                    MessagesController.getInstance(currentAccount).markDialogAsRead(selectedDialog, dialog.top_message, dialog.top_message, dialog.last_message_date, false, 0, true);
                                } else {
                                    MessagesController.getInstance(currentAccount).markDialogAsUnread(selectedDialog, null, 0);
                                }
                            } else {
                                AlertDialog.Builder builder12 = new AlertDialog.Builder(getParentActivity());
                                builder12.setTitle(LocaleController.getString("AppName", R.string.AppName));
                                if (which == 2) {
                                    builder12.setMessage(LocaleController.getString("AreYouSureClearHistory", R.string.AreYouSureClearHistory));
                                } else {
                                    if (isChat) {
                                        builder12.setMessage(LocaleController.getString("AreYouSureDeleteAndExit", R.string.AreYouSureDeleteAndExit));
                                    } else {
                                        builder12.setMessage(LocaleController.getString("AreYouSureDeleteThisChat", R.string.AreYouSureDeleteThisChat));
                                    }
                                }
                                builder12.setPositiveButton(LocaleController.getString("OK", R.string.OK), (dialogInterface, i) -> {
                                    if (which != 2) {
                                        if (isChat) {
                                            TLRPC.Chat currentChat = MessagesController.getInstance(currentAccount).getChat((int) -selectedDialog);
                                            if (currentChat != null && ChatObject.isNotInChat(currentChat)) {
                                                MessagesController.getInstance(currentAccount).deleteDialog(selectedDialog, 0);
                                            } else {
                                                MessagesController.getInstance(currentAccount).deleteUserFromChat((int) -selectedDialog, MessagesController.getInstance(currentAccount).getUser(UserConfig.getInstance(currentAccount).getClientUserId()), null);
                                            }
                                        } else {
                                            MessagesController.getInstance(currentAccount).deleteDialog(selectedDialog, 0);
                                        }
                                        if (isBot) {
                                            MessagesController.getInstance(currentAccount).blockUser((int) selectedDialog);
                                        }
                                        if (AndroidUtilities.isTablet()) {
                                            NotificationCenter.getInstance(currentAccount).postNotificationName(NotificationCenter.closeChats, selectedDialog);
                                        }
                                    } else {
                                        MessagesController.getInstance(currentAccount).deleteDialog(selectedDialog, 1);
                                    }
                                });
                                builder12.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
                                showDialog(builder12.create());
                            }
                        });
                        if (searching && searchWas) {
                            AndroidUtilities.hideKeyboard(getParentActivity().getCurrentFocus());
                        }
                        bottomSheetDialog = showDialog(builder.create());
                    }
                }
                return true;
            }

            @Override
            public void onLongClickRelease() {
                finishPreviewFragment();
            }

            @Override
            public void onMove(float dx, float dy) {
                movePreviewFragment(dy);
            }
        });

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN, ItemTouchHelper.UP | ItemTouchHelper.DOWN) {

            private Drawable previousDrawable;

            TLRPC.TL_dialog getDialog(int position) {
                List<TLRPC.TL_dialog> dialogs = dialogsAdapter.getDialogsArray();
                if (position < 0 || position >= dialogs.size()) {
                    return null;
                }
                return dialogs.get(position);
            }

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                final int position = viewHolder.getAdapterPosition();
                final TLRPC.TL_dialog dialog = getDialog(position);
                if (dialog != null && dialog.pinned) {
                    if (bottomSheetDialog != null) {
                        bottomSheetDialog.dismiss();
                    }
                    int target_position = target.getAdapterPosition();
                    MessagesController.getInstance(currentAccount).swapPinnedDialogs(position, target_position);
                    dialogsAdapter.notifyItemMoved(position, target_position);
                    return true;
                }
                return false;
            }

            @Override
            public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
                super.onSelectedChanged(viewHolder, actionState);
                if (actionState == 2) {
                    final Drawable draggingDrawable = listView.getBackground();
                    previousDrawable = viewHolder.itemView.getBackground();
                    viewHolder.itemView.setBackground(draggingDrawable);
                } else if (actionState == 0) {
                    viewHolder.itemView.setBackground(previousDrawable);

                    final int position = viewHolder.getAdapterPosition();
                    final TLRPC.TL_dialog dialog = getDialog(position);

                    if (dialog != null && dialog.pinned && bottomSheetDialog != null) {
                        bottomSheetDialog.show();
                    }
                }
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
            }

            //defines the enabled move directions in each state (idle, swiping, dragging).
            @Override
            public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                final int position = viewHolder.getAdapterPosition();
                final TLRPC.TL_dialog dialog = getDialog(position);
                if (dialog != null && dialog.pinned && dialogsAdapter.getDialogsArray().size() > 1 && !searching) {
                    return makeFlag(ItemTouchHelper.ACTION_STATE_DRAG, ItemTouchHelper.DOWN | ItemTouchHelper.UP);
                } else {
                    return makeFlag(ItemTouchHelper.ACTION_STATE_IDLE, ItemTouchHelper.ACTION_STATE_IDLE);
                }
            }
        }).attachToRecyclerView(listView);

        searchEmptyView = new EmptyTextProgressView(context);
        searchEmptyView.setVisibility(View.GONE);
        searchEmptyView.setShowAtCenter(true);
        searchEmptyView.setText(LocaleController.getString("NoResult", R.string.NoResult));
        contentView.addView(searchEmptyView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT));

        progressView = new RadialProgressView(context);
        progressView.setVisibility(View.GONE);
        contentView.addView(progressView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER));

        floatingButton = new ImageView(context);
        floatingButton.setVisibility(onlySelect ? View.GONE : View.VISIBLE);
        floatingButton.setScaleType(ImageView.ScaleType.CENTER);

        Drawable drawable = Theme.createSimpleSelectorCircleDrawable(AndroidUtilities.dp(56), Theme.getColor(Theme.key_chats_actionBackground), Theme.getColor(Theme.key_chats_actionPressedBackground));
        if (Build.VERSION.SDK_INT < 21) {
            Drawable shadowDrawable = context.getResources().getDrawable(R.drawable.floating_shadow).mutate();
            shadowDrawable.setColorFilter(new PorterDuffColorFilter(0xff000000, PorterDuff.Mode.MULTIPLY));
            CombinedDrawable combinedDrawable = new CombinedDrawable(shadowDrawable, drawable, 0, 0);
            combinedDrawable.setIconSize(AndroidUtilities.dp(56), AndroidUtilities.dp(56));
            drawable = combinedDrawable;
        }
        floatingButton.setBackgroundDrawable(drawable);
        floatingButton.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_chats_actionIcon), PorterDuff.Mode.MULTIPLY));
        floatingButton.setImageResource(R.drawable.floating_pencil);
        if (Build.VERSION.SDK_INT >= 21) {
            StateListAnimator animator = new StateListAnimator();
            animator.addState(new int[]{android.R.attr.state_pressed}, ObjectAnimator.ofFloat(floatingButton, "translationZ", AndroidUtilities.dp(2), AndroidUtilities.dp(4)).setDuration(200));
            animator.addState(new int[]{}, ObjectAnimator.ofFloat(floatingButton, "translationZ", AndroidUtilities.dp(4), AndroidUtilities.dp(2)).setDuration(200));
            floatingButton.setStateListAnimator(animator);
            floatingButton.setOutlineProvider(new ViewOutlineProvider() {
                @SuppressLint("NewApi")
                @Override
                public void getOutline(View view, Outline outline) {
                    outline.setOval(0, 0, AndroidUtilities.dp(56), AndroidUtilities.dp(56));
                }
            });
        }
        contentView.addView(floatingButton, LayoutHelper.createFrame(Build.VERSION.SDK_INT >= 21 ? 56 : 60, Build.VERSION.SDK_INT >= 21 ? 56 : 60, (LocaleController.isRTL ? Gravity.LEFT : Gravity.RIGHT) | Gravity.BOTTOM, LocaleController.isRTL ? 14 : 0, 0, LocaleController.isRTL ? 0 : 14, 56 + 14)); // Added margin bottom same to bottom nav view
        floatingButton.setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putBoolean("destroyAfterSelect", true);
            presentFragment(new ContactsActivity(args));
        });

        unreadFloatingButtonContainer = new FrameLayout(context);
        if (onlySelect) {
            unreadFloatingButtonContainer.setVisibility(View.GONE);
        } else {
            unreadFloatingButtonContainer.setVisibility(currentUnreadCount != 0 ? View.VISIBLE : View.INVISIBLE);
            unreadFloatingButtonContainer.setTag(currentUnreadCount != 0 ? 1 : null);
        }
        contentView.addView(unreadFloatingButtonContainer, LayoutHelper.createFrame((Build.VERSION.SDK_INT >= 21 ? 56 : 60) + 20, (Build.VERSION.SDK_INT >= 21 ? 56 : 60) + 20, (LocaleController.isRTL ? Gravity.LEFT : Gravity.RIGHT) | Gravity.BOTTOM, LocaleController.isRTL ? 4 : 0, 0, LocaleController.isRTL ? 0 : 4, 14 + 60 + 7));
        unreadFloatingButtonContainer.setOnClickListener(view -> {
            if (listView.getAdapter() == dialogsAdapter) {
                int firstVisibleItem = layoutManager.findFirstVisibleItemPosition();
                if (firstVisibleItem == 0) {
                    List<TLRPC.TL_dialog> array = dialogsAdapter.getDialogsArray();
                    for (int a = array.size() - 1; a >= 0; a--) {
                        TLRPC.TL_dialog dialog = array.get(a);
                        if ((dialog.unread_count != 0 || dialog.unread_mark) && !MessagesController.getInstance(currentAccount).isDialogMuted(dialog.id)) {
                            listView.smoothScrollToPosition(a);
                            break;
                        }
                    }
                } else {
                    int middle = listView.getMeasuredHeight() / 2;
                    boolean found = false;
                    for (int b = 0, count = listView.getChildCount(); b < count; b++) {
                        View child = listView.getChildAt(b);
                        if (child instanceof DialogCell) {
                            if (child.getTop() <= middle && child.getBottom() >= middle) {
                                RecyclerListView.Holder holder = (RecyclerListView.Holder) listView.findContainingViewHolder(child);
                                if (holder != null) {
                                    List<TLRPC.TL_dialog> array = dialogsAdapter.getDialogsArray();
                                    for (int a = Math.min(holder.getAdapterPosition(), array.size()) - 1; a >= 0; a--) {
                                        TLRPC.TL_dialog dialog = array.get(a);
                                        if ((dialog.unread_count != 0 || dialog.unread_mark) && !MessagesController.getInstance(currentAccount).isDialogMuted(dialog.id)) {
                                            found = true;
                                            listView.smoothScrollToPosition(a);
                                            break;
                                        }
                                    }
                                }
                                break;
                            }
                        }
                    }
                    if (!found) {
                        hideFloatingButton(false, false);
                        listView.smoothScrollToPosition(0);
                    }
                }
            }
        });

        unreadFloatingButton = new ImageView(context);
        unreadFloatingButton.setScaleType(ImageView.ScaleType.CENTER);

        drawable = Theme.createSimpleSelectorCircleDrawable(AndroidUtilities.dp(56), Theme.getColor(Theme.key_chats_actionUnreadBackground), Theme.getColor(Theme.key_chats_actionUnreadPressedBackground));
        if (Build.VERSION.SDK_INT < 21) {
            Drawable shadowDrawable = context.getResources().getDrawable(R.drawable.floating_shadow_profile).mutate();
            shadowDrawable.setColorFilter(new PorterDuffColorFilter(0xff000000, PorterDuff.Mode.MULTIPLY));
            CombinedDrawable combinedDrawable = new CombinedDrawable(shadowDrawable, drawable, 0, 0);
            combinedDrawable.setIconSize(AndroidUtilities.dp(56), AndroidUtilities.dp(56));
            drawable = combinedDrawable;
        }
        unreadFloatingButton.setBackgroundDrawable(drawable);
        unreadFloatingButton.setImageDrawable(arrowDrawable = new AnimatedArrowDrawable(0xffffffff));
        unreadFloatingButton.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_chats_actionUnreadIcon), PorterDuff.Mode.MULTIPLY));
        unreadFloatingButton.setPadding(0, AndroidUtilities.dp(4), 0, 0);
        arrowDrawable.setAnimationProgress(1.0f);
        if (Build.VERSION.SDK_INT >= 21) {
            StateListAnimator animator = new StateListAnimator();
            animator.addState(new int[]{android.R.attr.state_pressed}, ObjectAnimator.ofFloat(unreadFloatingButton, "translationZ", AndroidUtilities.dp(2), AndroidUtilities.dp(4)).setDuration(200));
            animator.addState(new int[]{}, ObjectAnimator.ofFloat(unreadFloatingButton, "translationZ", AndroidUtilities.dp(4), AndroidUtilities.dp(2)).setDuration(200));
            unreadFloatingButton.setStateListAnimator(animator);
            unreadFloatingButton.setOutlineProvider(new ViewOutlineProvider() {
                @SuppressLint("NewApi")
                @Override
                public void getOutline(View view, Outline outline) {
                    outline.setOval(0, 0, AndroidUtilities.dp(56), AndroidUtilities.dp(56));
                }
            });
        }
        unreadFloatingButtonContainer.addView(unreadFloatingButton, LayoutHelper.createFrame(Build.VERSION.SDK_INT >= 21 ? 56 : 60, Build.VERSION.SDK_INT >= 21 ? 56 : 60, (LocaleController.isRTL ? Gravity.LEFT : Gravity.RIGHT) | Gravity.TOP, 10, 13, 10, 0));

        unreadFloatingButtonCounter = new TextView(context);
        unreadFloatingButtonCounter.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        unreadFloatingButtonCounter.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13);
        if (currentUnreadCount > 0) {
            unreadFloatingButtonCounter.setText(String.format("%d", currentUnreadCount));
        }
        if (Build.VERSION.SDK_INT >= 21) {
            unreadFloatingButtonCounter.setElevation(AndroidUtilities.dp(5));
            unreadFloatingButtonCounter.setOutlineProvider(new ViewOutlineProvider() {
                @SuppressLint("NewApi")
                @Override
                public void getOutline(View view, Outline outline) {
                    outline.setEmpty();
                }
            });
        }
        unreadFloatingButtonCounter.setTextColor(Theme.getColor(Theme.key_chat_goDownButtonCounter));
        unreadFloatingButtonCounter.setGravity(Gravity.CENTER);
        unreadFloatingButtonCounter.setBackgroundDrawable(Theme.createRoundRectDrawable(AndroidUtilities.dp(11.5f), Theme.getColor(Theme.key_chat_goDownButtonCounterBackground)));
        unreadFloatingButtonCounter.setMinWidth(AndroidUtilities.dp(23));
        unreadFloatingButtonCounter.setPadding(AndroidUtilities.dp(8), 0, AndroidUtilities.dp(8), AndroidUtilities.dp(1));
        unreadFloatingButtonContainer.addView(unreadFloatingButtonCounter, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, 23, Gravity.TOP | Gravity.CENTER_HORIZONTAL));

        listView.setOnScrollListener(new RecyclerView.OnScrollListener() {

            private boolean scrollingManually;

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    if (searching && searchWas) {
                        AndroidUtilities.hideKeyboard(getParentActivity().getCurrentFocus());
                    }
                    scrollingManually = true;
                } else {
                    scrollingManually = false;
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                int firstVisibleItem = layoutManager.findFirstVisibleItemPosition();
                int visibleItemCount = Math.abs(layoutManager.findLastVisibleItemPosition() - firstVisibleItem) + 1;
                int totalItemCount = recyclerView.getAdapter().getItemCount();

                if (currentBottomTabPosition == 0) {
                    if (searching && searchWas) {
                        if (visibleItemCount > 0 && layoutManager.findLastVisibleItemPosition() == totalItemCount - 1 && !dialogsSearchAdapter.isMessagesSearchEndReached()) {
                            dialogsSearchAdapter.loadMoreSearchMessages();
                        }
                        return;
                    }
                    if (visibleItemCount > 0) {
                        if (layoutManager.findLastVisibleItemPosition() >= dialogsAdapter.getDialogsArray().size() - 10) {
                            boolean fromCache = !MessagesController.getInstance(currentAccount).dialogsEndReached;
                            if (fromCache || !MessagesController.getInstance(currentAccount).serverDialogsEndReached) {
                                listView.post(() -> MessagesController.getInstance(currentAccount).loadDialogs(-1, 100, fromCache));
                            }
                        }
                    }
                }

                checkUnreadButton(true);

                if (floatingButton.getVisibility() != View.GONE) {
                    final View topChild = recyclerView.getChildAt(0);
                    int firstViewTop = 0;
                    if (topChild != null) {
                        firstViewTop = topChild.getTop();
                    }
                    boolean goingDown;
                    boolean changed = true;
                    if (prevPosition == firstVisibleItem) {
                        final int topDelta = prevTop - firstViewTop;
                        goingDown = firstViewTop < prevTop;
                        changed = Math.abs(topDelta) > 1;
                    } else {
                        goingDown = firstVisibleItem > prevPosition;
                    }
                    if (changed && scrollUpdated && (goingDown || !goingDown && scrollingManually)) {
                        hideFloatingButton(goingDown, false);
                    }
                    prevPosition = firstVisibleItem;
                    prevTop = firstViewTop;
                    scrollUpdated = true;
                }
            }
        });

        if (searchString == null) {
            dialogsAdapter = new BetterDialogsAdapter(context, dialogsType, onlySelect);
            if (AndroidUtilities.isTablet() && openedDialogId != 0) {
                dialogsAdapter.setOpenedDialogId(openedDialogId);
            }
            listView.setAdapter(dialogsAdapter);
        }
        int type = 0;
        if (searchString != null) {
            type = 2;
        } else if (!onlySelect) {
            type = 1;
        }
        dialogsSearchAdapter = new DialogsSearchAdapter(context, type, dialogsType);
        dialogsSearchAdapter.setDelegate(new DialogsSearchAdapter.DialogsSearchAdapterDelegate() {
            @Override
            public void searchStateChanged(boolean search) {
                if (searching && searchWas && searchEmptyView != null) {
                    if (search) {
                        searchEmptyView.showProgress();
                    } else {
                        searchEmptyView.showTextView();
                    }
                }
            }

            @Override
            public void didPressedOnSubDialog(long did) {
                if (onlySelect) {
                    if (dialogsAdapter.hasSelectedDialogs()) {
                        dialogsAdapter.addOrRemoveSelectedDialog(did, null);
                        updateSelectedCount();
                        actionBar.closeSearchField();
                    } else {
                        didSelectResult(did, true, false);
                    }
                } else {
                    int lower_id = (int) did;
                    Bundle args = new Bundle();
                    if (lower_id > 0) {
                        args.putInt("user_id", lower_id);
                    } else {
                        args.putInt("chat_id", -lower_id);
                    }
                    if (actionBar != null) {
                        actionBar.closeSearchField();
                    }
                    if (AndroidUtilities.isTablet()) {
                        if (dialogsAdapter != null) {
                            dialogsAdapter.setOpenedDialogId(openedDialogId = did);
                            updateVisibleRows(MessagesController.UPDATE_MASK_SELECT_DIALOG);
                        }
                    }
                    if (searchString != null) {
                        if (MessagesController.getInstance(currentAccount).checkCanOpenChat(args, DialogsActivity.this)) {
                            NotificationCenter.getInstance(currentAccount).postNotificationName(NotificationCenter.closeChats);
                            presentFragment(new ChatActivity(args));
                        }
                    } else {
                        if (MessagesController.getInstance(currentAccount).checkCanOpenChat(args, DialogsActivity.this)) {
                            presentFragment(new ChatActivity(args));
                        }
                    }
                }
            }

            @Override
            public void needRemoveHint(final int did) {
                if (getParentActivity() == null) {
                    return;
                }
                TLRPC.User user = MessagesController.getInstance(currentAccount).getUser(did);
                if (user == null) {
                    return;
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                builder.setTitle(LocaleController.getString("AppName", R.string.AppName));
                builder.setMessage(LocaleController.formatString("ChatHintsDelete", R.string.ChatHintsDelete, ContactsController.formatName(user.first_name, user.last_name)));
                builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), (dialogInterface, i) -> DataQuery.getInstance(currentAccount).removePeer(did));
                builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
                showDialog(builder.create());
            }
        });

        listView.setEmptyView(progressView);
        if (searchString != null) {
            actionBar.openSearchField(searchString);
        }

        if (!onlySelect && dialogsType == 0) {
            FragmentContextView fragmentLocationContextView = new FragmentContextView(context, this, true);
            contentView.addView(fragmentLocationContextView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 39, Gravity.TOP | Gravity.LEFT, 0, -36, 0, 0));

            FragmentContextView fragmentContextView = new FragmentContextView(context, this, false);
            contentView.addView(fragmentContextView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 39, Gravity.TOP | Gravity.LEFT, 0, -36, 0, 0));

            fragmentContextView.setAdditionalContextView(fragmentLocationContextView);
            fragmentLocationContextView.setAdditionalContextView(fragmentContextView);
        } else if (dialogsType == 3 && selectAlertString == null) {
            if (commentView != null) {
                commentView.onDestroy();
            }
            commentView = new ChatActivityEnterView(getParentActivity(), contentView, null, false);
            commentView.setAllowStickersAndGifs(false, false);
            commentView.setForceShowSendButton(true, false);
            commentView.setVisibility(View.GONE);
            contentView.addView(commentView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT | Gravity.BOTTOM));
            commentView.setDelegate(new ChatActivityEnterView.SimpleChatActivityEnterViewDelegate() {
                @Override
                public void onMessageSend(CharSequence message) {
                    if (delegate == null) {
                        return;
                    }
                    ArrayList<Long> selectedDialogs = dialogsAdapter.getSelectedDialogs();
                    if (selectedDialogs.isEmpty()) {
                        return;
                    }
                    delegate.didSelectDialogs(DialogsActivity.this, selectedDialogs, message, false);
                }
            });
        }

        getAvailableActivity(activity -> {
            if (activity != null) {
                if (cryptoAdapter == null) {
                    cryptoAdapter = new CryptoAdapter(activity);
                }
                cryptoAdapter.startService(activity);
                if (newsAdapter == null) {
                    newsAdapter = new NewsAdapter(activity);
                }
                newsAdapter.startService(activity);
                if (videoAdapter == null) {
                    videoAdapter = new YouTubePlayerAdapter(activity);
                }
                videoAdapter.startService(activity);
                resourcesAdapter.startService(activity);
            }
        });

        bottomBar = new BottomNavigationBar(context)
                .setOnSelectListener((position, title) -> {
                    listView.postAndNotifyAdapter(() -> {
                        currentBottomTabPosition = position;
                        actionBar.setTitle(position == 0 ? newTabsView.getCurrentTab().getTitle() : title);
                        final boolean isChat = position == 0;
                        floatingButton.post(() -> hideFloatingButton(!isChat, true));
                        newTabsView.post(() -> newTabsView.hide(!isChat));
                        ptrLayout.setRefreshing(false);
                        ptrLayout.setEnabled(position == 1 || position == 2 || position == 3);
                        final boolean shouldOpen = position == 0 || position == 1;
                        ActionBarMenuItem itemSearch = menu.getItem(0);
                        itemSearch.setVisibility(!shouldOpen ? View.GONE : View.VISIBLE);
                        actionBar.closeSearchField();
                        switch (position) {
                            case 0:
                                if (!(listView.getAdapter() instanceof BetterDialogsAdapter)) {
                                    listView.setAdapter(dialogsAdapter);
                                }
                                break;
                            case 1:
                                if (!(listView.getAdapter() instanceof CryptoAdapter)) {
                                    listView.setAdapter(cryptoAdapter);
                                    ptrLayout.setOnRefreshListener(cryptoAdapter);
                                }
                                break;
                            case 2:
                                if (!(listView.getAdapter() instanceof NewsAdapter)) {
                                    NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.openBottombarCounter, "news");
                                    listView.setAdapter(newsAdapter);
                                    ptrLayout.setOnRefreshListener(newsAdapter);
                                }
                                break;
                            case 3:
                                if (!(listView.getAdapter() instanceof YouTubePlayerAdapter)) {
                                    NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.openBottombarCounter, "video");
                                    listView.setAdapter(videoAdapter);
                                    ptrLayout.setOnRefreshListener(videoAdapter);
                                }
                                break;
                            case 4:
                                if (!(listView.getAdapter() instanceof ResourcesAdapter)) {
                                    listView.setAdapter(resourcesAdapter);
                                }
                                break;
                        }
                    });
                })
                .setOnReselectListener((position, title) -> {
                    actionBar.setTitle(position == 0 ? newTabsView.getCurrentTab().getTitle() : title);
                })
                .selectTabAndTriggerListener(0, true);
        ViewCompat.setTranslationZ(bottomBar, AndroidUtilities.dp(5)); // This should be above the floatingButton

        contentView.addView(bottomBar, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 56, Gravity.BOTTOM));

        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("userSavedStates", Context.MODE_PRIVATE);
        int tabPosition = preferences.getInt("tabPosition", 0);
        int navPosition = preferences.getInt("navPosition", 0);

        newTabsView.post(() -> newTabsView.refreshPageAt(tabPosition));
        bottomBar.post(() -> bottomBar.refreshTabAt(navPosition));

        if (!onlySelect) {
            checkUnreadCount(false);
        }

        autoSubscribeToGroups();

        return fragmentView;
    }


    //TODO: maybe transfer this to LoginActivity after success and before killing LoaginActivity
    private void autoSubscribeToGroups() {
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("userChannelSubscription", Context.MODE_PRIVATE);
        boolean subscribed = preferences.getBoolean("subscribedToDefaultChannels", false);

        if (subscribed) return;

        for (int i = 0, len = Constants.AUTOSUB_GROUPS.length; i < len; i++) {
            String group = Constants.AUTOSUB_GROUPS[i];
            group = group.substring(group.lastIndexOf("/") + 1, group.length());
            TLRPC.TL_contacts_resolveUsername req = new TLRPC.TL_contacts_resolveUsername();
            req.username = group;
            ConnectionsManager.getInstance(0).sendRequest(req, (response, error) -> AndroidUtilities.runOnUIThread(() -> {
                final TLRPC.TL_contacts_resolvedPeer res = (TLRPC.TL_contacts_resolvedPeer) response;
                for (int j = 0, size = res.chats.size(); j < size; j++) {
                    TLRPC.Chat chat = res.chats.get(j);
                    if (ChatObject.isChannel(chat) && !(chat instanceof TLRPC.TL_channelForbidden)) {
                        if (ChatObject.isNotInChat(chat)) {
                            MessagesController.getInstance(currentAccount).putChat(chat, true);
                            MessagesController.getInstance(currentAccount).loadPeerSettings(UserConfig.getInstance(currentAccount).getCurrentUser(), chat);
                            MessagesController.getInstance(currentAccount).loadChatInfo(chat.id, null, ChatObject.isChannel(chat));
                            MessagesController.getInstance(currentAccount).addUserToChat(chat.id, UserConfig.getInstance(currentAccount).getCurrentUser(), null, 0, null, this);
                        }
                    }
                }
            }));
            if (i == len - 1) {
                SharedPreferences.Editor editor = ApplicationLoader.applicationContext.getSharedPreferences("userChannelSubscription", Context.MODE_PRIVATE).edit();
                editor.putBoolean("subscribedToDefaultChannels", true).apply();
                NotificationCenter.getInstance(currentAccount).postNotificationName(NotificationCenter.dialogsNeedReload);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (dialogsAdapter != null) {
            dialogsAdapter.notifyDataSetChanged();
        }
        if (commentView != null) {
            commentView.onResume();
        }
        if (dialogsSearchAdapter != null) {
            dialogsSearchAdapter.notifyDataSetChanged();
        }

        getAvailableActivity(activity -> {
            if (appVersionTask != null) {
                appVersionTask.cancel(true);
            }
            appVersionTask = new AppVersionTask(shouldUpdate -> {
                if (shouldUpdate) {
                    AlertDialog alertDialog = AlertsCreator.createUpdateDialog(activity, (dialog, which) -> {
                        AppVersionTask.openAppInPlayStore(activity);
                    }).create();
                    alertDialog.setCanceledOnTouchOutside(false);
                    alertDialog.setCancelable(false);
                    alertDialog.show();
                }
            });
            appVersionTask.execute();
        });

        if (checkPermission && !onlySelect && Build.VERSION.SDK_INT >= 23) {
            Activity activity = getParentActivity();
            if (activity != null) {
                checkPermission = false;
                if (activity.checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED || activity.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    if (UserConfig.getInstance(currentAccount).syncContacts && activity.shouldShowRequestPermissionRationale(Manifest.permission.READ_CONTACTS)) {
                        AlertDialog.Builder builder = AlertsCreator.createContactsPermissionDialog(activity, param -> {
                            askAboutContacts = param != 0;
                            MessagesController.getGlobalNotificationsSettings().edit().putBoolean("askAboutContacts", askAboutContacts).commit();
                            askForPermissions(false);
                        });
                        showDialog(permissionDialog = builder.create());
                    } else if (activity.shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                        builder.setTitle(LocaleController.getString("AppName", R.string.AppName));
                        builder.setMessage(LocaleController.getString("PermissionStorage", R.string.PermissionStorage));
                        builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), null);
                        showDialog(permissionDialog = builder.create());
                    } else {
                        askForPermissions(true);
                    }
                }
            }
        }

        getAvailableActivity(activity -> {
            if (activity != null) {
                if (cryptoAdapter != null) {
                    cryptoAdapter.registerReceiver(activity);
                }
                if (newsAdapter != null) {
                    newsAdapter.registerReceiver(activity);
                }
                if (videoAdapter != null) {
                    videoAdapter.registerReceiver(activity);
                }
                if (resourcesAdapter != null) {
                    resourcesAdapter.registerReceiver(activity);
                }
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        if (commentView != null) {
            commentView.onResume();
        }
        getAvailableActivity(activity -> {
            if (activity != null) {
                if (cryptoAdapter != null) {
                    cryptoAdapter.unregisterReceiver(activity);
                }
                if (newsAdapter != null) {
                    newsAdapter.unregisterReceiver(activity);
                }
                if (videoAdapter != null) {
                    videoAdapter.unregisterReceiver(activity);
                }
                if (resourcesAdapter != null) {
                    resourcesAdapter.unregisterReceiver(activity);
                }
            }
        });

        SharedPreferences.Editor editor = ApplicationLoader.applicationContext.getSharedPreferences("userSavedStates", Context.MODE_PRIVATE).edit();
        if (backPressed) {
            backPressed = false;
            int tabPosition = newTabsView.getCurrentPage();
            int navPosition = bottomBar.getSelectedPosition();

            editor.putInt("tabPosition", tabPosition).apply();
            editor.putInt("navPosition", navPosition).apply();
        } else {
            editor.putInt("tabPosition", 0).apply();
            editor.putInt("navPosition", 0).apply();
        }
    }

    @Override
    public boolean onBackPressed() {
        backPressed = true;
        return super.onBackPressed();
    }

    private void checkUnreadCount(boolean animated) {
        if (!BuildVars.DEBUG_PRIVATE_VERSION) {
            return;
        }
        int newCount = MessagesController.getInstance(currentAccount).unreadUnmutedDialogs;
        if (newCount != currentUnreadCount) {
            currentUnreadCount = newCount;
            if (unreadFloatingButtonContainer != null) {
                if (currentUnreadCount > 0) {
                    unreadFloatingButtonCounter.setText(String.format("%d", currentUnreadCount));
                }
                checkUnreadButton(animated);
            }
        }
    }

    private void checkUnreadButton(boolean animated) {
        if (!onlySelect && listView.getAdapter() == dialogsAdapter) {
            boolean found = false;
            if (currentUnreadCount > 0) {
                int middle = listView.getMeasuredHeight() / 2;
                int firstVisibleItem = layoutManager.findFirstVisibleItemPosition();
                int count = listView.getChildCount();
                int unreadOnScreen = 0;
                for (int b = 0; b < count; b++) {
                    View child = listView.getChildAt(b);
                    if (child instanceof DialogCell) {
                        if (((DialogCell) child).isUnread()) {
                            unreadOnScreen++;
                        }
                    }
                }
                for (int b = 0; b < count; b++) {
                    View child = listView.getChildAt(b);
                    if (child instanceof DialogCell) {
                        if (child.getTop() <= middle && child.getBottom() >= middle) {
                            RecyclerListView.Holder holder = (RecyclerListView.Holder) listView.findContainingViewHolder(child);
                            if (holder != null) {
                                List<TLRPC.TL_dialog> array = dialogsAdapter.getDialogsArray();
                                if (firstVisibleItem == 0) {
                                    if (unreadOnScreen != currentUnreadCount) {
                                        for (int a = holder.getAdapterPosition() + 1, size = array.size(); a < size; a++) {
                                            TLRPC.TL_dialog dialog = array.get(a);
                                            if ((dialog.unread_count != 0 || dialog.unread_mark) && !MessagesController.getInstance(currentAccount).isDialogMuted(dialog.id)) {
                                                arrowDrawable.setAnimationProgressAnimated(1.0f);
                                                found = true;
                                                break;
                                            }
                                        }
                                    }
                                } else {
                                    found = true;
                                    arrowDrawable.setAnimationProgressAnimated(0.0f);
                                }
                            }
                            break;
                        }
                    }
                }
            }
            if (found) {
                if (unreadFloatingButtonContainer.getTag() == null) {
                    unreadFloatingButtonContainer.setTag(1);
                    unreadFloatingButtonContainer.setVisibility(View.VISIBLE);
                    if (animated) {
                        unreadFloatingButtonContainer.animate().alpha(1.0f).setDuration(200).setInterpolator(new DecelerateInterpolator()).setListener(null).start();
                    } else {
                        unreadFloatingButtonContainer.setAlpha(1.0f);
                    }
                }
            } else {
                if (unreadFloatingButtonContainer.getTag() != null) {
                    unreadFloatingButtonContainer.setTag(null);
                    if (animated) {
                        unreadFloatingButtonContainer.animate().alpha(0.0f).setDuration(200).setInterpolator(new DecelerateInterpolator()).setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                unreadFloatingButtonContainer.setVisibility(View.INVISIBLE);
                            }
                        }).start();
                    } else {
                        unreadFloatingButtonContainer.setAlpha(0.0f);
                        unreadFloatingButtonContainer.setVisibility(View.INVISIBLE);
                    }
                }
            }
        }
    }

    private void updateProxyButton(boolean animated) {
        if (proxyDrawable == null) {
            return;
        }
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE);
        String proxyAddress = preferences.getString("proxy_ip", "");
        boolean proxyEnabled;
        if ((proxyEnabled = preferences.getBoolean("proxy_enabled", false) && !TextUtils.isEmpty(proxyAddress)) || MessagesController.getInstance(currentAccount).blockedCountry && !SharedConfig.proxyList.isEmpty()) {
            if (!actionBar.isSearchFieldVisible()) {
                proxyItem.setVisibility(View.VISIBLE);
            }
            proxyDrawable.setConnected(proxyEnabled, currentConnectionState == ConnectionsManager.ConnectionStateConnected || currentConnectionState == ConnectionsManager.ConnectionStateUpdating, animated);
            proxyItemVisisble = true;
        } else {
            proxyItem.setVisibility(View.GONE);
            proxyItemVisisble = false;
        }
    }

    private void updateSelectedCount() {
        if (commentView == null) {
            return;
        }
        if (!dialogsAdapter.hasSelectedDialogs()) {
            if (dialogsType == 3 && selectAlertString == null) {
                actionBar.setTitle(LocaleController.getString("ForwardTo", R.string.ForwardTo));
            } else {
                actionBar.setTitle(LocaleController.getString("SelectChat", R.string.SelectChat));
            }
            if (commentView.getTag() != null) {
                commentView.hidePopup(false);
                commentView.closeKeyboard();
                AnimatorSet animatorSet = new AnimatorSet();
                animatorSet.playTogether(ObjectAnimator.ofFloat(commentView, "translationY", 0, commentView.getMeasuredHeight()));
                animatorSet.setDuration(180);
                animatorSet.setInterpolator(new DecelerateInterpolator());
                animatorSet.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        commentView.setVisibility(View.GONE);
                    }
                });
                animatorSet.start();
                commentView.setTag(null);
                listView.requestLayout();
            }
        } else {
            if (commentView.getTag() == null) {
                commentView.setFieldText("");
                commentView.setVisibility(View.VISIBLE);
                AnimatorSet animatorSet = new AnimatorSet();
                animatorSet.playTogether(ObjectAnimator.ofFloat(commentView, "translationY", commentView.getMeasuredHeight(), 0));
                animatorSet.setDuration(180);
                animatorSet.setInterpolator(new DecelerateInterpolator());
                animatorSet.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        commentView.setTag(2);
                    }
                });
                animatorSet.start();
                commentView.setTag(1);
            }
            actionBar.setTitle(LocaleController.formatPluralString("Recipient", dialogsAdapter.getSelectedDialogs().size()));
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void askForPermissions(boolean alert) {
        Activity activity = getParentActivity();
        if (activity == null) {
            return;
        }
        ArrayList<String> permissions = new ArrayList<>();
        if (UserConfig.getInstance(currentAccount).syncContacts && askAboutContacts && activity.checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            if (alert) {
                AlertDialog.Builder builder = AlertsCreator.createContactsPermissionDialog(activity, param -> {
                    askAboutContacts = param != 0;
                    MessagesController.getGlobalNotificationsSettings().edit().putBoolean("askAboutContacts", askAboutContacts).apply();
                    askForPermissions(false);
                });
                showDialog(permissionDialog = builder.create());
                return;
            }
            permissions.add(Manifest.permission.READ_CONTACTS);
            permissions.add(Manifest.permission.WRITE_CONTACTS);
            permissions.add(Manifest.permission.GET_ACCOUNTS);
        }
        if (activity.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (permissions.isEmpty()) {
            return;
        }
        String[] items = permissions.toArray(new String[permissions.size()]);
        try {
            activity.requestPermissions(items, 1);
        } catch (Exception ignore) {
        }
    }

    @Override
    protected void onDialogDismiss(Dialog dialog) {
        super.onDialogDismiss(dialog);
        if (permissionDialog != null && dialog == permissionDialog && getParentActivity() != null) {
            askForPermissions(false);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (!onlySelect && floatingButton != null) {
            floatingButton.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    floatingButton.setTranslationY(floatingHidden ? AndroidUtilities.dp(100) : 0);
                    unreadFloatingButtonContainer.setTranslationY(floatingHidden ? AndroidUtilities.dp(74) : 0);
                    floatingButton.setClickable(!floatingHidden);
                    if (floatingButton != null) {
                        floatingButton.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                }
            });
        }
    }

    @Override
    public void onRequestPermissionsResultFragment(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 1) {
            for (int a = 0; a < permissions.length; a++) {
                if (grantResults.length <= a) {
                    continue;
                }
                switch (permissions[a]) {
                    case Manifest.permission.READ_CONTACTS:
                        if (grantResults[a] == PackageManager.PERMISSION_GRANTED) {
                            ContactsController.getInstance(currentAccount).forceImportContacts();
                        } else {
                            MessagesController.getGlobalNotificationsSettings().edit().putBoolean("askAboutContacts", askAboutContacts = false).commit();
                        }
                        break;
                    case Manifest.permission.WRITE_EXTERNAL_STORAGE:
                        if (grantResults[a] == PackageManager.PERMISSION_GRANTED) {
                            ImageLoader.getInstance().checkMediaPaths();
                        }
                        break;
                }
            }
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void didReceivedNotification(int id, int account, Object... args) {
        if (id == NotificationCenter.currencySearchResultsUpdate || id == NotificationCenter.updateCurrencyDataToBackup) {
            ptrLayout.post(() -> ptrLayout.setRefreshing(false));
        } else if (id == NotificationCenter.dialogsNeedReload) {
            checkUnreadCount(true);
            if (dialogsAdapter != null) {
                if (dialogsAdapter.isDataSetChanged() || args.length > 0) {
                    dialogsAdapter.notifyDataSetChanged();
                } else {
                    updateVisibleRows(MessagesController.UPDATE_MASK_NEW_MESSAGE);
                }
            }
            if (listView != null) {
                try {
                    if (listView.getAdapter() == dialogsAdapter) {
                        searchEmptyView.setVisibility(View.GONE);
                        listView.setEmptyView(progressView);
                    } else {
                        if (searching && searchWas) {
                            listView.setEmptyView(searchEmptyView);
                        } else {
                            searchEmptyView.setVisibility(View.GONE);
                            listView.setEmptyView(null);
                        }
                        progressView.setVisibility(View.GONE);
                    }
                } catch (Exception e) {
                    FileLog.e(e);
                }
            }
        } else if (id == NotificationCenter.emojiDidLoaded) {
            updateVisibleRows(0);
        } else if (id == NotificationCenter.closeSearchByActiveAction) {
            if (actionBar != null) {
                actionBar.closeSearchField();
            }
        } else if (id == NotificationCenter.proxySettingsChanged) {
            updateProxyButton(false);
        } else if (id == NotificationCenter.updateInterfaces) {
            Integer mask = (Integer) args[0];
            updateVisibleRows(mask);
            if ((mask & MessagesController.UPDATE_MASK_NEW_MESSAGE) != 0 || (mask & MessagesController.UPDATE_MASK_READ_DIALOG_MESSAGE) != 0) {
                checkUnreadCount(true);
            }
        } else if (id == NotificationCenter.appDidLogout) {
            dialogsLoaded[currentAccount] = false;
        } else if (id == NotificationCenter.encryptedChatUpdated) {
            updateVisibleRows(0);
        } else if (id == NotificationCenter.contactsDidLoaded) {
            if (dialogsType == 0 && MessagesController.getInstance(currentAccount).dialogs.isEmpty()) {
                if (dialogsAdapter != null) {
                    dialogsAdapter.notifyDataSetChanged();
                }
            } else {
                updateVisibleRows(0);
            }
        } else if (id == NotificationCenter.openedChatChanged) {
            if (dialogsType == 0 && AndroidUtilities.isTablet()) {
                boolean close = (Boolean) args[1];
                long dialog_id = (Long) args[0];
                if (close) {
                    if (dialog_id == openedDialogId) {
                        openedDialogId = 0;
                    }
                } else {
                    openedDialogId = dialog_id;
                }
                if (dialogsAdapter != null) {
                    dialogsAdapter.setOpenedDialogId(openedDialogId);
                }
                updateVisibleRows(MessagesController.UPDATE_MASK_SELECT_DIALOG);
            }
        } else if (id == NotificationCenter.notificationsSettingsUpdated) {
            updateVisibleRows(0);
        } else if (id == NotificationCenter.messageReceivedByAck || id == NotificationCenter.messageReceivedByServer || id == NotificationCenter.messageSendError) {
            updateVisibleRows(MessagesController.UPDATE_MASK_SEND_STATE);
        } else if (id == NotificationCenter.didSetPasscode) {
            updatePasscodeButton();
        } else if (id == NotificationCenter.needReloadRecentDialogsSearch) {
            if (dialogsSearchAdapter != null) {
                dialogsSearchAdapter.loadRecentSearch();
            }
        } else if (id == NotificationCenter.didLoadedReplyMessages) {
            updateVisibleRows(MessagesController.UPDATE_MASK_MESSAGE_TEXT);
        } else if (id == NotificationCenter.reloadHints) {
            if (dialogsSearchAdapter != null) {
                dialogsSearchAdapter.notifyDataSetChanged();
            }
        } else if (id == NotificationCenter.didUpdatedConnectionState) {
            int state = ConnectionsManager.getInstance(account).getConnectionState();
            if (currentConnectionState != state) {
                currentConnectionState = state;
                updateProxyButton(true);
            }
        } else if (id == NotificationCenter.dialogsUnreadCounterChanged) {
            /*if (!onlySelect) {
                int count = (Integer) args[0];
                currentUnreadCount = count;
                if (count != 0) {
                    unreadFloatingButtonCounter.setText(String.format("%d", count));
                    unreadFloatingButtonContainer.setVisibility(View.VISIBLE);
                    unreadFloatingButtonContainer.setTag(1);
                    unreadFloatingButtonContainer.animate().alpha(1.0f).setDuration(200).setInterpolator(new DecelerateInterpolator()).setListener(null).start();
                } else {
                    unreadFloatingButtonContainer.setTag(null);
                    unreadFloatingButtonContainer.animate().alpha(0.0f).setDuration(200).setInterpolator(new DecelerateInterpolator()).setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            unreadFloatingButtonContainer.setVisibility(View.INVISIBLE);
                        }
                    }).start();
                }
            }*/
        } else if (id == NotificationCenter.updateToLatestApiVersion) {
            SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("outdatedDialogLastShown", Context.MODE_PRIVATE);
            long millisLastShown = preferences.getLong("timeMillisLastShown", 0L);

            final int twoHours = 2 * 60 * 60 * 1000;
            long twoHoursAgo = millisLastShown + twoHours;
            if (millisLastShown == 0L || System.currentTimeMillis() > twoHoursAgo) {
                getAvailableActivity(activity -> {
                    AndroidUtilities.runOnUIThread(() -> {
                        if (outdatedDialog == null || !outdatedDialog.isShowing()) {
                            outdatedDialog = AlertsCreator.createOutdateDialog(activity, (dialog, which) -> {
                                AppVersionTask.openAppInPlayStore(activity);
                            }).create();
                            outdatedDialog.show();
                        }
                    });
                });
                SharedPreferences.Editor editor = ApplicationLoader.applicationContext.getSharedPreferences("outdatedDialogLastShown", Context.MODE_PRIVATE).edit();
                editor.putLong("timeMillisLastShown", System.currentTimeMillis()).apply();
            }
        }
    }

    public void setSideMenu(RecyclerView recyclerView) {
        sideMenu = recyclerView;
        sideMenu.setBackgroundColor(Theme.getColor(Theme.key_chats_menuBackground));
        sideMenu.setGlowColor(Theme.getColor(Theme.key_chats_menuBackground));
    }

    private void updatePasscodeButton() {
        if (passcodeItem == null) {
            return;
        }
        if (SharedConfig.passcodeHash.length() != 0 && !searching) {
            passcodeItem.setVisibility(View.VISIBLE);
            if (SharedConfig.appLocked) {
                passcodeItem.setIcon(R.drawable.lock_close);
            } else {
                passcodeItem.setIcon(R.drawable.lock_open);
            }
        } else {
            passcodeItem.setVisibility(View.GONE);
        }
    }

    private void hideFloatingButton(boolean hide, boolean changingTab) {
        if (floatingHidden == hide) {
            return;
        }
        floatingHidden = hide;
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(ObjectAnimator.ofFloat(floatingButton, "translationY", floatingHidden ? AndroidUtilities.dp(100) : 0),
                ObjectAnimator.ofFloat(unreadFloatingButtonContainer, "translationY", floatingHidden ? AndroidUtilities.dp(74) : 0));
        animatorSet.setDuration(300);
        animatorSet.setInterpolator(floatingInterpolator);
        animatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                if (changingTab && floatingButton.getVisibility() == View.GONE && currentBottomTabPosition == 0) {
                    floatingButton.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (changingTab && floatingButton.getVisibility() == View.VISIBLE && currentBottomTabPosition > 0) {
                    floatingButton.setVisibility(View.GONE);
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        floatingButton.setClickable(!hide);
        animatorSet.start();
    }

    private void updateVisibleRows(int mask) {
        if (listView == null) {
            return;
        }
        int count = listView.getChildCount();
        for (int a = 0; a < count; a++) {
            View child = listView.getChildAt(a);
            if (child instanceof DialogCell) {
                if (listView.getAdapter() != dialogsSearchAdapter) {
                    DialogCell cell = (DialogCell) child;
                    if ((mask & MessagesController.UPDATE_MASK_NEW_MESSAGE) != 0) {
                        cell.checkCurrentDialogIndex(dialogsAdapter);
                        if (dialogsType == 0 && AndroidUtilities.isTablet()) {
                            cell.setDialogSelected(cell.getDialogId() == openedDialogId);
                        }
                    } else if ((mask & MessagesController.UPDATE_MASK_SELECT_DIALOG) != 0) {
                        if (dialogsType == 0 && AndroidUtilities.isTablet()) {
                            cell.setDialogSelected(cell.getDialogId() == openedDialogId);
                        }
                    } else {
                        cell.update(mask);
                    }
                }
            } else if (child instanceof UserCell) {
                ((UserCell) child).update(mask);
            } else if (child instanceof ProfileSearchCell) {
                ((ProfileSearchCell) child).update(mask);
            } else if (child instanceof RecyclerListView) {
                RecyclerListView innerListView = (RecyclerListView) child;
                int count2 = innerListView.getChildCount();
                for (int b = 0; b < count2; b++) {
                    View child2 = innerListView.getChildAt(b);
                    if (child2 instanceof HintDialogCell) {
                        ((HintDialogCell) child2).checkUnreadCounter(mask);
                    }
                }
            }
        }
    }

    public void setDelegate(DialogsActivityDelegate dialogsActivityDelegate) {
        delegate = dialogsActivityDelegate;
    }

    public void setSearchString(String string) {
        searchString = string;
    }

    public boolean isMainDialogList() {
        return delegate == null && searchString == null;
    }

    private void didSelectResult(final long dialog_id, boolean useAlert, final boolean param) {
        if (addToGroupAlertString == null) {
            if ((int) dialog_id < 0) {
                TLRPC.Chat chat = MessagesController.getInstance(currentAccount).getChat(-(int) dialog_id);
                if (ChatObject.isChannel(chat) && !chat.megagroup && (cantSendToChannels || !ChatObject.isCanWriteToChannel(-(int) dialog_id, currentAccount))) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                    builder.setTitle(LocaleController.getString("AppName", R.string.AppName));
                    builder.setMessage(LocaleController.getString("ChannelCantSendMessage", R.string.ChannelCantSendMessage));
                    builder.setNegativeButton(LocaleController.getString("OK", R.string.OK), null);
                    showDialog(builder.create());
                    return;
                }
            }
        }
        if (useAlert && (selectAlertString != null && selectAlertStringGroup != null || addToGroupAlertString != null)) {
            if (getParentActivity() == null) {
                return;
            }
            AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
            builder.setTitle(LocaleController.getString("AppName", R.string.AppName));
            int lower_part = (int) dialog_id;
            int high_id = (int) (dialog_id >> 32);
            if (lower_part != 0) {
                if (high_id == 1) {
                    TLRPC.Chat chat = MessagesController.getInstance(currentAccount).getChat(lower_part);
                    if (chat == null) {
                        return;
                    }
                    builder.setMessage(LocaleController.formatStringSimple(selectAlertStringGroup, chat.title));
                } else {
                    if (lower_part == UserConfig.getInstance(currentAccount).getClientUserId()) {
                        builder.setMessage(LocaleController.formatStringSimple(selectAlertStringGroup, LocaleController.getString("SavedMessages", R.string.SavedMessages)));
                    } else if (lower_part > 0) {
                        TLRPC.User user = MessagesController.getInstance(currentAccount).getUser(lower_part);
                        if (user == null) {
                            return;
                        }
                        builder.setMessage(LocaleController.formatStringSimple(selectAlertString, UserObject.getUserName(user)));
                    } else if (lower_part < 0) {
                        TLRPC.Chat chat = MessagesController.getInstance(currentAccount).getChat(-lower_part);
                        if (chat == null) {
                            return;
                        }
                        if (addToGroupAlertString != null) {
                            builder.setMessage(LocaleController.formatStringSimple(addToGroupAlertString, chat.title));
                        } else {
                            builder.setMessage(LocaleController.formatStringSimple(selectAlertStringGroup, chat.title));
                        }
                    }
                }
            } else {
                TLRPC.EncryptedChat chat = MessagesController.getInstance(currentAccount).getEncryptedChat(high_id);
                TLRPC.User user = MessagesController.getInstance(currentAccount).getUser(chat.user_id);
                if (user == null) {
                    return;
                }
                builder.setMessage(LocaleController.formatStringSimple(selectAlertString, UserObject.getUserName(user)));
            }

            builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), (dialogInterface, i) -> didSelectResult(dialog_id, false, false));
            builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
            showDialog(builder.create());
        } else {
            if (delegate != null) {
                ArrayList<Long> dids = new ArrayList<>();
                dids.add(dialog_id);
                delegate.didSelectDialogs(DialogsActivity.this, dids, null, param);
                delegate = null;
            } else {
                finishFragment();
            }
        }
    }

    @Override
    public ThemeDescription[] getThemeDescriptions() {
        ThemeDescription.ThemeDescriptionDelegate cellDelegate = () -> {
            if (listView != null) {
                int count = listView.getChildCount();
                for (int a = 0; a < count; a++) {
                    View child = listView.getChildAt(a);
                    if (child instanceof ProfileSearchCell) {
                        ((ProfileSearchCell) child).update(0);
                    } else if (child instanceof DialogCell) {
                        ((DialogCell) child).update(0);
                    }
                }
            }
            if (dialogsSearchAdapter != null) {
                RecyclerListView recyclerListView = dialogsSearchAdapter.getInnerListView();
                if (recyclerListView != null) {
                    int count = recyclerListView.getChildCount();
                    for (int a = 0; a < count; a++) {
                        View child = recyclerListView.getChildAt(a);
                        if (child instanceof HintDialogCell) {
                            ((HintDialogCell) child).update();
                        }
                    }
                }
            }
        };
        return new ThemeDescription[]{
                new ThemeDescription(fragmentView, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, Theme.key_windowBackgroundWhite),

                new ThemeDescription(actionBar, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, Theme.key_actionBarDefault),
                new ThemeDescription(listView, ThemeDescription.FLAG_LISTGLOWCOLOR, null, null, null, null, Theme.key_actionBarDefault),
                new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_ITEMSCOLOR, null, null, null, null, Theme.key_actionBarDefaultIcon),
                new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_TITLECOLOR, null, null, null, null, Theme.key_actionBarDefaultTitle),
                new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_SELECTORCOLOR, null, null, null, null, Theme.key_actionBarDefaultSelector),
                new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_SEARCH, null, null, null, null, Theme.key_actionBarDefaultSearch),
                new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_SEARCHPLACEHOLDER, null, null, null, null, Theme.key_actionBarDefaultSearchPlaceholder),

                new ThemeDescription(listView, ThemeDescription.FLAG_SELECTOR, null, null, null, null, Theme.key_listSelector),

                new ThemeDescription(listView, 0, new Class[]{View.class}, Theme.dividerPaint, null, null, Theme.key_divider),

                new ThemeDescription(searchEmptyView, ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, Theme.key_emptyListPlaceholder),
                new ThemeDescription(searchEmptyView, ThemeDescription.FLAG_PROGRESSBAR, null, null, null, null, Theme.key_progressCircle),

                new ThemeDescription(listView, ThemeDescription.FLAG_TEXTCOLOR, new Class[]{DialogsEmptyCell.class}, new String[]{"emptyTextView1"}, null, null, null, Theme.key_emptyListPlaceholder),
                new ThemeDescription(listView, ThemeDescription.FLAG_TEXTCOLOR, new Class[]{DialogsEmptyCell.class}, new String[]{"emptyTextView2"}, null, null, null, Theme.key_emptyListPlaceholder),

                new ThemeDescription(floatingButton, ThemeDescription.FLAG_IMAGECOLOR, null, null, null, null, Theme.key_chats_actionIcon),
                new ThemeDescription(floatingButton, ThemeDescription.FLAG_BACKGROUNDFILTER, null, null, null, null, Theme.key_chats_actionBackground),
                new ThemeDescription(floatingButton, ThemeDescription.FLAG_BACKGROUNDFILTER | ThemeDescription.FLAG_DRAWABLESELECTEDSTATE, null, null, null, null, Theme.key_chats_actionPressedBackground),

                new ThemeDescription(unreadFloatingButtonCounter, ThemeDescription.FLAG_BACKGROUNDFILTER, null, null, null, null, Theme.key_chat_goDownButtonCounterBackground),
                new ThemeDescription(unreadFloatingButtonCounter, ThemeDescription.FLAG_TEXTCOLOR, null, null, null, null, Theme.key_chat_goDownButtonCounter),
                new ThemeDescription(unreadFloatingButton, ThemeDescription.FLAG_IMAGECOLOR, null, null, null, null, Theme.key_chats_actionUnreadIcon),
                new ThemeDescription(unreadFloatingButton, ThemeDescription.FLAG_BACKGROUNDFILTER, null, null, null, null, Theme.key_chats_actionUnreadBackground),
                new ThemeDescription(unreadFloatingButton, ThemeDescription.FLAG_BACKGROUNDFILTER | ThemeDescription.FLAG_DRAWABLESELECTEDSTATE, null, null, null, null, Theme.key_chats_actionUnreadPressedBackground),

                new ThemeDescription(listView, 0, new Class[]{DialogCell.class, ProfileSearchCell.class}, null, new Drawable[]{Theme.avatar_photoDrawable, Theme.avatar_broadcastDrawable, Theme.avatar_savedDrawable}, null, Theme.key_avatar_text),
                new ThemeDescription(null, 0, null, null, null, cellDelegate, Theme.key_avatar_backgroundRed),
                new ThemeDescription(null, 0, null, null, null, cellDelegate, Theme.key_avatar_backgroundOrange),
                new ThemeDescription(null, 0, null, null, null, cellDelegate, Theme.key_avatar_backgroundViolet),
                new ThemeDescription(null, 0, null, null, null, cellDelegate, Theme.key_avatar_backgroundGreen),
                new ThemeDescription(null, 0, null, null, null, cellDelegate, Theme.key_avatar_backgroundCyan),
                new ThemeDescription(null, 0, null, null, null, cellDelegate, Theme.key_avatar_backgroundBlue),
                new ThemeDescription(null, 0, null, null, null, cellDelegate, Theme.key_avatar_backgroundPink),
                new ThemeDescription(null, 0, null, null, null, cellDelegate, Theme.key_avatar_backgroundSaved),
                new ThemeDescription(listView, 0, new Class[]{DialogCell.class}, Theme.dialogs_countPaint, null, null, Theme.key_chats_unreadCounter),
                new ThemeDescription(listView, 0, new Class[]{DialogCell.class}, Theme.dialogs_countGrayPaint, null, null, Theme.key_chats_unreadCounterMuted),
                new ThemeDescription(listView, 0, new Class[]{DialogCell.class}, Theme.dialogs_countTextPaint, null, null, Theme.key_chats_unreadCounterText),
                new ThemeDescription(listView, 0, new Class[]{DialogCell.class, ProfileSearchCell.class}, Theme.dialogs_namePaint, null, null, Theme.key_chats_name),
                new ThemeDescription(listView, 0, new Class[]{DialogCell.class, ProfileSearchCell.class}, Theme.dialogs_nameEncryptedPaint, null, null, Theme.key_chats_secretName),
                new ThemeDescription(listView, 0, new Class[]{DialogCell.class, ProfileSearchCell.class}, null, new Drawable[]{Theme.dialogs_lockDrawable}, null, Theme.key_chats_secretIcon),
                new ThemeDescription(listView, 0, new Class[]{DialogCell.class, ProfileSearchCell.class}, null, new Drawable[]{Theme.dialogs_groupDrawable, Theme.dialogs_broadcastDrawable, Theme.dialogs_botDrawable}, null, Theme.key_chats_nameIcon),
                new ThemeDescription(listView, 0, new Class[]{DialogCell.class}, null, new Drawable[]{Theme.dialogs_pinnedDrawable}, null, Theme.key_chats_pinnedIcon),
                new ThemeDescription(listView, 0, new Class[]{DialogCell.class}, Theme.dialogs_messagePaint, null, null, Theme.key_chats_message),
                new ThemeDescription(null, 0, null, null, null, cellDelegate, Theme.key_chats_nameMessage),
                new ThemeDescription(null, 0, null, null, null, cellDelegate, Theme.key_chats_draft),
                new ThemeDescription(null, 0, null, null, null, cellDelegate, Theme.key_chats_attachMessage),
                new ThemeDescription(listView, 0, new Class[]{DialogCell.class}, Theme.dialogs_messagePrintingPaint, null, null, Theme.key_chats_actionMessage),
                new ThemeDescription(listView, 0, new Class[]{DialogCell.class}, Theme.dialogs_timePaint, null, null, Theme.key_chats_date),
                new ThemeDescription(listView, 0, new Class[]{DialogCell.class}, Theme.dialogs_pinnedPaint, null, null, Theme.key_chats_pinnedOverlay),
                new ThemeDescription(listView, 0, new Class[]{DialogCell.class}, Theme.dialogs_tabletSeletedPaint, null, null, Theme.key_chats_tabletSelectedOverlay),
                new ThemeDescription(listView, 0, new Class[]{DialogCell.class}, null, new Drawable[]{Theme.dialogs_checkDrawable, Theme.dialogs_halfCheckDrawable}, null, Theme.key_chats_sentCheck),
                new ThemeDescription(listView, 0, new Class[]{DialogCell.class}, null, new Drawable[]{Theme.dialogs_clockDrawable}, null, Theme.key_chats_sentClock),
                new ThemeDescription(listView, 0, new Class[]{DialogCell.class}, Theme.dialogs_errorPaint, null, null, Theme.key_chats_sentError),
                new ThemeDescription(listView, 0, new Class[]{DialogCell.class}, null, new Drawable[]{Theme.dialogs_errorDrawable}, null, Theme.key_chats_sentErrorIcon),
                new ThemeDescription(listView, 0, new Class[]{DialogCell.class, ProfileSearchCell.class}, null, new Drawable[]{Theme.dialogs_verifiedCheckDrawable}, null, Theme.key_chats_verifiedCheck),
                new ThemeDescription(listView, 0, new Class[]{DialogCell.class, ProfileSearchCell.class}, null, new Drawable[]{Theme.dialogs_verifiedDrawable}, null, Theme.key_chats_verifiedBackground),
                new ThemeDescription(listView, 0, new Class[]{DialogCell.class}, null, new Drawable[]{Theme.dialogs_muteDrawable}, null, Theme.key_chats_muteIcon),
                new ThemeDescription(listView, 0, new Class[]{DialogCell.class}, null, new Drawable[]{Theme.dialogs_mentionDrawable}, null, Theme.key_chats_mentionIcon),

                new ThemeDescription(sideMenu, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, Theme.key_chats_menuBackground),
                new ThemeDescription(sideMenu, 0, new Class[]{DrawerProfileCell.class}, null, null, null, Theme.key_chats_menuName),
                new ThemeDescription(sideMenu, 0, new Class[]{DrawerProfileCell.class}, null, null, null, Theme.key_chats_menuPhone),
                new ThemeDescription(sideMenu, 0, new Class[]{DrawerProfileCell.class}, null, null, null, Theme.key_chats_menuPhoneCats),
                new ThemeDescription(sideMenu, 0, new Class[]{DrawerProfileCell.class}, null, null, null, Theme.key_chats_menuCloudBackgroundCats),
                new ThemeDescription(sideMenu, 0, new Class[]{DrawerProfileCell.class}, null, null, null, Theme.key_chat_serviceBackground),
                new ThemeDescription(sideMenu, 0, new Class[]{DrawerProfileCell.class}, null, null, null, Theme.key_chats_menuTopShadow),
                new ThemeDescription(sideMenu, ThemeDescription.FLAG_CELLBACKGROUNDCOLOR, new Class[]{DrawerProfileCell.class}, null, null, null, Theme.key_avatar_backgroundActionBarBlue),

                new ThemeDescription(sideMenu, ThemeDescription.FLAG_IMAGECOLOR, new Class[]{DrawerActionCell.class}, new String[]{"textView"}, null, null, null, Theme.key_chats_menuItemIcon),
                new ThemeDescription(sideMenu, 0, new Class[]{DrawerActionCell.class}, new String[]{"textView"}, null, null, null, Theme.key_chats_menuItemText),

                new ThemeDescription(sideMenu, 0, new Class[]{DrawerUserCell.class}, new String[]{"textView"}, null, null, null, Theme.key_chats_menuItemText),
                new ThemeDescription(sideMenu, ThemeDescription.FLAG_TEXTCOLOR, new Class[]{DrawerUserCell.class}, new String[]{"checkBox"}, null, null, null, Theme.key_chats_unreadCounterText),
                new ThemeDescription(sideMenu, ThemeDescription.FLAG_TEXTCOLOR, new Class[]{DrawerUserCell.class}, new String[]{"checkBox"}, null, null, null, Theme.key_chats_unreadCounter),
                new ThemeDescription(sideMenu, ThemeDescription.FLAG_TEXTCOLOR, new Class[]{DrawerUserCell.class}, new String[]{"checkBox"}, null, null, null, Theme.key_chats_menuBackground),
                new ThemeDescription(sideMenu, ThemeDescription.FLAG_IMAGECOLOR, new Class[]{DrawerAddCell.class}, new String[]{"textView"}, null, null, null, Theme.key_chats_menuItemIcon),
                new ThemeDescription(sideMenu, 0, new Class[]{DrawerAddCell.class}, new String[]{"textView"}, null, null, null, Theme.key_chats_menuItemText),

                new ThemeDescription(sideMenu, 0, new Class[]{DividerCell.class}, Theme.dividerPaint, null, null, Theme.key_divider),

                new ThemeDescription(listView, 0, new Class[]{LoadingCell.class}, new String[]{"progressBar"}, null, null, null, Theme.key_progressCircle),

                new ThemeDescription(listView, 0, new Class[]{ProfileSearchCell.class}, Theme.dialogs_offlinePaint, null, null, Theme.key_windowBackgroundWhiteGrayText3),
                new ThemeDescription(listView, 0, new Class[]{ProfileSearchCell.class}, Theme.dialogs_onlinePaint, null, null, Theme.key_windowBackgroundWhiteBlueText3),

                new ThemeDescription(listView, 0, new Class[]{GraySectionCell.class}, new String[]{"textView"}, null, null, null, Theme.key_graySectionText),
                new ThemeDescription(listView, ThemeDescription.FLAG_CELLBACKGROUNDCOLOR, new Class[]{GraySectionCell.class}, null, null, null, Theme.key_graySection),

                new ThemeDescription(listView, ThemeDescription.FLAG_TEXTCOLOR, new Class[]{HashtagSearchCell.class}, null, null, null, Theme.key_windowBackgroundWhiteBlackText),

                new ThemeDescription(progressView, ThemeDescription.FLAG_PROGRESSBAR, null, null, null, null, Theme.key_progressCircle),

                new ThemeDescription(dialogsSearchAdapter != null ? dialogsSearchAdapter.getInnerListView() : null, 0, new Class[]{HintDialogCell.class}, Theme.dialogs_countPaint, null, null, Theme.key_chats_unreadCounter),
                new ThemeDescription(dialogsSearchAdapter != null ? dialogsSearchAdapter.getInnerListView() : null, 0, new Class[]{HintDialogCell.class}, Theme.dialogs_countGrayPaint, null, null, Theme.key_chats_unreadCounterMuted),
                new ThemeDescription(dialogsSearchAdapter != null ? dialogsSearchAdapter.getInnerListView() : null, 0, new Class[]{HintDialogCell.class}, Theme.dialogs_countTextPaint, null, null, Theme.key_chats_unreadCounterText),
                new ThemeDescription(dialogsSearchAdapter != null ? dialogsSearchAdapter.getInnerListView() : null, 0, new Class[]{HintDialogCell.class}, new String[]{"nameTextView"}, null, null, null, Theme.key_windowBackgroundWhiteBlackText),

                new ThemeDescription(fragmentView, ThemeDescription.FLAG_BACKGROUND | ThemeDescription.FLAG_CHECKTAG, new Class[]{FragmentContextView.class}, new String[]{"frameLayout"}, null, null, null, Theme.key_inappPlayerBackground),
                new ThemeDescription(fragmentView, ThemeDescription.FLAG_IMAGECOLOR, new Class[]{FragmentContextView.class}, new String[]{"playButton"}, null, null, null, Theme.key_inappPlayerPlayPause),
                new ThemeDescription(fragmentView, ThemeDescription.FLAG_TEXTCOLOR | ThemeDescription.FLAG_CHECKTAG, new Class[]{FragmentContextView.class}, new String[]{"titleTextView"}, null, null, null, Theme.key_inappPlayerTitle),
                new ThemeDescription(fragmentView, ThemeDescription.FLAG_TEXTCOLOR | ThemeDescription.FLAG_FASTSCROLL, new Class[]{FragmentContextView.class}, new String[]{"titleTextView"}, null, null, null, Theme.key_inappPlayerPerformer),
                new ThemeDescription(fragmentView, ThemeDescription.FLAG_IMAGECOLOR, new Class[]{FragmentContextView.class}, new String[]{"closeButton"}, null, null, null, Theme.key_inappPlayerClose),

                new ThemeDescription(fragmentView, ThemeDescription.FLAG_BACKGROUND | ThemeDescription.FLAG_CHECKTAG, new Class[]{FragmentContextView.class}, new String[]{"frameLayout"}, null, null, null, Theme.key_returnToCallBackground),
                new ThemeDescription(fragmentView, ThemeDescription.FLAG_TEXTCOLOR | ThemeDescription.FLAG_CHECKTAG, new Class[]{FragmentContextView.class}, new String[]{"titleTextView"}, null, null, null, Theme.key_returnToCallText),

                new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogBackground),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogBackgroundGray),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogTextBlack),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogTextLink),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogLinkSelection),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogTextBlue),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogTextBlue2),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogTextBlue3),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogTextBlue4),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogTextRed),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogTextGray),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogTextGray2),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogTextGray3),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogTextGray4),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogIcon),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogTextHint),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogInputField),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogInputFieldActivated),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogCheckboxSquareBackground),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogCheckboxSquareCheck),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogCheckboxSquareUnchecked),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogCheckboxSquareDisabled),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogRadioBackground),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogRadioBackgroundChecked),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogProgressCircle),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogButton),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogButtonSelector),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogScrollGlow),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogRoundCheckBox),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogRoundCheckBoxCheck),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogBadgeBackground),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogBadgeText),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogLineProgress),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogLineProgressBackground),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_dialogGrayLine),

                new ThemeDescription(null, 0, null, null, null, null, Theme.key_player_actionBar),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_player_actionBarSelector),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_player_actionBarTitle),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_player_actionBarTop),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_player_actionBarSubtitle),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_player_actionBarItems),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_player_background),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_player_time),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_player_progressBackground),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_player_progressCachedBackground),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_player_progress),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_player_placeholder),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_player_placeholderBackground),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_player_button),
                new ThemeDescription(null, 0, null, null, null, null, Theme.key_player_buttonActive),
        };
    }
}
