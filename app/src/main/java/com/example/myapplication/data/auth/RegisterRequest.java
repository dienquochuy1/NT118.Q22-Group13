package com.example.myapplication.data.auth;

import com.google.gson.annotations.SerializedName;

public class RegisterRequest {
    @SerializedName("username")
    private final String username;

    @SerializedName("email")
    private final String email;

    @SerializedName("password")
    private final String password;

    public RegisterRequest(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
    }
}

