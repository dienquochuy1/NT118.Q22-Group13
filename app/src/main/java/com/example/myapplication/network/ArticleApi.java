package com.example.myapplication.network;

import com.example.myapplication.data.ApiResponse;
import com.example.myapplication.data.ArticleDetailResponse;
import com.example.myapplication.data.ArticlePagedResponse;
import com.example.myapplication.data.HistoryRequest;
import com.example.myapplication.data.article.ArticleDto;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ArticleApi {
    // Truyền động ID của bài viết lên cuối đường dẫn REST API của Backend
    @GET("articles/{id}")
    Call<ArticleDetailResponse> getArticleDetail(
            @Header("Authorization") String authorization,
            @Path("id") int id
    );

    @GET("articles")
    Call<ArticlePagedResponse> getArticlesByCategory(
            @Header("Authorization") String authorization,
            @Query("category") String category,
            @Query("page") int page,
            @Query("per_page") int perPage
    );

    @POST("history")
    Call<Void> storeReadingHistory(
            @Header("Authorization") String authorization,
            @Body HistoryRequest body
    );

    @GET("history")
    Call<ApiResponse<List<ArticleDto>>> getReadingHistory(
            @Header("Authorization") String authorization
    );
}