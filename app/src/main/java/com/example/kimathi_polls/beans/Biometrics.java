package com.example.kimathi_polls.beans;

import static android.hardware.biometrics.BiometricManager.Authenticators.BIOMETRIC_STRONG;
import static android.hardware.biometrics.BiometricManager.Authenticators.DEVICE_CREDENTIAL;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;


public class Biometrics {
    public static final int REQUEST_CODE = 1072;
    private static final String PREFS_NAME = "AuthPrefs";
    private static final String AUTH_METHOD_KEY = "authMethod";

    private FragmentActivity fragmentActivity;
    private Runnable onSuccess;

    public static String getPrefsName() {
        return PREFS_NAME;
    }

    public static String getAuthMethodKey() {
        return AUTH_METHOD_KEY;
    }

    public Biometrics(FragmentActivity fragmentActivity, Runnable onSuccess) {
        // accept fragments and activities
        this.fragmentActivity = fragmentActivity;
        this.onSuccess = onSuccess;
    }

    public void showBiometricPrompt() {
        SharedPreferences settings = fragmentActivity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        if (settings.contains(AUTH_METHOD_KEY)) {
            boolean isFingerprint = settings.getBoolean(AUTH_METHOD_KEY, false);
            if (!isFingerprint) {
                // The user has chosen PIN/Pattern/Password authentication, don't show the biometric prompt
                // Perform the necessary action for PIN/Pattern/Password authentication
                return;
            }
        }

        BiometricManager biometricManager = BiometricManager.from(fragmentActivity);
        switch (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK | BiometricManager.Authenticators.DEVICE_CREDENTIAL)) {
            case BiometricManager.BIOMETRIC_SUCCESS:
                // Device supports biometric authentication
                break;
            case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
            case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                // Device does not support biometric authentication
                Toast.makeText(fragmentActivity, "Biometric authentication is not available", Toast.LENGTH_SHORT).show();
                break;
            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                // User hasn't enrolled any biometrics to authenticate with
                Intent enrollIntent = new Intent(Settings.ACTION_BIOMETRIC_ENROLL);
                enrollIntent.putExtra(Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
                        BIOMETRIC_STRONG | DEVICE_CREDENTIAL);
                fragmentActivity.startActivity(enrollIntent);
                break;
        }

        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Authentication Required")
                .setSubtitle("Please authenticate to proceed")
                .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_WEAK | BiometricManager.Authenticators.DEVICE_CREDENTIAL)
                .build();

        BiometricPrompt biometricPrompt = new BiometricPrompt(fragmentActivity, ContextCompat.getMainExecutor(fragmentActivity), new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                // handle error
                Log.e("Biometrics", "Authentication error, code " + errorCode + ": " + errString);
                Toast.makeText(fragmentActivity, "Authentication error: " + errString, Toast.LENGTH_SHORT).show();

                if (errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON || errorCode == BiometricPrompt.ERROR_USER_CANCELED || errorCode == BiometricPrompt.ERROR_CANCELED) {
                    // Launch device credentials screen
                    Intent intent = new Intent(Settings.ACTION_SECURITY_SETTINGS);
                    fragmentActivity.startActivityForResult(intent, REQUEST_CODE);
                }
            }

            @Override
            public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                // handle success
                Log.d("Biometrics", "Authentication succeeded");
                onSuccess.run();

            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                // handle failure
                Log.e("Biometrics", "Authentication failed");
                Toast.makeText(fragmentActivity, "Authentication failed", Toast.LENGTH_SHORT).show();

            }
        });
        biometricPrompt.authenticate(promptInfo);
    }
}