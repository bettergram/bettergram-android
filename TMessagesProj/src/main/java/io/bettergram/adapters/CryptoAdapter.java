package io.bettergram.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.TouchDelegate;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sackcentury.shinebuttonlib.ShineButton;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import io.bettergram.data.CryptoCurrencyInfo;
import io.bettergram.data.CryptoCurrencyInfoData;
import io.bettergram.data.CryptoCurrencyInfoData__JsonHelper;
import io.bettergram.data.CryptoCurrencyInfoResponse;
import io.bettergram.data.CryptoCurrencyInfoResponse__JsonHelper;
import io.bettergram.messenger.R;
import io.bettergram.service.CryptoCurrencyDataService;
import io.bettergram.telegram.messenger.AndroidUtilities;
import io.bettergram.telegram.messenger.FileLog;
import io.bettergram.telegram.messenger.ImageReceiver;
import io.bettergram.telegram.messenger.NotificationCenter;
import io.bettergram.telegram.messenger.support.widget.RecyclerView;
import io.bettergram.telegram.ui.ActionBar.Theme;
import io.bettergram.telegram.ui.Components.CardView.CardView;
import io.bettergram.telegram.ui.Components.LayoutHelper;
import io.bettergram.telegram.ui.Components.PullToRefresh.PullRefreshLayout;
import io.bettergram.telegram.ui.Components.TabStrip.SlidingTabLayout;
import io.bettergram.utils.Number;
import io.bettergram.utils.SpanBuilder;

import static android.text.TextUtils.isEmpty;
import static io.bettergram.service.CryptoCurrencyDataService.EXTRA_LIMIT;
import static io.bettergram.service.CryptoCurrencyDataService.faveCurrency;
import static io.bettergram.service.CryptoCurrencyDataService.saveCurrencies;
import static io.bettergram.telegram.messenger.AndroidUtilities.dp;

public class CryptoAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements PullRefreshLayout.OnRefreshListener, NotificationCenter.NotificationCenterDelegate {

