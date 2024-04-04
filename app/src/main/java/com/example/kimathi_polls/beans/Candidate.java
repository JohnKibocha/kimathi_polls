package com.example.kimathi_polls.beans;

public class Candidate {
    private String candidateId;
    private String manifesto;
    private String position;
    private String course;
    private String email;
    private String profilePhoto;
    private String firstName;
    private String middleName;
    private String lastName;
    private String creationDate;
    private String applicationStatus;
    private String registrationNumber;
    private String violation;
    private int votes;


    public Candidate() {
        // Default constructor required for calls to DataSnapshot.getValue(Candidate.class)
    }

    public Candidate(String candidateId, String manifesto, String position, String course, String email, String profilePhoto, String firstName, String middleName, String lastName, String registrationNumber, String creationDate, String applicationStatus, String violation, int votes) {
        this.candidateId = candidateId;
        this.manifesto = manifesto;
        this.position = position;
        this.course = course;
        this.email = email;
        this.profilePhoto = profilePhoto;
        this.firstName = firstName;
        this.middleName = middleName;
        this.lastName = lastName;
        this.registrationNumber = registrationNumber;
        this.creationDate = creationDate;
        this.applicationStatus = applicationStatus;
        this.violation = violation;
        this.votes = votes;

    }

    public String getCandidateId() {
        return candidateId;
    }

    public String getManifesto() {
        return manifesto;
    }

    public String getPosition() {
        return position;
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

    public String getFirstName() {
        return firstName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public String getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(String creationDate) {
        this.creationDate = creationDate;

    }

    public String getApplicationStatus() {
        return applicationStatus;
    }

    public void setApplicationStatus(String applicationStatus) {
        this.applicationStatus = applicationStatus;

    }

    public String getViolation() {
        return violation;
    }

    public void setViolation(String violation) {
        this.violation = violation;
    }

    public int getVotes() {
        return votes;
    }

    public void setVotes(int votes) {
        this.votes = votes;
    }

    public void incrementVotes() {
        this.votes++;
    }

    public String getUserId(String userId) {
        return userId;
    }

    public void setCandidateId(String candidateId) {
        this.candidateId = candidateId;
    }

    public void candidateUserId(String userId) {
        this.candidateId = userId;
    }


}
