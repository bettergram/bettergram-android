package io.bettergram.adapters;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.bettergram.data.News;
import io.bettergram.data.NewsList;
import io.bettergram.data.NewsList__JsonHelper;
import io.bettergram.messenger.R;
import io.bettergram.service.NewsDataService;
import io.bettergram.telegram.messenger.AndroidUtilities;
import io.bettergram.telegram.messenger.ImageReceiver;
import io.bettergram.telegram.messenger.support.widget.RecyclerView;
import io.bettergram.telegram.ui.ActionBar.Theme;
import io.bettergram.telegram.ui.Components.CardView.CardView;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.NewsViewHolder> {

    /**
     * Receives data from {@link NewsDataService}
     */
    public BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                new Thread(new JsonRunnable(bundle.getString(NewsDataService.RESULT))).start();
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
                NewsList newsList = NewsList__JsonHelper.parseFromJson(json);
                AndroidUtilities.runOnUIThread(() -> setNewsList(newsList.articles));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private List<News> newsList = new ArrayList<>();

    class NewsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, ImageReceiver.ImageReceiverDelegate {

        ImageReceiver newsPhoto;

        ImageView imageThumb;
        TextView textTitle, textAccount, textDatePosted;

        News news;

        NewsViewHolder(View itemView) {
            super(itemView);
            imageThumb = itemView.findViewById(R.id.imageThumb);
            textTitle = itemView.findViewById(R.id.textTitle);
            textAccount = itemView.findViewById(R.id.textAccount);
            textDatePosted = itemView.findViewById(R.id.textDatePosted);

            newsPhoto = new ImageReceiver(imageThumb);
            newsPhoto.setNeedsQualityThumb(true);
            newsPhoto.setDelegate(this);

            textTitle.setOnClickListener(this);
            imageThumb.setOnClickListener(this);

            AndroidUtilities.setTextViewsColor(Theme.getColor(Theme.key_panel_labelColor),
                    textTitle);

            AndroidUtilities.setTextViewsColor(Theme.getColor(Theme.key_panel_subLabelColor),
                    textAccount,
                    textDatePosted);

            AndroidUtilities.setTextViewsRelativeDrawableColor(
                    Theme.getColor(Theme.key_panel_subLabelColor),
                    textAccount,
                    textDatePosted);

            CardView cardView = (CardView) itemView;
            cardView.setCardBackgroundColor(Theme.getColor(Theme.key_panel_backgroundColor));
        }

        @Override
        public void didSetImage(ImageReceiver imageReceiver, boolean set, boolean thumb) {
            Bitmap bitmap = imageReceiver.getBitmap();
            imageThumb.setImageBitmap(bitmap);
        }

        @Override
        public void onClick(View v) {
            if (news != null) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW);
                browserIntent.setData(Uri.parse(news.url));
                v.getContext().startActivity(browserIntent);
            }
        }
    }

    public void setNewsList(List<News> newsList) {
        this.newsList.clear();
        this.newsList.addAll(newsList);
        AndroidUtilities.runOnUIThread(this::notifyDataSetChanged);
    }

    @NonNull
    @Override
    public NewsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int type) {
        final Context context = parent.getContext();
        final LayoutInflater inflater = LayoutInflater.from(context);

        final View content = inflater
                .inflate(type == 1 ? R.layout.item_news_big : R.layout.item_news_small,
                        parent,
                        false
                );

        return new NewsViewHolder(content);
    }

    @Override
    public void onBindViewHolder(NewsViewHolder holder, int position) {
        final News news = newsList.get(position);

        holder.news = news;

        holder.newsPhoto.setImage(
                news.urlToImage,
                null,
                null,
                null,
                0);

        holder.textTitle.setText(news.title);

        holder.textAccount.setText(news.source.name);

        holder.textDatePosted.setText(news.publishedAt);
    }

    @Override
    public int getItemViewType(int position) {
        return position == 0 ? 1 : 2;
    }

    @Override
    public int getItemCount() {
        return newsList.size();
    }

    public void startService(Activity activity) {
        Intent intent = new Intent(activity, NewsDataService.class);
        activity.startService(intent);
    }

    /**
     * Register {@link BroadcastReceiver} of {@link NewsDataService}
     */
    public void registerReceiver(Activity activity) {
        activity.registerReceiver(receiver, new IntentFilter(NewsDataService.NOTIFICATION));
    }

    /**
     * Unregister {@link BroadcastReceiver} of {@link NewsDataService}
     */
    public void unregisterReceiver(Activity activity) {
        try {
            activity.unregisterReceiver(receiver);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

}
