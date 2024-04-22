package com.example.plagascan;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.VisibleForTesting;
import androidx.camera.core.ImageProxy;

import com.google.mediapipe.framework.image.BitmapImageBuilder;
import com.google.mediapipe.framework.image.MPImage;
import com.google.mediapipe.tasks.core.BaseOptions;
import com.google.mediapipe.tasks.core.Delegate;
import com.google.mediapipe.tasks.vision.core.ImageProcessingOptions;
import com.google.mediapipe.tasks.vision.core.RunningMode;
import com.google.mediapipe.tasks.vision.objectdetector.ObjectDetector;
import com.google.mediapipe.tasks.vision.objectdetector.ObjectDetectorResult;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Future;

public class ObjectDetectorHelper {
    private static com.google.mediapipe.tasks.vision.core.RunningMode RunningMode;

    private float threshold = THRESHOLD_DEFAULT;
    private int maxResults = MAX_RESULTS_DEFAULT;
    private Delegate currentDelegate = DELEGATE_GPU;
    private String currentModel = MODEL;
    private RunningMode runningMode = RunningMode.IMAGE;
    private Context context;
    private DetectorListener objectDetectorListener;

    private ObjectDetector objectDetector;
    private int imageRotation = 0;
    private ImageProcessingOptions imageProcessingOptions;

    public ObjectDetectorHelper(Context context, DetectorListener listener) {
        this.context = context;
        this.objectDetectorListener = listener;
        setupObjectDetector();
    }

    public ObjectDetectorHelper(camera context, float currentThreshold, Delegate currentDelegate, String currentModel, int currentMaxResults, camera camera, com.google.mediapipe.tasks.vision.core.RunningMode liveStream) {
        this.context = context;
        this.threshold= currentThreshold;
        this.currentDelegate = currentDelegate;
        this.currentModel = currentModel;
        this.maxResults = currentMaxResults;
        this.objectDetectorListener=camera;
        this.runningMode = liveStream;

    }

    public void clearObjectDetector() {
        if (objectDetector != null) {
            objectDetector.close();
            objectDetector = null;
        }
    }

    public void setupObjectDetector() {
        // Set general detection options, including number of used threads
        BaseOptions.Builder baseOptionsBuilder = BaseOptions.builder();

        // Use the specified hardware for running the model. Default to CPU
        baseOptionsBuilder.setDelegate(this.currentDelegate);

        String modelName = this.currentModel;

        baseOptionsBuilder.setModelAssetPath(modelName);

        // Check if runningMode is consistent with objectDetectorListener
        switch (runningMode) {
            case LIVE_STREAM:
                if (objectDetectorListener == null) {
                    throw new IllegalStateException(
                            "objectDetectorListener must be set when runningMode is LIVE_STREAM."
                    );
                }
                break;

            case IMAGE:
                break;
        }

        try {
            ObjectDetector.ObjectDetectorOptions.Builder optionsBuilder =
                    ObjectDetector.ObjectDetectorOptions.builder()
                            .setBaseOptions(baseOptionsBuilder.build())
                            .setScoreThreshold(threshold)
                            .setRunningMode(runningMode)
                            .setMaxResults(maxResults);

            imageProcessingOptions = ImageProcessingOptions.builder()
                    .setRotationDegrees(imageRotation)
                    .build();

            switch (runningMode) {
                case IMAGE:
                case LIVE_STREAM:
                    optionsBuilder.setRunningMode(runningMode)
                            .setResultListener(this:: returnLivestreamResult)
                            .setErrorListener(this::returnLivestreamError);
                    break;
            }

            ObjectDetector.ObjectDetectorOptions options = optionsBuilder.build();
            objectDetector = ObjectDetector.createFromOptions(context, options);
        } catch (IllegalStateException e) {
            if (objectDetectorListener != null) {
                int errorCode = 0;

                // Llama a onError con el mensaje de error y el código de error
                objectDetectorListener.onError(
                        e.getMessage() != null ? e.getMessage() : "An unknown error has occurred",
                        errorCode
                );
            }
            Log.e(TAG, "TFLite failed to load model with error: " + e.getMessage());
        } catch (RuntimeException e) {
            if (objectDetectorListener != null) {
                objectDetectorListener.onError(
                        "Object detector failed to initialize. See error logs for details",
                        GPU_ERROR
                );
            }
            Log.e(TAG, "Object detector failed to load model with error: " + e.getMessage());
        }
    }

