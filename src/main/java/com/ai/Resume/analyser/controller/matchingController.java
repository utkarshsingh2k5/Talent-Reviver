package com.ai.Resume.analyser.controller;

import com.ai.Resume.analyser.service.candidateMatchingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/hr/matching")
public class matchingController {

    @Autowired
    private candidateMatchingService matchingService;

    @PostMapping("/{id}/match")
    public ResponseEntity<?> startMatching(@PathVariable Long id) {
        return matchingService.runMatching(id);
    }

    @GetMapping("/{id}/matches")
    public ResponseEntity<?> getMatches(@PathVariable Long id) {
        return matchingService.getRankedMatches(id);
    }
}
