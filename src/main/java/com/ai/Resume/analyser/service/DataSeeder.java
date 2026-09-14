package com.ai.Resume.analyser.service;

import com.ai.Resume.analyser.model.Candidate;
import com.ai.Resume.analyser.model.CandidateStatus;
import com.ai.Resume.analyser.repository.CandidateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class DataSeeder {

    @Autowired
    private CandidateRepository candidateRepository;

    public int seedCandidates() {
        int count = 0;
        String[] names = {"Amit", "Priya", "Rahul", "Sneha", "Vikram", "Anjali", "Sandeep", "Kavita", "Arjun", "Meera"};
        String[] surnames = {"Sharma", "Verma", "Gupta", "Singh", "Kumar", "Yadav", "Patel", "Reddy", "Iyer", "Chatterjee"};

        String[][] skillSets = {
            {"Java", "Spring Boot", "Hibernate", "MySQL", "AWS", "Microservices"},
            {"Python", "Django", "FastAPI", "PostgreSQL", "PyTorch", "Scikit-learn"},
            {"React", "Node.js", "Express", "MongoDB", "TypeScript", "Tailwind"},
            {"Angular", "Java", "Oracle DB", "Maven", "JUnit", "Jenkins"},
            {"Vue.js", "PHP", "Laravel", "MySQL", "JavaScript", "CSS3"}
        };

        Random random = new Random();

        for (int i = 1; i <= 50; i++) {
            String name = names[random.nextInt(names.length)] + " " + surnames[random.nextInt(surnames.length)];
            String email = "student" + i + "@example.com";
            int[] skillSetIdx = {random.nextInt(skillSets.length)};
            String[] skills = skillSets[skillSetIdx[0]];

            // Create a realistic-looking resume text
            String resumeText = "Name: " + name + "\n" +
                                "Experience: " + (random.nextInt(5) + 1) + " years\n" +
                                "Skills: " + String.join(", ", skills) + "\n" +
                                "Professional Summary: Experienced developer with a strong background in " + skills[0] +
                                " and " + skills[1] + ". Proven track record of delivering scalable applications.\n" +
                                "Projects: Built a high-performance system using " + skills[2] + " and " + skills[3] + ".";

            Candidate candidate = Candidate.builder()
                    .email(email)
                    .name(name)
                    .resumeText(resumeText)
                    .experienceYears(random.nextInt(6)) // 0 to 5 years
                    .targetRoles("Software Engineer, Developer")
                    .status(CandidateStatus.NEW)
                    .doNotContact(false)
                    .build();

            candidateRepository.save(candidate);
            count++;
        }
        return count;
    }
}
