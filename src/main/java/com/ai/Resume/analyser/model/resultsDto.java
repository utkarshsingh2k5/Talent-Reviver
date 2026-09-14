package com.ai.Resume.analyser.model;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class resultsDto {


    private  int score;
    private int atsoptimizationscore;
    private List<String> pros;
    private List<String> cons;
    private List<String> suggestions;
    private List<Job>  jobs;

    // TalentRevive: AI's best-effort estimate of total years of professional
    // experience from the resume (0 if unclear/entry-level). Extracted in the
    // same Gemini call rather than firing a second AI request just for this.
    private int experienceYears;

}
