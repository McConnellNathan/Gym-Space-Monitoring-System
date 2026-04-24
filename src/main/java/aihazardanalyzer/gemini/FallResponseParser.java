package aihazardanalyzer.gemini;

import aihazardanalyzer.model.FallAnalysisResult;
import com.fasterxml.jackson.databind.ObjectMapper;

public class FallResponseParser {
    private final ObjectMapper objectMapper = new ObjectMapper();

    public FallAnalysisResult parseFallAnalysisResult(String json) throws Exception {
        return objectMapper.readValue(json, FallAnalysisResult.class);
    }
}
