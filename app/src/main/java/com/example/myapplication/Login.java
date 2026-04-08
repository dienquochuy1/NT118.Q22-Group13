package com.example.myapplication;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myapplication.Entity.User;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.mindrot.jbcrypt.BCrypt;


public class Login extends Fragment {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private FirebaseFirestore db;

    private TextView register;

    public Login() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.login, container, false);

//        Ánh xạ id
        db = FirebaseFirestore.getInstance();
        etEmail = view.findViewById(R.id.et_email);
        etPassword = view.findViewById(R.id.et_password);
        btnLogin = view.findViewById(R.id.btn_login);
        register = view.findViewById(R.id.tv_register_now);
        View btnBack = view.findViewById(R.id.btn_back);

        btnLogin.setOnClickListener(v -> loginUser());

        register.setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new Register()) // Thay thế bằng Fragment Register
                    .addToBackStack(null) // LỆNH QUAN TRỌNG: Lưu trang này vào lịch sử để lùi lại được
                    .commit();
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Hủy Fragment hiện tại và lùi lại 1 bước trong lịch sử (Về lại Home_user)
                getParentFragmentManager().popBackStack();
            }
        });

        View register_now = view.findViewById(R.id.tv_register_now);
        register_now.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new Register()) // Thay thế bằng Fragment Register
                        .addToBackStack(null) // LỆNH QUAN TRỌNG: Lưu trang này vào lịch sử để lùi lại được
                        .commit();
            }
        });

        return view;
    }

    private void loginUser(){
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(getActivity(), "Vui lòng nhập đủ Email và Mật khẩu!", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("users")
                .whereEqualTo("email", email) // Tìm user có email này
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // Chuyển dữ liệu từ Firestore thành đối tượng User
                            User user = document.toObject(User.class);

                            // 5. SO SÁNH MẬT KHẨU BẰNG BCRYPT
                            // password là pass người dùng nhập, user.getPassword() là pass đã mã hóa trên database
                            if (BCrypt.checkpw(password, user.getPasswordHash())) {
                                // ĐĂNG NHẬP THÀNH CÔNG
                                saveLoginState(user.getUsername());
                                navigateToHome();

                            } else {
                                Toast.makeText(getActivity(), "Sai mật khẩu!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    } else {
                        Toast.makeText(getActivity(), "Email này chưa được đăng ký!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getActivity(), "Lỗi kết nối: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void saveLoginState(String username) {
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("isLoggedIn", true);
        editor.putString("username", username);
        editor.apply();
    }

    private void navigateToHome() {
        Toast.makeText(getActivity(), "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();

        // Gọi lại MainActivity để cập nhật lại UI (hiện Bottom Nav và về Home)
        if (getActivity() != null) {
            View nav = getActivity().findViewById(R.id.home_bottom_navigation);
            if (nav != null) {
                ((com.google.android.material.bottomnavigation.BottomNavigationView) nav)
                        .setSelectedItemId(R.id.bottom_nav_home);
            }
        }
    }

}