package soundmonitor.gemini;

import soundmonitor.model.AudioAnalysisResult;
import com.fasterxml.jackson.databind.ObjectMapper;

public class AudioResponseParser {
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AudioAnalysisResult parseAudioAnalysisResult(String json) throws Exception {
        return objectMapper.readValue(json, AudioAnalysisResult.class);
    }
}
