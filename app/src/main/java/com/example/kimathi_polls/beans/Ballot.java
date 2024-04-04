package com.example.kimathi_polls.beans;

import com.google.gson.Gson;

public class Ballot {
    private String voterId;
    private String candidateId;
    private String verificationId;
    private String votedOn;
    private String batch;
    private String voterIpAddress;
    private String voterMacAddress;
    private String ballotSelection;
    private String seat;
    private String status;


    public String toJson() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    public static Ballot fromJson(String json) {
        Gson gson = new Gson();
        return gson.fromJson(json, Ballot.class);
    }

    public Ballot() {
        // Default constructor required for calls to DataSnapshot.getValue(Ballot.class)
    }

    public String getVoterId() {
        return voterId;
    }

    public void setVoterId(String voterId) {
        this.voterId = voterId;
    }

    public String getCandidateId() {
        return candidateId;
    }

    public void setCandidateId(String candidateId) {
        this.candidateId = candidateId;
    }

    public String getVerificationId() {
        return verificationId;
    }

    public void setVerificationId(String verificationId) {
        this.verificationId = verificationId;
    }

    public String getVotedOn() {
        return votedOn;
    }

    public void setVotedOn(String votedOn) {
        this.votedOn = votedOn;
    }

    public String getBatch() {
        return batch;
    }

    public void setBatch(String batch) {
        this.batch = batch;
    }

    public String getVoterIpAddress() {
        return voterIpAddress;
    }

    public void setVoterIpAddress(String voterIpAddress) {
        this.voterIpAddress = voterIpAddress;
    }

    public String getVoterMacAddress() {
        return voterMacAddress;
    }

    public void setVoterMacAddress(String voterMacAddress) {
        this.voterMacAddress = voterMacAddress;
    }

    public String getBallotSelection() {
        return ballotSelection;
    }

    public void setBallotSelection(String ballotSelection) {
        this.ballotSelection = ballotSelection;
    }

    public String getSeat() {
        return seat;
    }

    public void setSeat(String seat) {
        this.seat = seat;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

}