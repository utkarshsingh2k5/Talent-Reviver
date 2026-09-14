package com.ai.Resume.analyser.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class OutreachRequest {
    @NotEmpty(message = "select at least one candidate")
    private List<String> candidateEmails;
}
