package com.filevault.functions.dto;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class JobFitAnalysisPayload {

    private SkillMatch skillMatch;
    private EducationEvaluation educationEvaluation;
    private ExperienceEvaluation experienceEvaluation;
    private CompletenessCheck completenessCheck;
    private int atsScore;
    private String finalVerdict;
    private List<String> topThreeFixes;

    // --- Getters and Setters ---

    public SkillMatch getSkillMatch() { return skillMatch; }
    public void setSkillMatch(SkillMatch skillMatch) { this.skillMatch = skillMatch; }

    public EducationEvaluation getEducationEvaluation() { return educationEvaluation; }
    public void setEducationEvaluation(EducationEvaluation educationEvaluation) { this.educationEvaluation = educationEvaluation; }

    public ExperienceEvaluation getExperienceEvaluation() { return experienceEvaluation; }
    public void setExperienceEvaluation(ExperienceEvaluation experienceEvaluation) { this.experienceEvaluation = experienceEvaluation; }

    public CompletenessCheck getCompletenessCheck() { return completenessCheck; }
    public void setCompletenessCheck(CompletenessCheck completenessCheck) { this.completenessCheck = completenessCheck; }

    public int getAtsScore() { return atsScore; }
    public void setAtsScore(int atsScore) { this.atsScore = atsScore; }

    public String getFinalVerdict() { return finalVerdict; }
    public void setFinalVerdict(String finalVerdict) { this.finalVerdict = finalVerdict; }

    public List<String> getTopThreeFixes() { return topThreeFixes; }
    public void setTopThreeFixes(List<String> topThreeFixes) { this.topThreeFixes = topThreeFixes; }

    // --- Nested Classes ---
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SkillMatch {
        private List<String> matched;
        private List<String> missing;
        private List<String> bonus;

        public List<String> getMatched() { return matched; }
        public void setMatched(List<String> matched) { this.matched = matched; }

        public List<String> getMissing() { return missing; }
        public void setMissing(List<String> missing) { this.missing = missing; }

        public List<String> getBonus() { return bonus; }
        public void setBonus(List<String> bonus) { this.bonus = bonus; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class EducationEvaluation {
        private boolean meetsRequirement;
        private boolean degreeRelevant;
        private List<String> certificationsFound;
        @JsonProperty("isComplete")
        private boolean isComplete;
        private String notes;

        public boolean isMeetsRequirement() { return meetsRequirement; }
        public void setMeetsRequirement(boolean meetsRequirement) { this.meetsRequirement = meetsRequirement; }

        public boolean isDegreeRelevant() { return degreeRelevant; }
        public void setDegreeRelevant(boolean degreeRelevant) { this.degreeRelevant = degreeRelevant; }

        public List<String> getCertificationsFound() { return certificationsFound; }
        public void setCertificationsFound(List<String> certificationsFound) { this.certificationsFound = certificationsFound; }

        public boolean isComplete() { return isComplete; }
        public void setComplete(boolean complete) { isComplete = complete; }

        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ExperienceEvaluation {
        private int yearsFound;
        private boolean meetsRequirement;
        private boolean domainRelevant;
        private boolean achievementsQuantified;
        private boolean employmentGapsFound;
        private String notes;

        public int getYearsFound() { return yearsFound; }
        public void setYearsFound(int yearsFound) { this.yearsFound = yearsFound; }

        public boolean isMeetsRequirement() { return meetsRequirement; }
        public void setMeetsRequirement(boolean meetsRequirement) { this.meetsRequirement = meetsRequirement; }

        public boolean isDomainRelevant() { return domainRelevant; }
        public void setDomainRelevant(boolean domainRelevant) { this.domainRelevant = domainRelevant; }

        public boolean isAchievementsQuantified() { return achievementsQuantified; }
        public void setAchievementsQuantified(boolean achievementsQuantified) { this.achievementsQuantified = achievementsQuantified; }

        public boolean isEmploymentGapsFound() { return employmentGapsFound; }
        public void setEmploymentGapsFound(boolean employmentGapsFound) { this.employmentGapsFound = employmentGapsFound; }

        public String getNotes() { return notes; }
        public void setNotes(String notes) { this.notes = notes; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class CompletenessCheck {
        private boolean hasContactInfo;
        private boolean hasSummary;
        private boolean hasWorkExperience;
        private boolean hasSkills;
        private boolean hasEducation;
        private boolean hasCertifications;
        private boolean hasProjects;

        public boolean isHasContactInfo() { return hasContactInfo; }
        public void setHasContactInfo(boolean hasContactInfo) { this.hasContactInfo = hasContactInfo; }

        public boolean isHasSummary() { return hasSummary; }
        public void setHasSummary(boolean hasSummary) { this.hasSummary = hasSummary; }

        public boolean isHasWorkExperience() { return hasWorkExperience; }
        public void setHasWorkExperience(boolean hasWorkExperience) { this.hasWorkExperience = hasWorkExperience; }

        public boolean isHasSkills() { return hasSkills; }
        public void setHasSkills(boolean hasSkills) { this.hasSkills = hasSkills; }

        public boolean isHasEducation() { return hasEducation; }
        public void setHasEducation(boolean hasEducation) { this.hasEducation = hasEducation; }

        public boolean isHasCertifications() { return hasCertifications; }
        public void setHasCertifications(boolean hasCertifications) { this.hasCertifications = hasCertifications; }

        public boolean isHasProjects() { return hasProjects; }
        public void setHasProjects(boolean hasProjects) { this.hasProjects = hasProjects; }
    }
}