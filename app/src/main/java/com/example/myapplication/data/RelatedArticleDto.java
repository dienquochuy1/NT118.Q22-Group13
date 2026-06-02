package com.example.myapplication.data;

import com.google.gson.annotations.SerializedName;

public class RelatedArticleDto {
    @SerializedName("id")
    private int id;

    @SerializedName("title")
    private String title;

    @SerializedName("slug")
    private String slug;

    @SerializedName("thumbnail_url")
    private String thumbnailUrl;

    @SerializedName("source")
    private String source;

    @SerializedName("content")
    private String content;

    @SerializedName("published_at")
    private String publishedAt;

    @SerializedName("stats")
    private ArticleStats stats;

    // Getters and Setters
    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getSlug() { return slug; }
    public String getThumbnailUrl() { return thumbnailUrl; }
    public String getSource() { return source; }
    public String getContent() { return content; }
    public String getPublishedAt() { return publishedAt; }
    public ArticleStats getStats() { return stats; }
}