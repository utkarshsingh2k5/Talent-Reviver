package com.ai.Resume.analyser.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(indexes = {
        @Index(name = "idx_emaillog_candidate_job", columnList = "candidateEmail, jobPostingId")
})
public class EmailLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String candidateEmail;

    private Long jobPostingId;

    @Enumerated(EnumType.STRING)
    private EmailType emailType;

    private String recipientEmail;

    // Unguessable, single-use token embedded in the candidate response link.
    @Column(unique = true, length = 64)
    private String responseToken;

    private Date sentAt;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private DeliveryStatus deliveryStatus = DeliveryStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private CandidateResponse response = CandidateResponse.PENDING;

    private Date responseAt;

    @Builder.Default
    private int retryCount = 0;
}
