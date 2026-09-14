package com.ai.Resume.analyser.dto;

import com.ai.Resume.analyser.model.EmploymentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class JobPostingRequest {
    @NotBlank(message = "title must not be empty")
    private String title;

    @NotBlank(message = "description must not be empty")
    private String description;

    @NotEmpty(message = "at least one required skill must be provided")
    private List<String> requiredSkills;

    private List<String> preferredSkills;

    private int minExperience;
    private int maxExperience;

    private String location;
    private EmploymentType employmentType;
}
