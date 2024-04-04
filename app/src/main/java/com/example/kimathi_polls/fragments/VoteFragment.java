package com.example.kimathi_polls.fragments;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kimathi_polls.R;
import com.example.kimathi_polls.adapters.CandidateAdapter;
import com.example.kimathi_polls.beans.Alert;
import com.example.kimathi_polls.beans.Ballot;
import com.example.kimathi_polls.beans.Batch;
import com.example.kimathi_polls.beans.Biometrics;
import com.example.kimathi_polls.beans.Candidate;
import com.example.kimathi_polls.beans.User;
import com.example.kimathi_polls.interfaces.CandidateSelectionListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class VoteFragment extends Fragment implements CandidateSelectionListener {

    private RecyclerView recyclerView;
    private List<Candidate> candidateList = new ArrayList<>();
    private CandidateAdapter adapter;
    private Spinner positionSpinner;
    private Map<String, User> userMap = new HashMap<>();
    private TextView errorTextView;
    private FirebaseAuth mAuth;
    private Map<String, Ballot> selectedCandidates = new HashMap<>(); // Changed to Map
    Biometrics biometrics;


    @Override
    public void onCandidateSelected(Candidate candidate) {
        String position = candidate.getPosition();
        adapter.checkDatabaseForSelectedCandidate(candidate.getCandidateId(), isSelected -> {
            if (!isSelected) {
                selectedCandidate(candidate.getCandidateId(), position);
            } else {
                Toast.makeText(getContext(), "You have already voted for this candidate.", Toast.LENGTH_SHORT).show();
            }
        });
    }


    public void selectedCandidate(String candidateId, String seat) {
        Ballot ballot = new Ballot();
        ballot.setCandidateId(candidateId);
        ballot.setSeat(seat);

        // Populate the other fields of the Ballot object
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            ballot.setVoterId(user.getUid());
        }

        String verificationId = UUID.randomUUID().toString();
        ballot.setVerificationId(verificationId);

        String votedOn = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault()).format(Calendar.getInstance().getTime());
        ballot.setVotedOn(votedOn);

        ballot.setBatch(String.valueOf(Calendar.getInstance().get(Calendar.YEAR)));

        ballot.setVoterIpAddress(getIpAddress());
        ballot.setVoterMacAddress(getMacAddress());

        // Check if an entry with the same seat already exists
        if (!selectedCandidates.containsKey(seat)) {
            // If not, add a new entry
            selectedCandidates.put(seat, ballot);
        } else {
            // If yes, replace the existing entry with the new one
            selectedCandidates.put(seat, ballot);
        }
    }


    public void confirmSelection() {
        biometrics.showBiometricPrompt();

        // Create a new Batch object
        int year = Calendar.getInstance().get(Calendar.YEAR);
        Date electionStartDate = new Date(); // The start date is the current date
        Date electionEndDate = new Date(electionStartDate.getTime() + 240 * 60 * 1000); // The end date is 2 hours after the start date
        Date electionResultsDate = new Date(electionEndDate.getTime() + 20 * 60 * 1000); // The results date is 20 minutes after the end date
        Batch batch = new Batch(year, new ArrayList<>(), new ArrayList<>(), electionStartDate, electionEndDate, electionResultsDate);

        // Save the batch to Firebase
        DatabaseReference batchRef = FirebaseDatabase.getInstance().getReference("batches");
        batchRef.child(String.valueOf(year)).setValue(batch);

        // Validate each ballot but don't save it yet
        for (Ballot ballot : selectedCandidates.values()) {
            validateBallot(ballot, batch);
        }
    }

    private void validateBallot(Ballot ballot, Batch batch) {
        // Check if the ballot is after the election end date
        if (new Date().after(batch.getElectionEndDate())) {
            ballot.setStatus("spoilt");
            AlertDialog alertDialog = new AlertDialog.Builder(getContext())
                    .setMessage("The election has ended. Your vote has been marked as spoilt.")
                    .setPositiveButton("OK", null)
                    .create();
            alertDialog.show();
            return;
        }

        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("ballots");
        dbRef.orderByChild("voterIpAddress").equalTo(ballot.getVoterIpAddress()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // A vote has already been cast from this IP address
                    ballot.setStatus("spoilt");
                    AlertDialog alertDialog = new AlertDialog.Builder(getContext())
                            .setMessage("You have already cast a vote from this IP address. Your vote has been marked as spoilt.")
                            .setPositiveButton("OK", null)
                            .create();
                    alertDialog.show();
                } else {
                    // No vote has been cast from this IP address, proceed to check the alerts
                    DatabaseReference alertRef = FirebaseDatabase.getInstance().getReference("alerts");
                    alertRef.orderByChild("violatorRegistrationNumber").equalTo(ballot.getVoterId()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                ballot.setStatus("spoilt");
                                AlertDialog alertDialog = new AlertDialog.Builder(getContext())
                                        .setMessage("You have been flagged for violating the rules of the election. Your vote has been marked as spoilt.")
                                        .setPositiveButton("OK", null)
                                        .create();
                                alertDialog.show();
                            } else {
                                alertRef.orderByChild("violatorEmail").equalTo(FirebaseAuth.getInstance().getCurrentUser().getEmail()).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if (dataSnapshot.exists()) {
                                            ballot.setStatus("spoilt");
                                            AlertDialog alertDialog = new AlertDialog.Builder(getContext())
                                                    .setMessage("You have been flagged for violating the rules of the election. Your vote has been marked as spoilt.")
                                                    .setPositiveButton("OK", null)
                                                    .create();
                                            alertDialog.show();
                                        } else {
                                            ballot.setStatus("valid");
                                            AlertDialog alertDialog = new AlertDialog.Builder(getContext())
                                                    .setMessage("Your vote has been successfully cast.")
                                                    .setPositiveButton("OK", null)
                                                    .create();
                                            alertDialog.show();
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {
                                    }
                                });
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private void saveBallot(Ballot ballot) {
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("ballots");
        dbRef.push().setValue(ballot);
    }

    private void showAlert(String message, boolean isSuccess) {
        AlertDialog alertDialog = new AlertDialog.Builder(getContext())
                .setMessage(message)
                .setPositiveButton("OK", null)
                .create();

        alertDialog.show();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (alertDialog.isShowing()) {
                    alertDialog.dismiss();
                }
            }
        }, 3000);

        if (isSuccess) {
            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.flFragment, new HomeFragment()).commit();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_vote, container, false);

        final TextView voteDescriptionTextView = view.findViewById(R.id.voteDescriptionTextView);
        errorTextView = view.findViewById(R.id.errorTextView);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                voteDescriptionTextView.setVisibility(View.GONE);
            }
        }, 10000);

        recyclerView = view.findViewById(R.id.ballotRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        positionSpinner = view.findViewById(R.id.position);
        mAuth = FirebaseAuth.getInstance();

        Button backButton = view.findViewById(R.id.backButton);
        Button nextButton = view.findViewById(R.id.nextButton);
        Button confirmButton = view.findViewById(R.id.confirmButton);

        Drawable backIcon = backButton.getCompoundDrawables()[0];
        if (backIcon != null) {
            backIcon.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
        }

        Drawable nextIcon = nextButton.getCompoundDrawables()[0];
        if (nextIcon != null) {
            nextIcon.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
        }

        Drawable confirmIcon = confirmButton.getCompoundDrawables()[0];
        if (confirmIcon != null) {
            confirmIcon.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
        }

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int currentPosition = positionSpinner.getSelectedItemPosition();
                int newPosition = (currentPosition - 1 + positionSpinner.getCount()) % positionSpinner.getCount();
                positionSpinner.setSelection(newPosition);
            }
        });

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int currentPosition = positionSpinner.getSelectedItemPosition();
                int newPosition = (currentPosition + 1) % positionSpinner.getCount();
                positionSpinner.setSelection(newPosition);
            }
        });

        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                confirmSelection();
            }
        });

        positionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateCandidates();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        biometrics = new Biometrics(getActivity(), new Runnable() {
            @Override
            public void run() {
                for (Ballot ballot : selectedCandidates.values()) {
                    // Save the ballot only after the biometric authentication is successful
                    saveBallot(ballot);
                }
            }
        });
    }

    private void updateCandidates() {
        DatabaseReference candidatesRef = FirebaseDatabase.getInstance().getReference("candidates");
        candidatesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                candidateList.clear();
                String selectedPosition = positionSpinner.getSelectedItem().toString();
                for (DataSnapshot candidateSnapshot : dataSnapshot.getChildren()) {
                    Candidate candidate = candidateSnapshot.getValue(Candidate.class);
                    if (candidate.getPosition().equals(selectedPosition)) {
                        candidateList.add(candidate);
                    }
                }
                if (candidateList.isEmpty()) {
                    errorTextView.setVisibility(View.VISIBLE);
                    errorTextView.setText("No candidates vying for the selected position.");
                } else {
                    errorTextView.setVisibility(View.GONE);
                }

                adapter = new CandidateAdapter(getContext(), candidateList, userMap, VoteFragment.this);
                recyclerView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private String generateVerificationId() {
        return UUID.randomUUID().toString();
    }

    private String getIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException ex) {
            Log.e("VoteFragment", ex.toString());
        }
        return null;
    }

    private String getMacAddress() {
        try {
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : all) {
                byte[] macBytes = nif.getHardwareAddress();
                if (macBytes == null) {
                    continue;
                }

                StringBuilder res1 = new StringBuilder();
                for (byte b : macBytes) {
                    res1.append(Integer.toHexString(b & 0xFF) + ":");
                }

                if (res1.length() > 0) {
                    res1.deleteCharAt(res1.length() - 1);
                }
                return res1.toString();
            }
        } catch (Exception ex) {
            Log.e("VoteFragment", ex.toString());
        }
        return "02:00:00:00:00:00";
    }

    private Candidate getSelectedCandidate(String candidateId) {
        for (Candidate candidate : candidateList) {
            if (candidate.getCandidateId().equals(candidateId)) {
                return candidate;
            }
        }
        return null;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Biometrics.REQUEST_CODE) {
            // The result from the biometric prompt
            biometrics.showBiometricPrompt();
        }
    }
}