package com.example.kimathi_polls.beans;

public class Alert {
    private String violation;
    private String violatorEmail;
    private String violatorRegistrationNumber;
    private String dateOfViolation;
    private String reportedOn;
    private String evidence;
    private String description;
    private String actionStatus;
    private String actionTaken;
    private String actionExpiryDate;
    private String actionJustification;

    public Alert() {
        // Default constructor required for calls to DataSnapshot.getValue(Alert.class)
    }

    public Alert(String violation, String violatorEmail, String violatorRegistrationNumber, String dateOfViolation, String reportedOn, String evidence, String description, String actionStatus, String actionTaken, String actionExpiryDate, String actionJustification) {
        this.violation = violation;
        this.violatorEmail = violatorEmail;
        this.violatorRegistrationNumber = violatorRegistrationNumber;
        this.dateOfViolation = dateOfViolation;
        this.reportedOn = reportedOn;
        this.evidence = evidence;
        this.description = description;
        this.actionStatus = actionStatus;
        this.actionTaken = actionTaken;
        this.actionExpiryDate = actionExpiryDate;
        this.actionJustification = actionJustification;
    }


    public String getViolation() {
        return violation;
    }

    public void setViolation(String violation) {
        this.violation = violation;
    }

    public String getViolatorEmail() {
        return violatorEmail;
    }

    public void setViolatorEmail(String violatorEmail) {
        this.violatorEmail = violatorEmail;
    }

    public String getViolatorRegistrationNumber() {
        return violatorRegistrationNumber;
    }

    public void setViolatorRegistrationNumber(String violatorRegistrationNumber) {
        this.violatorRegistrationNumber = violatorRegistrationNumber;
    }

    public String getDateOfViolation() {
        return dateOfViolation;
    }

    public void setDateOfViolation(String dateOfViolation) {
        this.dateOfViolation = dateOfViolation;
    }

    public String getReportedOn() {
        return reportedOn;
    }

    public void setReportedOn(String dateOfReport) {
        this.reportedOn = dateOfReport;
    }

    public String getEvidence() {
        return evidence;
    }

    public void setEvidence(String evidence) {
        this.evidence = evidence;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


    public String getActionStatus() {
        return actionStatus;
    }

    public void setActionStatus(String actionStatus) {
        this.actionStatus = actionStatus;
    }

    public String getActionTaken() {
        return actionTaken;
    }

    public void setActionTaken(String actionTaken) {
        this.actionTaken = actionTaken;
    }

    public String getActionDuration() {
        return actionExpiryDate;
    }

    public void setActionExpiryDate(String actionExpiryDate) {
        this.actionExpiryDate = actionExpiryDate;
    }

    public String getActionJustification() {
        return actionJustification;
    }

    public void setActionJustification(String actionJustification) {
        this.actionJustification = actionJustification;
    }

}
