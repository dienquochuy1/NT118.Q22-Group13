# Google Login Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Enable Google login in the Android app while keeping Firebase forgot-password unchanged.

**Architecture:** Reuse the existing `GoogleSignInClient` and activity-result launcher in `Login`. Add a small pure Java profile mapper so username fallback behavior is tested independently from Android/Firebase framework classes.

**Tech Stack:** Android Java, Firebase Auth, Google Play Services Auth, JUnit 4, Gradle.

---

### Task 1: Add Profile Fallback Helper

**Files:**
- Create: `app/src/main/java/com/example/myapplication/auth/GoogleLoginProfile.java`
- Test: `app/src/test/java/com/example/myapplication/auth/GoogleLoginProfileTest.java`

- [ ] **Step 1: Write the failing test**

```java
package com.example.myapplication.auth;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class GoogleLoginProfileTest {
    @Test
    public void displayNameWinsOverEmail() {
        assertEquals("An Nguyen", GoogleLoginProfile.resolveUsername("An Nguyen", "an@example.com"));
    }

    @Test
    public void emailIsUsedWhenDisplayNameIsBlank() {
        assertEquals("an@example.com", GoogleLoginProfile.resolveUsername("  ", "an@example.com"));
    }

    @Test
    public void defaultNameIsUsedWhenProfileIsEmpty() {
        assertEquals("Nguoi dung", GoogleLoginProfile.resolveUsername(null, null));
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `.\gradlew.bat testDebugUnitTest --tests com.example.myapplication.auth.GoogleLoginProfileTest`
Expected: FAIL because `GoogleLoginProfile` does not exist.

- [ ] **Step 3: Write minimal implementation**

```java
package com.example.myapplication.auth;

public final class GoogleLoginProfile {
    private static final String DEFAULT_USERNAME = "Nguoi dung";

    private GoogleLoginProfile() {}

    public static String resolveUsername(String displayName, String email) {
        if (displayName != null && !displayName.trim().isEmpty()) {
            return displayName.trim();
        }

        if (email != null && !email.trim().isEmpty()) {
            return email.trim();
        }

        return DEFAULT_USERNAME;
    }
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `.\gradlew.bat testDebugUnitTest --tests com.example.myapplication.auth.GoogleLoginProfileTest`
Expected: PASS.

### Task 2: Re-enable Google Login in Login Fragment

**Files:**
- Modify: `app/src/main/java/com/example/myapplication/Login.java`

- [ ] **Step 1: Add Firebase Auth imports and profile helper import**

```java
import com.example.myapplication.auth.GoogleLoginProfile;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
```

- [ ] **Step 2: Add FirebaseAuth field and initialize it in `onCreateView`**

```java
private FirebaseAuth firebaseAuth;
```

```java
firebaseAuth = FirebaseAuth.getInstance();
```

- [ ] **Step 3: Make the Google button start Google Sign-In**

```java
btnGoogle.setOnClickListener(v -> startGoogleSignIn());
```

- [ ] **Step 4: Implement `startGoogleSignIn()`**

```java
private void startGoogleSignIn() {
    if (googleSignInClient == null) {
        Toast.makeText(getActivity(), "Chua cau hinh Google Sign-In.", Toast.LENGTH_SHORT).show();
        return;
    }

    googleSignInLauncher.launch(googleSignInClient.getSignInIntent());
}
```

- [ ] **Step 5: Implement Firebase exchange in `firebaseAuthWithGoogle()`**

```java
private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
    AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
    firebaseAuth.signInWithCredential(credential)
            .addOnSuccessListener(authResult -> {
                FirebaseUser user = authResult.getUser();
                if (user == null) {
                    Toast.makeText(getActivity(), "Dang nhap Google that bai.", Toast.LENGTH_SHORT).show();
                    return;
                }

                String username = GoogleLoginProfile.resolveUsername(user.getDisplayName(), user.getEmail());
                completeLogin(user.getUid(), username);
            })
            .addOnFailureListener(e -> Toast.makeText(
                    getActivity(),
                    "Dang nhap Google that bai: " + e.getMessage(),
                    Toast.LENGTH_LONG
            ).show());
}
```

- [ ] **Step 6: Run verification**

Run: `.\gradlew.bat testDebugUnitTest`
Expected: unit tests pass.

Run: `.\gradlew.bat assembleDebug`
Expected: debug APK builds.
