package io.bettergram.ui.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
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
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.sackcentury.shinebuttonlib.ShineButton;
import com.squareup.picasso.Picasso;
import io.bettergram.data.CryptoCurrencyInfo;
import io.bettergram.data.CryptoCurrencyInfoResponse;
import io.bettergram.data.CryptoCurrencyInfoResponse__JsonHelper;
import io.bettergram.messenger.R;
import io.bettergram.service.CryptoDataService;
import io.bettergram.utils.Number;
import io.bettergram.utils.SpanBuilder;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.support.widget.RecyclerView;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.TabStrip.SlidingTabLayout;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static android.text.TextUtils.isEmpty;
import static io.bettergram.service.CryptoDataService.EXTRA_LIMIT;

public class CryptoAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    /**
     * Receives data from {@link CryptoDataService}
     */
    public BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                new Thread(new JsonRunnable(bundle.getString(CryptoDataService.RESULT))).start();
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

    class MainViewHolder extends RecyclerView.ViewHolder implements ShineButton.OnCheckedChangeListener {

        ImageView imageCrypto;
        TextView textCryptoName, textCryptoPrice, textDayDelta;
        String crypto;
        ShineButton star;

        public void setCrypto(String crypto) {
            this.crypto = crypto;
        }

        MainViewHolder(@NonNull View itemView) {
            super(itemView);
            imageCrypto = itemView.findViewWithTag("imageCrypto");
            textCryptoName = itemView.findViewWithTag("textCryptoName");
            textCryptoPrice = itemView.findViewWithTag("textCryptoPrice");
            textDayDelta = itemView.findViewWithTag("textDayDelta");

            Activity activity = (Activity) itemView.getContext();
            star = itemView.findViewWithTag("star");
            if (star.activity == null) {
                star.init(activity);
            }
            star.setOnCheckStateChangeListener(this);
        }

        @Override
        public void onCheckedChanged(View view, boolean checked) {
            for (int i = 0, size = data.size(); i < size; i++) {
                if (data.get(i).code.equals(crypto)) {
                    data.get(i).favorite = checked;
                    CryptoDataService.faveCrypto(checked, data.get(i));
                    break;
                }
            }
            backup.clear();
            backup.addAll(data);
            favorites.clear();
            favorites.addAll(data);
            for (Iterator<CryptoCurrencyInfo> iterator = favorites.iterator(); iterator.hasNext(); ) {
                CryptoCurrencyInfo value = iterator.next();
                if (!value.favorite) {
                    iterator.remove();
                }
            }
        }
    }

    class HeaderViewHolder extends RecyclerView.ViewHolder {

        TextView textCap, textDom, textVol;

        HeaderViewHolder(View itemView) {
            super(itemView);
            textCap = itemView.findViewById(R.id.textCap);
            textDom = itemView.findViewById(R.id.textDom);
            textVol = itemView.findViewById(R.id.textVol);
        }
    }

    class TabsViewHolder extends RecyclerView.ViewHolder {
        public TabsViewHolder(View itemView) {
            super(itemView);
        }
    }

    class LabelViewHolder extends RecyclerView.ViewHolder {
        public LabelViewHolder(View itemView) {
            super(itemView);
        }
    }

    private CryptoCurrencyInfoResponse cryptoData;

    private List<CryptoCurrencyInfo> data = new ArrayList<>();
    private List<CryptoCurrencyInfo> backup = new ArrayList<>();
    private List<CryptoCurrencyInfo> favorites = new ArrayList<>();

    public CryptoAdapter() {
    }

    public void setCryptoData(CryptoCurrencyInfoResponse cryptoData) {
        if (cryptoData != null) {
            this.cryptoData = cryptoData;
            if (cryptoData.data != null && !cryptoData.data.list.isEmpty()) {
                data.clear();
                data.addAll(cryptoData.data.favorites);
                data.addAll(cryptoData.data.list);
                backup.clear();
                backup.addAll(data);
                favorites.clear();
                favorites.addAll(data);
                for (Iterator<CryptoCurrencyInfo> iterator = favorites.iterator(); iterator.hasNext(); ) {
                    CryptoCurrencyInfo value = iterator.next();
                    if (!value.favorite) {
                        iterator.remove();
                    }
                }
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        return position == 0 ? 0 : position == 1 ? 1 : position == 2 ? 2 : 3;
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
                tabLayout.setSelectedIndicatorColors(context.getResources().getColor(R.color.actionBarDefault));
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
                tabLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                return new TabsViewHolder(tabLayout);
            case 2:
                return new LabelViewHolder(inflater.inflate(R.layout.item_crypto_top, parent, false));
            case 3:
                LinearLayout content = new LinearLayout(context);
                content.setOrientation(LinearLayout.HORIZONTAL);
                content.setWeightSum(1);
                LinearLayout.MarginLayoutParams margin = LayoutHelper.createMargin(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT);
                margin.topMargin = margin.bottomMargin = AndroidUtilities.dp(2);
                content.setLayoutParams(margin);

                LinearLayout container = new LinearLayout(context);
                container.setOrientation(LinearLayout.HORIZONTAL);
                container.setPadding(AndroidUtilities.dp(8), AndroidUtilities.dp(8), AndroidUtilities.dp(8), AndroidUtilities.dp(8));

                ShineButton star = new ShineButton(context);
                star.setBtnColor(context.getResources().getColor(android.R.color.darker_gray));
                star.setBtnFillColor(Color.parseColor("#ffc200"));
                star.enableFlashing(true);
                star.setShineSize(AndroidUtilities.dp(14));
                star.setShapeResource(R.raw.star);
                star.setTag("star");
                container.addView(star, LayoutHelper.createLinear(22, 22, Gravity.CENTER, 0, 0, 8, 0));

                ImageView cryptoImage = new ImageView(context);
                cryptoImage.setTag("imageCrypto");
                container.addView(cryptoImage, LayoutHelper.createLinear(32, 32, Gravity.CENTER, 16, 0, 4, 0));

                TextView cryptoName = new TextView(context);
                cryptoName.setTypeface(Typeface.DEFAULT_BOLD);
                cryptoName.setMaxLines(1);
                cryptoName.setTextColor(context.getResources().getColor(android.R.color.black));
                cryptoName.setTextSize(14);
                cryptoName.setGravity(Gravity.CENTER);
                cryptoName.setEllipsize(TextUtils.TruncateAt.END);
                cryptoName.setTag("textCryptoName");
                container.addView(cryptoName, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER, 8, 0, 0, 0));

                content.addView(container, LayoutHelper.createLinear(0, LayoutHelper.WRAP_CONTENT, 0.5f));

                TextView cryptoPrice = new TextView(context);
                cryptoPrice.setGravity(Gravity.CENTER);
                cryptoPrice.setTextSize(14);
                cryptoPrice.setTag("textCryptoPrice");
                content.addView(cryptoPrice, LayoutHelper.createLinear(0, LayoutHelper.WRAP_CONTENT, 0.25f, Gravity.CENTER));

                TextView cryptoDelta = new TextView(context);
                cryptoDelta.setTextSize(14);
                cryptoDelta.setGravity(Gravity.CENTER | Gravity.END);
                cryptoDelta.setTag("textDayDelta");
                content.addView(cryptoDelta, LayoutHelper.createLinear(0, LayoutHelper.WRAP_CONTENT, 0.25f, Gravity.CENTER, 0, 0, 8, 0));

                return new MainViewHolder(content);
            default:
                throw new IllegalStateException("Unrecognizable viewType");
        }
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        final Context context = holder.itemView.getContext();

        if (holder instanceof HeaderViewHolder) {
            HeaderViewHolder header = (HeaderViewHolder) holder;
            double cap = cryptoData.cap;
            header.textCap.setText(formatHeaderValue(context, "MARKET CAP($)", Number.truncateNumber(cap)));
            double dom = cryptoData.btcDominance;
            header.textDom.setText(formatHeaderValue(context, "BTC DOM.", String.format("%.2f%%", (dom * 100))));
            double vol = cryptoData.volume;
            header.textVol.setText(formatHeaderValue(context, "24H VOL($)", Number.truncateNumber(vol)));
        } else if (holder instanceof LabelViewHolder) {

        } else if (holder instanceof MainViewHolder) {
            int realPosition = position - 3;
            CryptoCurrencyInfo info = data.get(realPosition);

            MainViewHolder main = (MainViewHolder) holder;
            main.setCrypto(info.code);
            main.textCryptoName.setText(info.name);
            double price = info.price;
            if (price > 0) {
                boolean isGreaterZero = Math.floor(price) > 0;
                double deltaMinute = -1 * ((1 - info.delta.minute) * 100);
                main.textCryptoPrice.setTextColor(deltaMinute > 0 ? Color.parseColor("#ff69bc35") : Color.RED);
                main.textCryptoPrice.setText(String.format(isGreaterZero ? "$%,.2f" : "$%.4f", price));
            }
            if (!isEmpty(info.icon)) {
                Picasso.get().load(info.icon).into(main.imageCrypto);
            }
            if (info.delta != null) {
                double deltaDay = -1 * ((1 - info.delta.day) * 100);
                main.textDayDelta.setTextColor(deltaDay > 0 ? Color.parseColor("#ff69bc35") : Color.RED);
                main.textDayDelta.setText(String.format(deltaDay > 0 ? "+%s%%" : "%s%%", Number.truncateNumber(deltaDay)));

                main.textDayDelta.setCompoundDrawablesWithIntrinsicBounds(
                        null,
                        null,
                        deltaDay > 0 ? Theme.crypto_priceUpDrawable : Theme.crypto_priceDownDrawable,
                        null
                );
                main.textDayDelta.setCompoundDrawablePadding(2);
            }
            main.star.setChecked(info.favorite);
        }
    }

    @Override
    public int getItemCount() {
        return data.size() + 3;
    }

    /**
     * Creates formatted header for price
     */
    public SpannableStringBuilder formatHeaderValue(Context context, String s1, String s2) {
        int grey73 = ContextCompat.getColor(context, R.color.grey73);
        int grey2c = ContextCompat.getColor(context, R.color.grey2c);

        SpanBuilder spanBuilder = new SpanBuilder();
        spanBuilder
                .appendWithLineBreak(s1,
                        new RelativeSizeSpan(0.7f),
                        new ForegroundColorSpan(grey73)
                )
                .append(s2,
                        new RelativeSizeSpan(1.2f),
                        new ForegroundColorSpan(grey2c)
                );
        return spanBuilder.build();
    }

    public void startService(Activity activity) {
        Intent intent = new Intent(activity, CryptoDataService.class);
        intent.putExtra(EXTRA_LIMIT, 100);
        activity.startService(intent);
    }

    /**
     * Register {@link BroadcastReceiver} of {@link CryptoDataService}
     */
    public void registerReceiver(Activity activity) {
        activity.registerReceiver(receiver, new IntentFilter(CryptoDataService.NOTIFICATION));
    }

    /**
     * Unregister {@link BroadcastReceiver} of {@link CryptoDataService}
     */
    public void unregisterReceiver(Activity activity) {
        activity.unregisterReceiver(receiver);
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
