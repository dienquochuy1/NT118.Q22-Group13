package com.example.myapplication;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class Successful_Nofitication extends Fragment {

    public Successful_Nofitication() {

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.successful_nofitication, container, false);

        View btnTiepTuc = view.findViewById(R.id.btnContinue);

        btnTiepTuc.setOnClickListener(v -> {
            // 1. Hiện lại Bottom Nav
            showBottomNav(true);

            // 2. Chuyển thẳng về Trang Chủ (Home)
            if (getActivity() != null) {
                com.google.android.material.bottomnavigation.BottomNavigationView bottomNavigation =
                        getActivity().findViewById(R.id.home_bottom_navigation); // ID lấy từ MainActivity của bạn

                if (bottomNavigation != null) {
                    View layoutNav = getActivity().findViewById(R.id.layoutBottomNav);
                    layoutNav.setVisibility(View.VISIBLE);
                    // Lệnh này sẽ tự động kích hoạt showHomeUI() bên MainActivity
                    bottomNavigation.setSelectedItemId(R.id.bottom_nav_home);
                }
            }
        });


        return view;
    }

    private void showBottomNav(boolean isVisible) {
        if (getActivity() != null) {
            View nav = getActivity().findViewById(R.id.layoutBottomNav);
            if (nav != null) {
                nav.setVisibility(isVisible ? View.VISIBLE : View.GONE);
            }
        }
    }
}
