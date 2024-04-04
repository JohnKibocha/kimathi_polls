package com.example.kimathi_polls.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kimathi_polls.R;
import com.example.kimathi_polls.beans.Ballot;
import com.example.kimathi_polls.beans.Candidate;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;
import java.util.Locale;

public class ResultAdapter extends RecyclerView.Adapter<ResultAdapter.ResultViewHolder> {

    private List<Candidate> candidatesList;
    private DatabaseReference ballotRef = FirebaseDatabase.getInstance().getReference("ballots");

    public ResultAdapter(List<Candidate> candidatesList) {
        this.candidatesList = candidatesList;
    }

    @NonNull
    @Override
    public ResultViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_result, parent, false);
        return new ResultViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ResultViewHolder holder, int position) {
        Candidate candidate = candidatesList.get(position);
        holder.name.setText(candidate.getFirstName() + " " + candidate.getLastName());
        holder.applicationStatus.setText(candidate.getApplicationStatus());
        holder.position.setText(candidate.getPosition());
        holder.validVotes.setText(String.valueOf(candidate.getVotes()));

        // Fetch all the ballots
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

                // Set the values for spoiltVotes and percentageVotes
                holder.spoiltVotes.setText(String.valueOf(candidateSpoiltVotes));
                holder.percentageVotes.setText(String.format(Locale.getDefault(), "%.2f%%", percentageVotes));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("ResultAdapter", "Failed to read value.", databaseError.toException());
            }
        });
    }

    @Override
    public int getItemCount() {
        return candidatesList.size();
    }

    static class ResultViewHolder extends RecyclerView.ViewHolder {

        TextView name;
        TextView applicationStatus;
        TextView position;
        TextView validVotes;
        TextView spoiltVotes;
        TextView percentageVotes;

        ResultViewHolder(@NonNull View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.name);
            applicationStatus = itemView.findViewById(R.id.application_status);
            position = itemView.findViewById(R.id.position);
            validVotes = itemView.findViewById(R.id.valid_votes);
            spoiltVotes = itemView.findViewById(R.id.spoilt_votes);
            percentageVotes = itemView.findViewById(R.id.percentage_votes);
        }
    }
}