    /**
     * Receives data from {@link CryptoCurrencyDataService}
     */
    public BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                new Thread(new JsonRunnable(bundle.getString(CryptoCurrencyDataService.RESULT))).start();
            }
        }
    };

    /**
     * Runnable the processes json response
     */
    class JsonRunnable implements Runnable {

        String json;

        JsonRunnable(String json) {
            this.json = json;
        }

        @Override
        public void run() {
            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
            try {
                CryptoCurrencyInfoResponse res = CryptoCurrencyInfoResponse__JsonHelper.parseFromJson(json);
                AndroidUtilities.runOnUIThread(() -> setCryptoData(res));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onRefresh(PullRefreshLayout ptrLayout) {
        if (this.ptrLayout == null) {
            this.ptrLayout = ptrLayout;
        }
        startService();
    }

    @Override
    public void didReceivedNotification(int id, int account, Object... args) {
        if (id == NotificationCenter.currencySearchResultsUpdate) {
            String json = (String) args[0];
            if (!isEmpty(json)) {
                try {
                    json = json.replace("data", "list");
                    CryptoCurrencyInfoData _data = CryptoCurrencyInfoData__JsonHelper.parseFromJson(json);
                    AndroidUtilities.runOnUIThread(() -> {
                        data.clear();
                        data.addAll(_data.list);
                        notifyDataSetChanged();
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else if (id == NotificationCenter.updateCurrencyDataToBackup) {
            AndroidUtilities.runOnUIThread(() -> {
                data.clear();
                data.addAll(backup);
                notifyDataSetChanged();
            });
        } else if (id == NotificationCenter.updateCurrencyData) {
            String json = (String) args[0];
            try {
                CryptoCurrencyInfoResponse res = CryptoCurrencyInfoResponse__JsonHelper.parseFromJson(json);
                AndroidUtilities.runOnUIThread(() -> setCryptoData(res));
            } catch (IOException e) {
                e.printStackTrace();
                FileLog.e(e);
            }
        }
    }

    class MainViewHolder extends RecyclerView.ViewHolder implements ShineButton.OnCheckedChangeListener, ImageReceiver.ImageReceiverDelegate, View.OnClickListener {

        ImageReceiver cryptoPhoto;

        ImageView imageCrypto;
        TextView textCryptoName, textCryptoPrice, textDayDelta;
        String crypto;
        ShineButton star;

        boolean fromBind = false;

        public void setCrypto(String crypto) {
            this.crypto = crypto;
        }

        MainViewHolder(@NonNull View itemView) {
            super(itemView);
            imageCrypto = itemView.findViewWithTag("imageCrypto");
            textCryptoName = itemView.findViewWithTag("textCryptoName");
            textCryptoPrice = itemView.findViewWithTag("textCryptoPrice");
            textDayDelta = itemView.findViewWithTag("textDayDelta");

            cryptoPhoto = new ImageReceiver(imageCrypto);
            cryptoPhoto.setNeedsQualityThumb(false);
            cryptoPhoto.setDelegate(this);

            Activity activity = (Activity) itemView.getContext();
            star = itemView.findViewWithTag("star");
            if (star.activity == null) {
                star.activity = activity;
            }
            star.setOnCheckStateChangeListener(this);

            FrameLayout starLayout = itemView.findViewWithTag("starLayout");
            starLayout.setOnClickListener(this);

            final View parent = (View) starLayout.getParent();
            parent.post(() -> {
                final Rect r = new Rect();
                starLayout.getHitRect(r);
                r.top -= dp(10);
                r.bottom += dp(10);
                r.left -= dp(10);
                r.right += dp(10);
                parent.setTouchDelegate(new TouchDelegate(r, starLayout));
            });
        }

        @Override
        public void onClick(View v) {
            if (!star.isChecked()) {
                star.setChecked(true, true);
            } else {
                star.setChecked(false, true);
            }
        }

        @Override
        public void onCheckedChanged(View view, boolean checked) {
            if (fromBind) {
                fromBind = false;
                return;
            }
            for (int i = 0, size = data.size(); i < size; i++) {
                if (data.get(i).code.equals(crypto)) {
                    data.get(i).favorite = checked;
                    faveCurrency(checked, data.get(i));
                    break;
                }
            }
            backup.clear();
            backup.addAll(data);
            favorites.clear();
            favorites.addAll(data);
            for (Iterator<CryptoCurrencyInfo> iterator = favorites.iterator();
                 iterator.hasNext(); ) {
                CryptoCurrencyInfo value = iterator.next();
                if (!value.favorite) {
                    iterator.remove();
                }
            }
            saveCurrencies(data);
        }

        @Override
        public void didSetImage(ImageReceiver imageReceiver, boolean set, boolean thumb) {
            Bitmap bitmap = imageReceiver.getBitmap();
            imageCrypto.setImageBitmap(bitmap);
        }
    }

    class HeaderViewHolder extends RecyclerView.ViewHolder {

        TextView textCap, textDom, textVol;

        HeaderViewHolder(View itemView) {
            super(itemView);
            textCap = itemView.findViewById(R.id.textCap);
            textDom = itemView.findViewById(R.id.textDom);
            textVol = itemView.findViewById(R.id.textVol);

            CardView cardView = (CardView) itemView;
            cardView.setCardBackgroundColor(Theme.getColor(Theme.key_panel_backgroundColor));
        }
    }

    class TabsViewHolder extends RecyclerView.ViewHolder {

        public TabsViewHolder(View itemView) {
            super(itemView);
        }
    }

    class LabelViewHolder extends RecyclerView.ViewHolder {
        TextView textCode, textPrice, text24H;

        public LabelViewHolder(View itemView) {
            super(itemView);
            textCode = itemView.findViewById(R.id.textCode);
            textPrice = itemView.findViewById(R.id.textPrice);
            text24H = itemView.findViewById(R.id.text24H);

            textCode.setTextColor(Theme.getColor(Theme.key_crypto_topTab_nameInactiveColor));
            textPrice.setTextColor(Theme.getColor(Theme.key_crypto_topTab_nameInactiveColor));
            text24H.setTextColor(Theme.getColor(Theme.key_crypto_topTab_nameInactiveColor));
        }
    }

    private Activity activity;
    private PullRefreshLayout ptrLayout;
    private CryptoCurrencyInfoResponse cryptoData;
    private List<CryptoCurrencyInfo> data = new ArrayList<>();
    private List<CryptoCurrencyInfo> backup = new ArrayList<>();
    private List<CryptoCurrencyInfo> favorites = new ArrayList<>();
    private boolean searchMode = false;

    public void setSearchMode(boolean searchMode) {
        this.searchMode = searchMode;
        notifyDataSetChanged();
    }

    public CryptoAdapter(Activity activity) {
        this.activity = activity;
        NotificationCenter.getGlobalInstance().addObserver(this, NotificationCenter.currencySearchResultsUpdate);
        NotificationCenter.getGlobalInstance().addObserver(this, NotificationCenter.updateCurrencyDataToBackup);
        NotificationCenter.getGlobalInstance().addObserver(this, NotificationCenter.updateCurrencyData);
    }

    public void setCryptoData(CryptoCurrencyInfoResponse cryptoData) {
        if (cryptoData != null) {
            this.cryptoData = cryptoData;
            if (cryptoData.data != null && !cryptoData.data.list.isEmpty()) {
                data.clear();
                //data.addAll(cryptoData.data.favorites);
                data.addAll(cryptoData.data.list);
                backup.clear();
                backup.addAll(data);
                favorites.clear();
                favorites.addAll(data);
                for (Iterator<CryptoCurrencyInfo> iterator = favorites.iterator();
                     iterator.hasNext(); ) {
                    CryptoCurrencyInfo value = iterator.next();
                    if (!value.favorite) {
                        iterator.remove();
                    }
                }
                notifyDataSetChanged();
            }
        }
        if (ptrLayout != null) {
            ptrLayout.postDelayed(() -> ptrLayout.setRefreshing(false), 1500);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return searchMode ? 3 : position == 0 ? 0 : position == 1 ? 1 : position == 2 ? 2 : 3;
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        switch (viewType) {
            case 0:
                View header = inflater.inflate(R.layout.header_crypto, parent, false);
                return new HeaderViewHolder(header);
            case 1:
                SlidingTabLayout tabLayout = new SlidingTabLayout(context);
                tabLayout.setAdapter(new TabsPagerAdapter());
                tabLayout.setDividerColors(context.getResources().getColor(android.R.color.transparent));
                tabLayout.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
                    @Override
                    public void onPageSelected(int position) {
                        super.onPageSelected(position);
                        switch (position) {
                            case 0:
                                data.clear();
                                data.addAll(backup);
                                notifyDataSetChanged();
                                break;
                            case 1:
                                data.clear();
                                data.addAll(favorites);
                                notifyDataSetChanged();
                                break;
                        }
                    }
                });
                tabLayout.setLayoutParams(new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT));
                return new TabsViewHolder(tabLayout);
            case 2:
                View labelView = inflater.inflate(R.layout.item_crypto_top, parent, false);
                return new LabelViewHolder(labelView);
            case 3:
                LinearLayout container = new LinearLayout(context);
                container.setOrientation(LinearLayout.HORIZONTAL);
                container.setPadding(
                        0, 0,
                        AndroidUtilities.dp(4), 0);

                ShineButton star = new ShineButton(context);
                star.setBtnColor(context.getResources().getColor(android.R.color.darker_gray));
                star.setBtnFillColor(Color.parseColor("#ffc200"));
                star.enableFlashing(true);
                star.setShineSize(AndroidUtilities.dp(14));
                star.setShapeResource(R.raw.star);
                star.setTag("star");

                FrameLayout starLayout = new FrameLayout(context) {
                    @Override
                    public boolean onInterceptTouchEvent(MotionEvent ev) {
                        return true;
                    }
                };
                starLayout.addView(star, LayoutHelper.createFrame(
                        22, 22,
                        Gravity.CENTER));
                starLayout.setTag("starLayout");

                container.addView(starLayout, LayoutHelper.createLinear(
                        AndroidUtilities.isTablet() ? 60 : 58, AndroidUtilities.isTablet() ? 44 : 42,
                        Gravity.CENTER));

                ImageView cryptoImage = new ImageView(context);
                cryptoImage.setTag("imageCrypto");
                container.addView(cryptoImage, LayoutHelper.createLinear(
                        25, 25,
                        Gravity.CENTER,
                        -4, 8, 4, 8));
                TextView cryptoName = new TextView(context);
                cryptoName.setTypeface(Typeface.DEFAULT);
                cryptoName.setMaxLines(1);
                cryptoName.setTextColor(Theme.getColor(Theme.key_crypto_page_labelColor));
                cryptoName.setTextSize(14);
                cryptoName.setGravity(Gravity.CENTER);
                cryptoName.setEllipsize(TextUtils.TruncateAt.END);
                cryptoName.setTag("textCryptoName");
                container.addView(cryptoName, LayoutHelper.createLinear(
                        LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT,
                        Gravity.CENTER,
                        8, 0, 0, 0));
                LinearLayout content = new LinearLayout(context) {
                    @Override
                    protected void onDraw(Canvas canvas) {
                        super.onDraw(canvas);
                        final int width = getMeasuredWidth();
                        final int height = getMeasuredHeight();
                        final float offset_y = height - 1;
                        final float offset_x = cryptoName.getX();
                        canvas.drawLine(offset_x, offset_y, width, offset_y, Theme.dividerPaint);
                    }
                };
                content.setWillNotDraw(false);
                content.setOrientation(LinearLayout.HORIZONTAL);
                content.setWeightSum(1);
                LinearLayout.MarginLayoutParams margin = LayoutHelper.createMargin(
                        LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT);
                margin.topMargin = margin.bottomMargin = AndroidUtilities.dp(2);
                content.setLayoutParams(margin);
                content.addView(container, LayoutHelper.createLinear(
                        0, AndroidUtilities.isTablet() ? 44 : 42,
                        0.45f));
                TextView cryptoPrice = new TextView(context);
                cryptoPrice.setGravity(Gravity.CENTER);
                cryptoPrice.setTextSize(14);
                cryptoPrice.setTag("textCryptoPrice");
                cryptoPrice.setTextColor(Color.GRAY);
                content.addView(cryptoPrice, LayoutHelper.createLinear(
                        0, LayoutHelper.WRAP_CONTENT,
                        0.25f,
                        Gravity.CENTER));
                TextView cryptoDelta = new TextView(context);
                cryptoDelta.setTextSize(14);
                cryptoDelta.setGravity(Gravity.CENTER | Gravity.END);
                cryptoDelta.setTag("textDayDelta");
                content.addView(cryptoDelta, LayoutHelper.createLinear(
                        0, LayoutHelper.WRAP_CONTENT,
                        0.30f, Gravity.CENTER,
                        0, 0, 8, 0));

                return new MainViewHolder(content);
            default:
                throw new IllegalStateException("Unrecognizable viewType");
        }
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        final Context context = holder.itemView.getContext();
        if (holder instanceof LabelViewHolder) {
            LabelViewHolder label = (LabelViewHolder) holder;
            label.itemView.setVisibility(searchMode ? View.GONE : View.VISIBLE);
        } else if (holder instanceof HeaderViewHolder) {
            HeaderViewHolder header = (HeaderViewHolder) holder;
            holder.itemView.setVisibility(searchMode ? View.GONE : View.VISIBLE);
            double cap = cryptoData != null ? cryptoData.cap : 0.0f;
            header.textCap.setText(formatHeaderValue(context, "MARKET CAP($)", Number.truncateNumber(cap)));
            double dom = cryptoData != null ? cryptoData.btcDominance : 0.0f;
            header.textDom.setText(formatHeaderValue(context, "BTC DOM.", String.format("%.2f%%", (dom * 100))));
            double vol = cryptoData != null ? cryptoData.volume : 0.0f;
            header.textVol.setText(formatHeaderValue(context, "24H VOL($)", Number.truncateNumber(vol)));
        } else if (holder instanceof MainViewHolder) {

            int real_position = position - (searchMode ? 0 : 3);

            if (real_position < 0 || real_position > data.size()) {
                return;
            }

            CryptoCurrencyInfo info = data.get(real_position);

            if (info == null) {
                return;
            }

            MainViewHolder main = (MainViewHolder) holder;
            main.setCrypto(info.code);
            main.textCryptoName.setText(info.name);
            double price = info.price;
            if (price > 0) {
                boolean isGreaterZero = Math.floor(price) > 0;
                double deltaMinute = -1 * ((1 - info.delta.minute) * 100);
                //main.textCryptoPrice.setTextColor(deltaMinute > 0 ? Color.parseColor("#ff69bc35") : Color.RED);
                main.textCryptoPrice.setText(String.format(isGreaterZero ? "$%,.2f" : "$%.4f", price));
            }
            if (!isEmpty(info.getIcon())) {
                main.cryptoPhoto.setImage(
                        info.getIcon(),
                        null,
                        Theme.circle_placeholderDrawable,
                        null,
                        AndroidUtilities.dp(24)
                );
            }
            if (info.delta != null) {
                double deltaDay = -1 * ((1 - info.delta.day) * 100);
                main.textDayDelta.setTextColor(deltaDay > 0 ? Color.parseColor("#ff69bc35") : Color.RED);
                main.textDayDelta.setText(String.format(deltaDay > 0 ? "+%s%%" : "%s%%", Number.truncateNumber(deltaDay)));

                main.textDayDelta.setCompoundDrawablesWithIntrinsicBounds(
                        null,
                        null,
                        deltaDay > 0 ? Theme.crypto_priceUpDrawable
                                : Theme.crypto_priceDownDrawable,
                        null
                );
                main.textDayDelta.setCompoundDrawablePadding(2);
            }
            main.fromBind = true;
            main.star.setChecked(info.favorite, false);
        }
    }

    @Override
    public int getItemCount() {
        return data.size() + (searchMode ? 0 : 3);
    }

    /**
     * Creates formatted header for price
     */
    public SpannableStringBuilder formatHeaderValue(Context context, String s1, String s2) {
        int grey73 = ContextCompat.getColor(context, R.color.grey73);
        int bigColor = Theme.getColor(Theme.key_panel_labelColor);

        SpanBuilder spanBuilder = new SpanBuilder();
        spanBuilder
                .appendWithLineBreak(s1,
                        new RelativeSizeSpan(0.7f),
                        new ForegroundColorSpan(grey73)
                )
                .append(s2,
                        new RelativeSizeSpan(1.2f),
                        new ForegroundColorSpan(bigColor)
                );
        return spanBuilder.build();
    }

    public void startService() {
        CryptoCurrencyDataService.isCurrentActive = true;
        Intent intent = new Intent(activity, CryptoCurrencyDataService.class);
        intent.putExtra(EXTRA_LIMIT, 100);
        activity.startService(intent);
    }

    public void pauseService() {
        CryptoCurrencyDataService.isCurrentActive = false;
    }

    /**
     * Register {@link BroadcastReceiver} of {@link CryptoCurrencyDataService}
     */
    public void registerReceiver(Activity activity) {
        activity.registerReceiver(receiver, new IntentFilter(CryptoCurrencyDataService.NOTIFICATION));
    }

    /**
     * Unregister {@link BroadcastReceiver} of {@link CryptoCurrencyDataService}
     */
    public void unregisterReceiver(Activity activity) {
        try {
            activity.unregisterReceiver(receiver);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    class TabsPagerAdapter extends PagerAdapter {

        final String[] ITEMS = {"All", "Favorites"};

        /**
         * @return the number of pages to display
         */
        @Override
        public int getCount() {
            return ITEMS.length;
        }

        @Override
        public boolean isViewFromObject(View view, Object o) {
            return o == view;
        }


        @Override
        public CharSequence getPageTitle(int position) {
            return ITEMS[position];
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

    }
}
