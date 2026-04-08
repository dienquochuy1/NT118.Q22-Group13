package com.example.myapplication;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class Home_user extends Fragment {
    public Home_user() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.home_users, container, false);

        View btnLogin = view.findViewById(R.id.btn_login);
        View btnRegister = view.findViewById(R.id.btn_register);
        TextView tvUserTitle = view.findViewById(R.id.tv_role);
        Button btnLogout = view.findViewById(R.id.btn_logout);

        SharedPreferences prefs = getActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        boolean isLoggedIn = prefs.getBoolean("isLoggedIn", false);

        if (isLoggedIn) {
            // TH1: ĐÃ ĐĂNG NHẬP
            String username = prefs.getString("username", "Người dùng");
            tvUserTitle.setText(username); // Đổi chữ "Khách" thành Tên

            btnLogin.setVisibility(View.GONE);    // Ẩn nút đăng nhập
            btnRegister.setVisibility(View.GONE); // Ẩn nút đăng ký
            btnLogout.setVisibility(View.VISIBLE); // Hiện nút đăng xuất
        } else {
            // TH2: CHƯA ĐĂNG NHẬP
            tvUserTitle.setText("Khách");
            btnLogin.setVisibility(View.VISIBLE);
            btnRegister.setVisibility(View.VISIBLE);
            btnLogout.setVisibility(View.GONE);
        }


        btnLogout.setOnClickListener(v -> {
            // Xóa dữ liệu đăng nhập
            prefs.edit().clear().apply();

            // F5 (Refresh) lại chính Fragment này để giao diện quay về chữ "Khách"
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new Home_user())
                    .commit();
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Thực hiện chuyển đổi Fragment sang trang Login

                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new Login()) // Thay thế bằng Fragment Login
                        .addToBackStack(null) // LỆNH QUAN TRỌNG: Lưu trang này vào lịch sử để lùi lại được
                        .commit();
            }
        });

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Thực hiện chuyển đổi Fragment sang trang Login

                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new Register()) // Thay thế bằng Fragment Register
                        .addToBackStack(null) // LỆNH QUAN TRỌNG: Lưu trang này vào lịch sử để lùi lại được
                        .commit();
            }
        });

        // 4. Trả về view đã cấu hình xong cho hệ thống hiển thị
        return view;
    }
}