package com.example.myapplication.network;

import com.example.myapplication.data.ApiResponse;
import com.example.myapplication.data.article.ArticleDto;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ArticleApi {
    @GET("articles")
    Call<ApiResponse<List<ArticleDto>>> getArticles(
            @Query("page") int page,
            @Query("per_page") int perPage
    );

    @GET("articles/{id}")
    Call<ApiResponse<ArticleDto>> getArticle(@Path("id") int articleId);
}
