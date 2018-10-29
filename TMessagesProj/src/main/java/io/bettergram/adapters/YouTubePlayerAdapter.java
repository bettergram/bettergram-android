package io.bettergram.adapters;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.flipkart.youtubeview.YouTubePlayerView;
import com.flipkart.youtubeview.models.ImageLoader;
import com.squareup.picasso.Picasso;
import io.bettergram.data.Video;
import io.bettergram.data.VideoList;
import io.bettergram.data.VideoList__JsonHelper;
import io.bettergram.messenger.R;
import io.bettergram.service.YoutubeDataService;
import io.bettergram.utils.RoundedCornersTransform;
import io.bettergram.telegram.messenger.AndroidUtilities;
import io.bettergram.telegram.messenger.support.widget.RecyclerView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.flipkart.youtubeview.models.YouTubePlayerType.STRICT_NATIVE;

public class YouTubePlayerAdapter extends RecyclerView.Adapter<YouTubePlayerAdapter.YouTubePlayerViewHolder> {

    /**
     * Receives data from {@link YoutubeDataService}
     */
    public BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                new Thread(new JsonRunnable(bundle.getString(YoutubeDataService.RESULT))).start();
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
                VideoList videoList = VideoList__JsonHelper.parseFromJson(json);
                AndroidUtilities.runOnUIThread(() -> setVideos(videoList.videos));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private List<Video> videos = new ArrayList<>();
    private Context context;
    private FragmentManager fragmentManager;
    private int playerType;
    private String apiKey;
    private String webviewUrl;

    class YouTubePlayerViewHolder extends RecyclerView.ViewHolder {

        YouTubePlayerView playerView;

        TextView textTitle, textAccount, textDatePosted, textViewCount;

        YouTubePlayerViewHolder(View view) {
            super(view);
            playerView = view.findViewById(R.id.youtube_player_view);
            textTitle = view.findViewById(R.id.textTitle);
            textAccount = view.findViewById(R.id.textAccount);
            textDatePosted = view.findViewById(R.id.textDatePosted);
            textViewCount = view.findViewById(R.id.textViewCount);
        }
    }

    private ImageLoader imageLoader = (imageView, url, height, width) -> {
        //Picasso.get().invalidate(url);// temporarily invalidated so the app wont load from memory as it shows blank image
        Picasso.get()
                .load(url)
                .resize((int) (width * 0.30f), (int) (height * 0.30f))
                .centerCrop()
                .placeholder(R.color.grey70)
                .transform(RoundedCornersTransform.getInstance())
                .into(imageView);
    };

    public void setVideos(List<Video> videos) {
        if (videos == null || videos.isEmpty()) return;
        this.videos.clear();
        this.videos.addAll(videos);
        notifyDataSetChanged();
    }

    public YouTubePlayerAdapter(Activity activity) {
        this.context = activity;
        this.fragmentManager = activity.getFragmentManager();
        this.playerType = STRICT_NATIVE;
        this.apiKey = context.getString(R.string.youtube_api_key);
        this.webviewUrl = context.getString(R.string.youtube_webview_url);
        Picasso.get().setLoggingEnabled(true);
    }

    @Override
    public int getItemCount() {
        return videos.size();
    }

    @NonNull
    @Override
    public YouTubePlayerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final Context context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.youtube_player, parent, false);
        return new YouTubePlayerViewHolder(view);
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public void onBindViewHolder(final YouTubePlayerViewHolder holder, int position) {
        YouTubePlayerView playerView = holder.playerView;
        Video video = videos.get(position);

        String videoId = video.id;
        String title = video.title;
        String channelTitle = video.channelTitle;
        String publishedAt = video.publishedAt;
        String viewCount = video.viewCount;

        holder.textTitle.setText(title);
        holder.textAccount.setText(channelTitle);
        holder.textDatePosted.setText(publishedAt);
        holder.textViewCount.setText(String.format("%s views", viewCount));

        if (!playerView.initted) {
            playerView.initPlayer(apiKey, videoId, webviewUrl, playerType, null, fragmentManager, imageLoader);
        } else {
            playerView.load(videoId);
        }
    }

    public void startService(Activity activity) {
        Intent intent = new Intent(activity, YoutubeDataService.class);
        activity.startService(intent);
    }

    /**
     * Register {@link BroadcastReceiver} of {@link YoutubeDataService}
     */
    public void registerReceiver(Activity activity) {
        activity.registerReceiver(receiver, new IntentFilter(YoutubeDataService.NOTIFICATION));
    }

    /**
     * Unregister {@link BroadcastReceiver} of {@link YoutubeDataService}
     */
    public void unregisterReceiver(Activity activity) {
        activity.unregisterReceiver(receiver);
    }
}
