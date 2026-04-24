package aihazardanalyzer.service;

import aihazardanalyzer.gemini.GeminiVisionClient;
import aihazardanalyzer.gemini.OccupancyResponseParser;
import aihazardanalyzer.model.OccupancyAnalysisResult;

public class OccupancyAnalysisService {
    private final GeminiVisionClient visionClient;
    private final OccupancyResponseParser parser;

    public OccupancyAnalysisService() {
        this.visionClient = new GeminiVisionClient();
        this.parser = new OccupancyResponseParser();
    }

    public String analyzeRaw(byte[] imageBytes, String mimeType) throws Exception {
        return visionClient.requestOccupancyAnalysis(imageBytes, mimeType);
    }

    public OccupancyAnalysisResult analyze(byte[] imageBytes, String mimeType) throws Exception {
        String rawJson = analyzeRaw(imageBytes, mimeType);
        return parser.parseOccupancyAnalysisResult(rawJson);
    }

    public OccupancyAnalysisResult parseRawJson(String rawJson) throws Exception {
        return parser.parseOccupancyAnalysisResult(rawJson);
    }
}
