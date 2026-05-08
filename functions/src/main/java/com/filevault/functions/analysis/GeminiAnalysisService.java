package com.filevault.functions.analysis;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.filevault.functions.dto.JobFitAnalysisPayload;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Calls Groq API to analyze resume fit against a job description.
 */
public class GeminiAnalysisService {

    private static final String GROQ_API_URL =
            "https://api.groq.com/openai/v1/chat/completions";
    private static final String GROQ_MODEL = "llama-3.3-70b-versatile";

    private final String apiKey;
    private final HttpClient httpClient;
    private final ObjectMapper mapper;

    public GeminiAnalysisService(String apiKey) {
        this.apiKey = apiKey;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();
        this.mapper = new ObjectMapper();
    }

    public JobFitAnalysisPayload analyze(String resumeText, String jobDescription) throws Exception {
        String prompt = buildPrompt(resumeText, jobDescription);
        String requestBody = buildRequestBody(prompt);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(GROQ_API_URL))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .timeout(Duration.ofSeconds(60))
                .build();

        HttpResponse<String> response = httpClient.send(request,
                HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Groq API error: " + response.statusCode() + " " + response.body());
        }

        return parseGroqResponse(response.body());
    }

    private String buildPrompt(String resumeText, String jobDescription) {
        return "You are an expert HR analyst and ATS system. " +
                "Analyze the resume against the job description and return ONLY a valid JSON object with no markdown, no explanation, no code blocks. " +
                "Return exactly this structure:\n" +
                "{\n" +
                "  \"skillMatch\": {\n" +
                "    \"matched\": [\"skill1\"],\n" +
                "    \"missing\": [\"skill2\"],\n" +
                "    \"bonus\": [\"skill3\"]\n" +
                "  },\n" +
                "  \"educationEvaluation\": {\n" +
                "    \"meetsRequirement\": true,\n" +
                "    \"degreeRelevant\": true,\n" +
                "    \"certificationsFound\": [],\n" +
                "    \"isComplete\": true,\n" +
                "    \"notes\": \"...\"\n" +
                "  },\n" +
                "  \"experienceEvaluation\": {\n" +
                "    \"yearsFound\": 0,\n" +
                "    \"meetsRequirement\": true,\n" +
                "    \"domainRelevant\": true,\n" +
                "    \"achievementsQuantified\": true,\n" +
                "    \"employmentGapsFound\": false,\n" +
                "    \"notes\": \"...\"\n" +
                "  },\n" +
                "  \"completenessCheck\": {\n" +
                "    \"hasContactInfo\": true,\n" +
                "    \"hasSummary\": true,\n" +
                "    \"hasWorkExperience\": true,\n" +
                "    \"hasSkills\": true,\n" +
                "    \"hasEducation\": true,\n" +
                "    \"hasCertifications\": false,\n" +
                "    \"hasProjects\": false\n" +
                "  },\n" +
                "  \"atsScore\": 75,\n" +
                "  \"finalVerdict\": \"...\",\n" +
                "  \"topThreeFixes\": [\"fix1\", \"fix2\", \"fix3\"]\n" +
                "}\n\n" +
                "RESUME TEXT:\n" + resumeText + "\n\n" +
                "JOB DESCRIPTION:\n" + jobDescription;
    }

    private String buildRequestBody(String prompt) throws Exception {
        String escapedPrompt = prompt
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");

        return "{" +
                "\"model\":\"" + GROQ_MODEL + "\"," +
                "\"messages\":[{\"role\":\"user\",\"content\":\"" + escapedPrompt + "\"}]," +
                "\"temperature\":0.1," +
                "\"max_tokens\":2048" +
                "}";
    }

    private JobFitAnalysisPayload parseGroqResponse(String responseBody) throws Exception {
        JsonNode root = mapper.readTree(responseBody);

        // Extract text from Groq's OpenAI-compatible response
        String text = root
                .path("choices")
                .path(0)
                .path("message")
                .path("content")
                .asText();

        // Clean up any accidental markdown wrapping
        text = text.trim();
        if (text.startsWith("```json")) {
            text = text.substring(7);
        }
        if (text.startsWith("```")) {
            text = text.substring(3);
        }
        if (text.endsWith("```")) {
            text = text.substring(0, text.length() - 3);
        }
        text = text.trim();

        // Find JSON object boundaries
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        if (start >= 0 && end > start) {
            text = text.substring(start, end + 1);
        }

        return mapper.readValue(text, JobFitAnalysisPayload.class);
    }
}