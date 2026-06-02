package com.example.myapplication.data.home;

import com.google.gson.annotations.SerializedName;

public class HomeArticle {
    @SerializedName("id")
    private int id; // ID số nguyên gốc từ database chính (Aiven Cloud)

    @SerializedName("title")
    private String title;

    @SerializedName("source")
    private String source;

    @SerializedName("time")
    private String time;

    @SerializedName("thumbnail")
    private String thumbnail;

    // Getters và Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public String getThumbnail() { return thumbnail; }
    public void setThumbnail(String thumbnail) { this.thumbnail = thumbnail; }
}