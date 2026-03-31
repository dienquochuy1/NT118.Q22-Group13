package com.example.myapplication;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class Home_user extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    public Home_user() {
        // Required empty public constructor
    }

    // ĐÃ FIX: Đổi từ Login sang Home_user để tránh lỗi crash
    public static Home_user newInstance(String param1, String param2) {
        Home_user fragment = new Home_user();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // 1. Gán giao diện vào một biến View thay vì return ngay lập tức
        View view = inflater.inflate(R.layout.home_users, container, false);

        // 2. Tìm nút Đăng nhập qua ID
        View btnLogin = view.findViewById(R.id.btn_login);

        // 3. Bắt sự kiện click cho nút Đăng nhập
        if (btnLogin != null) {
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
        }

        // 4. Trả về view đã cấu hình xong cho hệ thống hiển thị
        return view;
    }
}