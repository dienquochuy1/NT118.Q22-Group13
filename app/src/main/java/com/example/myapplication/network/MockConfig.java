package com.example.myapplication.network;

import com.example.myapplication.BuildConfig;

public class MockConfig {
    // Default to false so the app uses the real backend by default during testing.
    // You can enable mocks for offline/dev testing by setting this to true or
    // calling MockConfig.setMockEnabled(true) at runtime.
    private static boolean mockEnabled = false;

    private MockConfig() {
    }

    public static boolean isEnabled() {
        return BuildConfig.DEBUG && mockEnabled;
    }

    public static void setMockEnabled(boolean enabled) {
        mockEnabled = enabled;
    }
}
