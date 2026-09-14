package com.ai.Resume.analyser.service;

import com.ai.Resume.analyser.dto.CandidateMatchView;
import com.ai.Resume.analyser.dto.MatchResultDto;
import com.ai.Resume.analyser.model.*;
import com.ai.Resume.analyser.repository.AiMatchScoreRepository;
import com.ai.Resume.analyser.repository.CandidateRepository;
import com.ai.Resume.analyser.repository.JobPostingRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.genai.Client;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Part;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Stage 1 (MySQL hard filter) + Stage 2 (AI matching), spec sections 5-8.
 * Reuses the same Gemini client/config pattern as appService.extract()
 * instead of standing up a second AI integration.
 */
@Service
public class candidateMatchingService {

    @Value("${genKey}")
    private String genKey;

    @Value("${talentrevive.match.max-candidates-per-run:50}")
    private int maxCandidatesPerRun;

    @Autowired
    private JobPostingRepository jobPostingRepository;

    @Autowired
    private CandidateRepository candidateRepository;

    @Autowired
    private AiMatchScoreRepository aiMatchScoreRepository;

    @Autowired
    private matchThresholdService thresholdService;

    public ResponseEntity<?> runMatching(Long jobPostingId) {
        JobPosting job = jobPostingRepository.findById(jobPostingId).orElse(null);
        if (job == null) {
            return new ResponseEntity<>("Job posting not found", HttpStatus.NOT_FOUND);
        }
        if (job.getStatus() == JobPostingStatus.CLOSED) {
            return new ResponseEntity<>("Cannot match against a closed job", HttpStatus.CONFLICT);
        }

        List<Candidate> eligible = candidateRepository.findEligibleForMatching(job.getMinExperience(), job.getMaxExperience());

        int scored = 0;
        int failed = 0;
        for (Candidate candidate : eligible) {
            if (scored >= maxCandidatesPerRun) break;
            if (candidate.getResumeText() == null || candidate.getResumeText().isBlank()) continue;

            try {
                MatchResultDto result = callAiMatcher(job, candidate);
                persistMatch(job, candidate, result);
                scored++;
            } catch (Exception e) {
                // Section 23: one bad AI response/timeout must not abort the whole batch.
                failed++;
                System.out.println("TalentRevive AI matching failed for job " + jobPostingId + " / candidate " + candidate.getEmail() + ": " + e.getMessage());
            }
        }

        return ResponseEntity.ok("Matched " + scored + " candidate(s), " + failed + " failed, out of " + eligible.size() + " eligible.");
    }

    private MatchResultDto callAiMatcher(JobPosting job, Candidate candidate) throws Exception {
        String prompt = "You are an AI recruitment matching assistant.\n" +
                "Compare the candidate resume with the job description.\n" +
                "Evaluate: required technical skills, preferred skills, relevant experience, years of experience, " +
                "previous job roles, relevant projects, domain relevance, and missing requirements.\n" +
                "Return a match score from 0 to 100.\n" +
                "Do not invent candidate experience or skills. Only use information available in the resume and job description.\n" +
                "Return ONLY a single JSON object, no markdown fences, no preamble, in exactly this shape:\n" +
                "{\n" +
                "  \"matchScore\": number,\n" +
                "  \"matchingSkills\": [array of strings],\n" +
                "  \"missingSkills\": [array of strings],\n" +
                "  \"experienceMatch\": boolean,\n" +
                "  \"recommendation\": \"HIGH_PRIORITY\" | \"MEDIUM_PRIORITY\" | \"LOW_PRIORITY\" | \"NOT_RECOMMENDED\",\n" +
                "  \"reason\": string\n" +
                "}\n\n" +
                "Job Title: " + job.getTitle() + "\n" +
                "Job Description: " + job.getDescription() + "\n" +
                "Required Skills: " + String.join(", ", job.getRequiredSkills() == null ? List.of() : job.getRequiredSkills()) + "\n" +
                "Preferred Skills: " + String.join(", ", job.getPreferredSkills() == null ? List.of() : job.getPreferredSkills()) + "\n" +
                "Required Experience: " + job.getMinExperience() + "-" + job.getMaxExperience() + " years\n\n" +
                "Candidate Years of Experience: " + candidate.getExperienceYears() + "\n" +
                "Candidate Resume Content:\n" + candidate.getResumeText();

        Client client = Client.builder().apiKey(genKey).build();
        Content content = Content.builder().parts(Part.fromText(prompt)).build();
        GenerateContentConfig config = GenerateContentConfig.builder().temperature(0.0f).build();

        String results = null;
        int attempts = 0;
        while (attempts < 3) {
            attempts++;
            try {
                GenerateContentResponse response = client.models.generateContent("gemini-3.5-flash-lite", content, config);
                results = response.text();
                break;
            } catch (Exception e) {
                if (attempts >= 3) throw e;
                Thread.sleep(1000L * attempts);
            }
        }

        if (results == null) {
            throw new IllegalStateException("Empty AI response");
        }
        if (results.startsWith("```")) {
            int firstBrace = results.indexOf("{");
            int lastBrace = results.lastIndexOf("}");
            if (firstBrace != -1 && lastBrace != -1) {
                results = results.substring(firstBrace, lastBrace + 1);
            }
        }

        return new ObjectMapper().readValue(results, MatchResultDto.class);
    }

    private void persistMatch(JobPosting job, Candidate candidate, MatchResultDto result) {
        // Recommendation bucket is recomputed from our configured thresholds
        // rather than trusted verbatim from the AI, so it always matches
        // talentrevive.match.* regardless of what the model returned.
        RecommendationLevel recommendation = thresholdService.classify(result.getMatchScore());

        AiMatchScore match = aiMatchScoreRepository
                .findByJobPostingIdAndCandidateEmail(job.getId(), candidate.getEmail())
                .orElse(new AiMatchScore());

        match.setCandidateEmail(candidate.getEmail());
        match.setJobPostingId(job.getId());
        match.setMatchScore(result.getMatchScore());
        match.setMatchingSkills(result.getMatchingSkills() != null ? result.getMatchingSkills() : new ArrayList<>());
        match.setMissingSkills(result.getMissingSkills() != null ? result.getMissingSkills() : new ArrayList<>());
        match.setExperienceMatch(result.isExperienceMatch());
        match.setRecommendation(recommendation);
        match.setReason(result.getReason());
        aiMatchScoreRepository.save(match);
    }

    public ResponseEntity<?> getRankedMatches(Long jobPostingId) {
        if (!jobPostingRepository.existsById(jobPostingId)) {
            return new ResponseEntity<>("Job posting not found", HttpStatus.NOT_FOUND);
        }
        List<AiMatchScore> scores = aiMatchScoreRepository.findByJobPostingIdOrderByMatchScoreDesc(jobPostingId);
        List<CandidateMatchView> views = new ArrayList<>();
        for (AiMatchScore score : scores) {
            Candidate candidate = candidateRepository.findById(score.getCandidateEmail()).orElse(null);
            views.add(new CandidateMatchView(
                    score.getCandidateEmail(),
                    candidate != null ? candidate.getName() : score.getCandidateEmail(),
                    score.getMatchScore(),
                    score.getRecommendation(),
                    score.getMatchingSkills(),
                    score.getMissingSkills(),
                    score.isExperienceMatch(),
                    score.getReason(),
                    candidate != null ? candidate.getStatus() : null
            ));
        }
        return ResponseEntity.ok(views);
    }
}
