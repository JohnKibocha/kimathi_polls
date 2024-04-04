package com.example.kimathi_polls.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.kimathi_polls.beans.Candidate;
import com.example.kimathi_polls.interfaces.CandidateSelectionListener;
import com.example.kimathi_polls.interfaces.OnCheckDatabaseCallback;
import com.example.kimathi_polls.R;
import com.example.kimathi_polls.fragments.VoteFragment;
import com.example.kimathi_polls.beans.Ballot;
import com.example.kimathi_polls.beans.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;
import java.util.Map;

public class CandidateAdapter extends RecyclerView.Adapter<CandidateAdapter.CandidateViewHolder> {

    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("ballots");
    private List<Candidate> candidateList;
    private Map<String, User> userMap;
    private CandidateSelectionListener candidateSelectionListener;

    // add context member variable
    private Context context;

    public CandidateAdapter(Context context, List<Candidate> candidateList, Map<String, User> userMap, CandidateSelectionListener candidateSelectionListener) {
        this.context = context;
        this.candidateList = candidateList;
        this.userMap = userMap;
        this.candidateSelectionListener = candidateSelectionListener;

        // Clear the selected candidate from the shared preferences
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("selectedCandidate", null);
        editor.apply();
    }

    @NonNull
    @Override
    public CandidateViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_vote, parent, false);

        return new CandidateViewHolder(itemView);
    }

    public void updateCandidateList(List<Candidate> updatedCandidateList) {
        this.candidateList = updatedCandidateList;
        notifyDataSetChanged();
    }

    private void saveBallotToPreferences(Candidate candidate) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(((Fragment) candidateSelectionListener).getContext());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("selectedCandidate", candidate.getCandidateId());
        editor.apply();
    }

    private void updateButton(Button button, Candidate candidate, Button confirmButton, CandidateViewHolder holder) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(((Fragment) candidateSelectionListener).getContext());
        String selectedCandidateId = sharedPreferences.getString("selectedCandidate", null);
        if (selectedCandidateId != null && selectedCandidateId.equals(candidate.getCandidateId())) {
            button.setText("Selected");
            button.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_confirm, 0, 0, 0);
            button.setBackgroundColor(ContextCompat.getColor(((Fragment) candidateSelectionListener).getContext(), R.color.material_green_dark));
        } else {
            checkDatabaseForSelectedCandidate(candidate.getCandidateId(), new OnCheckDatabaseCallback() {
                @Override
                public void onCallback(boolean isSelected) {
                    if (isSelected) {
                        button.setText("Already voted");
                        button.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_prohibited, 0, 0, 0);
                        // change icon color to white
                        Drawable buttonIcon = button.getCompoundDrawables()[0];
                        if (buttonIcon != null) {
                            buttonIcon.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
                        }
                        button.setBackgroundColor(ContextCompat.getColor(((Fragment) candidateSelectionListener).getContext(), R.color.material_grey_dark));
                        button.setEnabled(false);

                        confirmButton.setText("Voted");
                        confirmButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_prohibited, 0, 0, 0);
                        // change icon color to white
                        Drawable confirmButtonIcon = confirmButton.getCompoundDrawables()[0];
                        if (confirmButtonIcon != null) {
                            confirmButtonIcon.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
                        }
                        confirmButton.setBackgroundColor(ContextCompat.getColor(((Fragment) candidateSelectionListener).getContext(), R.color.material_grey_dark));
                        confirmButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Toast.makeText(v.getContext(), "You have already voted", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        button.setText("Select");
                        button.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_select, 0, 0, 0);
                        button.setBackgroundColor(ContextCompat.getColor(((Fragment) candidateSelectionListener).getContext(), R.color.material_blue_dark));
                        button.setEnabled(true);
                    }
                }
            });
        }
    }

    @Override
    public void onBindViewHolder(@NonNull CandidateViewHolder holder, int position) {
        Candidate candidate = candidateList.get(position);
        holder.nameTextView.setText("Name: " + candidate.getFirstName() + " " + candidate.getLastName());
        holder.courseTextView.setText("Course: " + candidate.getCourse());
        holder.registrationNumberTextView.setText("Registration Number: " + candidate.getRegistrationNumber());
        holder.positionTextView.setText("Position: " + candidate.getPosition());
        holder.manifestoTextView.setText(candidate.getManifesto());
        Glide.with(holder.itemView.getContext())
                .load(candidate.getProfilePhoto())
                .into(holder.profileImageView);

        if ("disqualified".equals(candidate.getApplicationStatus())) {
            // Handle disqualified candidates
            holder.selectCandidateButton.setText("Disqualified");
            holder.selectCandidateButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_disqualified, 0, 0, 0);
            holder.selectCandidateButton.setBackgroundColor(ContextCompat.getColor(((Fragment) candidateSelectionListener).getContext(), R.color.material_red_dark));
            holder.selectCandidateButton.setEnabled(false);

            // change the icon color to white
            Drawable buttonIcon = holder.selectCandidateButton.getCompoundDrawables()[0];
            if (buttonIcon != null) {
                buttonIcon.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
            }

            // change the manifesto to the value in the violation field
            holder.manifestoTextView.setText("Violation: " + candidate.getViolation());
        } else {
            // Handle regular candidates
            updateButton(holder.selectCandidateButton, candidate, holder.confirmButton, holder);
        }

        holder.selectCandidateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkDatabaseForSelectedCandidate(candidate.getCandidateId(), new OnCheckDatabaseCallback() {
                    @Override
                    public void onCallback(boolean isSelected) {
                        if (isSelected) {
                            Toast.makeText(v.getContext(), "This candidate has already been selected", Toast.LENGTH_SHORT).show();
                        } else {
                            if (holder.selectedCandidate != null) {
                                notifyItemChanged(candidateList.indexOf(holder.selectedCandidate));
                            }
                            holder.selectedCandidate = candidate;
                            candidateSelectionListener.selectedCandidate(holder.selectedCandidate.getCandidateId(), holder.selectedCandidate.getPosition());
                            saveBallotToPreferences(holder.selectedCandidate);
                            holder.selectCandidateButton.setText("Selected");
                            holder.selectCandidateButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_confirm, 0, 0, 0);
                            holder.selectCandidateButton.setBackgroundColor(ContextCompat.getColor(v.getContext(), R.color.material_green_dark));
                        }
                    }
                });
            }
        });
    }

    @Override
    public int getItemCount() {
        return candidateList.size();
    }

    public void checkDatabaseForSelectedCandidate(String candidateId, OnCheckDatabaseCallback callback) {
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("ballots");
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        dbRef.orderByChild("voterId").equalTo(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Ballot ballot = snapshot.getValue(Ballot.class);
                        if (ballot != null && ballot.getCandidateId().equals(candidateId)) {
                            callback.onCallback(true);
                            return;
                        }
                    }
                }
                callback.onCallback(false);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    public class CandidateViewHolder extends RecyclerView.ViewHolder {
        public TextView nameTextView;
        public TextView courseTextView;
        public TextView manifestoTextView;
        public TextView registrationNumberTextView;
        public TextView positionTextView;
        public ImageView profileImageView;
        Button selectCandidateButton;
        Button confirmButton;
        Candidate selectedCandidate; // Move the selectedCandidate variable here

        public CandidateViewHolder(View view) {
            super(view);
            nameTextView = view.findViewById(R.id.nameTextView);
            courseTextView = view.findViewById(R.id.courseTextView);
            manifestoTextView = view.findViewById(R.id.manifestoTextView);
            profileImageView = view.findViewById(R.id.profileImageView);
            registrationNumberTextView = view.findViewById(R.id.registrationNumberTextView);
            positionTextView = view.findViewById(R.id.positionTextView);
            selectCandidateButton = view.findViewById(R.id.selectCandidateButton);
            confirmButton = ((VoteFragment) candidateSelectionListener).getView().findViewById(R.id.confirmButton);

            Drawable buttonIcon = selectCandidateButton.getCompoundDrawables()[0];
            if (buttonIcon != null) {
                buttonIcon.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
            }
        }
    }
}