package com.example.myapplication.data.article;

import com.google.gson.annotations.SerializedName;

public class BookmarkCountData {
    @SerializedName("count")
    private int count;

    public int getCount() {
        return count;
    }
}
