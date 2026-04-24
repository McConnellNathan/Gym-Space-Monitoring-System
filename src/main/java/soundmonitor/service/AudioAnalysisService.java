package soundmonitor.service;

import soundmonitor.gemini.AudioResponseParser;
import soundmonitor.gemini.GeminiAudioClient;
import soundmonitor.model.AudioAnalysisResult;

public class AudioAnalysisService {
    private final GeminiAudioClient audioClient;
    private final AudioResponseParser parser;

    public AudioAnalysisService() {
        this.audioClient = new GeminiAudioClient();
        this.parser = new AudioResponseParser();
    }

    public AudioAnalysisResult analyzeWavClip(byte[] wavBytes) throws Exception {
        String rawJson = audioClient.classifyAudioClip(wavBytes, "audio/wav");
        return parser.parseAudioAnalysisResult(rawJson);
    }
}
