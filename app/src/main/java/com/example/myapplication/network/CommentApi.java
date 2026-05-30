package com.example.myapplication.network;

import com.example.myapplication.data.ApiResponse;
import com.example.myapplication.data.comment.CommentCreateRequest;
import com.example.myapplication.data.comment.CommentDto;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface CommentApi {
    @GET("articles/{id}/comments")
    Call<ApiResponse<List<CommentDto>>> getComments(
            @Path("id") int articleId,
            @Query("page") int page,
            @Query("per_page") int perPage
    );

    @POST("comments")
    Call<ApiResponse<CommentDto>> createComment(
            @Header("Authorization") String authorization,
            @Body CommentCreateRequest request
    );
}
