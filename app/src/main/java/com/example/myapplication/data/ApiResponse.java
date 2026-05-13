package com.example.myapplication.data;

import com.google.gson.annotations.SerializedName;

public class ApiResponse<T> {
    @SerializedName("success")
    private boolean success;

    @SerializedName("message")
    private String message;

    @SerializedName("data")
    private T data;

    @SerializedName("meta")
    private Meta meta;

    @SerializedName("error")
    private ApiError error;

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public T getData() {
        return data;
    }

    public Meta getMeta() {
        return meta;
    }

    public ApiError getError() {
        return error;
    }
}

