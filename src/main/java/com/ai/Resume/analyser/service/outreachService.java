package com.ai.Resume.analyser.service;

import com.ai.Resume.analyser.model.*;
import com.ai.Resume.analyser.repository.CandidateRepository;
import com.ai.Resume.analyser.repository.EmailLogRepository;
import com.ai.Resume.analyser.repository.JobPostingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class outreachService {

    @Autowired
    private JobPostingRepository jobPostingRepository;

    @Autowired
    private CandidateRepository candidateRepository;

    @Autowired
    private EmailLogRepository emailLogRepository;

    @Autowired
    private emailDispatchService dispatchService;

    public ResponseEntity<?> startOutreach(Long jobPostingId, List<String> candidateEmails) {
        try {
            JobPosting job = jobPostingRepository.findById(jobPostingId).orElse(null);
            if (job == null) {
                return new ResponseEntity<>("Job posting not found", HttpStatus.NOT_FOUND);
            }
            if (job.getStatus() != JobPostingStatus.ACTIVE) {
                return new ResponseEntity<>("Job posting must be ACTIVE to start outreach", HttpStatus.CONFLICT);
            }

            List<String> queued = new ArrayList<>();
            List<String> skipped = new ArrayList<>();

            for (String email : candidateEmails) {
                Candidate candidate = candidateRepository.findById(email).orElse(null);
                if (candidate == null) {
                    skipped.add(email + " (not found)");
                    continue;
                }
                if (candidate.isDoNotContact()) {
                    skipped.add(email + " (opted out)");
                    continue;
                }
                if (emailLogRepository.existsByCandidateEmailAndJobPostingIdAndEmailType(email, jobPostingId, EmailType.INITIAL_OUTREACH)) {
                    skipped.add(email + " (already contacted for this job)");
                    continue;
                }

                EmailLog log = EmailLog.builder()
                        .candidateEmail(email)
                        .jobPostingId(jobPostingId)
                        .emailType(EmailType.INITIAL_OUTREACH)
                        .recipientEmail(email)
                        .responseToken(UUID.randomUUID().toString())
                        .deliveryStatus(DeliveryStatus.PENDING)
                        .response(CandidateResponse.PENDING)
                        .retryCount(0)
                        .build();
                emailLogRepository.save(log);
                queued.add(email);

                dispatchService.sendAsync(log.getId(), candidate.getName(), email, job.getTitle(), log.getResponseToken(), false);
            }

            return ResponseEntity.ok("Queued: " + queued + " | Skipped: " + skipped);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Outreach failed: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<?> listOutreach(Long jobPostingId) {
        try {
            return ResponseEntity.ok(emailLogRepository.findByJobPostingId(jobPostingId));
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Failed to fetch logs", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<?> listAllOutreach() {
        try {
            return ResponseEntity.ok(emailLogRepository.findAll());
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Failed to fetch all logs", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
