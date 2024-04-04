package com.example.kimathi_polls.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.kimathi_polls.R;
import com.example.kimathi_polls.beans.FirstTimeUserDialogFragment;

public class HomeFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        CardView cardWinner = view.findViewById(R.id.card_winner);
        cardWinner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                transaction.replace(R.id.flFragment, new AnalyticsFragment());
                transaction.commit();
                Toast.makeText(getActivity(), "Switched to Analytics", Toast.LENGTH_SHORT).show();
            }
        });

        CardView cardVote = view.findViewById(R.id.card_vote);
        cardVote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                transaction.replace(R.id.flFragment, new VoteFragment());
                transaction.commit();
                Toast.makeText(getActivity(), "Switched to Vote", Toast.LENGTH_SHORT).show();
            }
        });

        CardView cardAlert = view.findViewById(R.id.card_alert);
        cardAlert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                transaction.replace(R.id.flFragment, new AlertFragment());
                transaction.commit();
                Toast.makeText(getActivity(), "Switched to Alert", Toast.LENGTH_SHORT).show();
            }
        });

        CardView cardVie = view.findViewById(R.id.card_vie);
        cardVie.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                transaction.replace(R.id.flFragment, new VieFragment());
                transaction.commit();
                Toast.makeText(getActivity(), "Switched to Vie", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // run the alert dialog first time the user opens the app
        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
        boolean hidePopup = sharedPref.getBoolean("hidePopup", false);
        if (!hidePopup) {
            // show the dialog
            FirstTimeUserDialogFragment firstTimeUserDialogFragment = new FirstTimeUserDialogFragment();
            firstTimeUserDialogFragment.show(getFragmentManager(), "firstTimeUserDialogFragment");
        }
    }
}