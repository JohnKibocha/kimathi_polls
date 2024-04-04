package com.example.kimathi_polls.beans;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.appcompat.app.AlertDialog;

public class FirstTimeUserDialogFragment extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Welcome to Kimathi Polls")
                .setMessage("To vote:\n\n" +
                        "1. The person must be a student of Dedan Kimathi.\n" +
                        "2. The person must have an account with Kimathi Polls.\n" +
                        "3. They must be within the school geographical area.\n\n" +
                        "Warnings:\n\n" +
                        "1. Any ballot cast twice from the same person shall be spoilt and will not be counted.\n" +
                        "2. Any ballot cast by the same phone, even with a different account shall be spoilt and will not be counted.\n" +
                        "3. Any report against a candidate will lead to their disqualification and any ballot they cast will be spoilt.\n\n" +
                        "To vie:\n\n" +
                        "1. You must have cleared at least 60% of your school fees.\n" +
                        "2. You must not have engaged in election misconduct.\n" +
                        "3. You must be a student at Dedan Kimathi University of Technology.\n" +
                        "4. You must clear with the Kimathi Electoral Commission.\n" +
                        "5. You must have an account with Kimathi Polls.\n\n" +
                        "Warnings:\n\n" +
                        "1. Any report against a candidate will lead to their disqualification and any ballot they cast will be spoilt.\n" +
                        "2. Any candidate who engages in election misconduct will be disqualified.\n" +
                        "3. Any candidate who does not meet the above requirements will be disqualified."
                )
                .setPositiveButton("I Know", null)
                .setNegativeButton("I Know & Don't Show Again", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        SharedPreferences sharedPref = getActivity().getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putBoolean("hidePopup", true);
                        editor.apply();
                    }
                });
        return builder.create();
    }
}