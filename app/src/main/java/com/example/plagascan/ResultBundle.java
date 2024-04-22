package com.example.plagascan;

import com.google.mediapipe.tasks.vision.objectdetector.ObjectDetectorResult;

import java.util.List;

public class ResultBundle {
    private List<ObjectDetectorResult> results;
    private long inferenceTime;
    private int inputImageHeight;
    private int inputImageWidth;
    private int inputImageRotation;

    public ResultBundle(List<ObjectDetectorResult> results, long inferenceTime, int inputImageHeight, int inputImageWidth, int inputImageRotation) {
        this.results = results;
        this.inferenceTime = inferenceTime;
        this.inputImageHeight = inputImageHeight;
        this.inputImageWidth = inputImageWidth;
        this.inputImageRotation = inputImageRotation;
    }

    public ResultBundle(List<ObjectDetectorResult> results, long inferenceTime, int inputImageHeight, int inputImageWidth) {
        this.results = results;
        this.inferenceTime = inferenceTime;
        this.inputImageHeight = inputImageHeight;
        this.inputImageWidth = inputImageWidth;
    }

    public List<ObjectDetectorResult> getResults() {
        return results;
    }

    public long getInferenceTime() {
        return inferenceTime;
    }

    public int getInputImageHeight() {
        return inputImageHeight;
    }

    public int getInputImageWidth() {
        return inputImageWidth;
    }

    public int getInputImageRotation() {
        return inputImageRotation;
    }
}
