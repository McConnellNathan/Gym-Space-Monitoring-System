package aihazardanalyzer.capture;

import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

public class WebcamFrameCapture {
    private final VideoCapture camera;
    private final int targetWidth;

    public WebcamFrameCapture(int cameraIndex, int targetWidth) {
        this.camera = new VideoCapture(cameraIndex);
        this.targetWidth = targetWidth;

        if (!camera.isOpened()) {
            throw new IllegalStateException("Could not open webcam at index " + cameraIndex);
        }

        warmUpCamera();
    }

    private void warmUpCamera() {
        Mat throwaway = new Mat();
        for (int i = 0; i < 10; i++) {
            camera.read(throwaway);
            try {
                Thread.sleep(75);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Camera warm-up interrupted.", e);
            }
        }
    }

    public byte[] captureJpegFrame() {
        Mat frame = new Mat();

        for (int attempt = 1; attempt <= 5; attempt++) {
            boolean success = camera.read(frame);

            if (success && !frame.empty()) {
                Mat resized = resizeIfNeeded(frame);
                MatOfByte buffer = new MatOfByte();
                boolean encoded = Imgcodecs.imencode(".jpg", resized, buffer);

                if (encoded) {
                    return buffer.toArray();
                }
            }

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Frame capture interrupted.", e);
            }
        }

        throw new IllegalStateException("Failed to capture a valid webcam frame after multiple attempts.");
    }

    private Mat resizeIfNeeded(Mat frame) {
        if (frame.width() <= targetWidth) {
            return frame;
        }

        double scale = (double) targetWidth / frame.width();
        int targetHeight = (int) Math.round(frame.height() * scale);

        Mat resized = new Mat();
        Imgproc.resize(frame, resized, new Size(targetWidth, targetHeight));
        return resized;
    }

    public void release() {
        if (camera != null) {
            camera.release();
        }
    }
}