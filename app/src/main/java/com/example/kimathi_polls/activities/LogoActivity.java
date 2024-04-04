package com.example.kimathi_polls.activities;

import static androidx.core.app.PendingIntentCompat.getActivity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.example.kimathi_polls.R;
import com.example.kimathi_polls.beans.Biometrics;
import com.google.firebase.auth.FirebaseAuth;

public class LogoActivity extends AppCompatActivity {


    FirebaseAuth mAuth;
    Biometrics biometrics = new Biometrics(this, new Runnable() {
        @Override
        public void run() {
            int secondsDelayed = 1;
            new Handler().postDelayed(() -> {
                startActivity(new Intent(LogoActivity.this, MainActivity.class));
                finish();
            }, secondsDelayed);
        }
    });


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logo);

        mAuth = FirebaseAuth.getInstance();

        //check if the user is logged in. if so, navigate to MainActivity if not, navigate to WelcomeActivity
        if (mAuth.getCurrentUser() != null) {
            biometrics.showBiometricPrompt();
        } else {

            int secondsDelayed = 1;
            new Handler().postDelayed(() -> {
                startActivity(new Intent(LogoActivity.this, WelcomeActivity.class));
                finish();
            }, secondsDelayed * 500);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Biometrics.REQUEST_CODE) {
            // The result from the biometric prompt
            biometrics.showBiometricPrompt();
        }
    }
}