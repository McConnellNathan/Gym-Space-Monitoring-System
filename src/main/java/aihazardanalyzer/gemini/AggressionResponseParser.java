package aihazardanalyzer.gemini;

import aihazardanalyzer.model.AggressionAnalysisResult;
import com.fasterxml.jackson.databind.ObjectMapper;

public class AggressionResponseParser {
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AggressionAnalysisResult parseAggressionAnalysisResult(String json) throws Exception {
        return objectMapper.readValue(json, AggressionAnalysisResult.class);
    }
}
