export interface ExtractedTable {
  rowCount: number;
  columnCount: number;
  cells: string[];
}

export interface SkillMatch {
  matched: string[];
  missing: string[];
  bonus: string[];
}

export interface EducationEvaluation {
  meetsRequirement: boolean;
  degreeRelevant: boolean;
  certificationsFound: string[];
  isComplete: boolean;
  notes: string;
}

export interface ExperienceEvaluation {
  yearsFound: number;
  meetsRequirement: boolean;
  domainRelevant: boolean;
  achievementsQuantified: boolean;
  employmentGapsFound: boolean;
  notes: string;
}

export interface CompletenessCheck {
  hasContactInfo: boolean;
  hasSummary: boolean;
  hasWorkExperience: boolean;
  hasSkills: boolean;
  hasEducation: boolean;
  hasCertifications: boolean;
  hasProjects: boolean;
}

export interface JobFitAnalysis {
  skillMatch: SkillMatch;
  educationEvaluation: EducationEvaluation;
  experienceEvaluation: ExperienceEvaluation;
  completenessCheck: CompletenessCheck;
  atsScore: number;
  finalVerdict: string;
  topThreeFixes: string[];
}

export interface DocumentAnalysisResult {
  blobName: string;
  fileName: string;
  extractedText: string;
  keyValuePairs: Record<string, string>;
  extractedTables: ExtractedTable[];
  pageCount: number;
  analyzedAt: string;
  jobFitAnalysis?: JobFitAnalysis;
}