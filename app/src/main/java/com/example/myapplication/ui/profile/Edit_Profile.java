package com.example.myapplication.ui.profile;

import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.databinding.ActivityEditProfileBinding;
import com.example.myapplication.network.ApiClient;
import com.example.myapplication.data.ApiResponse;
import com.example.myapplication.data.auth.UserDto;
import com.example.myapplication.auth.SessionStore;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.PUT;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class Edit_Profile extends AppCompatActivity {

    private ActivityEditProfileBinding binding;
    private SessionStore sessionStore;
    private Uri imageUri;
    private String currentUserId;

    private final ActivityResultLauncher<String> getContent = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    imageUri = uri;
                    binding.ivEditAvatar.setImageURI(uri);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityEditProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Use backend session store to obtain current user info/token
        sessionStore = new SessionStore(this);
        if (!sessionStore.isLoggedIn()) {
            finish();
            return;
        }

        currentUserId = sessionStore.getUserId();

        loadUserData();

        binding.btnBack.setOnClickListener(v -> finish());

        binding.btnChangeAvatar.setOnClickListener(v -> getContent.launch("image/*"));

        binding.btnSaveProfile.setOnClickListener(v -> saveChanges());
    }

    private void loadUserData() {
        Retrofit retrofit = ApiClient.getRetrofit();
        UserApi userApi = retrofit.create(UserApi.class);
        String authorization = sessionStore.getTokenType() + " " + sessionStore.getAccessToken();
        userApi.getMe(authorization).enqueue(new Callback<ApiResponse<UserDto>>() {
            @Override
            public void onResponse(Call<ApiResponse<UserDto>> call, Response<ApiResponse<UserDto>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    UserDto user = response.body().getData();
                    if (user != null) {
                        binding.etEditUsername.setText(user.getUsername() != null ? user.getUsername() : user.getName());
                        // backend must provide 'bio' and 'avatar' fields inside user DTO if needed; fallback to empty
                        try {
                            java.lang.reflect.Method m = user.getClass().getMethod("getBio");
                            Object bioObj = m.invoke(user);
                            String bio = bioObj != null ? bioObj.toString() : "";
                            binding.etEditBio.setText(bio);
                        } catch (Exception ignored) {
                            binding.etEditBio.setText("");
                        }

                        try {
                            java.lang.reflect.Method m2 = user.getClass().getMethod("getAvatar");
                            Object avatarObj = m2.invoke(user);
                            String avatar = avatarObj != null ? avatarObj.toString() : "";
                            if (avatar != null && !avatar.isEmpty()) {
                                loadAvatarImage(avatar);
                            }
                        } catch (Exception ignored) {
                            // ignore if fields not present in DTO
                        }
                    }
                } else {
                    Toast.makeText(Edit_Profile.this, "Không thể tải dữ liệu", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<UserDto>> call, Throwable t) {
                Toast.makeText(Edit_Profile.this, "Không thể tải dữ liệu: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveChanges() {
        String newUsername = binding.etEditUsername.getText().toString().trim();
        String newBio = binding.etEditBio.getText().toString().trim();

        if (newUsername.isEmpty()) {
            binding.tilUsername.setError("Tên không được để trống");
            return;
        }

        binding.btnSaveProfile.setEnabled(false);
        binding.btnSaveProfile.setText("ĐANG LƯU...");

        if (imageUri != null) {
            uploadAvatar(newUsername, newBio);
        } else {
            updateProfile(newUsername, newBio, null);
        }
    }

    private void uploadAvatar(String username, String bio) {
        // Thay vì upload lên Storage, chúng ta chuyển ảnh sang Base64 để lưu vào Firestore
        String base64Image = uriToBase64(imageUri);
        if (base64Image != null) {
            updateProfile(username, bio, base64Image);
        } else {
            binding.btnSaveProfile.setEnabled(true);
            binding.btnSaveProfile.setText("LƯU THAY ĐỔI");
            Toast.makeText(this, "Lỗi xử lý ảnh", Toast.LENGTH_SHORT).show();
        }
    }

    private String uriToBase64(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            
            // 1. Thực hiện Center Crop để đưa ảnh về hình vuông
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            int dimension = Math.min(width, height); // Lấy cạnh nhỏ hơn làm chuẩn

            // Tính toán vị trí bắt đầu cắt để lấy phần chính giữa
            int x = (width - dimension) / 2;
            int y = (height - dimension) / 2;
            
            Bitmap croppedBitmap = Bitmap.createBitmap(bitmap, x, y, dimension, dimension);

            // 2. Nén ảnh xuống kích thước nhỏ (200x200) để tối ưu bộ nhớ
            int maxSize = 200;
            Bitmap finalBitmap = Bitmap.createScaledBitmap(croppedBitmap, maxSize, maxSize, true);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream);
            byte[] byteArray = outputStream.toByteArray();
            return Base64.encodeToString(byteArray, Base64.DEFAULT);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void loadAvatarImage(String avatarData) {
        if (avatarData.startsWith("http")) {
            // Nếu là URL (ảnh cũ)
            Glide.with(this).load(avatarData).into(binding.ivEditAvatar);
        } else {
            // Nếu là chuỗi Base64
            try {
                byte[] decodedString = Base64.decode(avatarData, Base64.DEFAULT);
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                binding.ivEditAvatar.setImageBitmap(decodedByte);
            } catch (Exception e) {
                binding.ivEditAvatar.setImageResource(R.drawable.user);
            }
        }
    }

    private void updateProfile(String username, String bio, String avatarUrl) {
        Retrofit retrofit = ApiClient.getRetrofit();
        UserApi userApi = retrofit.create(UserApi.class);
        String authorization = sessionStore.getTokenType() + " " + sessionStore.getAccessToken();

        Map<String, Object> updates = new HashMap<>();
        updates.put("username", username);
        updates.put("bio", bio);
        if (avatarUrl != null) {
            updates.put("avatar", avatarUrl);
        }

        userApi.updateMe(authorization, updates).enqueue(new Callback<ApiResponse<UserDto>>() {
            @Override
            public void onResponse(Call<ApiResponse<UserDto>> call, Response<ApiResponse<UserDto>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    // Cập nhật local SharedPreferences (UserPrefs)
                    android.content.SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs",
                            android.content.Context.MODE_PRIVATE);
                    android.content.SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("username", username);
                    editor.apply();

                    // Also update the centralized Auth session store so UI reading from SessionStore
                    // (Home_user and other screens) reflect the new username immediately.
                    try {
                        if (sessionStore != null) {
                            sessionStore.updateUserUsername(username);
                            sessionStore.updateUserName(username);
                        }
                    } catch (Exception ignored) {
                    }

                    Toast.makeText(Edit_Profile.this, "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    binding.btnSaveProfile.setEnabled(true);
                    binding.btnSaveProfile.setText("LƯU THAY ĐỔI");
                    Toast.makeText(Edit_Profile.this, "Lỗi cập nhật profile", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<UserDto>> call, Throwable t) {
                binding.btnSaveProfile.setEnabled(true);
                binding.btnSaveProfile.setText("LƯU THAY ĐỔI");
                Toast.makeText(Edit_Profile.this, "Lỗi cập nhật profile: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Define a small Retrofit interface locally to avoid creating new files
    interface UserApi {
        @GET("me")
        Call<ApiResponse<UserDto>> getMe(@Header("Authorization") String authorization);

        @PUT("me")
        Call<ApiResponse<UserDto>> updateMe(@Header("Authorization") String authorization, @Body Map<String, Object> body);
    }
}
