package com.ai.Resume.analyser.model;

// Top-level (was previously a nested enum inside a since-removed EmailLog draft).
// Includes OPTED_OUT - the old version could only record interested/not-interested.
public enum CandidateResponse {
    PENDING, INTERESTED, NOT_INTERESTED, OPTED_OUT
}
