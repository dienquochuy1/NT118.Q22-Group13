package com.example.myapplication.data;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ArticleDetailData {
    @SerializedName("id")
    private int id;

    @SerializedName("title")
    private String title;

    @SerializedName("slug")
    private String slug;

    @SerializedName("summary_text")
    private String summaryText; // Chứa nội dung Tóm tắt bằng AI

    @SerializedName("thumbnail_url")
    private String thumbnailUrl;

    @SerializedName("source")
    private String source;

    @SerializedName("content")
    private String content; // Nội dung bài báo chính đầy đủ

    @SerializedName("published_at")
    private String publishedAt;

    @SerializedName("stats")
    private ArticleStats stats;

    @SerializedName("related")
    private List<RelatedArticleDto> related; // Danh sách bài viết liên quan dưới đáy màn hình

    // Getters and Setters
    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getSlug() { return slug; }
    public String getSummaryText() { return summaryText; }
    public String getThumbnailUrl() { return thumbnailUrl; }
    public String getSource() { return source; }
    public String getContent() { return content; }
    public String getPublishedAt() { return publishedAt; }
    public ArticleStats getStats() { return stats; }
    public List<RelatedArticleDto> getRelated() { return related; }
}