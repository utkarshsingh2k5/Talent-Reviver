package com.ai.Resume.analyser.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * Candidate history against a job posting (prior application/interview
 * status). Schema-ready per spec section 8; starts empty and is populated
 * going forward as HR records outcomes.
 */
@Data
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String candidateEmail;

    private Long jobPostingId;

    private Date applicationDate;

    private String status;

    @Column(length = 1000)
    private String rejectionReason;

    @Column(length = 2000)
    private String interviewFeedback;
}
