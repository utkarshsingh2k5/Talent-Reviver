package com.ai.Resume.analyser.service;

import com.ai.Resume.analyser.model.RecommendationLevel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

// Centralizes match-score -> recommendation mapping (spec section 29) so
// thresholds aren't hard-coded across the matching/outreach/dashboard code.
@Service
public class matchThresholdService {

    @Value("${talentrevive.match.high-threshold:90}")
    private int highThreshold;

    @Value("${talentrevive.match.medium-threshold:75}")
    private int mediumThreshold;

    @Value("${talentrevive.match.low-threshold:60}")
    private int lowThreshold;

    public RecommendationLevel classify(int matchScore) {
        if (matchScore >= highThreshold) return RecommendationLevel.HIGH_PRIORITY;
        if (matchScore >= mediumThreshold) return RecommendationLevel.MEDIUM_PRIORITY;
        if (matchScore >= lowThreshold) return RecommendationLevel.LOW_PRIORITY;
        return RecommendationLevel.NOT_RECOMMENDED;
    }

    public int getHighThreshold() {
        return highThreshold;
    }
}
