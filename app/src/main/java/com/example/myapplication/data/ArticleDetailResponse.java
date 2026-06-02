package com.example.myapplication.data;

import com.google.gson.annotations.SerializedName;

public class ArticleDetailResponse {
    @SerializedName("success")
    private boolean success;

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private ArticleDetailData data;

    // Getters and Setters
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public ArticleDetailData getData() { return data; }
    public void setData(ArticleDetailData data) { this.data = data; }
}