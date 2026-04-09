package com.example.myapplication;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;

public class ForgotPassword extends Fragment {

    private EditText etEmail;
    private FirebaseAuth auth;

    public ForgotPassword() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.forgot_password, container, false);

        auth = FirebaseAuth.getInstance();
        etEmail = view.findViewById(R.id.et_email_reset);
        Button btnSend = view.findViewById(R.id.btn_send_reset);
        View btnBack = view.findViewById(R.id.btn_back);

        btnSend.setOnClickListener(v -> sendResetEmail());
        btnBack.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        return view;
    }

    private void sendResetEmail() {
        String email = etEmail.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(getActivity(), "Vui long nhap email.", Toast.LENGTH_SHORT).show();
            return;
        }

        auth.sendPasswordResetEmail(email)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(getActivity(), "Da gui email dat lai mat khau.", Toast.LENGTH_LONG).show();
                    getParentFragmentManager().popBackStack();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getActivity(), "Khong gui duoc email: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }
}

