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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class Register extends Fragment {
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private EditText etUsername, etEmail, etPassword;
    private Button btnRegister;
    private TextView login;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.register, container, false);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        etUsername = view.findViewById(R.id.et_name);
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

        btnRegister.setOnClickListener(v -> registerUser());
        view.findViewById(R.id.btn_back).setOnClickListener(v -> getParentFragmentManager().popBackStack());

        return view;
    }

    private void registerUser() {
        String username = etUsername.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(getActivity(), "Vui long nhap day du thong tin.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(getActivity(), "Mat khau phai co it nhat 6 ky tu.", Toast.LENGTH_SHORT).show();
            return;
        }

        auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser firebaseUser = authResult.getUser();
                    if (firebaseUser == null) {
                        Toast.makeText(getActivity(), "Khong the tao tai khoan.", Toast.LENGTH_LONG).show();
                        return;
                    }

                    String uid = firebaseUser.getUid();
                    User user = new User(uid, username, email, "", "", "user");

                    db.collection("users").document(uid)
                            .set(user)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(getActivity(), "Tao tai khoan thanh cong!", Toast.LENGTH_LONG).show();

                                android.content.SharedPreferences sharedPreferences = getActivity()
                                        .getSharedPreferences("UserPrefs", android.content.Context.MODE_PRIVATE);
                                android.content.SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putBoolean("isLoggedIn", true);
                                editor.putString("username", username);
                                editor.putString("uid", uid);
                                editor.apply();

                                if (getActivity() != null) {
                                    View bottomNav = getActivity().findViewById(R.id.layoutBottomNav);
                                    if (bottomNav != null) {
                                        bottomNav.setVisibility(View.GONE);
                                    }
                                }

                                new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                                    getParentFragmentManager().beginTransaction()
                                            .replace(R.id.fragment_container, new Successful_Nofitication())
                                            .addToBackStack(null)
                                            .commit();
                                }, 100);
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(getActivity(), "Tao profile that bai: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getActivity(), "Dang ky that bai: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}