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
        if (MockConfig.isEnabled()) {
            mockLogin(email, callback);
            return;
        }

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
        if (MockConfig.isEnabled()) {
            mockRegister(username, email, callback);
            return;
        }

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
        if (MockConfig.isEnabled()) {
            mockLogout(callback);
            return;
        }

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
        if (response.isSuccessful() && body != null && body.isSuccess()) {
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

    private void mockLogin(String email, AuthCallback<AuthData> callback) {
        mainHandler.postDelayed(() -> {
            if (email != null && email.contains("invalid")) {
                callback.onError(new AuthErrorInfo("AUTH_INVALID_CREDENTIALS", "Invalid credentials.", null));
                return;
            }
            AuthData data = MockDataFactory.createAuthData(email, "user");
            sessionStore.saveSession(data);
            callback.onSuccess(data);
        }, 350);
    }

    private void mockRegister(String username, String email, AuthCallback<AuthData> callback) {
        mainHandler.postDelayed(() -> {
            if (email != null && email.contains("taken")) {
                callback.onError(new AuthErrorInfo("AUTH_EMAIL_TAKEN", "Email already exists.", null));
                return;
            }
            if (username != null && username.contains("taken")) {
                callback.onError(new AuthErrorInfo("AUTH_USERNAME_TAKEN", "Username already exists.", null));
                return;
            }
            AuthData data = MockDataFactory.createAuthData(email, username);
            sessionStore.saveSession(data);
            callback.onSuccess(data);
        }, 350);
    }

    private void mockLogout(AuthCallback<Object> callback) {
        mainHandler.postDelayed(() -> {
            sessionStore.clearSession();
            callback.onSuccess(null);
        }, 200);
    }

    private static class MockDataFactory {
        private static AuthData createAuthData(String email, String username) {
            com.example.myapplication.data.auth.UserDto user = new com.example.myapplication.data.auth.UserDto(
                    "mock-user-1",
                    username != null ? username : "Nguoi dung",
                    username != null ? username : "user",
                    email != null ? email : "user@example.com",
                    "user"
            );

            return new AuthData(
                    "mock_access_token",
                    "Bearer",
                    3600L,
                    "mock_refresh_token",
                    user
            );
        }
    }
}

