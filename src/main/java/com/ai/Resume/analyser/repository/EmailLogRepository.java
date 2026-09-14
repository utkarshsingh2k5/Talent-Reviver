package com.ai.Resume.analyser.repository;

import com.ai.Resume.analyser.model.CandidateResponse;
import com.ai.Resume.analyser.model.EmailLog;
import com.ai.Resume.analyser.model.EmailType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmailLogRepository extends JpaRepository<EmailLog, Long> {
    Optional<EmailLog> findByResponseToken(String responseToken);
    boolean existsByCandidateEmailAndJobPostingIdAndEmailType(String candidateEmail, Long jobPostingId, EmailType emailType);
    List<EmailLog> findByJobPostingId(Long jobPostingId);
    List<EmailLog> findByEmailTypeAndResponse(EmailType emailType, CandidateResponse response);
}
