package com.ai.Resume.analyser.repository;

import com.ai.Resume.analyser.model.Candidate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CandidateRepository extends JpaRepository<Candidate, String> {

    // Stage 1 (MySQL hard filter): experience range + not opted out.
    // AI matching (Stage 2) only ever runs against whatever this returns.
    @Query("SELECT c FROM Candidate c WHERE c.experienceYears BETWEEN :minExp AND :maxExp AND c.doNotContact = false")
    List<Candidate> findEligibleForMatching(@Param("minExp") int minExperience, @Param("maxExp") int maxExperience);
}
