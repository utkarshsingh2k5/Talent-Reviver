package com.ai.Resume.analyser.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

// Shape of the structured JSON the AI matching prompt is instructed to return.
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MatchResultDto {
    private int matchScore;
    private List<String> matchingSkills;
    private List<String> missingSkills;
    private boolean experienceMatch;
    private String recommendation;
    private String reason;
}
