package com.example.myapplication.auth;

public interface AuthCallback<T> {
    void onSuccess(T data);
    void onError(AuthErrorInfo error);
}

