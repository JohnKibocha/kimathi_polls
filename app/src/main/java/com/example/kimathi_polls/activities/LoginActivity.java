package com.example.kimathi_polls.activities;

import static android.content.ContentValues.TAG;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.kimathi_polls.R;
import com.example.kimathi_polls.beans.Biometrics;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {


    FirebaseAuth mAuth;
    EditText loginEmailOrRegNoEditText, loginPasswordEditText;
    ProgressBar progressBar;
    Button submitLoginButton;
    DatabaseReference mDatabase;
    Button forgotPasswordButton;


    Biometrics biometrics = new Biometrics(this, new Runnable() {
        @Override
        public void run() {
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();
        }
    });


    //check if the user is already logged in
    @Override
    protected void onStart() {
        super.onStart();
        if (mAuth.getCurrentUser() != null) {
            biometrics.showBiometricPrompt();
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loginEmailOrRegNoEditText = findViewById(R.id.loginEmailOrRegNoEditText);
        loginPasswordEditText = findViewById(R.id.loginPasswordEditText);
        progressBar = findViewById(R.id.progressBar);
        submitLoginButton = findViewById(R.id.submitLoginButton);
        forgotPasswordButton = findViewById(R.id.forgotPasswordButton);
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();


        submitLoginButton.setOnClickListener(v -> {
            String emailOrRegNo = loginEmailOrRegNoEditText.getText().toString().trim();
            String password = loginPasswordEditText.getText().toString().trim();

            if (emailOrRegNo.isEmpty()) {
                loginEmailOrRegNoEditText.setError("Email or Registration Number is required");
                loginEmailOrRegNoEditText.requestFocus();
                return;
            }

            if (emailOrRegNo.contains("@")) {
                //it is a valid school email
                loginUserWithEmail(emailOrRegNo, password);
            } else {
                //checking if it is a valid registration number
                if (!emailOrRegNo.matches("[A-Z]+[0-9]{3}-[0-9]{2}-[0-9]{4}/[0-9]{4}")) {
                    loginEmailOrRegNoEditText.setError("Please enter a valid registration number");
                    loginEmailOrRegNoEditText.requestFocus();
                    return;
                }
                //it is a valid registration number
                mDatabase.child("users").orderByChild("regNo").equalTo(emailOrRegNo)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                                    String email = (String) userSnapshot.child("email").getValue();
                                    loginUserWithEmail(email, password);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
            }

            if (password.isEmpty()) {
                loginPasswordEditText.setError("Password is required");
                loginPasswordEditText.requestFocus();
                return;
            }

            if (password.length() < 6) {
                loginPasswordEditText.setError("Password must be at least 6 characters");
                loginPasswordEditText.requestFocus();
                return;
            }

            progressBar.setVisibility(android.view.View.VISIBLE);

            mAuth.signInWithEmailAndPassword(emailOrRegNo, password).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    progressBar.setVisibility(android.view.View.GONE);
                    Toast.makeText(this, "Welcome Back!", Toast.LENGTH_SHORT).show();
                    // Check if biometric authentication is supported
                    biometrics.showBiometricPrompt();
                } else {
                    progressBar.setVisibility(android.view.View.GONE);
                    android.widget.Toast.makeText(LoginActivity.this, "Authentication Error! Please check your credentials", android.widget.Toast.LENGTH_LONG).show();
                }
            });
        });


        forgotPasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final EditText resetMail = new EditText(v.getContext());
                AlertDialog.Builder passwordResetDialog = new AlertDialog.Builder(v.getContext());
                passwordResetDialog.setTitle("Reset Password?");
                passwordResetDialog.setMessage("Enter Your Email To Receive Reset Link.");
                passwordResetDialog.setView(resetMail);

                passwordResetDialog.setPositiveButton("Send", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // extract the email and send reset link
                        String mail = resetMail.getText().toString();
                        mAuth.sendPasswordResetEmail(mail).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(LoginActivity.this, "Reset Link Sent To Your Email.", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(LoginActivity.this, "Error ! Reset Link is Not Sent" + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });

                passwordResetDialog.setNegativeButton("Don't Send", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // close the dialog
                    }
                });

                passwordResetDialog.create().show();
            }
        });
    }

    private void loginUserWithEmail(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");
                            // Check if biometric authentication is supported
                            biometrics.showBiometricPrompt();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
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