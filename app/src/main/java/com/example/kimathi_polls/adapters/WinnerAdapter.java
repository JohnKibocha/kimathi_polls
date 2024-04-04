
package com.example.kimathi_polls.adapters;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.graphics.Color;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.kimathi_polls.R;
import com.example.kimathi_polls.beans.Candidate;

import java.util.List;

public class WinnerAdapter extends RecyclerView.Adapter<WinnerAdapter.WinnerViewHolder> {

    private List<Candidate> winnersList;

    public WinnerAdapter(List<Candidate> winnersList) {
        this.winnersList = winnersList;
    }

    @NonNull
    @Override
    public WinnerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (winnersList.isEmpty()) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_default, parent, false);
        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_winner, parent, false);
        }
        return new WinnerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WinnerViewHolder holder, int position) {
        Candidate winner = winnersList.get(position);

        holder.name.setText(winner.getFirstName() + " " + winner.getLastName());
        holder.registrationNumber.setText(winner.getRegistrationNumber());
        holder.course.setText(Html.fromHtml("<b>Course:</b> " + winner.getCourse()));
        holder.position.setText(Html.fromHtml("<b>Position:</b> " + winner.getPosition()));
        holder.votes.setText(Html.fromHtml("<b>Votes:</b> " + winner.getVotes()));
        Glide.with(holder.itemView.getContext()).load(winner.getProfilePhoto()).into(holder.profilePhoto);

        // Add animation to the congratulations message
        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), Color.BLUE, Color.RED, Color.GREEN, Color.MAGENTA, Color.CYAN, Color.BLUE);
        colorAnimation.setDuration(3000);
        colorAnimation.setRepeatCount(ValueAnimator.INFINITE);
        colorAnimation.setRepeatMode(ValueAnimator.REVERSE);
        colorAnimation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(@NonNull ValueAnimator animation) {
                holder.congratulationsMessage.setTextColor((Integer) animation.getAnimatedValue());
            }
        });
        colorAnimation.start();
    }

    @Override
    public int getItemCount() {
        return winnersList.size();
    }

    static class WinnerViewHolder extends RecyclerView.ViewHolder {

        ImageView profilePhoto;
        TextView name;
        TextView registrationNumber;
        TextView course;
        TextView position;
        TextView votes;
        TextView congratulationsMessage;

        WinnerViewHolder(@NonNull View itemView) {
            super(itemView);

            profilePhoto = itemView.findViewById(R.id.profile_photo);
            name = itemView.findViewById(R.id.name);
            registrationNumber = itemView.findViewById(R.id.registration_number);
            course = itemView.findViewById(R.id.course);
            position = itemView.findViewById(R.id.position);
            votes = itemView.findViewById(R.id.votes);
            congratulationsMessage = itemView.findViewById(R.id.congratulations_message);
        }
    }
}