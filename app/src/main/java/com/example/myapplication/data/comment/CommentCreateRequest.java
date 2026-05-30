package com.example.myapplication.data.comment;

import com.google.gson.annotations.SerializedName;

public class CommentCreateRequest {
    @SerializedName("article_id")
    private final int articleId;

    @SerializedName("content")
    private final String content;

    public CommentCreateRequest(int articleId, String content) {
        this.articleId = articleId;
        this.content = content;
    }
}
