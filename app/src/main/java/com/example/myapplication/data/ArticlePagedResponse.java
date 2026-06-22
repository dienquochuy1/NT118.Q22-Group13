package com.example.myapplication.data;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ArticlePagedResponse {

    @SerializedName("data")
    private List<ArticleItemDto> items;

    @SerializedName("pagination")
    private PaginationDto pagination;

    // Getters
    public List<ArticleItemDto> getItems() { return items; }
    public PaginationDto getPagination() { return pagination; }

    // Setters
    public void setItems(List<ArticleItemDto> items) { this.items = items; }
    public void setPagination(PaginationDto pagination) { this.pagination = pagination; }

    /**
     * Lớp DTO đại diện cho từng bài viết rút gọn truyền về từ API phân trang
     */
    public static class ArticleItemDto {
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

        @SerializedName("category")
        private String category;

        @SerializedName("published_at")
        private String publishedAt;

        // Getters
        public int getId() { return id; }
        public String getTitle() { return title; }
        public String getSlug() { return slug; }
        public String getThumbnailUrl() { return thumbnailUrl; }
        public String getSource() { return source; }
        public String getCategory() { return category; }
        public String getPublishedAt() { return publishedAt; }

        // Setters
        public void setId(int id) { this.id = id; }
        public void setTitle(String title) { this.title = title; }
        public void setSlug(String slug) { this.slug = slug; }
        public void setThumbnailUrl(String thumbnailUrl) { this.thumbnailUrl = thumbnailUrl; }
        public void setSource(String source) { this.source = source; }
        public void setCategory(String category) { this.category = category; }
        public void setPublishedAt(String publishedAt) { this.publishedAt = publishedAt; }
    }
}