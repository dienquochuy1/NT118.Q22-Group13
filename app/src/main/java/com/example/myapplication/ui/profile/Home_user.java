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
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class Home_user extends Fragment {
    private TextView tvUserTitle, tvSubtitle, menuTheme;
    private android.widget.ImageView iconUser;
    private View btnLogin, btnRegister;
    private Button btnLogout;
    private TextView tvStatArticles, tvStatSaved;
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

        View menuHelp = view.findViewById(R.id.menu_help);
        if (menuHelp != null) {
            menuHelp.setOnClickListener(v -> {
                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Trợ giúp & Phản hồi")
                        .setMessage("Thông tin liên hệ hỗ trợ:\n\n" +
                                "📧 Email: support@techbyte.vn\n" +
                                "📞 Hotline: 1900 8198 (8:00 - 22:00)\n" +
                                "📍 Địa chỉ: TP. Hồ Chí Minh")
                        .setPositiveButton("Đồng ý", (dialog, which) -> dialog.dismiss())
                        .show();
            });
        }

        View menuPolicy = view.findViewById(R.id.menu_policy);
        if (menuPolicy != null) {
            menuPolicy.setOnClickListener(v -> {
                new MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Điều khoản sử dụng")
                        .setMessage("Chào mừng bạn đến với ứng dụng đọc tin tức hàng đầu. Khi sử dụng ứng dụng, bạn đồng ý với các điều khoản sau:\n\n" +
                                "1. Bản quyền nội dung: Tất cả bài viết, hình ảnh, tài liệu được cung cấp trên ứng dụng thuộc sở hữu của chúng tôi hoặc các đối tác liên kết. Bạn không được tự ý sao chép hoặc phân phối khi chưa được phép.\n\n" +
                                "2. Sử dụng dịch vụ: Bạn cam kết sử dụng ứng dụng vào mục đích cá nhân, phi thương mại và không thực hiện các hành vi gây cản trở hoặc phá hoại hệ thống.\n\n" +
                                "3. Bảo mật tài khoản: Bạn có trách nhiệm bảo mật thông tin tài khoản đăng nhập của mình và chịu trách nhiệm về toàn bộ hoạt động diễn ra dưới tài khoản đó.\n\n" +
                                "4. Thay đổi điều khoản: Chúng tôi có quyền cập nhật, sửa đổi các điều khoản này bất kỳ lúc nào mà không cần thông báo trước. Việc bạn tiếp tục sử dụng ứng dụng đồng nghĩa với việc chấp nhận các thay đổi đó.")
                        .setPositiveButton("Đồng ý", (dialog, which) -> dialog.dismiss())
                        .show();
            });
        }

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
}
