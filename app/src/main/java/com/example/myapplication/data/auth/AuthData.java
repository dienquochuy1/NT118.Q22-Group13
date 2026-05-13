package com.example.myapplication.data.auth;

import com.google.gson.annotations.SerializedName;

public class AuthData {
    @SerializedName("access_token")
    private String accessToken;

    @SerializedName("token_type")
    private String tokenType;

    @SerializedName("expires_in")
    private long expiresIn;

    @SerializedName("refresh_token")
    private String refreshToken;

    @SerializedName("user")
    private UserDto user;

    public AuthData() {
    }

    public AuthData(String accessToken, String tokenType, long expiresIn, String refreshToken, UserDto user) {
        this.accessToken = accessToken;
        this.tokenType = tokenType;
        this.expiresIn = expiresIn;
        this.refreshToken = refreshToken;
        this.user = user;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public long getExpiresIn() {
        return expiresIn;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public UserDto getUser() {
        return user;
    }
}
