package com.example.myapplication.data;

import com.google.gson.annotations.SerializedName;

public class Meta {
    @SerializedName("request_id")
    private String requestId;

    @SerializedName("timestamp")
    private String timestamp;

    public String getRequestId() {
        return requestId;
    }

    public String getTimestamp() {
        return timestamp;
    }
}

