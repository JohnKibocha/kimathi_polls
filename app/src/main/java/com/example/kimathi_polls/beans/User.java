package com.example.kimathi_polls.beans;

import java.util.Date;

public class User {
    private String registrationNumber;
    private String firstName;
    private String middleName;
    private String lastName;
    private String course;
    private String email;
    private String profilePhoto;
    private Date creationDate;
    private String dob;
    private String admissionYear;
    private int currentYear;
    private String certificate;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String registrationNumber, String firstName, String middleName, String lastName, String course, String email, String profilePhoto, Date creationDate, String dob, String admissionYear, int currentYear, String certificate) {
        this.registrationNumber = registrationNumber;
        this.firstName = firstName;
        this.middleName = middleName;
        this.lastName = lastName;
        this.course = course;
        this.email = email;
        this.creationDate = creationDate;
        this.profilePhoto = profilePhoto;
        this.dob = dob;
        this.admissionYear = admissionYear;
        this.currentYear = currentYear;
        this.certificate = certificate;
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getCourse() {
        return course;
    }

    public String getEmail() {
        return email;
    }

    public String getProfilePhoto() {
        return profilePhoto;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public String getDob() {
        return dob;
    }

    public String getAdmissionYear() {
        return admissionYear;
    }

    public int getCurrentYear() {
        return currentYear;
    }

    public String getCertificate() {
        return certificate;
    }
}