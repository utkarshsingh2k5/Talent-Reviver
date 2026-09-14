package com.ai.Resume.analyser.repository;

import com.ai.Resume.analyser.model.Application;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {
    List<Application> findByCandidateEmail(String candidateEmail);
}
