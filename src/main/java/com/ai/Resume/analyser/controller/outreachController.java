package com.ai.Resume.analyser.controller;

import com.ai.Resume.analyser.dto.OutreachRequest;
import com.ai.Resume.analyser.service.outreachService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/hr")
public class outreachController {

    @Autowired
    private outreachService outreachService;

    @PostMapping("/jobs/{id}/outreach")
    public ResponseEntity<?> startOutreach(@PathVariable Long id, @Valid @RequestBody OutreachRequest req) {
        return outreachService.startOutreach(id, req.getCandidateEmails());
    }

    @GetMapping("/jobs/{id}/outreach")
    public ResponseEntity<?> listOutreach(@PathVariable Long id) {
        return outreachService.listOutreach(id);
    }

    @GetMapping("/outreach/all")
    public ResponseEntity<?> listAllOutreach() {
        return outreachService.listAllOutreach();
    }
}
