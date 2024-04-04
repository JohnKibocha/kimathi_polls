package com.example.kimathi_polls.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.kimathi_polls.R;
import com.example.kimathi_polls.fragments.AlertFragment;
import com.example.kimathi_polls.fragments.AnalyticsFragment;
import com.example.kimathi_polls.fragments.HomeFragment;
import com.example.kimathi_polls.fragments.ProfileFragment;
import com.example.kimathi_polls.fragments.VieFragment;
import com.example.kimathi_polls.fragments.VoteFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;


public class MainActivity extends AppCompatActivity {

    String userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
    StorageReference mStorageRef;
    DatabaseReference mDatabase;
    ImageView profilePhoto;
    DatabaseReference courseRef = FirebaseDatabase.getInstance().getReference("courses");

    @Override
    protected void onStart() {
        super.onStart();

        getSupportFragmentManager().beginTransaction().replace(R.id.flFragment, new HomeFragment()).commit();

        // Get the saved preference
        SharedPreferences sharedPreferences = getSharedPreferences("PREFS", MODE_PRIVATE);
        boolean askedToUpdateProfile = sharedPreferences.getBoolean("askedToUpdateProfile_" + userId, false);
        boolean showDialog = sharedPreferences.getBoolean("showDialog_" + userId, true);

        if (showDialog) {
            // Check if user's profile is complete
            mDatabase.child("users").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (!dataSnapshot.hasChild("profilePhoto") && !askedToUpdateProfile) {
                        // If profile is not complete, ask the user to update it
                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        builder.setTitle("Profile Incomplete");
                        builder.setMessage("Your profile is incomplete. You would not be able to vote with an incomplete profile. Would you like to complete it now?");

                        // Add a checkbox to the dialog
                        View checkBoxView = View.inflate(MainActivity.this, R.layout.item_checkbox, null);
                        CheckBox checkBox = checkBoxView.findViewById(R.id.checkbox);
                        checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                            // Save the user's preference
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putBoolean("showDialog_" + userId, !isChecked);
                            editor.apply();
                        });
                        builder.setView(checkBoxView);

                        builder.setPositiveButton("Yes", (dialog, which) -> {
                            // User clicked "Yes", start UserProfileActivity
                            Intent intent = new Intent(getApplicationContext(), UserProfileActivity.class);
                            startActivity(intent);
                        });

                        builder.setNegativeButton("No", (dialog, which) -> {
                            // User clicked "No", show another dialog
                            AlertDialog.Builder builder2 = new AlertDialog.Builder(MainActivity.this);
                            builder2.setTitle("Update Profile");
                            builder2.setMessage("You can update your profile in the Profile menu.");
                            builder2.setPositiveButton("Close", null);
                            AlertDialog dialog2 = builder2.create();
                            dialog2.show();

                            // Dismiss the dialog after 5 seconds
                            final Handler handler = new Handler();
                            final Runnable runnable = () -> {
                                if (dialog2.isShowing()) {
                                    dialog2.dismiss();
                                }
                            };

                            dialog2.setOnDismissListener(dialogInterface -> handler.removeCallbacks(runnable));

                            handler.postDelayed(runnable, 5000);
                        });

                        builder.setIcon(android.R.drawable.ic_dialog_alert);
                        builder.show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Handle error
                }
            });
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //check if the database has a node called course, if not, populate the course table
        courseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    populateCourses();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle possible errors.
                Log.e("UserProfileActivity", "Failed to read value.", databaseError.toException());
            }
        });

        String userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

        //setting the custom action bar
        Objects.requireNonNull(getSupportActionBar()).setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.item_custom_action_bar);
        mStorageRef = FirebaseStorage.getInstance().getReference("uploads");
        mDatabase = FirebaseDatabase.getInstance().getReference();
        profilePhoto = findViewById(R.id.profile_photo);

        mDatabase.child("users").child(userId).child("profilePhoto").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String imageUrl = dataSnapshot.getValue(String.class);
                if (imageUrl != null) {
                    // Load the image from the URL into the ImageView using a library like Glide
                    Glide.with(MainActivity.this)
                            .load(imageUrl)
                            .into(profilePhoto);
                } else {
                    // Load the default image into the ImageView
                    profilePhoto.setImageResource(R.drawable.ic_default_profile);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle error
            }
        });

        invalidateOptionsMenu();
        // Create a PopupMenu when the profile photo is clicked
        ImageView profilePhoto = findViewById(R.id.profile_photo);
        profilePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(MainActivity.this, v);
                popupMenu.getMenuInflater().inflate(R.menu.profile_menu, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {

                        //Use if statements rather than switch statements
                        int itemId = item.getItemId();
                        if (itemId == R.id.profile) {

                            Intent intent = new Intent(getApplicationContext(), UserProfileActivity.class);
                            startActivity(intent);

                        } else if (itemId == R.id.logout) {
                            // Navigate to LoginActivity
                            Toast.makeText(MainActivity.this, "Logged Out", Toast.LENGTH_SHORT).show();
                            FirebaseAuth.getInstance().signOut();
                            Intent intent = new Intent(getApplicationContext(), WelcomeActivity.class);
                            startActivity(intent);
                            finish();
                            return true;
                        }


                        return false;
                    }
                });
                popupMenu.show();
            }
        });


        // In your activity or fragment, create a BottomNavigationView object
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);

        // Set a listener for the item selection events
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                int itemId = item.getItemId();

                if (itemId == R.id.home) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.flFragment, new HomeFragment()).commit();
                    Toast.makeText(MainActivity.this, "Switched Back to Home", Toast.LENGTH_SHORT).show();
                } else if (itemId == R.id.vote) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.flFragment, new VoteFragment()).commit();
                    Toast.makeText(MainActivity.this, "Switched to Vote", Toast.LENGTH_SHORT).show();
                } else if (itemId == R.id.alert) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.flFragment, new AlertFragment()).commit();
                    Toast.makeText(MainActivity.this, "Switched to Alert", Toast.LENGTH_SHORT).show();
                } else if (itemId == R.id.analytics) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.flFragment, new AnalyticsFragment()).commit();
                    Toast.makeText(MainActivity.this, "Switched to Analytics", Toast.LENGTH_SHORT).show();
                } else if (itemId == R.id.vie) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.flFragment, new VieFragment()).commit();
                    Toast.makeText(MainActivity.this, "Switched to Vie", Toast.LENGTH_SHORT).show();
                }

                return true;
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_overflow) {
            // Create a PopupMenu when the overflow menu is clicked
            View menuItemView = findViewById(R.id.action_overflow); // SAME ID AS MENU ID
            PopupMenu popupMenu = new PopupMenu(MainActivity.this, menuItemView);
            popupMenu.getMenuInflater().inflate(R.menu.profile_menu, popupMenu.getMenu());
            popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {

                    //Use if statements rather than switch statements
                    int itemId = item.getItemId();
                    if (itemId == R.id.profile) {
                        // Navigate to SettingsFragment
                        getSupportFragmentManager().beginTransaction().replace(R.id.flFragment, new ProfileFragment()).commit();
                        return true;
                    } else if (itemId == R.id.logout) {
                        // Navigate to LoginActivity
                        Toast.makeText(MainActivity.this, "Logged Out", Toast.LENGTH_SHORT).show();
                        FirebaseAuth.getInstance().signOut();
                        Intent intent = new Intent(getApplicationContext(), WelcomeActivity.class);
                        startActivity(intent);
                        finish();
                        return true;
                    }

                    return false;
                }
            });
            popupMenu.show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void populateCourses() {
        List<String> courses = Arrays.asList
                (
                        "BBA", "BCom", "BSc Civil Eng", "BBIT", "BSc Comp Sci", "MBA", "MSc Civil Eng", "MSc Inf Tech", "MSc Comp Sci", "PhD Bus Admin", "PhD Civil Eng", "PhD Inf Tech", "Dip. Elec Elec Eng", "Dip. Mech Eng", "Dip. Civil Eng", "Dip. Inf Tech"
                );

        for (String course : courses) {
            String id = courseRef.push().getKey();
            assert id != null;
            courseRef.child(id).setValue(course);
        }
    }
}

