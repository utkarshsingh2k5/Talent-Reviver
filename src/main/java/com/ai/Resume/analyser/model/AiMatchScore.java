package com.ai.Resume.analyser.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.util.Date;
import java.util.List;

@Data
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(indexes = {
        @Index(name = "idx_match_job_score", columnList = "jobPostingId, matchScore")
})
public class AiMatchScore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String candidateEmail;

    private Long jobPostingId;

    private int matchScore;

    @ElementCollection
    @Column(length = 100)
    private List<String> matchingSkills;

    @ElementCollection
    @Column(length = 100)
    private List<String> missingSkills;

    private boolean experienceMatch;

    @Enumerated(EnumType.STRING)
    private RecommendationLevel recommendation;

    @Column(length = 1000)
    private String reason;

    @CreationTimestamp
    @Column(updatable = false)
    private Date createdAt;
}
