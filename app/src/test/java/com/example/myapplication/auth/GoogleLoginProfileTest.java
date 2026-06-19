package com.example.myapplication.auth;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class GoogleLoginProfileTest {
    @Test
    public void displayNameWinsOverEmail() {
        assertEquals("An Nguyen", GoogleLoginProfile.resolveUsername("An Nguyen", "an@example.com"));
    }

    @Test
    public void emailIsUsedWhenDisplayNameIsBlank() {
        assertEquals("an@example.com", GoogleLoginProfile.resolveUsername("  ", "an@example.com"));
    }

    @Test
    public void defaultNameIsUsedWhenProfileIsEmpty() {
        assertEquals("Nguoi dung", GoogleLoginProfile.resolveUsername(null, null));
    }

    @Test
    public void firebaseProfileCreatesAuthDataForSessionStore() {
        com.example.myapplication.data.auth.AuthData data = GoogleLoginProfile.toAuthData(
                "firebase-uid",
                "An Nguyen",
                "an@example.com",
                "firebase-id-token"
        );

        assertEquals("firebase-id-token", data.getAccessToken());
        assertEquals("Bearer", data.getTokenType());
        assertEquals(3600L, data.getExpiresIn());
        assertNotNull(data.getUser());
        assertEquals("firebase-uid", data.getUser().getId());
        assertEquals("An Nguyen", data.getUser().getName());
        assertEquals("An Nguyen", data.getUser().getUsername());
        assertEquals("an@example.com", data.getUser().getEmail());
        assertEquals("user", data.getUser().getRole());
    }
}
