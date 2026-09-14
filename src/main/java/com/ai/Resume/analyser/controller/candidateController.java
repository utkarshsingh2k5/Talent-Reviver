package com.ai.Resume.analyser.controller;

import com.ai.Resume.analyser.model.Candidate;
import com.ai.Resume.analyser.repository.ApplicationRepository;
import com.ai.Resume.analyser.repository.CandidateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// HR-only. Read-only listing/history + an explicit opt-out action -
// candidate creation is NOT exposed here: the pool is only ever populated
// via appService.extract() (candidateIngestionService), never by an
// unauthenticated caller supplying an arbitrary email + name directly.
@RestController
@RequestMapping("api/hr/candidates")
public class candidateController {

    @Autowired
    private CandidateRepository candidateRepository;

    @Autowired
    private ApplicationRepository applicationRepository;

    @GetMapping
    public ResponseEntity<?> list() {
        return ResponseEntity.ok(candidateRepository.findAll());
    }

    @GetMapping("/{email}")
    public ResponseEntity<?> get(@PathVariable String email) {
        return candidateRepository.findById(email)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(new ResponseEntity<>("Candidate not found", HttpStatus.NOT_FOUND));
    }

    @GetMapping("/{email}/history")
    public ResponseEntity<?> history(@PathVariable String email) {
        return ResponseEntity.ok(applicationRepository.findByCandidateEmail(email));
    }

    @PostMapping("/{email}/opt-out")
    public ResponseEntity<?> optOut(@PathVariable String email) {
        Candidate candidate = candidateRepository.findById(email).orElse(null);
        if (candidate == null) {
            return new ResponseEntity<>("Candidate not found", HttpStatus.NOT_FOUND);
        }
        candidate.setDoNotContact(true);
        candidateRepository.save(candidate);
        return ResponseEntity.ok("Candidate opted out of future outreach");
    }
}
