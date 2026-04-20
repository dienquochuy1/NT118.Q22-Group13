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
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

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
        TextView menuTheme = view.findViewById(R.id.menu_theme);

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

        SharedPreferences prefs = requireActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        boolean isLoggedIn = firebaseUser != null || prefs.getBoolean("isLoggedIn", false);

        if (isLoggedIn) {
            String username = prefs.getString("username", "");
            if (TextUtils.isEmpty(username) && firebaseUser != null) {
                username = firebaseUser.getEmail();
            }
            tvUserTitle.setText(TextUtils.isEmpty(username) ? "Nguoi dung" : username);

            btnLogin.setVisibility(View.GONE);
            btnRegister.setVisibility(View.GONE);
            btnLogout.setVisibility(View.VISIBLE);
        } else {
            tvUserTitle.setText("Khach");
            btnLogin.setVisibility(View.VISIBLE);
            btnRegister.setVisibility(View.VISIBLE);
            btnLogout.setVisibility(View.GONE);
        }

        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            prefs.edit().clear().apply();

            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new Home_user())
                    .commit();
        });

        btnLogin.setOnClickListener(v -> getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new Login())
                .addToBackStack(null)
                .commit());

        btnRegister.setOnClickListener(v -> getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new Register())
                .addToBackStack(null)
                .commit());

        return view;
    }
}