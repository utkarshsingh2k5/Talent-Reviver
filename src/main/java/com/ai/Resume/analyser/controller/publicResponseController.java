package com.ai.Resume.analyser.controller;

import com.ai.Resume.analyser.service.candidateResponseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

// Unauthenticated on purpose - see securityConfiguration ("/api/public/**").
// Authorization comes from the unguessable, single-use response token.
@RestController
@RequestMapping("resumeAnalyser/entry/v1/api/public")
public class publicResponseController {

    @Autowired
    private candidateResponseService candidateResponseService;

    @GetMapping("/respond")
    public ModelAndView respond(@RequestParam String token, @RequestParam String action) {
        String message = candidateResponseService.handleResponse(token, action);
        ModelAndView mav = new ModelAndView("candidate-response");
        mav.addObject("title", "Thanks for your response");
        mav.addObject("message", message);
        return mav;
    }
}
