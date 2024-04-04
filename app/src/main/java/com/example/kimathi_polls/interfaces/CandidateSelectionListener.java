package com.example.kimathi_polls.interfaces;

import com.example.kimathi_polls.beans.Candidate;

public interface CandidateSelectionListener {
    void selectedCandidate(String candidate, String position);
    void onCandidateSelected(Candidate candidate);

    void confirmSelection();
}
