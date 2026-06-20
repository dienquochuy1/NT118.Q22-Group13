package com.example.myapplication.ui.profile;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import android.widget.Toast;

import com.example.myapplication.ui.auth.Login;
import com.example.myapplication.R;
import com.example.myapplication.ui.auth.Register;
import com.example.myapplication.auth.AuthCallback;
import com.example.myapplication.auth.AuthErrorInfo;
import com.example.myapplication.auth.AuthRepository;
import com.example.myapplication.auth.SessionStore;

public class Home_user extends Fragment {
    private TextView tvUserTitle, tvSubtitle, menuTheme;
    private android.widget.ImageView iconUser;
    private View btnLogin, btnRegister;
    private Button btnLogout;
    private TextView tvStatArticles, tvStatSaved, tvStatInterests;
    private com.google.android.material.chip.ChipGroup cgInterests;
    private AuthRepository authRepository;
    private SessionStore sessionStore;

    public Home_user() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.home_users, container, false);

        authRepository = new AuthRepository(requireContext());
        sessionStore = authRepository.getSessionStore();

        btnLogin = view.findViewById(R.id.btn_login);
        btnRegister = view.findViewById(R.id.btn_register);
        tvUserTitle = view.findViewById(R.id.tv_role);
        btnLogout = view.findViewById(R.id.btn_logout);
        menuTheme = view.findViewById(R.id.menu_theme);
        iconUser = view.findViewById(R.id.icon_user);
        tvSubtitle = view.findViewById(R.id.tv_subtitle);
        tvStatArticles = view.findViewById(R.id.tv_stat_articles);
        tvStatSaved = view.findViewById(R.id.tv_stat_saved);
        tvStatInterests = view.findViewById(R.id.tv_stat_interests);
        cgInterests = view.findViewById(R.id.cg_interests);

        SharedPreferences themePrefs = requireActivity().getSharedPreferences("ThemePrefs", Context.MODE_PRIVATE);
        boolean isDarkMode = themePrefs.getBoolean("isDarkMode", false);
        if (isDarkMode) {
            menuTheme.setText("Giao diện (Sáng)");
        } else {
            menuTheme.setText("Giao diện (Tối)");
        }

        menuTheme.setOnClickListener(v -> {
            boolean currentMode = themePrefs.getBoolean("isDarkMode", false);
            themePrefs.edit().putBoolean("isDarkMode", !currentMode).apply();
            if (!currentMode) {
                androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO);
            }
        });

        btnLogout.setOnClickListener(v -> {
            authRepository.logout(new AuthCallback<Object>() {
                @Override
                public void onSuccess(Object data) {
                    loadUserData();
                    Toast.makeText(getActivity(), "Dang xuat thanh cong!", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onError(AuthErrorInfo error) {
                    sessionStore.clearSession();
                    loadUserData();
                    Toast.makeText(getActivity(), "Dang xuat that bai: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });

        btnLogin.setOnClickListener(v -> getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new Login())
                .addToBackStack(null)
                .commit());

        btnRegister.setOnClickListener(v -> getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new Register())
                .addToBackStack(null)
                .commit());

        iconUser.setOnClickListener(v -> {
            Toast.makeText(getActivity(), "Cap nhat ho so chua duoc ho tro.", Toast.LENGTH_SHORT).show();
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUserData();
    }

    private void loadUserData() {
        if (getContext() == null) return;

        boolean isLoggedIn = sessionStore.isLoggedIn();

        if (isLoggedIn) {
            String username = sessionStore.getUserUsername();
            if (TextUtils.isEmpty(username)) {
                username = sessionStore.getUserName();
            }
            if (TextUtils.isEmpty(username)) {
                username = sessionStore.getUserEmail();
            }
            tvUserTitle.setText(TextUtils.isEmpty(username) ? "Nguoi dung" : username);

            String role = sessionStore.getUserRole();
            if (!TextUtils.isEmpty(role)) {
                tvSubtitle.setText(role);
            }

            cgInterests.removeAllViews();

            btnLogin.setVisibility(View.GONE);
            btnRegister.setVisibility(View.GONE);
            btnLogout.setVisibility(View.VISIBLE);
        } else {
            tvUserTitle.setText("Khách");
            tvSubtitle.setText("Đăng nhập để trải nghiệm đầy đủ");
            btnLogin.setVisibility(View.VISIBLE);
            btnRegister.setVisibility(View.VISIBLE);
            btnLogout.setVisibility(View.GONE);
            iconUser.setImageResource(R.drawable.user);
            cgInterests.removeAllViews();
        }
    }


    private void addInterestChip(String text) {
        if (getContext() == null) return;
        com.google.android.material.chip.Chip chip = new com.google.android.material.chip.Chip(getContext());
        chip.setText(text);
        chip.setTextColor(androidx.core.content.ContextCompat.getColorStateList(getContext(), R.color.text_primary));
        chip.setChipBackgroundColor(androidx.core.content.ContextCompat.getColorStateList(getContext(), R.color.chip_background));
        chip.setChipStrokeColor(androidx.core.content.ContextCompat.getColorStateList(getContext(), R.color.chip_stroke));
        chip.setChipStrokeWidth(getResources().getDisplayMetrics().density * 1);
        cgInterests.addView(chip);
    }
}
