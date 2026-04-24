package app;

import aihazardanalyzer.capture.WebcamFrameCapture;
import aihazardanalyzer.model.AggressionAnalysisResult;
import aihazardanalyzer.model.FallAnalysisResult;
import aihazardanalyzer.model.OccupancyAnalysisResult;
import aihazardanalyzer.model.WalkwayAnalysisResult;
import aihazardanalyzer.service.AggressionAnalysisService;
import aihazardanalyzer.service.AggressionResultStabilizer;
import aihazardanalyzer.service.FallAnalysisService;
import aihazardanalyzer.service.FallResultStabilizer;
import aihazardanalyzer.service.OccupancyAnalysisService;
import aihazardanalyzer.service.OccupancyResultStabilizer;
import aihazardanalyzer.service.WalkwayAnalysisService;
import aihazardanalyzer.service.WalkwayResultStabilizer;
import soundmonitor.model.AudioAnalysisResult;
import soundmonitor.service.AudioAnalysisService;
import soundmonitor.service.AudioClipRecorder;
import nu.pattern.OpenCV;

public class ShortAiTest {
    public static void main(String[] args) throws Exception {
        OpenCV.loadLocally();

        WebcamFrameCapture webcam = new WebcamFrameCapture(0, 640);

        OccupancyAnalysisService occupancyAnalysisService = new OccupancyAnalysisService();
        AggressionAnalysisService aggressionAnalysisService = new AggressionAnalysisService();
        FallAnalysisService fallAnalysisService = new FallAnalysisService();
        WalkwayAnalysisService walkwayAnalysisService = new WalkwayAnalysisService();

        OccupancyResultStabilizer occupancyStabilizer = new OccupancyResultStabilizer(3);
        AggressionResultStabilizer aggressionStabilizer = new AggressionResultStabilizer(3);
        FallResultStabilizer fallStabilizer = new FallResultStabilizer(3);
        WalkwayResultStabilizer walkwayStabilizer = new WalkwayResultStabilizer(3);

        AudioClipRecorder audioClipRecorder = new AudioClipRecorder();
        AudioAnalysisService audioAnalysisService = new AudioAnalysisService();

        try {
            System.out.println("=== VISION TEST START ===");

            for (int i = 1; i <= 5; i++) {
                byte[] imageBytes = webcam.captureJpegFrame();

                OccupancyAnalysisResult rawOccupancy =
                        occupancyAnalysisService.analyze(imageBytes, "image/jpeg");
                OccupancyAnalysisResult stableOccupancy =
                        occupancyStabilizer.addAndStabilize(rawOccupancy);

                AggressionAnalysisResult rawAggression =
                        aggressionAnalysisService.analyze(imageBytes, "image/jpeg");
                AggressionAnalysisResult stableAggression =
                        aggressionStabilizer.addAndStabilize(rawAggression);

                FallAnalysisResult rawFall =
                        fallAnalysisService.analyze(imageBytes, "image/jpeg");
                FallAnalysisResult stableFall =
                        fallStabilizer.addAndStabilize(rawFall);

                WalkwayAnalysisResult rawWalkway =
                        walkwayAnalysisService.analyze(imageBytes, "image/jpeg");
                WalkwayAnalysisResult stableWalkway =
                        walkwayStabilizer.addAndStabilize(rawWalkway);

                System.out.println("Frame " + i + " occupancy raw:");
                System.out.println(rawOccupancy);
                System.out.println("Frame " + i + " occupancy stabilized:");
                System.out.println(stableOccupancy);
                System.out.println();

                System.out.println("Frame " + i + " aggression raw:");
                System.out.println(rawAggression);
                System.out.println("Frame " + i + " aggression stabilized:");
                System.out.println(stableAggression);
                System.out.println();

                System.out.println("Frame " + i + " fall raw:");
                System.out.println(rawFall);
                System.out.println("Frame " + i + " fall stabilized:");
                System.out.println(stableFall);
                System.out.println();

                System.out.println("Frame " + i + " walkway raw:");
                System.out.println(rawWalkway);
                System.out.println("Frame " + i + " walkway stabilized:");
                System.out.println(stableWalkway);
                System.out.println("--------------------------------------------------");
                System.out.println();

                Thread.sleep(1000);
            }

            System.out.println("=== VISION TEST END ===");
            System.out.println();

            System.out.println("=== AUDIO TEST START ===");
            System.out.println("Make a sound now: clap, speak loudly, or create a short noise burst...");
            Thread.sleep(1000);

            byte[] wavBytes = audioClipRecorder.recordWavClip(1000);
            AudioAnalysisResult audioResult = audioAnalysisService.analyzeWavClip(wavBytes);

            System.out.println("Audio analysis result:");
            System.out.println(audioResult);
            System.out.println("=== AUDIO TEST END ===");

        } finally {
            webcam.release();
        }
    }
}