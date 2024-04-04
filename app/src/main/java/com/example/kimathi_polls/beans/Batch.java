package com.example.kimathi_polls.beans;

import java.util.Date;
import java.util.List;

public class Batch {
    private int year;
    private List<String> ballots;
    private List<String> candidates;
    private Date electionStartDate;
    private Date electionEndDate;
    private Date electionResultsDate;

    public Batch() {
        // Default constructor required for calls to DataSnapshot.getValue(Batch.class)
    }

    public Batch(int year, List<String> ballots, List<String> candidates, Date electionStartDate, Date electionEndDate, Date electionResultsDate) {
        this.year = year;
        this.ballots = ballots;
        this.candidates = candidates;
        this.electionStartDate = electionStartDate;
        this.electionEndDate = electionEndDate;
        this.electionResultsDate = electionResultsDate;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public List<String> getBallots() {
        return ballots;
    }

    public void setBallots(List<String> ballots) {
        this.ballots = ballots;
    }

    public List<String> getCandidates() {
        return candidates;
    }

    public void setCandidates(List<String> candidates) {
        this.candidates = candidates;
    }

    public Date getElectionStartDate() {
        return electionStartDate;
    }

    public void setElectionStartDate(Date electionStartDate) {
        this.electionStartDate = electionStartDate;
    }

    public Date getElectionEndDate() {
        return electionEndDate;
    }

    public void setElectionEndDate(Date electionEndDate) {
        this.electionEndDate = electionEndDate;
    }
    public Date getElectionResultsDate() {
        return electionResultsDate;
    }
    public void setElectionResultsDate(Date electionResultsDate) {
        this.electionResultsDate = electionResultsDate;
    }

}
