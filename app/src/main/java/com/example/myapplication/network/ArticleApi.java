package com.example.myapplication.network;

import com.example.myapplication.data.ArticleDetailResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface ArticleApi {
    // Truyền động ID của bài viết lên cuối đường dẫn REST API của Backend
    @GET("articles/{id}")
    Call<ArticleDetailResponse> getArticleDetail(@Path("id") int id);
}