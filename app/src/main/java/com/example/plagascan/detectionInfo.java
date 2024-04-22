package com.example.plagascan;

import android.graphics.Rect;
import android.graphics.RectF;

public class detectionInfo {
    private RectF boundingBox;
    private int classIndex;
    private float score;
    private String className;

    public detectionInfo(RectF boundingBox, int classIndex, float score, String className) {
        this.boundingBox = boundingBox;
        this.classIndex = classIndex;
        this.score = score;
        this.className = className;
    }

    public detectionInfo(int classIndex, float score, String className) {
        this.classIndex = classIndex;
        this.score = score;
        this.className = className;
    }

    public RectF getBoundingBox() {
        return boundingBox;
    }

    public int getClassIndex() {
        return classIndex;
    }

    public float getScore() {
        return score;
    }

    public String getClassName() {
        return className;
    }
}
