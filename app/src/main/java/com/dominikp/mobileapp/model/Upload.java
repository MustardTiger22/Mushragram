package com.dominikp.mobileapp.model;

import com.google.firebase.database.Exclude;
import java.util.Map;

public class Upload {
    private String mUserId;
    private String mAuthor;
    private String mTitle;
    private String mImageUrl;
    private String mKey;
    private Integer mLikeCounter;
    private Map<String, Boolean> likes;

    public Upload() {
    }

    public Upload(String userId, String title, String author, String imageUrl, Integer likeCounter,  Map<String, Boolean> likes) {
        if(title.trim().equals("")) {
            title = "Bez tytułu";
        }

        this.mUserId = userId;
        this.mTitle = title;
        this.mImageUrl = imageUrl;
        this.mAuthor = author;
        this.mLikeCounter = likeCounter;
        this.likes = likes;
    }

    public String getUserId() {
        return mUserId;
    }

    public void setUserId(String mUsername) {
        this.mUserId = mUsername;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String mTitle) {
        this.mTitle = mTitle;
    }

    public String getImageUrl() {
        return mImageUrl;
    }

    public void setImageUrl(String mImageUrl) {
        this.mImageUrl = mImageUrl;
    }

    public Map<String, Boolean> getLikes() {
        return likes;
    }

    public void setLikes(Map<String, Boolean> likes) {
        this.likes = likes;
    }

    public String getAuthor() {
        return mAuthor;
    }

    public void setAuthor(String mAuthor) {
        this.mAuthor = mAuthor;
    }


    public void incrementLikeCounter() {
        this.mLikeCounter++;
    }

    public void decrementLikeCounter() {
        this.mLikeCounter--;
    }

    public Integer getLikeCounter() {
        return mLikeCounter;
    }

    public void setLikeCounter(Integer mLikeCounter) {
        this.mLikeCounter = mLikeCounter;
    }

    @Exclude
    public String getKey() {
        return mKey;
    }

    @Exclude
    public void setKey(String mKey) {
        this.mKey = mKey;
    }
}
