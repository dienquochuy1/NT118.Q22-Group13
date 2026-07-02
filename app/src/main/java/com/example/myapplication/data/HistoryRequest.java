package com.example.myapplication.data;

import com.google.gson.annotations.SerializedName;

public class HistoryRequest {
    @SerializedName("article_id")
    private final int articleId;

    public HistoryRequest(int articleId) {
        this.articleId = articleId;
    }
}