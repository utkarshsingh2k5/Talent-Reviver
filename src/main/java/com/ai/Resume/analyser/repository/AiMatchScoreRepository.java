package com.ai.Resume.analyser.repository;

import com.ai.Resume.analyser.model.AiMatchScore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AiMatchScoreRepository extends JpaRepository<AiMatchScore, Long> {
    List<AiMatchScore> findByJobPostingIdOrderByMatchScoreDesc(Long jobPostingId);
    Optional<AiMatchScore> findByJobPostingIdAndCandidateEmail(Long jobPostingId, String candidateEmail);
    List<AiMatchScore> findByCandidateEmail(String candidateEmail);
}
