package com.example.plagascan;

import static androidx.camera.core.impl.utils.ContextUtil.getApplicationContext;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;

import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import com.example.plagascan.databinding.ActivityCameraBinding;
import com.google.mediapipe.tasks.components.containers.Category;
import com.google.mediapipe.tasks.components.containers.Detection;
import com.google.mediapipe.tasks.vision.core.RunningMode;
import com.google.mediapipe.tasks.vision.objectdetector.ObjectDetectorResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.DatagramSocket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;


public class OverlayView extends View {
    private ObjectDetectorResult results;
    private Paint boxPaint,textBackgroundPaint,textPaint;
    private float scaleFactor, score;
    private Rect bounds = new Rect();
    private int outputWidth, outputHeight,classIndex,outputRotate;
    private RunningMode runningMode;
    private String className;


    private WeakReference<camera> cameraActivityRef;
    public OverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }


    private void init() {
        initPaints();
        
    }

    public void clear() {
        results = null;
        boxPaint.reset();
        invalidate();
        initPaints();
    }

    public void setRunningMode(RunningMode runningMode) {
        this.runningMode = runningMode;
    }
    public void setCameraActivity(camera activity) {
        this.cameraActivityRef = new WeakReference<>(activity);
    }

    @SuppressLint("ResourceType")
    private void initPaints() {
        boxPaint = new Paint();
        boxPaint.setColor(ContextCompat.getColor(getContext(), R.color.mp_red));
        boxPaint.setStrokeWidth(8f);
        boxPaint.setStyle(Paint.Style.STROKE);
    }

    @SuppressLint("RestrictedApi")
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        List<detectionInfo> detectionInfoList = new ArrayList<>();
        if (results != null) {
            List<Detection> detections = results.detections();
            for (Detection detection : detections) {
                RectF boxRect = new RectF(
                        detection.boundingBox().left,
                        detection.boundingBox().top,
                        detection.boundingBox().right,
                        detection.boundingBox().bottom
                );

                Matrix matrix = new Matrix();
                matrix.postTranslate(-outputWidth / 2f, -outputHeight / 2f);

                matrix.postRotate((float) outputRotate);

                if (outputRotate == 90 || outputRotate == 270) {
                    matrix.postTranslate(outputHeight / 2f, outputWidth / 2f);
                } else {
                    matrix.postTranslate(outputWidth / 2f, outputHeight / 2f);
                }
                matrix.mapRect(boxRect);

                float top = boxRect.top * scaleFactor;
                float bottom = boxRect.bottom * scaleFactor;
                float left = boxRect.left * scaleFactor;
                float right = boxRect.right * scaleFactor;

                RectF drawableRect = new RectF(left, top, right, bottom);
                canvas.drawRect(drawableRect, boxPaint);

                List<Category> categories = detection.categories();

                for (Category category : categories) {
                    classIndex = category.index();
                    score = category.score();
                    className = category.categoryName();
                }
                detectionInfo detectionInfo = new detectionInfo( classIndex, score, className);
                detectionInfoList.add(detectionInfo);
                camera cameraActivity = cameraActivityRef.get();
                if (cameraActivity != null) {
                    cameraActivity.infodetection(detectionInfoList);
                }
            }
        }
    }

    public void setResults(ObjectDetectorResult detectionResults, int outputHeight, int outputWidth, int imageRotation) {
        this.results = detectionResults;
        this.outputWidth = outputWidth;
        this.outputHeight = outputHeight;
        this.outputRotate = imageRotation;

        int rotatedWidth;
        int rotatedHeight;

        switch (imageRotation) {
            case 0:
            case 180:
                rotatedWidth = outputWidth;
                rotatedHeight = outputHeight;
                break;

            case 90:
            case 270:
                rotatedWidth = outputHeight;
                rotatedHeight = outputWidth;
                break;

            default:
                return;
        }

        scaleFactor = (runningMode == RunningMode.LIVE_STREAM)
                ? Math.max(getWidth() * 1f / rotatedWidth, getHeight() * 1f / rotatedHeight)
                : Math.min(getWidth() * 1f / rotatedWidth, getHeight() * 1f / rotatedHeight);

        invalidate();
    }
    //private static final int BOUNDING_RECT_TEXT_PADDING = 8;

}
