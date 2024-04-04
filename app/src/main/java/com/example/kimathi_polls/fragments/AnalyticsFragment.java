package com.example.kimathi_polls.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kimathi_polls.R;
import com.example.kimathi_polls.adapters.BallotHealthAdapter;
import com.example.kimathi_polls.adapters.ResultAdapter;
import com.example.kimathi_polls.adapters.WinnerAdapter;
import com.example.kimathi_polls.beans.Ballot;
import com.example.kimathi_polls.beans.Candidate;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnalyticsFragment extends Fragment {

    private RecyclerView winnersRecyclerView;
    private RecyclerView ballotHealthRecyclerView;
    private List<Candidate> candidatesList = new ArrayList<>();
    private Map<String, Candidate> winnersMap = new HashMap<>();
    private Handler handler = new Handler();
    private RecyclerView resultsRecyclerView;
    private DatabaseReference ballotRef = FirebaseDatabase.getInstance().getReference("ballots");


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_analytics, container, false);

        winnersRecyclerView = view.findViewById(R.id.winners_recycler_view);
        resultsRecyclerView = view.findViewById(R.id.results_recycler_view);
        ballotHealthRecyclerView = view.findViewById(R.id.ballot_health_recycler_view);


        // Run the updateData() method once when the view is created
        updateData();

        return view;
    }

    private void updateData() {
        DatabaseReference candidatesRef = FirebaseDatabase.getInstance().getReference("candidates");
        candidatesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                candidatesList.clear();
                for (DataSnapshot candidateSnapshot : dataSnapshot.getChildren()) {
                    Candidate candidate = candidateSnapshot.getValue(Candidate.class);
                    candidatesList.add(candidate);
                }

                // Fetch ballots and count votes for each candidate
                DatabaseReference ballotsRef = FirebaseDatabase.getInstance().getReference("ballots");
                ballotsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot ballotSnapshot : dataSnapshot.getChildren()) {
                            Ballot ballot = ballotSnapshot.getValue(Ballot.class);
                            for (Candidate candidate : candidatesList) {
                                if (ballot.getCandidateId().equals(candidate.getCandidateId())
                                        && !ballot.getStatus().equals("spoilt")
                                        && !candidate.getApplicationStatus().equals("disqualified")) {
                                    candidate.incrementVotes(); // Increment the votes for this candidate
                                }
                            }
                        }

                        // Determine winners
                        updateWinners();

                        // Update Firebase
                        updateFirebase();

                        // Update Ballot Health
                        updateBallotHealth();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.e("AnalyticsFragment", "Failed to fetch ballots", databaseError.toException());
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("AnalyticsFragment", "Failed to fetch candidates", databaseError.toException());
            }
        });
    }

    private void updateBallotHealth() {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
        DatabaseReference ballotsRef = FirebaseDatabase.getInstance().getReference("ballots");

        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int totalTurnout = (int) dataSnapshot.getChildrenCount();

                ballotsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        int totalVotesCast = (int) dataSnapshot.getChildrenCount();
                        int validVotes = 0;
                        int spoiltVotes = 0;

                        for (DataSnapshot ballotSnapshot : dataSnapshot.getChildren()) {
                            Ballot ballot = ballotSnapshot.getValue(Ballot.class);
                            if (ballot != null) {
                                if (ballot.getStatus().equals("valid")) {
                                    validVotes++;
                                } else if (ballot.getStatus().equals("spoilt")) {
                                    spoiltVotes++;
                                }
                            }
                        }

                        double percentageValidVotes = ((double) validVotes / totalVotesCast) * 100;
                        double percentageSpoiltVotes = ((double) spoiltVotes / totalVotesCast) * 100;

                        // Now you can use these values to update your UI. Use customlinearLayoutManager to disable scrolling
                        BallotHealthAdapter ballotHealthAdapter = new BallotHealthAdapter(totalTurnout, totalVotesCast, validVotes, spoiltVotes, percentageValidVotes, percentageSpoiltVotes);
                        ballotHealthRecyclerView.setAdapter(ballotHealthAdapter);
                        ballotHealthRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.e("AnalyticsFragment", "Failed to fetch ballots", databaseError.toException());
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("AnalyticsFragment", "Failed to fetch users", databaseError.toException());
            }
        });
    }

    private void updateWinners() {
        winnersMap.clear();
        for (Candidate candidate : candidatesList) {
            Candidate currentWinner = winnersMap.get(candidate.getPosition());
            if (currentWinner == null || candidate.getVotes() > currentWinner.getVotes()) {
                winnersMap.put(candidate.getPosition(), candidate);
            }
        }
    }

    private void updateFirebase() {
        DatabaseReference analyticsRef = FirebaseDatabase.getInstance().getReference("analytics");
        DatabaseReference winnersRef = analyticsRef.child("winners");
        DatabaseReference resultsRef = analyticsRef.child("results");
        DatabaseReference metricsRef = analyticsRef.child("candidate_metrics"); // New reference to the "candidate metrics" node

        // Update winners
        for (Candidate winner : winnersMap.values()) {
            Map<String, Object> winnerMap = new HashMap<>();
            winnerMap.put("userId", winner.getCandidateId());
            winnerMap.put("firstName", winner.getFirstName());
            winnerMap.put("lastName", winner.getLastName());
            winnerMap.put("position", winner.getPosition());
            winnerMap.put("votes", winner.getVotes());

            winnersRef.child(winner.getPosition()).setValue(winnerMap);
        }

        // Update Ballot Health
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
        DatabaseReference ballotsRef = FirebaseDatabase.getInstance().getReference("ballots");
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int totalTurnout = (int) dataSnapshot.getChildrenCount();

                ballotsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        int totalVotesCast = (int) dataSnapshot.getChildrenCount();
                        int validVotes = 0;
                        int spoiltVotes = 0;

                        for (DataSnapshot ballotSnapshot : dataSnapshot.getChildren()) {
                            Ballot ballot = ballotSnapshot.getValue(Ballot.class);
                            if (ballot != null) {
                                if (ballot.getStatus().equals("valid")) {
                                    validVotes++;
                                } else if (ballot.getStatus().equals("spoilt")) {
                                    spoiltVotes++;
                                }
                            }
                        }

                        double percentageValidVotes = ((double) validVotes / totalVotesCast) * 100;
                        double percentageSpoiltVotes = ((double) spoiltVotes / totalVotesCast) * 100;

                        // Create a map for the ballot health data
                        Map<String, Object> ballotHealthMap = new HashMap<>();
                        ballotHealthMap.put("totalTurnout", totalTurnout);
                        ballotHealthMap.put("totalVotesCast", totalVotesCast);
                        ballotHealthMap.put("validVotes", validVotes);
                        ballotHealthMap.put("spoiltVotes", spoiltVotes);
                        ballotHealthMap.put("percentageTotalValid", percentageValidVotes);
                        ballotHealthMap.put("percentageTotalSpoilt", percentageSpoiltVotes);

                        // Save the ballot health data to Firebase
                        DatabaseReference analyticsRef = FirebaseDatabase.getInstance().getReference("analytics");
                        DatabaseReference ballotHealthRef = analyticsRef.child("ballot_health");
                        ballotHealthRef.setValue(ballotHealthMap);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.e("AnalyticsFragment", "Failed to fetch ballots", databaseError.toException());
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("AnalyticsFragment", "Failed to fetch users", databaseError.toException());
            }
        });

        // Update results and candidate metrics
        for (Candidate candidate : candidatesList) {
            Map<String, Object> candidateMap = new HashMap<>();
            candidateMap.put("userId", candidate.getCandidateId());
            candidateMap.put("firstName", candidate.getFirstName());
            candidateMap.put("lastName", candidate.getLastName());
            candidateMap.put("position", candidate.getPosition());
            candidateMap.put("votes", candidate.getVotes());

            resultsRef.child(candidate.getCandidateId()).setValue(candidateMap);

            // Calculate the metrics for this candidate
            ballotRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    int totalValidVotes = 0;
                    int candidateSpoiltVotes = 0;

                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Ballot ballot = snapshot.getValue(Ballot.class);
                        if (ballot != null) {
                            if (!ballot.getStatus().equals("spoilt")) {
                                totalValidVotes++;
                            }
                            if (ballot.getCandidateId().equals(candidate.getCandidateId()) && ballot.getStatus().equals("spoilt")) {
                                candidateSpoiltVotes++;
                            }
                        }
                    }

                    // Calculate the percentage votes by dividing the valid votes by the total number of ballots cast and multiplying by 100
                    double percentageVotes = ((double) candidate.getVotes() / totalValidVotes) * 100;

                    // Create a map for the metrics
                    Map<String, Object> metricsMap = new HashMap<>();
                    metricsMap.put("spoiltVotes", candidateSpoiltVotes);
                    metricsMap.put("percentageVotes", percentageVotes);
                    metricsMap.put("totalValidVotes", totalValidVotes);
                    metricsMap.put("totalVotes", totalValidVotes + candidateSpoiltVotes);

                    // Store the metrics in the "candidate metrics" node
                    metricsRef.child(candidate.getCandidateId()).setValue(metricsMap);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e("AnalyticsFragment", "Failed to read value.", databaseError.toException());
                }
            });
        }


        // set the adapter to the recycler view
        WinnerAdapter winnerAdapter = new WinnerAdapter(new ArrayList<>(winnersMap.values()));
        winnersRecyclerView.setAdapter(winnerAdapter);
        winnersRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        // set the adapter to the results recycler view
        ResultAdapter resultAdapter = new ResultAdapter(candidatesList);
        resultsRecyclerView.setAdapter(resultAdapter);
        resultsRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        // Schedule updates
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                updateData();
            }
        }, 10000); // 10000 milliseconds = 10 seconds
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null); // Stop all scheduled tasks when the fragment is destroyed
    }
}