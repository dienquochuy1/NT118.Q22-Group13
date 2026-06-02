package com.example.myapplication.data;

import com.google.gson.annotations.SerializedName;

public class ArticleStats {
    @SerializedName("comments_count")
    private int commentsCount;

    @SerializedName("likes")
    private int likes;

    @SerializedName("bookmarks")
    private int bookmarks;

    // Getters and Setters
    public int getCommentsCount() { return commentsCount; }
    public void setCommentsCount(int commentsCount) { this.commentsCount = commentsCount; }

    public int getLikes() { return likes; }
    public void setLikes(int likes) { this.likes = likes; }

    public int getBookmarks() { return bookmarks; }
    public void setBookmarks(int bookmarks) { this.bookmarks = bookmarks; }
}