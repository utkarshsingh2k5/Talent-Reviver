package com.ai.Resume.analyser.service;

import com.ai.Resume.analyser.model.*;
import com.ai.Resume.analyser.repository.CandidateRepository;
import com.ai.Resume.analyser.repository.EmailLogRepository;
import com.ai.Resume.analyser.repository.JobPostingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Background follow-up/retry processing (spec sections 15-16). Candidate
 * rediscovery itself stays HR-triggered (candidateMatchingService.runMatching),
 * NOT this scheduler - this only manages the follow-up-email lifecycle for
 * outreach that's already been sent.
 */
@Service
public class scheduledFollowUpService {

    @Value("${talentrevive.outreach.followup-delay-days:3}")
    private int followUpDelayDays;

    @Value("${talentrevive.outreach.max-retries:3}")
    private int maxRetries;

    @Autowired
    private EmailLogRepository emailLogRepository;

    @Autowired
    private CandidateRepository candidateRepository;

    @Autowired
    private JobPostingRepository jobPostingRepository;

    @Autowired
    private emailDispatchService dispatchService;

    @Scheduled(cron = "${talentrevive.scheduler.cron:0 0 9 * * *}")
    public void processFollowUpsAndRetries() {
        sendFollowUps();
        retryFailedSends();
    }

    private void sendFollowUps() {
        long cutoffMillis = TimeUnit.DAYS.toMillis(followUpDelayDays);
        List<EmailLog> pendingInitial = emailLogRepository.findByEmailTypeAndResponse(EmailType.INITIAL_OUTREACH, CandidateResponse.PENDING);

        for (EmailLog initial : pendingInitial) {
            if (initial.getDeliveryStatus() != DeliveryStatus.SENT || initial.getSentAt() == null) continue;
            boolean dueForFollowUp = (new Date().getTime() - initial.getSentAt().getTime()) >= cutoffMillis;
            if (!dueForFollowUp) continue;

            boolean alreadyFollowedUp = emailLogRepository.existsByCandidateEmailAndJobPostingIdAndEmailType(
                    initial.getCandidateEmail(), initial.getJobPostingId(), EmailType.FOLLOW_UP);
            if (alreadyFollowedUp) continue;

            Candidate candidate = candidateRepository.findById(initial.getCandidateEmail()).orElse(null);
            JobPosting job = jobPostingRepository.findById(initial.getJobPostingId()).orElse(null);
            if (candidate == null || job == null || candidate.isDoNotContact()) continue;

            EmailLog followUp = EmailLog.builder()
                    .candidateEmail(initial.getCandidateEmail())
                    .jobPostingId(initial.getJobPostingId())
                    .emailType(EmailType.FOLLOW_UP)
                    .recipientEmail(initial.getRecipientEmail())
                    .responseToken(UUID.randomUUID().toString())
                    .deliveryStatus(DeliveryStatus.PENDING)
                    .response(CandidateResponse.PENDING)
                    .retryCount(0)
                    .build();
            emailLogRepository.save(followUp);

            dispatchService.sendAsync(followUp.getId(), candidate.getName(), candidate.getEmail(), job.getTitle(), followUp.getResponseToken(), true);
        }
    }

    private void retryFailedSends() {
        List<EmailLog> all = emailLogRepository.findAll();
        for (EmailLog log : all) {
            if (log.getDeliveryStatus() != DeliveryStatus.FAILED) continue;
            if (log.getRetryCount() >= maxRetries) continue;

            Candidate candidate = candidateRepository.findById(log.getCandidateEmail()).orElse(null);
            JobPosting job = jobPostingRepository.findById(log.getJobPostingId()).orElse(null);
            if (candidate == null || job == null || candidate.isDoNotContact()) continue;

            log.setDeliveryStatus(DeliveryStatus.PENDING);
            emailLogRepository.save(log);
            dispatchService.sendAsync(log.getId(), candidate.getName(), candidate.getEmail(), job.getTitle(), log.getResponseToken(), log.getEmailType() == EmailType.FOLLOW_UP);
        }
    }
}
