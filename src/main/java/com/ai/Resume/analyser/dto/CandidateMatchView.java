package com.ai.Resume.analyser.dto;

import com.ai.Resume.analyser.model.CandidateStatus;
import com.ai.Resume.analyser.model.RecommendationLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

// What HR sees on the job-matching page: candidate + their stored match result.
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CandidateMatchView {
    private String candidateEmail;
    private String candidateName;
    private int matchScore;
    private RecommendationLevel recommendation;
    private List<String> matchingSkills;
    private List<String> missingSkills;
    private boolean experienceMatch;
    private String reason;
    private CandidateStatus candidateStatus;
}
