package aihazardanalyzer.gemini;

import aihazardanalyzer.model.OccupancyAnalysisResult;
import com.fasterxml.jackson.databind.ObjectMapper;

public class OccupancyResponseParser {
    private final ObjectMapper objectMapper = new ObjectMapper();

    public OccupancyAnalysisResult parseOccupancyAnalysisResult(String json) throws Exception {
        return objectMapper.readValue(json, OccupancyAnalysisResult.class);
    }
}