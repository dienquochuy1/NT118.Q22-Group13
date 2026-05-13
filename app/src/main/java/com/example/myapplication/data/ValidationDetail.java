package com.example.myapplication.data;

import com.google.gson.annotations.SerializedName;

public class ValidationDetail {
    @SerializedName("field")
    private String field;

    @SerializedName("message")
    private String message;

    public String getField() {
        return field;
    }

    public String getMessage() {
        return message;
    }
}

