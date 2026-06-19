package com.example.myapplication.auth;

import com.example.myapplication.data.auth.AuthData;
import com.example.myapplication.data.auth.UserDto;

public final class GoogleLoginProfile {
    private static final String DEFAULT_USERNAME = "Nguoi dung";
    private static final long FIREBASE_SESSION_SECONDS = 3600L;

    private GoogleLoginProfile() {
    }

    public static String resolveUsername(String displayName, String email) {
        if (displayName != null && !displayName.trim().isEmpty()) {
            return displayName.trim();
        }

        if (email != null && !email.trim().isEmpty()) {
            return email.trim();
        }

        return DEFAULT_USERNAME;
    }

    public static AuthData toAuthData(String uid, String displayName, String email, String idToken) {
        String username = resolveUsername(displayName, email);
        UserDto user = new UserDto(
                uid,
                username,
                username,
                email,
                "user"
        );

        return new AuthData(
                idToken,
                "Bearer",
                FIREBASE_SESSION_SECONDS,
                null,
                user
        );
    }
}
