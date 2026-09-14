package com.ai.Resume.analyser.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.util.Date;
import java.util.List;

// Named JobPosting - NOT "Job" - because com.ai.Resume.analyser.model.Job is
// already the Adzuna external-job-search DTO used by the existing app
// (has company/location/redirect_url fields the frontend depends on).
@Data
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JobPosting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Lob
    @Column(length = 8000)
    private String description;

    @ElementCollection
    @Column(length = 100)
    private List<String> requiredSkills;

    @ElementCollection
    @Column(length = 100)
    private List<String> preferredSkills;

    private int minExperience;
    private int maxExperience;

    private String location;

    @Enumerated(EnumType.STRING)
    private EmploymentType employmentType;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private JobPostingStatus status = JobPostingStatus.DRAFT;

    // Email of the HR user who created this posting (FK-by-convention to usersTable.email).
    private String createdBy;

    @CreationTimestamp
    @Column(updatable = false)
    private Date createdAt;

    @UpdateTimestamp
    private Date updatedAt;
}
