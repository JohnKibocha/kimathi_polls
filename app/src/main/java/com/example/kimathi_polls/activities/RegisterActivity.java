package com.example.kimathi_polls.activities;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.kimathi_polls.R;
import com.example.kimathi_polls.beans.Biometrics;
import com.example.kimathi_polls.beans.DekutStudent;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;


public class RegisterActivity extends AppCompatActivity {
    private static final String PREFS_NAME = "AuthPrefs";
    private static final String AUTH_METHOD_KEY = "authMethod";

    EditText voterEmailEditText, voterPasswordEditText, confirmVoterPasswordEditText;
    Button submitRegisterButton;
    FirebaseAuth mAuth;
    ProgressBar progressBar;
    DatabaseReference dekutStudentRef;

    Biometrics biometrics = new Biometrics(this, new Runnable() {
        @Override
        public void run() {
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();
        }
    });

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
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();
        dekutStudentRef = FirebaseDatabase.getInstance().getReference("dekut_student");

        voterEmailEditText = findViewById(R.id.voterEmailEditText);
        voterPasswordEditText = findViewById(R.id.voterPasswordEditText);
        confirmVoterPasswordEditText = findViewById(R.id.confirmVoterPasswordEditText);
        submitRegisterButton = findViewById(R.id.submitRegisterButton);
        progressBar = findViewById(R.id.progressBar);

        String emailPattern = "[a-zA-Z]+\\.[a-zA-Z]+\\d{2}@students\\.dkut\\.ac\\.ke";
        voterEmailEditText.setHint("e.g., john.doe20@students.dkut.ac.ke");

        submitRegisterButton.setOnClickListener(v -> {
            String email = voterEmailEditText.getText().toString();
            String password = voterPasswordEditText.getText().toString();
            String confirmPassword = confirmVoterPasswordEditText.getText().toString();
            progressBar.setVisibility(ProgressBar.VISIBLE);

            if (!email.matches(emailPattern)) {
                // Handle invalid email...
                voterEmailEditText.setError("Invalid email");
            } else {
                // Query the dekut_student node to check if the email exists
                Query query = dekutStudentRef.orderByChild("email").equalTo(email);
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            // The email exists in the dekut_student node, continue with the registration process
                            mAuth.createUserWithEmailAndPassword(email, password)
                                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        if (task.isSuccessful()) {
                                            // Navigate to UserProfileActivity
                                            Intent intent = new Intent(RegisterActivity.this, UserProfileActivity.class);
                                            startActivity(intent);
                                        } else {
                                            // Handle failure...
                                        }
                                    }
                                });
                        } else {
                            // The email does not exist in the dekut_student node, show an error message
                            voterEmailEditText.setError("Email not found in dekut_student");
                            voterEmailEditText.requestFocus();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // Handle possible errors
                    }
                });
            }
        });

        // Prepopulate the dekut_student node if it is empty or does not exist
        dekutStudentRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    prepopulateDekutStudentNode();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle possible errors
            }
        });
    }

    private void prepopulateDekutStudentNode() {
        // Create 10 students using real life Kenyan names
        for (int i = 1; i <= 10; i++) {
            DekutStudent student = new DekutStudent();
            student.setEmail("student" + i + ".doe20@students.dkut.ac.ke");
            student.setFirstName("Student" + i);
            student.setMiddleName("Doe");
            student.setLastName("Smith");
            student.setRegistrationNumber("A000-00-0000/0000");
            student.setCourse("Course" + i);
            student.setDob("01-01-2000");
            student.setAdmissionYear("2020");
            student.setCurrentYear(1);
            student.setCurrentSemester(1);
            student.setCertificate("Certificate" + i);
            student.setProfilePhoto("@drawable/ic_default_profile");

            // Save the DekutStudent object to the dekut_student node
            dekutStudentRef.child("student" + i).setValue(student);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Biometrics.REQUEST_CODE) {
            biometrics.showBiometricPrompt();
        }
    }
}