    public boolean isClosed() {
        return objectDetector == null;
    }

    public void detectLivestreamFrame(ImageProxy imageProxy) {
        if (runningMode != com.example.plagascan.ObjectDetectorHelper.RunningMode.LIVE_STREAM) {
            throw new IllegalArgumentException(
                    "Attempting to call detectLivestreamFrame while not using RunningMode.LIVE_STREAM"
            );
        }

        long frameTime = SystemClock.uptimeMillis();

        // Copy out RGB bits from the frame to a bitmap buffer
        Bitmap bitmapBuffer = Bitmap.createBitmap(
                imageProxy.getWidth(), imageProxy.getHeight(), Bitmap.Config.ARGB_8888
        );
        Log.d(TAG, "Image Resolution: " + imageProxy.getWidth() + "x" + imageProxy.getHeight());

        try {
            ImageProxy.PlaneProxy plane = imageProxy.getPlanes()[0];
            ByteBuffer buffer = plane.getBuffer();
            bitmapBuffer.copyPixelsFromBuffer(buffer);
        } finally {
            imageProxy.close();
        }

        // If the input image rotation is changed, stop all detector
        if (imageProxy.getImageInfo().getRotationDegrees() != imageRotation) {
            imageRotation = imageProxy.getImageInfo().getRotationDegrees();
            clearObjectDetector();
            setupObjectDetector();
            return;
        }

        // Convert the input Bitmap object to an MPImage object to run inference
        MPImage mpImage = new BitmapImageBuilder(bitmapBuffer).build();

        detectAsync(mpImage, frameTime);

    }

    @VisibleForTesting
    public void detectAsync(MPImage mpImage, long frameTime) {
        if (objectDetector != null) {
            objectDetector.detectAsync(mpImage, imageProcessingOptions, frameTime);
        }
    }

    private void returnLivestreamResult(ObjectDetectorResult result, MPImage input) {
        long finishTimeMs = SystemClock.uptimeMillis();
        long inferenceTime = finishTimeMs - result.timestampMs();

        if (objectDetectorListener != null) {
            objectDetectorListener.onResults(new ResultBundle(
                    Collections.singletonList(result),
                    inferenceTime,
                    input.getHeight(),
                    input.getWidth(),
                    imageRotation
            ));
        }
    }


    private void returnLivestreamError(RuntimeException error) {
        int errorCode = 0;

        // Llama a onError con el mensaje de error y el código de error
        objectDetectorListener.onError(
                error.getMessage() != null ? error.getMessage() : "An unknown error has occurred",
                errorCode
        );
    }


    public ResultBundle detectImage(Bitmap image) {
        if (runningMode != RunningMode.IMAGE) {
            throw new IllegalArgumentException(
                    "Attempting to call detectImage while not using RunningMode.IMAGE"
            );
        }

        if (objectDetector == null) {
            return null;
        }

        // Inference time is the difference between the system time at the start and finish of the process
        long startTime = SystemClock.uptimeMillis();

        // Convert the input Bitmap object to an MPImage object to run inference
        MPImage mpImage = new BitmapImageBuilder(image).build();

        // Run object detection using MediaPipe Object Detector API
        ObjectDetectorResult detectionResult = objectDetector.detect(mpImage);
        if (detectionResult != null) {
            long inferenceTimeMs = SystemClock.uptimeMillis() - startTime;
            return new ResultBundle(
                    Collections.singletonList(detectionResult),
                    inferenceTimeMs,
                    image.getHeight(),
                    image.getWidth()
            );
        }


        // If objectDetector.detect() returns null, this is likely an error. Returning null to indicate this.
        return null;
    }


    public interface DetectorListener {
        void onError(String error, int errorCode);

        void onResults(ResultBundle resultBundle);
    }
    
    // Constantes y métodos estáticos
    public static final int DELEGATE_CPU = 0;
    public static final Delegate DELEGATE_GPU = Delegate.GPU;
    public static final String MODEL = "pest.tflite";
    public static final int MAX_RESULTS_DEFAULT = 1;
    public static final float THRESHOLD_DEFAULT = 0.5F;
    public static final int OTHER_ERROR = 0;
    public static final int GPU_ERROR = 1;

    public static final String TAG = "ObjectDetectorHelper";

}
