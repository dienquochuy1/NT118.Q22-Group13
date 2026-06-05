package com.example.myapplication.network;

import com.example.myapplication.data.ApiResponse;
import com.example.myapplication.data.article.ArticleActionState;
import com.example.myapplication.data.article.ArticleDto;
import com.example.myapplication.data.article.BookmarkCountData;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface ArticleActionApi {
    @POST("articles/{id}/like")
    Call<ApiResponse<ArticleActionState>> toggleLike(
            @Header("Authorization") String authorization,
            @Path("id") int articleId
    );

    @POST("articles/{id}/bookmark")
    Call<ApiResponse<ArticleActionState>> toggleBookmark(
            @Header("Authorization") String authorization,
            @Path("id") int articleId
    );

    @GET("me/bookmarks")
    Call<ApiResponse<List<ArticleDto>>> getBookmarks(
            @Header("Authorization") String authorization
    );

    @GET("me/bookmarks/count")
    Call<ApiResponse<BookmarkCountData>> getBookmarkCount(
            @Header("Authorization") String authorization
    );
}
