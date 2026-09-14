package com.ai.Resume.analyser.service;

import com.ai.Resume.analyser.mail.talentReviveMailService;
import com.ai.Resume.analyser.model.Candidate;
import com.ai.Resume.analyser.model.CandidateStatus;
import com.ai.Resume.analyser.model.DeliveryStatus;
import com.ai.Resume.analyser.model.EmailLog;
import com.ai.Resume.analyser.repository.CandidateRepository;
import com.ai.Resume.analyser.repository.EmailLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * Kept as its own bean (rather than a method on outreachService) so @Async
 * actually goes through the Spring proxy - calling an @Async method on
 * "this" from within the same class silently runs synchronously.
 */
@Service
public class emailDispatchService {

    @Autowired
    private EmailLogRepository emailLogRepository;

    @Autowired
    private CandidateRepository candidateRepository;

    @Autowired
    private talentReviveMailService mailService;

    @Async
    public void sendAsync(Long emailLogId, String candidateName, String candidateEmail, String jobTitle, String token, boolean isFollowUp) {
        EmailLog log = emailLogRepository.findById(emailLogId).orElse(null);
        if (log == null) return;
        try {
            if (isFollowUp) {
                mailService.sendFollowUpEmail(candidateName, candidateEmail, jobTitle, token);
            } else {
                mailService.sendOutreachEmail(candidateName, candidateEmail, jobTitle, token);
            }
            log.setDeliveryStatus(DeliveryStatus.SENT);
            log.setSentAt(new Date());
            System.out.println("TalentRevive: email sent to " + candidateEmail);

            Candidate candidate = candidateRepository.findById(candidateEmail).orElse(null);
            if (candidate != null && candidate.getStatus() == CandidateStatus.NEW) {
                candidate.setStatus(CandidateStatus.CONTACTED);
                candidateRepository.save(candidate);
            }
        } catch (Exception e) {
            // Section 23: never silently swallow email failures.
            log.setDeliveryStatus(DeliveryStatus.FAILED);
            log.setRetryCount(log.getRetryCount() + 1);
            System.out.println("TalentRevive: email FAILED to " + candidateEmail + " - " + e.getMessage());
        }
        emailLogRepository.save(log);
    }
}
