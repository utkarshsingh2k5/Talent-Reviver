package com.ai.Resume.analyser.service;

import com.ai.Resume.analyser.dto.DashboardStatsDto;
import com.ai.Resume.analyser.model.CandidateResponse;
import com.ai.Resume.analyser.model.JobPostingStatus;
import com.ai.Resume.analyser.repository.AiMatchScoreRepository;
import com.ai.Resume.analyser.repository.CandidateRepository;
import com.ai.Resume.analyser.repository.EmailLogRepository;
import com.ai.Resume.analyser.repository.JobPostingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class dashboardService {

    @Autowired
    private JobPostingRepository jobPostingRepository;

    @Autowired
    private CandidateRepository candidateRepository;

    @Autowired
    private AiMatchScoreRepository aiMatchScoreRepository;

    @Autowired
    private EmailLogRepository emailLogRepository;

    @Autowired
    private matchThresholdService thresholdService;

    public DashboardStatsDto getStats() {
        try {
            long activeJobs = jobPostingRepository.findAll().stream()
                    .filter(j -> j != null && j.getStatus() == JobPostingStatus.ACTIVE).count();
            long totalCandidates = candidateRepository.count();
            long aiMatches = aiMatchScoreRepository.count();

            long highMatches = aiMatchScoreRepository.findAll().stream()
                    .filter(m -> m != null && m.getMatchScore() >= thresholdService.getHighThreshold()).count();

            long contacted = emailLogRepository.count();

            long interested = emailLogRepository.findAll().stream()
                    .filter(e -> e != null && e.getResponse() == CandidateResponse.INTERESTED).count();

            long notInterested = emailLogRepository.findAll().stream()
                    .filter(e -> e != null && e.getResponse() == CandidateResponse.NOT_INTERESTED).count();

            long noResponse = emailLogRepository.findAll().stream()
                    .filter(e -> e != null && e.getResponse() == CandidateResponse.PENDING).count();

            return new DashboardStatsDto(activeJobs, totalCandidates, aiMatches, highMatches, contacted, interested, notInterested, noResponse);
        } catch (Exception e) {
            e.printStackTrace();
            // Return a zeroed DTO instead of crashing with 500
            return new DashboardStatsDto(0, 0, 0, 0, 0, 0, 0, 0);
        }
    }
}
