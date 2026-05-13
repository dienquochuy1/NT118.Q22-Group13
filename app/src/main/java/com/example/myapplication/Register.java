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

import com.example.myapplication.auth.AuthCallback;
import com.example.myapplication.auth.AuthErrorInfo;
import com.example.myapplication.auth.AuthRepository;
import com.example.myapplication.data.auth.AuthData;

public class Register extends Fragment {
    private EditText etUsername, etEmail, etPassword;
    private Button btnRegister;
    private TextView login;
    private AuthRepository authRepository;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.register, container, false);

        authRepository = new AuthRepository(requireContext());

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

        if (!username.matches("^[\\p{L}\\p{M}0-9\\- ]+$")) {
            Toast.makeText(getActivity(), "Ten nguoi dung khong hop le.", Toast.LENGTH_SHORT).show();
            return;
        }

        authRepository.register(username, email, password, new AuthCallback<AuthData>() {
            @Override
            public void onSuccess(AuthData data) {
                Toast.makeText(getActivity(), "Tao tai khoan thanh cong!", Toast.LENGTH_LONG).show();
                new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                    getParentFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, new Successful_Nofitication())
                            .addToBackStack(null)
                            .commit();
                }, 100);
            }

            @Override
            public void onError(AuthErrorInfo error) {
                String message = error.getMessage();
                if (error.getFieldErrors() != null && !error.getFieldErrors().isEmpty()) {
                    message = error.getFieldErrors().get(0);
                }
                Toast.makeText(getActivity(), "Dang ky that bai: " + message, Toast.LENGTH_LONG).show();
            }
        });
    }
}