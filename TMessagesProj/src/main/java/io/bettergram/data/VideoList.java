package io.bettergram.data;

import com.instagram.common.json.annotation.JsonField;
import com.instagram.common.json.annotation.JsonType;

import java.util.Collections;
import java.util.List;

@JsonType
public class VideoList {

    @JsonField(fieldName = "videos")
    public List<Video> videos;

    /**
     * Sorts article list by published date
     *
     * @return
     */
    public void sortVideosByDate() {
        Collections.sort(videos, (o1, o2) -> (o2.getPublishedAt() == null || o1.getPublishedAt() == null) ? 0 : o2.getPublishedAt().compareTo(o1.getPublishedAt()));
    }

    public boolean contains(String videoId) {
        for (Video item : videos) {
            if (item.id.equals(videoId)) {
                return true;
            }
        }
        return false;
    }
}
