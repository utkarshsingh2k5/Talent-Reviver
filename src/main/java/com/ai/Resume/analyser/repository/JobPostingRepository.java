package com.ai.Resume.analyser.repository;

import com.ai.Resume.analyser.model.JobPosting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobPostingRepository extends JpaRepository<JobPosting, Long> {
    List<JobPosting> findByCreatedBy(String createdBy);
}
