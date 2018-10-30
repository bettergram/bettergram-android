package io.bettergram.adapters;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import io.bettergram.data.News;
import io.bettergram.data.NewsList;
import io.bettergram.data.NewsList__JsonHelper;
import io.bettergram.messenger.R;
import io.bettergram.service.NewsDataService;
import io.bettergram.telegram.messenger.AndroidUtilities;
import io.bettergram.telegram.messenger.support.widget.RecyclerView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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

    class NewsViewHolder extends RecyclerView.ViewHolder {

        ImageView imageThumb;

        TextView textTitle, textAccount, textDatePosted;

        NewsViewHolder(View itemView) {
            super(itemView);
            imageThumb = itemView.findViewById(R.id.imageThumb);
            textTitle = itemView.findViewById(R.id.textTitle);
            textAccount = itemView.findViewById(R.id.textAccount);
            textDatePosted = itemView.findViewById(R.id.textDatePosted);
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

        holder.imageThumb.post(() -> {
            int width = (int) (holder.imageThumb.getMeasuredWidth() * 0.5f);
            int height = (int) (holder.imageThumb.getMeasuredHeight() * 0.5f);

            if (width > 0 && height > 0) {
                Picasso.get().load(news.urlToImage).resize(width, height).centerCrop().into(holder.imageThumb, new Callback.EmptyCallback() {
                    @Override
                    public void onSuccess() {
                        holder.imageThumb.requestLayout();
                    }
                });
            } else {
                Picasso.get().load(news.urlToImage).into(holder.imageThumb, new Callback.EmptyCallback() {
                    @Override
                    public void onSuccess() {
                        holder.imageThumb.requestLayout();
                    }
                });
            }
        });

        holder.textTitle.setText(news.title);
        holder.textTitle.setOnClickListener(v -> {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW);
            browserIntent.setData(Uri.parse(news.url));
            v.getContext().startActivity(browserIntent);
        });

        holder.textAccount.setText(news.source.name);

        //TODO: proper format
        holder.textDatePosted.setText(news.publishedAt.substring(0, 10));
//    try {
//      holder.textDatePosted.setText(NewsApi.getFormattedDate(news.publishedAt));
//    } catch (ParseException e) {
//      e.printStackTrace();
//    }

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
