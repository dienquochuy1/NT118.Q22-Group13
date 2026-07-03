package com.example.myapplication.auth;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.example.myapplication.data.ApiError;
import com.example.myapplication.data.ApiResponse;
import com.example.myapplication.data.ValidationDetail;
import com.example.myapplication.data.auth.AuthData;
import com.example.myapplication.data.auth.LoginRequest;
import com.example.myapplication.data.auth.RegisterRequest;
import com.example.myapplication.network.ApiClient;
import com.example.myapplication.network.AuthApi;
import com.example.myapplication.network.MockConfig;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthRepository {
    private final AuthApi authApi;
    private final SessionStore sessionStore;
    private final Handler mainHandler;
    private final Gson gson;

    public AuthRepository(Context context) {
        this.authApi = ApiClient.getAuthApi();
        this.sessionStore = new SessionStore(context);
        this.mainHandler = new Handler(Looper.getMainLooper());
        this.gson = new Gson();
    }

    public SessionStore getSessionStore() {
        return sessionStore;
    }

    public void login(String email, String password, AuthCallback<AuthData> callback) {
        authApi.login(new LoginRequest(email, password))
                .enqueue(new Callback<ApiResponse<AuthData>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<AuthData>> call, Response<ApiResponse<AuthData>> response) {
                        handleAuthResponse(response, callback, true);
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<AuthData>> call, Throwable t) {
                        callback.onError(new AuthErrorInfo("NETWORK_ERROR", t.getMessage(), null));
                    }
                });
    }

    public void register(String username, String email, String password, AuthCallback<AuthData> callback) {
        authApi.register(new RegisterRequest(username, email, password))
                .enqueue(new Callback<ApiResponse<AuthData>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<AuthData>> call, Response<ApiResponse<AuthData>> response) {
                        handleAuthResponse(response, callback, true);
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<AuthData>> call, Throwable t) {
                        callback.onError(new AuthErrorInfo("NETWORK_ERROR", t.getMessage(), null));
                    }
                });
    }

    public void logout(AuthCallback<Object> callback) {
        String token = sessionStore.getAccessToken();
        String tokenType = sessionStore.getTokenType();
        String authorization = tokenType + " " + token;

        authApi.logout(authorization)
                .enqueue(new Callback<ApiResponse<Object>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<Object>> call, Response<ApiResponse<Object>> response) {
                        handleLogoutResponse(response, callback);
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<Object>> call, Throwable t) {
                        callback.onError(new AuthErrorInfo("NETWORK_ERROR", t.getMessage(), null));
                    }
                });
    }

    private void handleAuthResponse(Response<ApiResponse<AuthData>> response, AuthCallback<AuthData> callback, boolean saveSession) {
        ApiResponse<AuthData> body = response.body();
        if (response.isSuccessful() && body != null && body.isSuccess()) {
            if (saveSession) {
                sessionStore.saveSession(body.getData());
            }
            callback.onSuccess(body.getData());
            return;
        }

        ApiResponse<?> errorBody = parseError(response.errorBody());
        String message = body != null ? body.getMessage() : null;
        ApiError apiError = body != null ? body.getError() : null;

        if (errorBody != null) {
            message = errorBody.getMessage();
            apiError = errorBody.getError();
        }

        callback.onError(toAuthError(message, apiError));
    }

    private void handleLogoutResponse(Response<ApiResponse<Object>> response, AuthCallback<Object> callback) {
        ApiResponse<Object> body = response.body();
        if (response.isSuccessful() && body != null && body.isSuccess() || response.code() == 401) {
            sessionStore.clearSession();
            callback.onSuccess(body.getData());
            return;
        }

        ApiResponse<?> errorBody = parseError(response.errorBody());
        String message = body != null ? body.getMessage() : null;
        ApiError apiError = body != null ? body.getError() : null;

        if (errorBody != null) {
            message = errorBody.getMessage();
            apiError = errorBody.getError();
        }

        callback.onError(toAuthError(message, apiError));
    }

    private ApiResponse<?> parseError(ResponseBody errorBody) {
        if (errorBody == null) {
            return null;
        }
        try {
            String json = errorBody.string();
            return gson.fromJson(json, ApiResponse.class);
        } catch (IOException e) {
            return null;
        }
    }

    private AuthErrorInfo toAuthError(String message, ApiError apiError) {
        String code = apiError != null ? apiError.getCode() : "UNKNOWN";
        List<String> fieldErrors = new ArrayList<>();
        if (apiError != null && apiError.getDetails() != null) {
            for (ValidationDetail detail : apiError.getDetails()) {
                if (detail != null && detail.getMessage() != null) {
                    fieldErrors.add(detail.getMessage());
                }
            }
        }
        String safeMessage = message != null ? message : "Yeu cau that bai.";
        return new AuthErrorInfo(code, safeMessage, fieldErrors);
    }
    public void loginWithGoogle(String idToken, AuthCallback<AuthData> callback) {
        com.example.myapplication.data.auth.GoogleLoginRequest request =
                new com.example.myapplication.data.auth.GoogleLoginRequest(idToken);

        authApi.loginWithGoogle(request)
                .enqueue(new Callback<ApiResponse<AuthData>>() {
                    @Override
                    public void onResponse(Call<ApiResponse<AuthData>> call, Response<ApiResponse<AuthData>> response) {
                        // Tận dụng hàm handleAuthResponse có sẵn để tự động saveSession(Laravel Token)
                        handleAuthResponse(response, callback, true);
                    }

                    @Override
                    public void onFailure(Call<ApiResponse<AuthData>> call, Throwable t) {
                        callback.onError(new AuthErrorInfo("NETWORK_ERROR", t.getMessage(), null));
                    }
                });
    }
}

