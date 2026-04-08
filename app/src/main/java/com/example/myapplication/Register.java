package com.example.myapplication;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.myapplication.Entity.User;
import com.google.firebase.firestore.FirebaseFirestore;

import org.mindrot.jbcrypt.BCrypt;

public class Register extends Fragment {
    private FirebaseFirestore db;
    private EditText etUsername, etEmail, etPassword;
    private Button btnRegister;

    private TextView login;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.register, container, false);

        // Khởi tạo Firestore
        db = FirebaseFirestore.getInstance();

        // Ánh xạ View (Bạn hãy đảm bảo các ID này khớp với file register.xml của bạn)
        etUsername = view.findViewById(R.id.et_name); // Thêm EditText này vào XML nếu chưa có
        etEmail = view.findViewById(R.id.et_email);
        etPassword = view.findViewById(R.id.et_password);
        btnRegister = view.findViewById(R.id.btn_register);
        login = view.findViewById(R.id.tv_login_now);

        login.setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new Login())
                    .addToBackStack(null)
                    .commit();
        });

        // Nút Đăng ký
        btnRegister.setOnClickListener(v -> saveUserToFirestore());

        // Nút Back
        view.findViewById(R.id.btn_back).setOnClickListener(v -> getParentFragmentManager().popBackStack());

        return view;
    }

    private void saveUserToFirestore() {
        String username = etUsername.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(getActivity(), "Vui lòng nhập đầy đủ thông tin (Mật khẩu > 6 ký tự)", Toast.LENGTH_SHORT).show();
            return;
        }

        String encryptedPassword = hashPassword(password);
        int randomNumber = new java.util.Random().nextInt(10000);
        String userId = String.format("user%04d", randomNumber);

        User user = new User(userId, username, email, "", "", "user", encryptedPassword);

        db.collection("users").document(userId)
                .set(user)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getActivity(), "Đã tạo tài khoản thành công! ", Toast.LENGTH_LONG).show();

                    android.content.SharedPreferences sharedPreferences = getActivity().getSharedPreferences("UserPrefs", android.content.Context.MODE_PRIVATE);
                    android.content.SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putBoolean("isLoggedIn", true); // Đánh dấu đã đăng nhập
                    editor.putString("username", username); // Lưu tên để hiển thị
                    editor.apply();

                    // 2. Ẩn Bottom Nav trước khi sang màn hình Thành công (như đã bàn ở câu trước)
                    if (getActivity() != null) {
                        View bottomNav = getActivity().findViewById(R.id.layoutBottomNav);
                        if (bottomNav != null) bottomNav.setVisibility(View.GONE);
                    }

                    new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            getParentFragmentManager().beginTransaction()
                                    .replace(R.id.fragment_container, new Successful_Nofitication())
                                    .addToBackStack(null)
                                    .commit();
                        }
                    }, 100);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getActivity(), "Lỗi: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private String hashPassword(String plainTextPassword) {
        return BCrypt.hashpw(plainTextPassword, BCrypt.gensalt(12));
    }
}