# Google Login Design

## Goal
Enable Google sign-in in the Android app while leaving the existing Firebase password reset flow unchanged.

## Scope
Only the Android project changes. The Laravel backend stays unchanged for this feature. `ForgotPassword.java` continues to call Firebase Auth for reset emails.

## Architecture
`Login` already contains the Google Sign-In launcher and client setup. The feature re-enables that path: the Google button starts the Google sign-in intent, the returned ID token is exchanged for a Firebase credential, and Firebase Auth completes the sign-in.

After Firebase sign-in succeeds, the app stores the Firebase user ID and display name or email in the existing `UserPrefs` shared preferences and navigates to home using the current `completeLogin()` path. A small pure Java helper maps a Firebase/Google profile into the username fallback used by `Login`, which gives this behavior a focused unit test.

## Error Handling
If the Google client is not configured, the app shows a clear toast instead of starting a broken flow. If the Google account has no ID token or Firebase rejects the credential, the app shows the failure message and keeps the user on the login screen.

## Testing
Add a unit test for the display-name/email fallback helper. Run the Android unit tests and build to catch Java compile errors in the Google/Firebase integration.
