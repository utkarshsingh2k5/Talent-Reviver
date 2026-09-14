package com.ai.Resume.analyser.service;

import com.ai.Resume.analyser.model.Candidate;
import com.ai.Resume.analyser.model.CandidateResponse;
import com.ai.Resume.analyser.model.CandidateStatus;
import com.ai.Resume.analyser.model.EmailLog;
import com.ai.Resume.analyser.repository.CandidateRepository;
import com.ai.Resume.analyser.repository.EmailLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * Handles clicks on the secure, single-use response links from outreach
 * emails. Unauthenticated on purpose (see securityConfiguration's
 * "/api/public/**") - the response_token is what authorizes the update,
 * not a login, so it must be unguessable and rejected once already used.
 *
 * A previous draft (TalentReviveController.handleCandidateResponse) had no
 * "already used" check, so the same link could be replayed to flip a
 * candidate's response back and forth, and had no OPTED_OUT action at all.
 */
@Service
public class candidateResponseService {

    @Autowired
    private EmailLogRepository emailLogRepository;

    @Autowired
    private CandidateRepository candidateRepository;

    public String handleResponse(String token, String action) {
        EmailLog log = emailLogRepository.findByResponseToken(token).orElse(null);
        if (log == null) {
            return "This link is invalid or has expired.";
        }
        if (log.getResponse() != CandidateResponse.PENDING) {
            return "This link has already been used - your response was already recorded.";
        }

        CandidateResponse response;
        try {
            response = CandidateResponse.valueOf(action);
        } catch (Exception e) {
            return "Unrecognized response action.";
        }
        if (response == CandidateResponse.PENDING) {
            return "Unrecognized response action.";
        }

        log.setResponse(response);
        log.setResponseAt(new Date());
        emailLogRepository.save(log);

        Candidate candidate = candidateRepository.findById(log.getCandidateEmail()).orElse(null);
        if (candidate != null) {
            switch (response) {
                case INTERESTED -> candidate.setStatus(CandidateStatus.ACTIVE_ENGAGEMENT);
                case NOT_INTERESTED -> candidate.setStatus(CandidateStatus.NOT_INTERESTED);
                case OPTED_OUT -> {
                    candidate.setStatus(CandidateStatus.NOT_INTERESTED);
                    candidate.setDoNotContact(true);
                }
                default -> { }
            }
            candidateRepository.save(candidate);
        }

        return switch (response) {
            case INTERESTED -> "Thanks for letting us know - our talent team will be in touch shortly.";
            case NOT_INTERESTED -> "Thanks for letting us know. We won't follow up on this particular role.";
            case OPTED_OUT -> "You've been opted out of future outreach. We won't contact you again.";
            default -> "Your response has been recorded.";
        };
    }
}
