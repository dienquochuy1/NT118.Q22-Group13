package com.example.myapplication.data.article;

import com.google.gson.annotations.SerializedName;

public class ArticleDto {
    @SerializedName("id")
    private String id;

    @SerializedName("title")
    private String title;

    @SerializedName("slug")
    private String slug;

    @SerializedName("summary")
    private String summary;

    @SerializedName("summary_text")
    private String summaryText;

    @SerializedName("content")
    private String content;

    @SerializedName("source")
    private String source;

    @SerializedName("time")
    private String time;

    @SerializedName("published_at")
    private String publishedAt;

    @SerializedName("thumbnail")
    private String thumbnail;

    @SerializedName("thumbnail_url")
    private String thumbnailUrl;

    @SerializedName("stats")
    private com.example.myapplication.data.ArticleStats stats;

    public com.example.myapplication.data.ArticleStats getStats() {
        return stats;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getSlug() {
        return slug;
    }

    public String getSummary() {
        return summary != null ? summary : summaryText;
    }

    public String getContent() {
        return content;
    }

    public String getSource() {
        return source;
    }

    public String getTime() {
        return time != null ? time : publishedAt;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl != null ? thumbnailUrl : thumbnail;
    }
}
