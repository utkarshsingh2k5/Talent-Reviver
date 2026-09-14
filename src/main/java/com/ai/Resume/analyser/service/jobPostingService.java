package com.ai.Resume.analyser.service;

import com.ai.Resume.analyser.dto.JobPostingRequest;
import com.ai.Resume.analyser.model.JobPosting;
import com.ai.Resume.analyser.model.JobPostingStatus;
import com.ai.Resume.analyser.repository.JobPostingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class jobPostingService {

    @Autowired
    private JobPostingRepository jobPostingRepository;

    public ResponseEntity<?> create(JobPostingRequest req, String hrEmail) {
        JobPosting posting = JobPosting.builder()
                .title(req.getTitle())
                .description(req.getDescription())
                .requiredSkills(req.getRequiredSkills())
                .preferredSkills(req.getPreferredSkills())
                .minExperience(req.getMinExperience())
                .maxExperience(req.getMaxExperience())
                .location(req.getLocation())
                .employmentType(req.getEmploymentType())
                .status(JobPostingStatus.ACTIVE)
                .createdBy(hrEmail)
                .build();
        return new ResponseEntity<>(jobPostingRepository.save(posting), HttpStatus.CREATED);
    }

    public ResponseEntity<?> update(Long id, JobPostingRequest req) {
        JobPosting posting = jobPostingRepository.findById(id).orElse(null);
        if (posting == null) {
            return new ResponseEntity<>("Job posting not found", HttpStatus.NOT_FOUND);
        }
        posting.setTitle(req.getTitle());
        posting.setDescription(req.getDescription());
        posting.setRequiredSkills(req.getRequiredSkills());
        posting.setPreferredSkills(req.getPreferredSkills());
        posting.setMinExperience(req.getMinExperience());
        posting.setMaxExperience(req.getMaxExperience());
        posting.setLocation(req.getLocation());
        posting.setEmploymentType(req.getEmploymentType());
        return ResponseEntity.ok(jobPostingRepository.save(posting));
    }

    public ResponseEntity<?> list() {
        return ResponseEntity.ok(jobPostingRepository.findAll());
    }

    public ResponseEntity<?> get(Long id) {
        return jobPostingRepository.findById(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(new ResponseEntity<>("Job posting not found", HttpStatus.NOT_FOUND));
    }

    public ResponseEntity<?> setStatus(Long id, JobPostingStatus status) {
        JobPosting posting = jobPostingRepository.findById(id).orElse(null);
        if (posting == null) {
            return new ResponseEntity<>("Job posting not found", HttpStatus.NOT_FOUND);
        }
        posting.setStatus(status);
        return ResponseEntity.ok(jobPostingRepository.save(posting));
    }

    public ResponseEntity<?> delete(Long id) {
        if (!jobPostingRepository.existsById(id)) {
            return new ResponseEntity<>("Job posting not found", HttpStatus.NOT_FOUND);
        }
        jobPostingRepository.deleteById(id);
        return new ResponseEntity<>("Deleted", HttpStatus.OK);
    }

    public ResponseEntity<?> deleteAll() {
        jobPostingRepository.deleteAll();
        return ResponseEntity.ok("All job postings deleted successfully");
    }
}
