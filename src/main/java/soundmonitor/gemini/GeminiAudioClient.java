package soundmonitor.gemini;

import config.AppConfig;
import com.google.genai.Client;
import com.google.genai.types.*;

public class GeminiAudioClient {
    private final Client client;
    private static final String DEFAULT_MODEL = "gemini-2.5-flash";

    public GeminiAudioClient() {
        this.client = Client.builder()
                .apiKey(AppConfig.getApiKey())
                .build();
    }

    public String classifyAudioClip(byte[] audioBytes, String mimeType) throws Exception {
        String prompt = """
                Return ONLY valid JSON with this exact shape:
                {
                  "tooLoud": boolean,
                  "triggerAlarm": boolean,
                  "soundType": "CLAP_BURST" | "RAISED_VOICES" | "NORMAL_GYM_NOISE" | "IMPACT_NOISE" | "UNKNOWN_LOUD_SOUND",
                  "confidence": number,
                  "notes": "string"
                }

                Rules:
                - Classify the main sound event in this clip.
                - Use CLAP_BURST for one or more sharp clap-like transient sounds.
                - Use RAISED_VOICES for obvious loud speech or shouting.
                - Use IMPACT_NOISE for dropped weights, bangs, or hard impacts.
                - Use NORMAL_GYM_NOISE for ordinary background activity.
                - Use UNKNOWN_LOUD_SOUND if loud but unclear.
                - confidence must be between 0.0 and 1.0.
                - No markdown. No extra text.
                """;

        Content content = Content.fromParts(
                Part.fromText(prompt),
                Part.fromBytes(audioBytes, mimeType)
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

        GenerateContentResponse response =
                client.models.generateContent(DEFAULT_MODEL, content, config);

        return response.text();
    }
}
