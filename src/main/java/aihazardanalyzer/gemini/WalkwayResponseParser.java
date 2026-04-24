package aihazardanalyzer.gemini;

import aihazardanalyzer.model.WalkwayAnalysisResult;
import com.fasterxml.jackson.databind.ObjectMapper;

public class WalkwayResponseParser {
    private final ObjectMapper objectMapper = new ObjectMapper();

    public WalkwayAnalysisResult parseWalkwayAnalysisResult(String json) throws Exception {
        return objectMapper.readValue(json, WalkwayAnalysisResult.class);
    }
}
