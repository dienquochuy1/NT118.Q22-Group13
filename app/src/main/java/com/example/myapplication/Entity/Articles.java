package com.example.myapplication.Entity;

public class Articles {
    private String id;
    private String title;
    private String summary;
    private String content;
    private String category;
    private String source;
    private String author;
    private String imageUrl;
    private String publishDate;
    private long timestamp;
    private boolean bookmarked;

    public Articles() {}

    public Articles(String id, String title, String summary, String content,
                    String category, String source, String author,
                    String imageUrl, String publishDate, long timestamp) {
        this.id = id;
        this.title = title;
        this.summary = summary;
        this.content = content;
        this.category = category;
        this.source = source;
        this.author = author;
        this.imageUrl = imageUrl;
        this.publishDate = publishDate;
        this.timestamp = timestamp;
        this.bookmarked = false;
    }

    // Getters
    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getSummary() { return summary; }
    public String getContent() { return content; }
    public String getCategory() { return category; }
    public String getSource() { return source; }
    public String getAuthor() { return author; }
    public String getImageUrl() { return imageUrl; }
    public String getPublishDate() { return publishDate; }
    public long getTimestamp() { return timestamp; }
    public boolean isBookmarked() { return bookmarked; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setSummary(String summary) { this.summary = summary; }
    public void setContent(String content) { this.content = content; }
    public void setCategory(String category) { this.category = category; }
    public void setSource(String source) { this.source = source; }
    public void setAuthor(String author) { this.author = author; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setPublishDate(String publishDate) { this.publishDate = publishDate; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public void setBookmarked(boolean bookmarked) { this.bookmarked = bookmarked; }
}
