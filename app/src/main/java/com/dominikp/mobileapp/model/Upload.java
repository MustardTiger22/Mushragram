package com.dominikp.mobileapp.model;

import com.google.firebase.database.Exclude;
import java.util.Map;

import lombok.Data;

@Data
public class Upload {
    private String userId;
    private String author;
    private String title;
    private String imageUrl;
    private String key;
    private Integer likeCounter;
    private UploadLocation location;
    private Map<String, Boolean> likes;

    public Upload() {
    }

    public Upload(String userId, String title, String author, String imageUrl, Integer likeCounter,  Map<String, Boolean> likes) {
        if(title.trim().equals("")) {
            title = "Bez tytułu";
        }

        this.userId = userId;
        this.title = title;
        this.imageUrl = imageUrl;
        this.author = author;
        this.likeCounter = likeCounter;
        this.likes = likes;
    }

    public void incrementLikeCounter() {
        this.likeCounter++;
    }

    public void decrementLikeCounter() {
        this.likeCounter--;
    }

    public Integer getLikeCounter() {
        return likeCounter;
    }

    @Exclude
    public String getKey() {
        return key;
    }

    @Exclude
    public void setKey(String mKey) {
        this.key = mKey;
    }
}
