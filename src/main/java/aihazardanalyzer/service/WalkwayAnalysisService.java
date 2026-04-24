package aihazardanalyzer.service;

import aihazardanalyzer.gemini.GeminiVisionClient;
import aihazardanalyzer.gemini.WalkwayResponseParser;
import aihazardanalyzer.model.WalkwayAnalysisResult;

public class WalkwayAnalysisService {
    private final GeminiVisionClient visionClient;
    private final WalkwayResponseParser parser;

    public WalkwayAnalysisService() {
        this.visionClient = new GeminiVisionClient();
        this.parser = new WalkwayResponseParser();
    }

    public String analyzeRaw(byte[] imageBytes, String mimeType) throws Exception {
        return visionClient.requestWalkwayAnalysis(imageBytes, mimeType);
    }

    public WalkwayAnalysisResult analyze(byte[] imageBytes, String mimeType) throws Exception {
        String rawJson = analyzeRaw(imageBytes, mimeType);
        return parser.parseWalkwayAnalysisResult(rawJson);
    }

    public WalkwayAnalysisResult parseRawJson(String rawJson) throws Exception {
        return parser.parseWalkwayAnalysisResult(rawJson);
    }
}