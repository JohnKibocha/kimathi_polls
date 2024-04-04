package com.example.kimathi_polls.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kimathi_polls.R;

public class BallotHealthAdapter extends RecyclerView.Adapter<BallotHealthAdapter.BallotHealthViewHolder> {

    private int totalTurnout;
    private int totalVotesCast;
    private int validVotes;
    private int spoiltVotes;
    private double percentageValidVotes;
    private double percentageSpoiltVotes;

    public BallotHealthAdapter(int totalTurnout, int totalVotesCast, int validVotes, int spoiltVotes, double percentageValidVotes, double percentageSpoiltVotes) {
        this.totalTurnout = totalTurnout;
        this.totalVotesCast = totalVotesCast;
        this.validVotes = validVotes;
        this.spoiltVotes = spoiltVotes;
        this.percentageValidVotes = percentageValidVotes;
        this.percentageSpoiltVotes = percentageSpoiltVotes;
    }

    @NonNull
    @Override
    public BallotHealthViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ballot_health, parent, false);
        return new BallotHealthViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BallotHealthViewHolder holder, int position) {
        holder.totalTurnout.setText(String.valueOf(totalTurnout));
        holder.totalVotesCast.setText(String.valueOf(totalVotesCast));
        holder.validVotes.setText(String.valueOf(validVotes));
        holder.spoiltVotes.setText(String.valueOf(spoiltVotes));
        holder.percentageValidVotes.setText(String.format("%.2f%%", percentageValidVotes));
        holder.percentageSpoiltVotes.setText(String.format("%.2f%%", percentageSpoiltVotes));
    }

    @Override
    public int getItemCount() {
        return 1; // Since we are displaying only one set of data
    }

    static class BallotHealthViewHolder extends RecyclerView.ViewHolder {
        TextView totalTurnout, totalVotesCast, validVotes, spoiltVotes, percentageValidVotes, percentageSpoiltVotes;

        BallotHealthViewHolder(@NonNull View itemView) {
            super(itemView);
            totalTurnout = itemView.findViewById(R.id.total_turnout);
            totalVotesCast = itemView.findViewById(R.id.total_votes_cast);
            validVotes = itemView.findViewById(R.id.valid_votes);
            spoiltVotes = itemView.findViewById(R.id.spoilt_votes);
            percentageValidVotes = itemView.findViewById(R.id.percentage_valid_votes);
            percentageSpoiltVotes = itemView.findViewById(R.id.percentage_spoilt_votes);
        }
    }
}