package com.example.kimathi_polls.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.kimathi_polls.R;
import com.example.kimathi_polls.adapters.CandidateAdapter;
import com.example.kimathi_polls.interfaces.CandidateSelectionListener;
import com.example.kimathi_polls.beans.Candidate;
import com.example.kimathi_polls.beans.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class VieFragment extends Fragment implements CandidateSelectionListener {


    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private EditText manifesto;
    private Spinner position;
    private Button vieButton;
    private EditText registrationNumber;
    private EditText firstName;
    private EditText middleName;
    private EditText lastName;
    private EditText course;
    private EditText email;
    private ImageView candidateProfilePhoto;
    private String profilePhoto;
    private Date creationDate;
    private CandidateAdapter candidateAdapter;
    private List<Candidate> candidateList;
    private List<Candidate> disqualifiedCandidates = new ArrayList<>();

    final SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault());

    @Override
    public void onCandidateSelected(Candidate candidate) {
        // This method is called when a candidate is selected
        // You can use this method to perform an action when a candidate is selected
    }

    @Override
    public void selectedCandidate(String candidate, String position) {
        // This method is called when a candidate is selected
        // You can use this method to perform an action when a candidate is selected
    }

    @Override
    public void confirmSelection() {
        // This method is called when a candidate is selected
        // You can use this method to perform an action when a candidate is selected
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_vie, container, false);


        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        manifesto = view.findViewById(R.id.manifesto);
        position = view.findViewById(R.id.position);
        vieButton = view.findViewById(R.id.vieButton);
        registrationNumber = view.findViewById(R.id.registrationNumber);
        firstName = view.findViewById(R.id.firstName);
        middleName = view.findViewById(R.id.middleName);
        lastName = view.findViewById(R.id.lastName);
        course = view.findViewById(R.id.course);
        email = view.findViewById(R.id.email);
        candidateProfilePhoto = view.findViewById(R.id.candidateProfilePhoto);
        creationDate = Calendar.getInstance().getTime();

        manifesto.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String[] words = s.toString().split("\\s+");
                int wordCount = words.length;

                if (wordCount > 30) {
                    //if the word count exceeds 30 remove the excess words
                    String trimmedInput = TextUtils.join(" ", Arrays.copyOfRange(words, 0, 30));
                    manifesto.setText(trimmedInput);
                    manifesto.setSelection(trimmedInput.length());
                    manifesto.setError("Manifesto should not exceed 30 words");
                } else {
                    //Update the word count
                    TextView wordCountDisplayTextView = view.findViewById(R.id.wordCountDisplayTextView);
                    wordCountDisplayTextView.setText(wordCount + "/30" + " words");
                }
            }
        });


        String candidateId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();

        mDatabase.child("users").child(candidateId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                if (user != null) {
                    // Set the user's details to the EditText fields
                    registrationNumber.setText(user.getRegistrationNumber());
                    firstName.setText(user.getFirstName());
                    middleName.setText(user.getMiddleName());
                    lastName.setText(user.getLastName());
                    course.setText(user.getCourse());

                    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                    if (currentUser != null) {
                        String userEmail = currentUser.getEmail();
                        // Now you can set this email to your EditText
                        email.setText(userEmail);
                    }

                    if (user != null && user.getProfilePhoto() != null) {
                        profilePhoto = user.getProfilePhoto();

                        // Load the image into the ImageView using Glide
                        Glide.with(VieFragment.this).load(profilePhoto).into(candidateProfilePhoto);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle possible errors.
            }
        });
        vieButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String candidateId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();

                // Check if the candidate has already applied for any seat
                mDatabase.child("candidates").orderByChild("candidateId").equalTo(candidateId).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            // The candidate has already applied for a seat, inform them and redirect to HomeFragment
                            Toast.makeText(getActivity(), "You cannot apply for more than one seat", Toast.LENGTH_SHORT).show();
                            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.flFragment, new HomeFragment()).commit();

                            return;
                        }

                        // Continue with the application process if the candidate has not applied for any seat
                        String manifestoText = manifesto.getText().toString();
                        String positionText = position.getSelectedItem().toString();
                        String courseText = course.getText().toString();
                        String emailText = email.getText().toString();
                        String firstNameText = firstName.getText().toString();
                        String middleNameText = middleName.getText().toString();
                        String lastNameText = lastName.getText().toString();
                        String registrationNumberText = registrationNumber.getText().toString();
                        String creationDate = sdf.format(Calendar.getInstance().getTime());

                        // Validate the input fields
                        if (TextUtils.isEmpty(manifestoText) || TextUtils.isEmpty(positionText) || TextUtils.isEmpty(courseText) || TextUtils.isEmpty(emailText)) {
                            // Show an error message and return
                            Toast.makeText(getActivity(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // Save the candidate's details to the Firebase Database
                        Candidate candidate = new Candidate(candidateId, manifestoText, positionText, courseText, emailText, profilePhoto, firstNameText, middleNameText, lastNameText, registrationNumberText, creationDate, "approved", null, 0);
                        mDatabase.child("candidates").child(candidateId).setValue(candidate).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(getActivity(), "Your application has been submitted successfully", Toast.LENGTH_SHORT).show();
                                    // Navigate the user back to the home fragment
                                    getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.flFragment, new HomeFragment()).commit();

                                } else {
                                    Toast.makeText(getActivity(), "Failed to save data: " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                                    getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.flFragment, new HomeFragment()).commit();
                                }
                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // Handle possible errors.
                    }
                });
            }
        });
        return view;
    }
}
