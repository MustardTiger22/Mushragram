package com.dominikp.mobileapp;

public class Upload {
    private String mUserId;
    private String mTitle;
    private String mImageUrl;

    public Upload() {
    }

    public Upload(String userId, String title, String imageUrl) {
        if(title.trim().equals("")) {
            title = "Bez tytułu";
        }

        this.mUserId = userId;
        this.mTitle = title;
        this.mImageUrl = imageUrl;
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
}
