package com.example.myapplication.Entity;

public class User {
    private String uid; // Firestore document id = FirebaseAuth UID
    private String username;
    private String email;
    private String bio;
    private String avatar;
    private String role;

    public User() {}

    public User(String uid, String username, String email, String bio, String avatar, String role) {
        this.uid = uid;
        this.username = username;
        this.email = email;
        this.bio = bio;
        this.avatar = avatar;
        this.role = role;
    }

    public String getUid() { return uid; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getBio() { return bio; }
    public String getAvatar() { return avatar; }
    public String getRole() { return role; }

    public void setUid(String uid) { this.uid = uid; }
    public void setUsername(String username) { this.username = username; }
    public void setEmail(String email) { this.email = email; }
    public void setBio(String bio) { this.bio = bio; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
    public void setRole(String role) { this.role = role; }
}
