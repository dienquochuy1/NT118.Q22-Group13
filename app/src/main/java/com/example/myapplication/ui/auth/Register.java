package com.example.myapplication.ui.auth;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.myapplication.R;
import com.example.myapplication.auth.AuthCallback;
import com.example.myapplication.auth.AuthErrorInfo;
import com.example.myapplication.auth.AuthRepository;
import com.example.myapplication.auth.GoogleLoginProfile;
import com.example.myapplication.data.auth.AuthData;
import com.example.myapplication.ui.profile.Successful_Nofitication;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class Register extends Fragment {
    private EditText etUsername, etEmail, etPassword;
    private Button btnRegister;
    private TextView login;

    // Thêm các biến kết nối Google & Firebase giống bên Login
    private AuthRepository authRepository;
    private FirebaseAuth firebaseAuth;
    private GoogleSignInClient googleSignInClient;
    private ActivityResultLauncher<Intent> googleSignInLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Đón nhận kết quả trả về khi chọn tài khoản Google
        googleSignInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() != android.app.Activity.RESULT_OK || result.getData() == null) {
                        android.util.Log.e("TechByte_Result_Debug", "Luồng bị hủy hoặc dữ liệu trả về bị Null!");
                        return;
                    }

                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                    try {
                        GoogleSignInAccount account = task.getResult(ApiException.class);
                        if (account == null || TextUtils.isEmpty(account.getIdToken())) {
                            Toast.makeText(getActivity(), "Không lấy được thông tin Google.", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        sendGoogleTokenToBackend(account.getIdToken());
                    } catch (ApiException e) {
//                        Toast.makeText(getActivity(), "Đăng ký Google thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        android.util.Log.e("TechByte_Google_Error", "Mã lỗi Google: " + e.getStatusCode(), e);
                        Toast.makeText(getActivity(), "Đăng nhập Google thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.register, container, false);

        authRepository = new AuthRepository(requireContext());
        firebaseAuth = FirebaseAuth.getInstance(); // Khởi tạo FirebaseAuth

        etUsername = view.findViewById(R.id.et_name);
        etEmail = view.findViewById(R.id.et_email);
        etPassword = view.findViewById(R.id.et_password);
        btnRegister = view.findViewById(R.id.btn_register);
        login = view.findViewById(R.id.tv_login_now);
        View btnBack = view.findViewById(R.id.btn_back);
        View btnGoogle = view.findViewById(R.id.btn_google); // Ánh xạ nút Google mở rộng

        // Cấu hình mạng Google Sign-In
        configureGoogleSignIn();

        login.setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new Login())
                    .addToBackStack(null)
                    .commit();
        });

        btnRegister.setOnClickListener(v -> registerUser());
        btnBack.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        // Gán sự kiện kích hoạt mở bảng chọn tài khoản Google
        btnGoogle.setOnClickListener(v -> startGoogleSignIn());

        return view;
    }

    private void configureGoogleSignIn() {
        if (getContext() == null) return;

        int clientIdRes = getResources().getIdentifier("default_web_client_id", "string", requireContext().getPackageName());
        if (clientIdRes == 0) {
            googleSignInClient = null;
            return;
        }

        String webClientId = getString(clientIdRes);
        if (TextUtils.isEmpty(webClientId)) {
            googleSignInClient = null;
            return;
        }

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(webClientId)
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso);
    }

    private void startGoogleSignIn() {
        if (googleSignInClient == null) {
            Toast.makeText(getActivity(), "Chưa cấu hình Google Sign-In.", Toast.LENGTH_SHORT).show();
            return;
        }
        googleSignInLauncher.launch(googleSignInClient.getSignInIntent());
    }

    private void sendGoogleTokenToBackend(String googleIdToken) {
        authRepository.loginWithGoogle(googleIdToken, new AuthCallback<AuthData>() {
            @Override
            public void onSuccess(AuthData data) {
                if (data != null && data.getUser() != null) {
                    String uid = data.getUser().getId();
                    String username = data.getUser().getUsername();
                    if (username == null || username.isEmpty()) {
                        username = data.getUser().getName() != null
                                ? data.getUser().getName()
                                : data.getUser().getEmail();
                    }
                    // Đồng bộ SharedPreferences và cập nhật UI Đã đăng nhập
                    completeGoogleRegistration(uid, username);
                }
            }

            @Override
            public void onError(AuthErrorInfo error) {
                Toast.makeText(getActivity(), "Lỗi đồng bộ hệ thống: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
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

    private void completeGoogleRegistration(String uid, String username) {
        // Lưu trạng thái đăng nhập hợp lệ vào SharedPreferences cục bộ
        if (getActivity() != null) {
            SharedPreferences sharedPreferences = getActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("isLoggedIn", true);
            editor.putString("uid", uid);
            editor.putString("username", username != null ? username : "Nguoi dung");
            editor.apply();
        }

        Toast.makeText(getActivity(), "Đăng ký bằng Google thành công!", Toast.LENGTH_SHORT).show();

        // Tự động đẩy User trực tiếp về màn hình trang chủ chính của dự án
        if (getActivity() != null) {
            View nav = getActivity().findViewById(R.id.home_bottom_navigation);
            if (nav != null) {
                ((com.google.android.material.bottomnavigation.BottomNavigationView) nav)
                        .setSelectedItemId(R.id.bottom_nav_home);
            }
        }
    }
}