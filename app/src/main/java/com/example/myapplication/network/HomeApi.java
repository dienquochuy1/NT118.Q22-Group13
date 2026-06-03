package com.example.myapplication.network;

import com.example.myapplication.data.home.HomeResponse;
import retrofit2.Call;
import retrofit2.http.GET;

public interface HomeApi {
    @GET("home") // Đường dẫn endpoint trỏ tới API xử lý trang chủ của Backend
    Call<HomeResponse> getHome();
}