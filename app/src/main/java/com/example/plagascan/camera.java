package com.example.plagascan;


import static android.app.PendingIntent.getActivity;

import static com.example.plagascan.ObjectDetectorHelper.TAG;

import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.AspectRatio;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import android.Manifest;


import com.example.plagascan.databinding.ActivityCameraBinding;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mediapipe.tasks.vision.core.RunningMode;
import com.google.mediapipe.tasks.vision.objectdetector.ObjectDetectorResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class camera extends AppCompatActivity implements ObjectDetectorHelper.DetectorListener{

    private static final int REQUEST_CODE_PERMISSIONS = 111;
    private ActivityCameraBinding viewBinding;
    private ObjectDetectorHelper objectDetectorHelper;
    private MainViewModel viewModel;
    private Preview preview;
    private ImageAnalysis imageAnalyzer;
    private Camera camera;
    OverlayView overlayView;
    private ProcessCameraProvider cameraProvider;
    private ExecutorService backgroundExecutor;

    private TextView common1,porcentaje1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewModel = new ViewModelProvider(this).get(MainViewModel.class);
        viewBinding = ActivityCameraBinding.inflate(getLayoutInflater());
        setContentView(viewBinding.getRoot());
        overlayView = findViewById(R.id.overlay);
        overlayView.setCameraActivity(this);


        common1= findViewById(R.id.txtCommon1);
        porcentaje1= findViewById(R.id.txtVPorcentaje1);

        backgroundExecutor = Executors.newSingleThreadExecutor();

        backgroundExecutor.execute(() -> {
            objectDetectorHelper = new ObjectDetectorHelper(
                    this,
                    viewModel.getCurrentThreshold(),
                    viewModel.getCurrentDelegate(),
                    viewModel.getCurrentModel(),
                    viewModel.getCurrentMaxResults(),
                    this,
                    RunningMode.LIVE_STREAM
            );

            viewBinding.viewFinder.post(() -> requestPermissions());
        });

        viewBinding.overlay.setRunningMode(RunningMode.LIVE_STREAM);
    }

    public void infodetection(List<detectionInfo> detectionInfoList) {
        //clear();
        String commonText = new String();
        String porcentajeText = new String();
        System.out.println(detectionInfoList.size());
        for (int i = 0; i < detectionInfoList.size(); i++) {
            detectionInfo info = detectionInfoList.get(i);
            if (detectionInfoList.get(i).getClassName().equals("worm-lepidoptera")) {
                commonText = "Plaga: Gusano cogollero o Gusano barrenador";
                porcentajeText =String.format("%.2f", (info.getScore()*100))+"%";
            } else {
                InputStream inputStream = null;
                try {
                    inputStream = getApplicationContext().getAssets().open("plagas.json");

                    int size = inputStream.available();
                    byte[] buffer = new byte[size];
                    inputStream.read(buffer);
                    inputStream.close();

                    String jsonString = new String(buffer, StandardCharsets.UTF_8);
                    JSONArray plagas = new JSONArray(jsonString);
                    for (int j = 0; j < plagas.length(); j++) {
                        JSONObject plaga = plagas.getJSONObject(j);
                        if (plaga.has("nombre_clase") && plaga.getString("nombre_clase").equals(info.getClassName())) {

                            commonText = "Nombre común: "+plaga.getString("nombre_comun");
                            porcentajeText =String.format("%.2f", (info.getScore()*100))+"%";
                        }
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
                System.out.println(i+" "+commonText);

                common1.setText(commonText);
                porcentaje1.setText("Confianza:" + porcentajeText);

            }
        }
    }
    public void clear(){
        common1.setText("");
        porcentaje1.setText("");
    }


    private void setUpCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();
                bindCameraUseCases();
            } catch (Exception e) {
                Log.e(TAG, "Unable to get camera provider.", e);
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void bindCameraUseCases() {
        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        preview = new Preview.Builder()
                .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                .setTargetRotation(viewBinding.viewFinder.getDisplay().getRotation())
                .build();

        imageAnalyzer = new ImageAnalysis.Builder()
                .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                .setTargetRotation(viewBinding.viewFinder.getDisplay().getRotation())
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                .build();

        imageAnalyzer.setAnalyzer(backgroundExecutor, objectDetectorHelper::detectLivestreamFrame);

        cameraProvider.unbindAll();
        try {
            camera = cameraProvider.bindToLifecycle(
                    this,
                    cameraSelector,
                    preview,
                    imageAnalyzer
            );

            preview.setSurfaceProvider(viewBinding.viewFinder.getSurfaceProvider());
        } catch (Exception exc) {
            Log.e(TAG, "Use case binding failed", exc);
        }
    }

    private void requestPermissions() {
        // Solicitar permisos al usuario
        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.CAMERA},
                REQUEST_CODE_PERMISSIONS
        );
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // Verificar si el código de solicitud es el mismo que el código de permisos
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            // Verificar si se han otorgado todos los permisos requeridos
            if (allPermissionsGranted()) {
                setUpCamera();
            } else {
                Toast.makeText(this,
                        "Permisos no otorgados por el usuario.",
                        Toast.LENGTH_SHORT).show();

                finish();
            }
        }
    }
   private boolean allPermissionsGranted() {
        // Lista de permisos necesarios para la cámara
        String[] requiredPermissions = {Manifest.permission.CAMERA};

        // Verificar si todos los permisos están otorgados
        for (String permission : requiredPermissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }

        return true;
    }
    
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (imageAnalyzer != null) {
            imageAnalyzer.setTargetRotation(viewBinding.viewFinder.getDisplay().getRotation());
        }
    }

    @Override
    public void onError(String error, int errorCode) {
        System.out.println("error Code: " +errorCode+" message: "+error);
    }

    @Override
    public void onResults(ResultBundle resultBundle) {
        runOnUiThread(() -> {
            if (viewBinding != null) {

                // Pasar la información necesaria a OverlayView para dibujar en el lienzo
                ObjectDetectorResult detectionResult = resultBundle.getResults().get(0);
                viewBinding.overlay.setResults(
                        detectionResult,
                        resultBundle.getInputImageHeight(),
                        resultBundle.getInputImageWidth(),
                        resultBundle.getInputImageRotation()
                );

                // Forzar un redibujo
                viewBinding.overlay.invalidate();
                //clear();
            }
        });

    }
}