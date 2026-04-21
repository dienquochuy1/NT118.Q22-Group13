package com.example.myapplication;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
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
import com.example.myapplication.databinding.ActivityEditProfileBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class Edit_Profile extends AppCompatActivity {

    private ActivityEditProfileBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
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

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            finish();
            return;
        }

        currentUserId = currentUser.getUid();

        loadUserData();

        binding.btnBack.setOnClickListener(v -> finish());

        binding.btnChangeAvatar.setOnClickListener(v -> getContent.launch("image/*"));

        binding.btnSaveProfile.setOnClickListener(v -> saveChanges());
    }

    private void loadUserData() {
        db.collection("users").document(currentUserId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String username = documentSnapshot.getString("username");
                        String bio = documentSnapshot.getString("bio");
                        String avatar = documentSnapshot.getString("avatar");

                        binding.etEditUsername.setText(username);
                        binding.etEditBio.setText(bio);

                        if (avatar != null && !avatar.isEmpty()) {
                            loadAvatarImage(avatar);
                        }
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Không thể tải dữ liệu", Toast.LENGTH_SHORT).show());
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
            updateFirestore(newUsername, newBio, null);
        }
    }

    private void uploadAvatar(String username, String bio) {
        // Thay vì upload lên Storage, chúng ta chuyển ảnh sang Base64 để lưu vào Firestore
        String base64Image = uriToBase64(imageUri);
        if (base64Image != null) {
            updateFirestore(username, bio, base64Image);
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

    private void updateFirestore(String username, String bio, String avatarUrl) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("username", username);
        updates.put("bio", bio);
        if (avatarUrl != null) {
            updates.put("avatar", avatarUrl);
        }

        db.collection("users").document(currentUserId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    // Cập nhật local SharedPreferences
                    android.content.SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs",
                            android.content.Context.MODE_PRIVATE);
                    android.content.SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("username", username);
                    editor.apply();

                    Toast.makeText(this, "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    binding.btnSaveProfile.setEnabled(true);
                    binding.btnSaveProfile.setText("LƯU THAY ĐỔI");
                    Toast.makeText(this, "Lỗi cập nhật Firestore", Toast.LENGTH_SHORT).show();
                });
    }
}
