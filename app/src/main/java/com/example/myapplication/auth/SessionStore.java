package com.example.myapplication.auth;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.example.myapplication.data.auth.AuthData;
import com.example.myapplication.data.auth.UserDto;

public class SessionStore {
    private static final String PREFS_NAME = "AuthPrefs";
    private static final String KEY_ACCESS_TOKEN = "access_token";
    private static final String KEY_REFRESH_TOKEN = "refresh_token";
    private static final String KEY_TOKEN_TYPE = "token_type";
    private static final String KEY_EXPIRES_AT = "expires_at";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_USERNAME = "user_username";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_USER_ROLE = "user_role";

    private final SharedPreferences prefs;

    public SessionStore(Context context) {
        this.prefs = context.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void saveSession(AuthData data) {
        if (data == null) {
            return;
        }
        long expiresAt = 0L;
        if (data.getExpiresIn() > 0) {
            expiresAt = System.currentTimeMillis() + (data.getExpiresIn() * 1000L);
        }

        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_ACCESS_TOKEN, data.getAccessToken());
        editor.putString(KEY_REFRESH_TOKEN, data.getRefreshToken());
        editor.putString(KEY_TOKEN_TYPE, data.getTokenType());
        editor.putLong(KEY_EXPIRES_AT, expiresAt);

        UserDto user = data.getUser();
        if (user != null) {
            editor.putString(KEY_USER_ID, user.getId());
            editor.putString(KEY_USER_NAME, user.getName());
            editor.putString(KEY_USER_USERNAME, user.getUsername());
            editor.putString(KEY_USER_EMAIL, user.getEmail());
            editor.putString(KEY_USER_ROLE, user.getRole());
        }
        editor.apply();
    }

    public boolean isLoggedIn() {
        return !TextUtils.isEmpty(getAccessToken());
    }

    public String getAccessToken() {
        return prefs.getString(KEY_ACCESS_TOKEN, "");
    }

    public String getRefreshToken() {
        return prefs.getString(KEY_REFRESH_TOKEN, "");
    }

    public String getTokenType() {
        return prefs.getString(KEY_TOKEN_TYPE, "Bearer");
    }

    public long getExpiresAt() {
        return prefs.getLong(KEY_EXPIRES_AT, 0L);
    }

    public String getUserId() {
        return prefs.getString(KEY_USER_ID, "");
    }

    public String getUserName() {
        return prefs.getString(KEY_USER_NAME, "");
    }

    public String getUserUsername() {
        return prefs.getString(KEY_USER_USERNAME, "");
    }

    public String getUserEmail() {
        return prefs.getString(KEY_USER_EMAIL, "");
    }

    public String getUserRole() {
        return prefs.getString(KEY_USER_ROLE, "");
    }

    public void clearSession() {
        prefs.edit().clear().apply();
    }
}

