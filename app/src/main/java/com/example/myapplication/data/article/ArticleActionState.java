package com.example.myapplication.data.article;

import com.google.gson.annotations.SerializedName;

public class ArticleActionState {
    @SerializedName("is_liked")
    private Boolean liked;

    @SerializedName("is_bookmarked")
    private Boolean bookmarked;

    public Boolean isLiked() {
        return liked;
    }

    public Boolean isBookmarked() {
        return bookmarked;
    }
}
