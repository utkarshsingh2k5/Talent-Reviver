package com.ai.Resume.analyser.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DashboardStatsDto {
    private long activeJobs;
    private long totalCandidates;
    private long aiMatchesGenerated;
    private long highMatches;
    private long contacted;
    private long interested;
    private long notInterested;
    private long noResponse;
}
