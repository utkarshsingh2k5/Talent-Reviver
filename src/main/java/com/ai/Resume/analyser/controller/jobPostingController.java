package com.ai.Resume.analyser.controller;

import com.ai.Resume.analyser.dto.JobPostingRequest;
import com.ai.Resume.analyser.model.JobPostingStatus;
import com.ai.Resume.analyser.service.jobPostingService;
import com.ai.Resume.analyser.service.DataSeeder;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

// HR-only (see securityConfiguration: "/api/hr/**" -> hasRole("hr")).
@RestController
@RequestMapping("api/hr/jobs")
public class jobPostingController {

    @Autowired
    private jobPostingService jobPostingService;

    @Autowired
    private DataSeeder dataSeeder;

    @GetMapping("/seed")
    public ResponseEntity<?> seedData() {
        int count = dataSeeder.seedCandidates();
        return ResponseEntity.ok("Successfully seeded " + count + " candidates into the pool!");
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody JobPostingRequest req) {
        String hrEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        return jobPostingService.create(req, hrEmail);
    }

    @GetMapping
    public ResponseEntity<?> list() {
        try {
            return jobPostingService.list();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Failed to list jobs: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable Long id) {
        try {
            return jobPostingService.get(id);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Failed to get job: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @Valid @RequestBody JobPostingRequest req) {
        try {
            return jobPostingService.update(id, req);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Failed to update job: " + e.getMessage());
        }
    }

    @PutMapping("/{id}/activate")
    public ResponseEntity<?> activate(@PathVariable Long id) {
        try {
            return jobPostingService.setStatus(id, JobPostingStatus.ACTIVE);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Failed to activate job: " + e.getMessage());
        }
    }

    @PutMapping("/{id}/close")
    public ResponseEntity<?> close(@PathVariable Long id) {
        try {
            return jobPostingService.setStatus(id, JobPostingStatus.CLOSED);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Failed to close job: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            return jobPostingService.delete(id);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Failed to delete job: " + e.getMessage());
        }
    }

    @DeleteMapping
    public ResponseEntity<?> deleteAll() {
        try {
            return jobPostingService.deleteAll();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Failed to delete all jobs: " + e.getMessage());
        }
    }
}
