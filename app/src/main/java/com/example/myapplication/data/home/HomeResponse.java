package com.example.myapplication.data.home;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class HomeResponse {
    @SerializedName("latest")
    private List<HomeArticle> latest; // Danh sách bài viết Tiêu điểm

    @SerializedName("trending")
    private List<HomeArticle> trending; // Danh sách bài viết Xu hướng

    @SerializedName("features")
    private List<HomeArticle> features; // Danh sách bài viết Khám phá

    // Getters và Setters
    public List<HomeArticle> getLatest() { return latest; }
    public void setLatest(List<HomeArticle> latest) { this.latest = latest; }

    public List<HomeArticle> getTrending() { return trending; }
    public void setTrending(List<HomeArticle> trending) { this.trending = trending; }

    public List<HomeArticle> getFeatures() { return features; }
    public void setFeatures(List<HomeArticle> features) { this.features = features; }
}