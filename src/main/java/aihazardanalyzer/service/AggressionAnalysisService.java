package aihazardanalyzer.service;

import aihazardanalyzer.gemini.AggressionResponseParser;
import aihazardanalyzer.gemini.GeminiVisionClient;
import aihazardanalyzer.model.AggressionAnalysisResult;

public class AggressionAnalysisService {
    private final GeminiVisionClient visionClient;
    private final AggressionResponseParser parser;

    public AggressionAnalysisService() {
        this.visionClient = new GeminiVisionClient();
        this.parser = new AggressionResponseParser();
    }

    public String analyzeRaw(byte[] imageBytes, String mimeType) throws Exception {
        return visionClient.requestAggressionAnalysis(imageBytes, mimeType);
    }

    public AggressionAnalysisResult analyze(byte[] imageBytes, String mimeType) throws Exception {
        String rawJson = analyzeRaw(imageBytes, mimeType);
        return parser.parseAggressionAnalysisResult(rawJson);
    }

    public AggressionAnalysisResult parseRawJson(String rawJson) throws Exception {
        return parser.parseAggressionAnalysisResult(rawJson);
    }
}
