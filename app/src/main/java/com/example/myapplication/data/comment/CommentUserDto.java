package com.example.myapplication.data.comment;

import com.google.gson.annotations.SerializedName;

public class CommentUserDto {
    @SerializedName("id")
    private String id;

    @SerializedName("name")
    private String name;

    @SerializedName("avatar")
    private String avatar;

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getAvatar() {
        return avatar;
    }
}
