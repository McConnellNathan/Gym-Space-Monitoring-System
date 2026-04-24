package aihazardanalyzer.service;

import aihazardanalyzer.gemini.FallResponseParser;
import aihazardanalyzer.gemini.GeminiVisionClient;
import aihazardanalyzer.model.FallAnalysisResult;

public class FallAnalysisService {
    private final GeminiVisionClient visionClient;
    private final FallResponseParser parser;

    public FallAnalysisService() {
        this.visionClient = new GeminiVisionClient();
        this.parser = new FallResponseParser();
    }

    public String analyzeRaw(byte[] imageBytes, String mimeType) throws Exception {
        return visionClient.requestFallAnalysis(imageBytes, mimeType);
    }

    public FallAnalysisResult analyze(byte[] imageBytes, String mimeType) throws Exception {
        String rawJson = analyzeRaw(imageBytes, mimeType);
        return parser.parseFallAnalysisResult(rawJson);
    }

    public FallAnalysisResult parseRawJson(String rawJson) throws Exception {
        return parser.parseFallAnalysisResult(rawJson);
    }
}
