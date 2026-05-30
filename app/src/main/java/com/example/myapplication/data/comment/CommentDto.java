package com.example.myapplication.data.comment;

import com.google.gson.annotations.SerializedName;

public class CommentDto {
    @SerializedName("id")
    private String id;

    @SerializedName("article_id")
    private String articleId;

    @SerializedName("user")
    private CommentUserDto user;

    @SerializedName("content")
    private String content;

    @SerializedName("status")
    private String status;

    @SerializedName("created_at")
    private String createdAt;

    public String getId() {
        return id;
    }

    public String getArticleId() {
        return articleId;
    }

    public CommentUserDto getUser() {
        return user;
    }

    public String getContent() {
        return content;
    }

    public String getStatus() {
        return status;
    }

    public String getCreatedAt() {
        return createdAt;
    }
}
