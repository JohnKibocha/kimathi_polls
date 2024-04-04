package com.example.kimathi_polls.fragments;

import static android.app.Activity.RESULT_OK;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.kimathi_polls.R;
import com.example.kimathi_polls.beans.Alert;
import com.example.kimathi_polls.beans.Candidate;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import androidx.core.graphics.drawable.DrawableCompat;

public class AlertFragment extends Fragment {
    public static final int FILE_CHOOSER_REQUEST_CODE = 173;
    DatabaseReference mDatabase;
    int documentCount = 0;
    private Spinner violationSpinner;
    private EditText violatorEmailEditText, violatorRegistrationNumberEditText, descriptionEditText;
    private TextView dateOfViolationEditText, evidenceDisplayTextView;
    private ArrayAdapter<CharSequence> violationAdapter;
    private String actionStatusDefault = "Reviewed";
    private String actionTakenDefault = "Disqualified";
    private String actionExpiryDateDefault = "0";
    private String actionJustificationDefault = "The alegation was proven beyond reasonable doubt";
    private Button submitReportButton;
    private Uri fileUri;
    private StorageReference mStorageRef;
    private Button uploadEvidenceButton;
    private Alert alert;
    private DatePickerDialog datePickerDialog;
    private Calendar calendar;


    public void onDocumentAdded() {
        documentCount++;
        String message = documentCount + (documentCount > 1 ? "  pieces of evidence submitted" : " piece of evidence submitted");

        // Get the drawable and wrap it to apply tint
        Drawable drawable = ContextCompat.getDrawable(getContext(), R.drawable.ic_evidence);
        DrawableCompat.setTint(drawable, ContextCompat.getColor(getContext(), R.color.material_blue_dark));

        // Resize the drawable
        drawable.setBounds(0, 0, 70, 70);

        //align the drawable to the centre of the textview
        evidenceDisplayTextView.setGravity(Gravity.CENTER_VERTICAL);

        //set bottom padding to the textview to accommodate the drawable
        evidenceDisplayTextView.setPadding(20, 0, 0, 20);


        evidenceDisplayTextView.setText(message);
        evidenceDisplayTextView.setCompoundDrawables(drawable, null, null, null);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_alert, container, false);

        // Initialize views
        violationSpinner = view.findViewById(R.id.violationSpinner);
        violatorEmailEditText = view.findViewById(R.id.violatorEmailEditText);
        violatorRegistrationNumberEditText = view.findViewById(R.id.violatorRegistrationNumberEditText);
        dateOfViolationEditText = view.findViewById(R.id.dateOfViolationEditText);
        evidenceDisplayTextView = view.findViewById(R.id.evidenceDisplayTextView);
        descriptionEditText = view.findViewById(R.id.descriptionEditText);
        submitReportButton = view.findViewById(R.id.submitReportButton);
        violationAdapter = ArrayAdapter.createFromResource(getContext(), R.array.violations, android.R.layout.simple_spinner_item);
        uploadEvidenceButton = view.findViewById(R.id.uploadEvidenceButton);
        calendar = Calendar.getInstance();
        //initialize the database reference
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mStorageRef = FirebaseStorage.getInstance().getReference();


        // Initialize DatePickerDialog
        datePickerDialog = new DatePickerDialog(
                getContext(),
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        // Update dateOfViolationEditText with the selected date
                        dateOfViolationEditText.setText(dayOfMonth + "-" + (month + 1) + "-" + year);
                        alert.setDateOfViolation(dayOfMonth + "-" + (month + 1) + "-" + year);
                    }
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        dateOfViolationEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show DatePickerDialog when dateOfViolationEditText is clicked
                datePickerDialog.show();
            }
        });


        uploadEvidenceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                startActivityForResult(intent, FILE_CHOOSER_REQUEST_CODE);
            }
        });

        // Specify the layout to use when the list of choices appears
        violationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Apply the adapter to the spinner
        violationSpinner.setAdapter(violationAdapter);

        alert = new Alert(
                violationSpinner.getSelectedItem().toString(),
                violatorEmailEditText.getText().toString(),
                violatorRegistrationNumberEditText.getText().toString(),
                dateOfViolationEditText.getText().toString(),
                evidenceDisplayTextView.getText().toString(),
                descriptionEditText.getText().toString(),
                actionStatusDefault,
                actionTakenDefault,
                actionExpiryDateDefault,
                actionJustificationDefault,
                new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault()).format(Calendar.getInstance().getTime())
        );

        submitReportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String reportedOn = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault()).format(Calendar.getInstance().getTime());

                alert.setReportedOn(reportedOn);
                alert.setViolation(violationSpinner.getSelectedItem().toString());
                alert.setViolatorEmail(violatorEmailEditText.getText().toString());
                alert.setViolatorRegistrationNumber(violatorRegistrationNumberEditText.getText().toString());
                alert.setDateOfViolation(dateOfViolationEditText.getText().toString());
                alert.setDescription(descriptionEditText.getText().toString());
                alert.setActionStatus(actionStatusDefault);
                alert.setActionTaken(actionTakenDefault);
                alert.setActionExpiryDate(actionExpiryDateDefault);
                alert.setActionJustification(actionJustificationDefault);

                DatabaseReference alertsRef = mDatabase.child("alerts");
                String alertId = alertsRef.push().getKey();

                alertsRef.child(alertId).setValue(alert).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(getActivity(), "Report Submitted successfully", Toast.LENGTH_SHORT).show();

                            DatabaseReference candidatesRef = mDatabase.child("candidates");
                            candidatesRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    for (DataSnapshot candidateSnapshot : dataSnapshot.getChildren()) {
                                        Candidate candidate = candidateSnapshot.getValue(Candidate.class);
                                        if (candidate != null) {
                                            if (candidate.getEmail().equals(alert.getViolatorEmail()) || candidate.getRegistrationNumber().equals(alert.getViolatorRegistrationNumber())) {
                                                candidateSnapshot.getRef().child("applicationStatus").setValue("disqualified");
                                                candidateSnapshot.getRef().child("violation").setValue(alert.getViolation());

                                            }
                                        }
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    // Handle error
                                }
                            });

                            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.flFragment, new HomeFragment()).commit();

                        } else {
                            Toast.makeText(getActivity(), "Report Submission failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == FILE_CHOOSER_REQUEST_CODE && resultCode == RESULT_OK) {
            fileUri = data.getData();

            StorageReference fileRef = mStorageRef.child("files/" + fileUri.getLastPathSegment());
            fileRef.putFile(fileUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    String fileUrl = uri.toString();
                                    alert.setEvidence(fileUrl);
                                    onDocumentAdded();
                                }
                            });
                        }
                    });
        }
    }
}