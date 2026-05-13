package com.example.myapplication.network;

import com.example.myapplication.BuildConfig;

public class MockConfig {
    private static boolean mockEnabled = true;

    private MockConfig() {
    }

    public static boolean isEnabled() {
        return BuildConfig.DEBUG && mockEnabled;
    }

    public static void setMockEnabled(boolean enabled) {
        mockEnabled = enabled;
    }
}
