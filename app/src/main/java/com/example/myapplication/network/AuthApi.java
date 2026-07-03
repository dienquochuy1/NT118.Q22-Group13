package com.example.myapplication.network;

import com.example.myapplication.data.ApiResponse;
import com.example.myapplication.data.auth.AuthData;
import com.example.myapplication.data.auth.GoogleLoginRequest;
import com.example.myapplication.data.auth.LoginRequest;
import com.example.myapplication.data.auth.RegisterRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface AuthApi {
    @POST("auth/login")
    Call<ApiResponse<AuthData>> login(@Body LoginRequest request);

    @POST("auth/register")
    Call<ApiResponse<AuthData>> register(@Body RegisterRequest request);

    @POST("auth/logout")
    Call<ApiResponse<Object>> logout(@Header("Authorization") String authorization);

    @POST("auth/google")
    Call<ApiResponse<AuthData>> loginWithGoogle(@Body GoogleLoginRequest request);
}

