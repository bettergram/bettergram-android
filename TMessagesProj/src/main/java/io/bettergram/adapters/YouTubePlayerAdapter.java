package io.bettergram.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.flipkart.youtubeview.YouTubePlayerView;
import io.bettergram.data.Video;
import io.bettergram.data.VideoList;
import io.bettergram.data.VideoList__JsonHelper;
import io.bettergram.messenger.R;
import io.bettergram.service.YoutubeDataService;
import io.bettergram.telegram.messenger.AndroidUtilities;
import io.bettergram.telegram.messenger.ImageReceiver;
import io.bettergram.telegram.messenger.support.widget.RecyclerView;
import io.bettergram.telegram.ui.ActionBar.Theme;
import io.bettergram.telegram.ui.Components.CardView.CardView;
import io.bettergram.telegram.ui.Components.PullToRefresh.PullRefreshLayout;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.flipkart.youtubeview.models.YouTubePlayerType.STRICT_NATIVE;
import static io.bettergram.service.api.VideosApi.formatToYesterdayOrToday;

public class YouTubePlayerAdapter extends
        RecyclerView.Adapter<YouTubePlayerAdapter.YouTubePlayerViewHolder> implements PullRefreshLayout.OnRefreshListener {

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

    @Override
    public void onRefresh(PullRefreshLayout ptrLayout) {
        if (this.ptrLayout == null) {
            this.ptrLayout = ptrLayout;
        }
        startService(activity);
    }

    private Activity activity;
    private PullRefreshLayout ptrLayout;
    private List<Video> videos = new ArrayList<>();
    private FragmentManager fragmentManager;
    private int playerType;
    private String apiKey;
    private String webviewUrl;

    public YouTubePlayerAdapter(Activity activity) {
        this.activity = activity;
        this.fragmentManager = activity.getFragmentManager();
        this.playerType = STRICT_NATIVE;
        this.apiKey = activity.getString(R.string.youtube_api_key);
        this.webviewUrl = activity.getString(R.string.youtube_webview_url);
    }

    class YouTubePlayerViewHolder extends RecyclerView.ViewHolder implements ImageReceiver.ImageReceiverDelegate {
        ImageReceiver videoPhoto;
        YouTubePlayerView playerView;
        ImageView thumbnailImage;
        TextView textTitle, textAccount, textDatePosted, textViewCount;

        YouTubePlayerViewHolder(View view) {
            super(view);
            playerView = view.findViewById(R.id.youtube_player_view);
            textTitle = view.findViewById(R.id.textTitle);
            textAccount = view.findViewById(R.id.textAccount);
            textDatePosted = view.findViewById(R.id.textDatePosted);
            textViewCount = view.findViewById(R.id.textViewCount);

            AndroidUtilities.setTextViewsColor(Theme.getColor(Theme.key_panel_labelColor),
                    textTitle);

            AndroidUtilities.setTextViewsColor(Theme.getColor(Theme.key_panel_subLabelColor),
                    textAccount,
                    textDatePosted,
                    textViewCount);

            AndroidUtilities.setTextViewsRelativeDrawableColor(
                    Theme.getColor(Theme.key_panel_subLabelColor),
                    textAccount,
                    textDatePosted,
                    textViewCount);

            thumbnailImage = playerView.findViewById(R.id.video_thumbnail_image);
            videoPhoto = new ImageReceiver(thumbnailImage);
            videoPhoto.setNeedsQualityThumb(true);
            videoPhoto.setDelegate(this);

            CardView cardView = (CardView) itemView;
            cardView.setCardBackgroundColor(Theme.getColor(Theme.key_panel_backgroundColor));
        }

        @Override
        public void didSetImage(ImageReceiver imageReceiver, boolean set, boolean thumb) {
            Bitmap bitmap = imageReceiver.getBitmap();
            thumbnailImage.setImageBitmap(bitmap);
        }
    }

    public void setVideos(List<Video> videos) {
        if (ptrLayout != null) {
            ptrLayout.setRefreshing(false);
        }
        if (videos == null || videos.isEmpty()) {
            return;
        }
        this.videos.clear();
        this.videos.addAll(videos);
        notifyDataSetChanged();
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

    @SuppressLint("SetTextI18n")
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
        holder.textDatePosted.setText("\u0020\u0020\u2022\u0020\u0020" + formatToYesterdayOrToday(publishedAt));
        holder.textViewCount.setText("\u0020\u0020\u2022\u0020\u0020" + String.format("%s views", viewCount));

        if (!playerView.initted) {
            playerView.initPlayer(
                    apiKey,
                    videoId,
                    webviewUrl,
                    playerType,
                    null,
                    fragmentManager,
                    (imageView, url, height, width) -> {
                        holder.videoPhoto.setImage(
                                url,
                                null,
                                null,
                                null,
                                width);
                    });
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
        try {
            activity.unregisterReceiver(receiver);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }
}
