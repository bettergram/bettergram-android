package io.bettergram.ui.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
                CryptoCurrencyInfoResponse cryptoData = CryptoCurrencyInfoResponse__JsonHelper.parseFromJson(json);
                AndroidUtilities.runOnUIThread(() -> setCryptoData(cryptoData));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    class MainViewHolder extends RecyclerView.ViewHolder {

        ImageView imageCrypto;

        TextView textCryptoName, textCryptoPrice, textDayDelta;

        MainViewHolder(@NonNull View itemView) {
            super(itemView);
            imageCrypto = itemView.findViewById(R.id.imageCrypto);
            textCryptoName = itemView.findViewById(R.id.textCryptoName);
            textCryptoPrice = itemView.findViewById(R.id.textCryptoPrice);
            textDayDelta = itemView.findViewById(R.id.textDayDelta);

            Activity activity = (Activity) itemView.getContext();
            ShineButton star = itemView.findViewById(R.id.star);
            star.init(activity);
        }
    }

    class HeaderViewHolder extends RecyclerView.ViewHolder {

        TextView textCap, textDom, textVol;

        public HeaderViewHolder(View itemView) {
            super(itemView);
            textCap = itemView.findViewById(R.id.textCap);
            textDom = itemView.findViewById(R.id.textDom);
            textVol = itemView.findViewById(R.id.textVol);
        }
    }

    class LabelViewHolder extends RecyclerView.ViewHolder {
        public LabelViewHolder(View itemView) {
            super(itemView);
        }
    }

    private CryptoCurrencyInfoResponse cryptoData;

    private List<CryptoCurrencyInfo> data = new ArrayList<>();

    public CryptoAdapter() {
    }

    public void setCryptoData(CryptoCurrencyInfoResponse cryptoData) {
        if (cryptoData != null) {
            this.cryptoData = cryptoData;
            if (cryptoData.data != null && !cryptoData.data.list.isEmpty()) {
                data.clear();
                data.addAll(cryptoData.data.favorites);
                data.addAll(cryptoData.data.list);
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        return position == 0 ? 0 : position == 1 ? 1 : 2;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        switch (viewType) {
            case 0:
                return new HeaderViewHolder(inflater.inflate(R.layout.header_crypto, parent, false));
            case 1:
                return new LabelViewHolder(inflater.inflate(R.layout.item_crypto_top, parent, false));
            case 2:
                return new MainViewHolder(inflater.inflate(R.layout.item_crypto, parent, false));
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
            int realPosition = position - 2;
            CryptoCurrencyInfo info = data.get(realPosition);

            MainViewHolder main = (MainViewHolder) holder;
            main.textCryptoName.setText(info.name);
            double price = info.price;
            boolean isGreaterZero = Math.floor(price) > 0;
            double deltaMinute = -1 * ((1 - info.delta.minute) * 100);
            main.textCryptoPrice.setTextColor(deltaMinute > 0 ? Color.parseColor("#ff69bc35") : Color.RED);
            main.textCryptoPrice.setText(String.format(isGreaterZero ? "$%,.2f" : "$%.4f", price));
            Picasso.get().load(info.icon).into(main.imageCrypto);
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
    }

    @Override
    public int getItemCount() {
        return data.size() + 2;
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
}
