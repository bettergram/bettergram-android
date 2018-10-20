package io.bettergram.ui.adapters;


import android.app.Activity;
import android.content.*;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.squareup.picasso.Picasso;
import io.bettergram.data.ResourceGroup;
import io.bettergram.data.ResourceItem;
import io.bettergram.data.ResourcesData;
import io.bettergram.data.ResourcesData__JsonHelper;
import io.bettergram.messenger.R;
import io.bettergram.service.ResourcesDataService;
import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.support.widget.RecyclerView;
import org.telegram.ui.Components.RecyclerListView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static android.text.TextUtils.isEmpty;

public class ResourcesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    /**
     * Receives data from {@link ResourcesDataService}
     */
    public BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            if (bundle != null) {
                new Thread(new JsonRunnable(bundle.getString(ResourcesDataService.RESULT))).start();
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
                ResourcesData data = ResourcesData__JsonHelper.parseFromJson(json);
                List<Object> objects = new ArrayList<>();
                for (int i = 0, size_i = data.resources.groups.size(); i < size_i; i++) {
                    ResourceGroup group = data.resources.groups.get(i);
                    objects.add(group.title);
                    for (int j = 0, size_j = group.items.size(); j < size_j; j++) {
                        ResourceItem item = group.items.get(j);
                        if (!isEmpty(item.title) && !isEmpty(item.url) && !isEmpty(item.iconUrl)) {
                            objects.add(item);
                        }
                    }
                }
                AndroidUtilities.runOnUIThread(() -> setResources(objects));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    class TitleViewHolder extends RecyclerView.ViewHolder {
        TextView textTitle;

        TitleViewHolder(View itemView) {
            super(itemView);
            textTitle = itemView.findViewById(R.id.textTitle);
        }

    }

    class ContentViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        View layoutContent;
        ImageView imageResource;
        TextView textName, textDesc;
        ResourceItem item;

        public void setItem(ResourceItem item) {
            this.item = item;
        }

        public ContentViewHolder(View itemView) {
            super(itemView);
            layoutContent = itemView.findViewById(R.id.layoutContent);
            imageResource = itemView.findViewById(R.id.imageResource);
            textName = itemView.findViewById(R.id.textName);
            textDesc = itemView.findViewById(R.id.textDesc);
            layoutContent.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            final Context context = v.getContext();
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(item.url));
            if (item.url.contains("tg://")) {
                ComponentName comp = new ComponentName(context.getPackageName(), "org.telegram.ui.LaunchActivity");
                intent.setComponent(comp);
            }
            context.startActivity(intent);
        }
    }

    private List<Object> objects = new ArrayList<>();


    public void setResources(List<Object> objects) {
        this.objects = objects;
    }

    @Override
    public int getItemViewType(int position) {
        for (int i = 0, size_i = objects.size(); i < size_i; i++) {
            if (i == position) {
                if (objects.get(i) instanceof String) return 0;
                if (objects.get(i) instanceof ResourceItem) return 1;
            }
        }
        return -1;
    }

    @Override
    public int getItemCount() {
        return objects.size() + 1;
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        switch (viewType) {
            case -1:
                View footer = new View(parent.getContext());
                footer.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, AndroidUtilities.dp(98)));
                return new RecyclerListView.Holder(footer);
            case 0:
                return new TitleViewHolder(inflater.inflate(R.layout.item_resource_header, parent, false));
            case 1:
                return new ContentViewHolder(inflater.inflate(R.layout.item_resource_content, parent, false));
            default:
                throw new IllegalStateException("Unrecognizable view type");
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        int type = getItemViewType(position);
        switch (type) {
            case -1:
                break;
            case 0:
                TitleViewHolder tvh = (TitleViewHolder) holder;
                String title = (String) objects.get(position);
                tvh.textTitle.setText(title);
                break;
            case 1:
                ContentViewHolder cvh = (ContentViewHolder) holder;
                ResourceItem item = (ResourceItem) objects.get(position);
                cvh.setItem(item);
                if (!isEmpty(item.title) && !isEmpty(item.url) && !isEmpty(item.description)) {
                    cvh.textName.setText(item.title);
                    cvh.textDesc.setText(item.description);
                    Picasso.get().load(item.thumbnail()).into(cvh.imageResource);
                }
                break;
            default:
                throw new IllegalStateException("Unrecognizable view type");
        }
    }

    public void startService(Activity activity) {
        Intent intent = new Intent(activity, ResourcesDataService.class);
        activity.startService(intent);
    }

    /**
     * Register {@link BroadcastReceiver} of {@link ResourcesDataService}
     */
    public void registerReceiver(Activity activity) {
        activity.registerReceiver(receiver, new IntentFilter(ResourcesDataService.NOTIFICATION));
    }

    /**
     * Unregister {@link BroadcastReceiver} of {@link ResourcesDataService}
     */
    public void unregisterReceiver(Activity activity) {
        activity.unregisterReceiver(receiver);
    }

}
