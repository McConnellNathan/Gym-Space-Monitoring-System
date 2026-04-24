package aihazardanalyzer.gemini;

import config.AppConfig;
import com.google.genai.Client;
import com.google.genai.types.*;

public class GeminiVisionClient {
    private final Client client;
    private static final String DEFAULT_MODEL = "gemini-2.5-flash";

    public GeminiVisionClient() {
        this.client = Client.builder()
                .apiKey(AppConfig.getApiKey())
                .build();
    }

    public String requestOccupancyAnalysis(byte[] imageBytes, String mimeType) throws Exception {
        String prompt = """
                Return ONLY valid JSON with this exact shape:
                {
                  "peopleCount": integer,
                  "sceneStatus": "EMPTY" | "NORMAL_ACTIVITY" | "CROWDING",
                  "confidence": number,
                  "notes": "string"
                }

                Rules:
                - Count only visible people.
                - EMPTY if no people are visible.
                - NORMAL_ACTIVITY if people are present without crowding.
                - CROWDING only if the visible area appears overly full or congested.
                - confidence must be between 0.0 and 1.0.
                - No markdown. No extra text.
                """;

        return sendVisionRequest(imageBytes, mimeType, prompt);
    }

    public String requestAggressionAnalysis(byte[] imageBytes, String mimeType) throws Exception {
        String prompt = """
                Return ONLY valid JSON with this exact shape:
                {
                  "possibleConflict": boolean,
                  "confidence": number,
                  "notes": "string"
                }

                Rules:
                - possibleConflict should be true only for clearly visible aggressive,
                  hostile, or physically confrontational behavior.
                - Do not infer emotions or intent from normal exercise movement.
                - If uncertain, prefer possibleConflict = false.
                - confidence must be between 0.0 and 1.0.
                - No markdown. No extra text.
                """;

        return sendVisionRequest(imageBytes, mimeType, prompt);
    }

    public String requestFallAnalysis(byte[] imageBytes, String mimeType) throws Exception {
        String prompt = """
                Return ONLY valid JSON with this exact shape:
                {
                  "possibleFall": boolean,
                  "confidence": number,
                  "notes": "string"
                }

                Rules:
                - possibleFall should be true only if a person appears to have fallen,
                  collapsed, or is lying in a way that suggests a possible fall event.
                - Do not mark a normal exercise position as a fall.
                - If uncertain, prefer possibleFall = false.
                - confidence must be between 0.0 and 1.0.
                - No markdown. No extra text.
                """;

        return sendVisionRequest(imageBytes, mimeType, prompt);
    }

    public String requestWalkwayAnalysis(byte[] imageBytes, String mimeType) throws Exception {
        String prompt = """
                Return ONLY valid JSON with this exact shape:
                {
                  "walkwayObstructed": boolean,
                  "confidence": number,
                  "notes": "string"
                }

                Rules:
                - walkwayObstructed should be true only if an object, bag, equipment item,
                  or other obstacle is visibly blocking a clear walking path.
                - Do not mark a walkway obstructed just because a person is standing or walking normally.
                - If uncertain, prefer walkwayObstructed = false.
                - confidence must be between 0.0 and 1.0.
                - No markdown. No extra text.
                """;

        return sendVisionRequest(imageBytes, mimeType, prompt);
    }

    private String sendVisionRequest(byte[] imageBytes, String mimeType, String prompt) throws Exception {
        Content content = Content.fromParts(
                Part.fromText(prompt),
                Part.fromBytes(imageBytes, mimeType)
        );

        GenerateContentConfig config = GenerateContentConfig.builder()
                .thinkingConfig(
                        ThinkingConfig.builder()
                                .thinkingBudget(0)
                                .build()
                )
                .candidateCount(1)
                .maxOutputTokens(120)
                .responseMimeType("application/json")
                .build();

        long startMs = System.currentTimeMillis();

        GenerateContentResponse response =
                client.models.generateContent(DEFAULT_MODEL, content, config);

        long endMs = System.currentTimeMillis();
        System.out.println("Gemini vision call took " + (endMs - startMs) + " ms");

        return response.text();
    }
}