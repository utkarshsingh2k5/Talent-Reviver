package com.ai.Resume.analyser.service;

import com.ai.Resume.analyser.model.Candidate;
import com.ai.Resume.analyser.model.CandidateStatus;
import com.ai.Resume.analyser.repository.CandidateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Called from appService.extract() after a resume analysis completes, so the
 * candidate pool stays current with no separate upload step. Never throws: a
 * candidate-pool hiccup must not break the job-seeker resume-analysis flow.
 */
@Service
public class candidateIngestionService {

    @Autowired
    private CandidateRepository candidateRepository;

    public void upsertFromAnalysis(String email, String name, String resumeText, String targetRoles, int experienceYears) {
        try {
            Candidate candidate = candidateRepository.findById(email).orElse(null);
            if (candidate == null) {
                candidate = Candidate.builder()
                        .email(email)
                        .name(name)
                        .resumeText(truncate(resumeText))
                        .experienceYears(experienceYears)
                        .targetRoles(targetRoles)
                        .status(CandidateStatus.NEW)
                        .doNotContact(false)
                        .build();
            } else {
                candidate.setName(name);
                candidate.setResumeText(truncate(resumeText));
                candidate.setExperienceYears(experienceYears);
                candidate.setTargetRoles(targetRoles);
                // Don't touch status/doNotContact here - HR/candidate-driven state
                // shouldn't reset just because the resume was re-analyzed.
            }
            candidateRepository.save(candidate);
        } catch (Exception e) {
            System.out.println("TalentRevive candidate ingestion failed for " + email + ": " + e.getMessage());
        }
    }

    private String truncate(String text) {
        if (text == null) return null;
        return text.length() > 19000 ? text.substring(0, 19000) : text;
    }
}
