package com.example.myapplication.Entity;

public class User {
    private String userId; // PK
    private String username;
    private String email;
    private String bio;
    private String avatar;
    private String role;
    private String passwordHash;

    public User() {} // Cần thiết cho Firebase

    public User(String userId, String username, String email, String bio, String avatar, String role, String passwordHash) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.bio = bio;
        this.avatar = avatar;
        this.role = role;
        this.passwordHash = passwordHash;
    }

    // Getter và Setter (Tạo tự động bằng Alt+Insert trong Android Studio)
    public String getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getBio() { return bio; }
    public String getAvatar() { return avatar; }
    public String getRole() { return role; }
    public String getPasswordHash() { return passwordHash; }


    public void setUserId(String userId) { this.userId = userId; }
    public void setUsername(String username) { this.username = username; }
    public void setEmail(String email) { this.email = email; }
    public void setBio(String bio) { this.bio = bio; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
    public void setRole(String role) { this.role = role; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }


}
