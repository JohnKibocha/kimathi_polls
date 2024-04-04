package com.example.kimathi_polls.fragments;

import static android.app.Activity.RESULT_OK;

import android.app.DatePickerDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.kimathi_polls.R;
import com.example.kimathi_polls.beans.Biometrics;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ProfileFragment extends Fragment {

    private static final int PICK_IMAGE_REQUEST = 1;
    EditText firstName, middleName, lastName, dob, admissionYear, registrationNumber;
    ImageView profilePhoto;
    Button saveButton;
    Spinner currentYear;
    Spinner certificate;
    AutoCompleteTextView course;
    FirebaseAuth mAuth;
    DatabaseReference mDatabase;
    SharedPreferences sharedPreferences;
    List<String> courses = new ArrayList<>();
    ArrayAdapter<String> adapter;
    private int selectedYear = 1;
    private int selectedSemester = 1;
    private StorageReference mStorageRef;
    Biometrics biometrics;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mStorageRef = FirebaseStorage.getInstance().getReference("uploads");

        firstName = view.findViewById(R.id.firstName);
        middleName = view.findViewById(R.id.middleName);
        lastName = view.findViewById(R.id.lastName);
        registrationNumber = view.findViewById(R.id.registrationNumber);
        course = view.findViewById(R.id.course);
        dob = view.findViewById(R.id.dob);
        admissionYear = view.findViewById(R.id.admissionYear);
        currentYear = view.findViewById(R.id.currentYear);
        certificate = view.findViewById(R.id.certificate);
        profilePhoto = view.findViewById(R.id.candidateProfilePhoto);
        saveButton = view.findViewById(R.id.saveButton);
        course = view.findViewById(R.id.course);

        ArrayAdapter<CharSequence> certificateAdapter = ArrayAdapter.createFromResource(getActivity(),
                R.array.certificate_options, android.R.layout.simple_spinner_item);
        certificateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        certificate.setAdapter(certificateAdapter);

        ArrayAdapter<String> coursesAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, courses);
        coursesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        course.setAdapter(coursesAdapter);

        dob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar c = Calendar.getInstance();
                int mYear = c.get(Calendar.YEAR);
                int mMonth = c.get(Calendar.MONTH);
                int mDay = c.get(Calendar.DAY_OF_MONTH);
                DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(),
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                dob.setText(dayOfMonth + "-" + (monthOfYear + 1) + "-" + year);
                            }
                        }, mYear, mMonth, mDay);
                datePickerDialog.show();
            }
        });

        List<String> years = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            for (int j = 1; j <= 2; j++) {
                years.add("Year " + i + ", Semester " + j);
            }
        }
        ArrayAdapter<String> currentYearAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, years);
        currentYearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        currentYear.setAdapter(currentYearAdapter);

        currentYear.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selected = (String) parent.getItemAtPosition(position);
                String[] parts = selected.split(", ");
                String yearPart = parts[0];
                String semesterPart = parts[1];
                selectedYear = Integer.parseInt(yearPart.split(" ")[1]);
                selectedSemester = Integer.parseInt(semesterPart.split(" ")[1]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        admissionYear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar c = Calendar.getInstance();
                int mYear = c.get(Calendar.YEAR);
                DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), DatePickerDialog.THEME_HOLO_LIGHT, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        admissionYear.setText(String.valueOf(year));
                    }
                }, mYear, 0, 0);
                ((ViewGroup) datePickerDialog.getDatePicker()).findViewById(Resources.getSystem().getIdentifier("day", "id", "android")).setVisibility(View.GONE);
                ((ViewGroup) datePickerDialog.getDatePicker()).findViewById(Resources.getSystem().getIdentifier("month", "id", "android")).setVisibility(View.GONE);
                datePickerDialog.setTitle("Select Year");
                datePickerDialog.show();
            }
        });

        profilePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
            }
        });

        DatabaseReference courseRef = FirebaseDatabase.getInstance().getReference("course");

        courseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                courses.clear();
                for (DataSnapshot courseSnapshot : dataSnapshot.getChildren()) {
                    String courseName = courseSnapshot.getValue(String.class);
                    courses.add(courseName);
                }
                ArrayAdapter<String> coursesAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, courses);
                coursesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                course.setAdapter(coursesAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        String userId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();

        mDatabase.child("users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    firstName.setText(dataSnapshot.child("firstName").getValue(String.class));
                    middleName.setText(dataSnapshot.child("middleName").getValue(String.class));
                    lastName.setText(dataSnapshot.child("lastName").getValue(String.class));
                    registrationNumber.setText(dataSnapshot.child("registrationNumber").getValue(String.class));
                    course.setText(dataSnapshot.child("course").getValue(String.class));
                    dob.setText(dataSnapshot.child("dob").getValue(String.class));
                    admissionYear.setText(dataSnapshot.child("admissionYear").getValue(String.class));
                    certificate.setSelection(certificateAdapter.getPosition(dataSnapshot.child("certificate").getValue(String.class)));
                    currentYear.setSelection(currentYearAdapter.getPosition("Year " + dataSnapshot.child("current_year").getValue(Integer.class) + ", Semester " + dataSnapshot.child("current_semester").getValue(Integer.class)));
                    Glide.with(getActivity()).load(dataSnapshot.child("profilePhoto").getValue(String.class)).into(profilePhoto);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String regNo = registrationNumber.getText().toString();

                if (!regNo.matches("[A-Z]+[0-9]{3}-[0-9]{2}-[0-9]{4}/[0-9]{4}")) {
                    Toast.makeText(getActivity(), "Invalid Registration Number", Toast.LENGTH_SHORT).show();
                    return;
                }

                mDatabase.child("users").orderByChild("registrationNumber").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists() && !dataSnapshot.hasChild(userId)) {
                            registrationNumber.setError("Registration number already exists");
                            registrationNumber.requestFocus();
                        } else {
                            mDatabase.child("users").child(userId).child("profilePhoto").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        String profilePhotoUrl = dataSnapshot.getValue(String.class);

                                        Map<String, Object> user = new HashMap<>();
                                        user.put("firstName", firstName.getText().toString());
                                        user.put("middleName", middleName.getText().toString());
                                        user.put("lastName", lastName.getText().toString());
                                        user.put("registrationNumber", regNo);
                                        user.put("course", course.getText().toString());
                                        user.put("dob", dob.getText().toString());
                                        user.put("admissionYear", admissionYear.getText().toString());
                                        user.put("current_year", selectedYear);
                                        user.put("current_semester", selectedSemester);
                                        user.put("certificate", certificate.getSelectedItem().toString());
                                        user.put("profilePhoto", profilePhotoUrl);

                                        mDatabase.child("users").child(userId).setValue(user)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            Toast.makeText(getActivity(), "Profile Saved Successfully", Toast.LENGTH_SHORT).show();
                                                        } else {
                                                            Toast.makeText(getActivity(), "Failed to Save Profile", Toast.LENGTH_SHORT).show();
                                                        }
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
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                });
            }
        });

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        String userId = Objects.requireNonNull(mAuth.getCurrentUser()).getUid();

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri filePath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), filePath);
                Bitmap squareBitmap = cropToSquare(bitmap);
                profilePhoto.setImageBitmap(squareBitmap);
                Uri squareBitmapUri = getImageUri(getActivity(), squareBitmap);
                StorageReference fileReference = mStorageRef.child(System.currentTimeMillis() + "." + getFileExtension(squareBitmapUri));
                fileReference.putFile(squareBitmapUri)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                fileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        mDatabase.child("users").child(userId).child("profilePhoto").setValue(uri.toString());
                                    }
                                });
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private Bitmap cropToSquare(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int newWidth = Math.min(width, height);
        int cropW = (width - newWidth) / 2;
        int cropH = (height - newWidth) / 2;
        Bitmap cropImg = Bitmap.createBitmap(bitmap, cropW, cropH, newWidth, newWidth);
        return cropImg;
    }

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    private String getFileExtension(Uri uri) {
        ContentResolver cR = getActivity().getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }
}