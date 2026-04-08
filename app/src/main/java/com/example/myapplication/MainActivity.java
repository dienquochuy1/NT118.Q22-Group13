package com.example.myapplication;

import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.myapplication.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding activityMainBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        activityMainBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(activityMainBinding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Xử lý sự kiện click trên Bottom Navigation
        com.google.android.material.bottomnavigation.BottomNavigationView navView = findViewById(R.id.home_bottom_navigation);
        if(navView != null){
            activityMainBinding.layoutBottomNav.homeBottomNavigation.setOnItemSelectedListener(item -> {
                int id = item.getItemId();

                if (id == R.id.bottom_nav_home) {
                    // Hiện lại giao diện Trang chủ
                    showHomeUI();
                    return true;
                } else if (id == R.id.bottom_nav_user) {
                    // Chuyển sang giao diện Cá nhân (Đăng nhập)
                    showUserUI();
                    return true;
                }
                return false;
            });
        }
        // Mặc định luôn ở Trang chủ khi khởi động
        activityMainBinding.layoutBottomNav.homeBottomNavigation.setSelectedItemId(R.id.bottom_nav_home);
        showHomeUI();
    }

    private void showHomeUI() {
        // Hiện Header và Views của Trang chủ
        activityMainBinding.layoutHeader.getRoot().setVisibility(View.VISIBLE);
        activityMainBinding.layoutHomeViews.getRoot().setVisibility(View.VISIBLE);
        // Ẩn vùng chứa Fragment cá nhân
        activityMainBinding.fragmentContainer.setVisibility(View.GONE);

    }

    private void showUserUI() {
        // Ẩn Header và Views của Trang chủ
        activityMainBinding.layoutHeader.getRoot().setVisibility(View.GONE);
        activityMainBinding.layoutHomeViews.getRoot().setVisibility(View.GONE);
        // Hiện vùng chứa Fragment cá nhân và thay thế bằng Home_UserFragment
        activityMainBinding.fragmentContainer.setVisibility(View.VISIBLE);
        replaceFragment(new Home_user());
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.commit();
    }
}
