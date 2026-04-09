package com.example.myapplication;

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
import androidx.fragment.app.Fragment;

import com.example.myapplication.Entity.User;
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
import com.google.firebase.firestore.FirebaseFirestore;

public class Login extends Fragment {

    private EditText etEmail, etPassword;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private GoogleSignInClient googleSignInClient;
    private ActivityResultLauncher<Intent> googleSignInLauncher;

    public Login() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        googleSignInLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() != android.app.Activity.RESULT_OK || result.getData() == null) {
                        return;
                    }

                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                    try {
                        GoogleSignInAccount account = task.getResult(ApiException.class);
                        if (account == null || TextUtils.isEmpty(account.getIdToken())) {
                            Toast.makeText(getActivity(), "Khong lay duoc thong tin Google.", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        firebaseAuthWithGoogle(account);
                    } catch (ApiException e) {
                        Toast.makeText(getActivity(), "Dang nhap Google that bai: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.login, container, false);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        etEmail = view.findViewById(R.id.et_email);
        etPassword = view.findViewById(R.id.et_password);
        Button btnLogin = view.findViewById(R.id.btn_login);
        TextView register = view.findViewById(R.id.tv_register_now);
        TextView forgotPassword = view.findViewById(R.id.tv_forgot_password);
        View btnBack = view.findViewById(R.id.btn_back);
        View btnGoogle = view.findViewById(R.id.btn_google);

        configureGoogleSignIn();

        btnLogin.setOnClickListener(v -> loginUser());

        register.setOnClickListener(v -> getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new Register())
                .addToBackStack(null)
                .commit());

        forgotPassword.setOnClickListener(v -> getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new ForgotPassword())
                .addToBackStack(null)
                .commit());

        btnGoogle.setOnClickListener(v -> startGoogleSignIn());
        btnBack.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        return view;
    }

    private void configureGoogleSignIn() {
        if (getContext() == null) {
            return;
        }

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
            Toast.makeText(getActivity(), "Google Sign-In chua duoc cau hinh trong Firebase.", Toast.LENGTH_LONG).show();
            return;
        }

        googleSignInClient.signOut().addOnCompleteListener(task -> {
            Intent signInIntent = googleSignInClient.getSignInIntent();
            googleSignInLauncher.launch(signInIntent);
        });
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);

        auth.signInWithCredential(credential)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser firebaseUser = authResult.getUser();
                    if (firebaseUser == null) {
                        Toast.makeText(getActivity(), "Dang nhap Google that bai.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String fallbackName = account.getDisplayName();
                    if (TextUtils.isEmpty(fallbackName)) {
                        fallbackName = firebaseUser.getEmail();
                    }
                    syncProfileAndCompleteLogin(firebaseUser, fallbackName);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getActivity(), "Dang nhap Google that bai: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void loginUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(getActivity(), "Vui long nhap du Email va Mat khau!", Toast.LENGTH_SHORT).show();
            return;
        }

        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser firebaseUser = authResult.getUser();
                    if (firebaseUser == null) {
                        Toast.makeText(getActivity(), "Dang nhap that bai.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    syncProfileAndCompleteLogin(firebaseUser, firebaseUser.getEmail());
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getActivity(), "Dang nhap that bai: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void syncProfileAndCompleteLogin(FirebaseUser firebaseUser, String fallbackName) {
        String uid = firebaseUser.getUid();
        String email = firebaseUser.getEmail() != null ? firebaseUser.getEmail() : "";
        String safeFallbackName = TextUtils.isEmpty(fallbackName) ? email : fallbackName;

        db.collection("users").document(uid)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        User user = snapshot.toObject(User.class);
                        String username = user != null ? user.getUsername() : safeFallbackName;
                        if (TextUtils.isEmpty(username)) {
                            username = safeFallbackName;
                        }
                        completeLogin(uid, username);
                        return;
                    }

                    User newUser = new User(uid, safeFallbackName, email, "", "", "user");
                    db.collection("users").document(uid)
                            .set(newUser)
                            .addOnSuccessListener(unused -> completeLogin(uid, safeFallbackName))
                            .addOnFailureListener(e -> completeLogin(uid, safeFallbackName));
                })
                .addOnFailureListener(e -> completeLogin(uid, safeFallbackName));
    }

    private void completeLogin(String uid, String username) {
        saveLoginState(uid, username);
        navigateToHome();
    }

    private void saveLoginState(String uid, String username) {
        if (getActivity() == null) {
            return;
        }
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("isLoggedIn", true);
        editor.putString("uid", uid);
        editor.putString("username", username != null ? username : "Nguoi dung");
        editor.apply();
    }

    private void navigateToHome() {
        Toast.makeText(getActivity(), "Dang nhap thanh cong!", Toast.LENGTH_SHORT).show();

        if (getActivity() != null) {
            View nav = getActivity().findViewById(R.id.home_bottom_navigation);
            if (nav != null) {
                ((com.google.android.material.bottomnavigation.BottomNavigationView) nav)
                        .setSelectedItemId(R.id.bottom_nav_home);
            }
        }
    }

}