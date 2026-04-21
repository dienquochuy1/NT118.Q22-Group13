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

import android.content.Intent;
import com.bumptech.glide.Glide;
import com.example.myapplication.Entity.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

public class Home_user extends Fragment {
    private TextView tvUserTitle, tvSubtitle, menuTheme;
    private android.widget.ImageView iconUser;
    private View btnLogin, btnRegister;
    private Button btnLogout;

    public Home_user() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.home_users, container, false);

        btnLogin = view.findViewById(R.id.btn_login);
        btnRegister = view.findViewById(R.id.btn_register);
        tvUserTitle = view.findViewById(R.id.tv_role);
        btnLogout = view.findViewById(R.id.btn_logout);
        menuTheme = view.findViewById(R.id.menu_theme);
        iconUser = view.findViewById(R.id.icon_user);
        tvSubtitle = view.findViewById(R.id.tv_subtitle);

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
            FirebaseAuth.getInstance().signOut();
            SharedPreferences prefs = requireActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
            prefs.edit().clear().apply();
            loadUserData();
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
            if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                Intent intent = new Intent(getActivity(), Edit_Profile.class);
                startActivity(intent);
            }
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

        SharedPreferences prefs = requireActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        boolean isLoggedIn = firebaseUser != null || prefs.getBoolean("isLoggedIn", false);

        if (isLoggedIn) {
            String username = prefs.getString("username", "");
            if (TextUtils.isEmpty(username) && firebaseUser != null) {
                username = firebaseUser.getEmail();
            }
            tvUserTitle.setText(TextUtils.isEmpty(username) ? "Người dùng" : username);

            // Load extra data from Firestore (Avatar & Bio)
            if (firebaseUser != null) {
                FirebaseFirestore.getInstance().collection("users").document(firebaseUser.getUid())
                        .get().addOnSuccessListener(documentSnapshot -> {
                            if (documentSnapshot.exists() && isAdded()) {
                                String usernameFs = documentSnapshot.getString("username");
                                String avatarUrl = documentSnapshot.getString("avatar");
                                String bio = documentSnapshot.getString("bio");

                                if (usernameFs != null && !usernameFs.isEmpty()) {
                                    tvUserTitle.setText(usernameFs);
                                }
                                if (avatarUrl != null && !avatarUrl.isEmpty()) {
                                    loadAvatarImage(avatarUrl);
                                }
                                if (bio != null && !bio.isEmpty()) {
                                    tvSubtitle.setText(bio);
                                }
                            }
                        });
            }

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
        }
    }

    private void loadAvatarImage(String avatarData) {
        if (avatarData.startsWith("http")) {
            Glide.with(this).load(avatarData).into(iconUser);
        } else {
            try {
                byte[] decodedString = Base64.decode(avatarData, Base64.DEFAULT);
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                iconUser.setImageBitmap(decodedByte);
            } catch (Exception e) {
                iconUser.setImageResource(R.drawable.user);
            }
        }
    }
}