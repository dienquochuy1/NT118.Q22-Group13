package com.example.myapplication.network;

import com.example.myapplication.BuildConfig;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    private static Retrofit retrofit;

    public interface OnTokenExpiredListener {
        void onTokenExpired();
    }

    private static OnTokenExpiredListener tokenExpiredListener;

    public static void setOnTokenExpiredListener(OnTokenExpiredListener listener) {
        tokenExpiredListener = listener;
    }
    private ApiClient() {
    }

    public static Retrofit getRetrofit() {
        if (retrofit == null) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(BuildConfig.DEBUG ? HttpLoggingInterceptor.Level.BODY : HttpLoggingInterceptor.Level.NONE);

            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                    .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                    .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                    .addInterceptor(chain -> {
                        Request request = chain.request();
                        Response response = chain.proceed(request);

                        if (response.code() == 401) {
                            if (tokenExpiredListener != null) {
                                okhttp3.ResponseBody responseBody = response.peekBody(Long.MAX_VALUE);
                                String jsonResponse = responseBody.string();

                                if (jsonResponse.contains("AUTH_INVALID_CREDENTIALS") || jsonResponse.contains("Invalid credentials")) {
                                    return response; // Trả về cho Login fragment tự xử lý hiển thị lỗi
                                }

                                if (tokenExpiredListener != null) {
                                    tokenExpiredListener.onTokenExpired();
                                }
                            }
                        }
                        return response;
                    })
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BuildConfig.API_BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client)
                    .build();
        }
        return retrofit;
    }

    public static AuthApi getAuthApi() {
        return getRetrofit().create(AuthApi.class);
    }

    public static ArticleApi getArticleApi() {
        return getRetrofit().create(ArticleApi.class);
    }

    public static CommentApi getCommentApi() {
        return getRetrofit().create(CommentApi.class);
    }

    public static ArticleActionApi getArticleActionApi() {
        return getRetrofit().create(ArticleActionApi.class);
    }

    public static HomeApi getHomeApi() {
        return getRetrofit().create(HomeApi.class);
    }
}

