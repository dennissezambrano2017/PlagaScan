package com.example.plagascan;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.media.Image;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.mediapipe.framework.image.BitmapImageBuilder;
import com.google.mediapipe.framework.image.MPImage;
import com.google.mediapipe.tasks.components.containers.Category;
import com.google.mediapipe.tasks.components.containers.Detection;
import com.google.mediapipe.tasks.core.BaseOptions;
import com.google.mediapipe.tasks.core.Delegate;
import com.google.mediapipe.tasks.vision.core.RunningMode;
import com.google.mediapipe.tasks.vision.objectdetector.ObjectDetector;
import com.google.mediapipe.tasks.vision.objectdetector.ObjectDetectorResult;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class probability_pests extends AppCompatActivity {

    ImageView imageViewPest;
    TextView result,txtscore, txtscienfic_name;
    ConstraintLayout btnVerdetalle;
    List<detectionInfo> detectionInfoList = new ArrayList<>();
    private ObjectDetector objectDetector;
    RectF boundingBox;
    int classIndex;
    float score;
    String className,nombreCientifico, nombresComunes;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_probability_pests);
        imageViewPest = findViewById(R.id.iivCapture);
        result = findViewById(R.id.txtcommon_name);
        txtscore = findViewById(R.id.txtpercentaje);
        btnVerdetalle = findViewById(R.id.cldetails);
        txtscienfic_name = findViewById(R.id.txtscientific_name);


        // Recibir byte array de la actividad anterior
        byte[] byteArray = getIntent().getByteArrayExtra("imageBytes");

        // Convertir byte array a Bitmap
        Bitmap receivedImage = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);


        // Muestra la imagen redimensionada en el ImageView
        imageViewPest.setImageBitmap(receivedImage);
        //originalImage = Bitmap.createScaledBitmap(originalImage,imageSize,imageSize,false);
        detectioObject(receivedImage);
    }
    public void onClickdetails(View view)
    {
        if(detectionInfoList.get(0).getClassName().equals("worm-lepidoptera")){
            Intent intent = new Intent(probability_pests.this, details_two_pest.class);
            startActivity(intent);
        }
        else {
            Intent intent= new Intent(probability_pests.this,details_pests.class);
            intent.putExtra("nameClass",detectionInfoList.get(0).getClassName());
            startActivity(intent);
        }
    }
    private void detectioObject(Bitmap image) {
        try {
            ObjectDetector.ObjectDetectorOptions options =
                ObjectDetector.ObjectDetectorOptions.builder()
                        .setBaseOptions(BaseOptions.builder().setDelegate(Delegate.GPU).setModelAssetPath("pest.tflite").build())
                        .setRunningMode(RunningMode.IMAGE)
                        .setScoreThreshold(0.3f)
                        .setMaxResults(1)
                        .build();
            objectDetector = ObjectDetector.createFromOptions(getApplicationContext(), options);

            // Convertir la imagen de tipo Bitmap a un objeto MPImage para ejecutar la inferencia
            MPImage mpImage = new BitmapImageBuilder(image).build();
            // Ejecutar la detección de objetos utilizando el objeto objectDetector
            ObjectDetectorResult detectionResult = objectDetector.detect(mpImage);
            List<Detection> detections = detectionResult.detections();
            // Recorre las detecciones y extrae los datos de categoría
            for (Detection detection : detections) {
                List<Category> categories = detection.categories();
                boundingBox = detection.boundingBox();
                for (Category category : categories) {
                    classIndex = category.index();
                    score = category.score();
                    className = category.categoryName();
                    System.out.println(className+score);
                }
                detectionInfo detectionInfo = new detectionInfo(boundingBox, classIndex, score, className);
                detectionInfoList.add(detectionInfo);
            }
            for (detectionInfo info : detectionInfoList) {

                if(info.getClassName().equals("worm-lepidoptera")){
                    result.setText("Nombre común: Gusano cogollero o Gusano barrenador");
                    ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) txtscore.getLayoutParams();
                    params.topToBottom = R.id.txtcommon_name;
                    txtscore.setLayoutParams(params);
                }
                else{
                    InputStream inputStream = getApplicationContext().getAssets().open("plagas.json");
                    int size = inputStream.available();
                    byte[] buffer = new byte[size];
                    inputStream.read(buffer);
                    inputStream.close();

                    String jsonString = new String(buffer, StandardCharsets.UTF_8);
                    JSONArray plagas = new JSONArray(jsonString);
                    for (int i = 0; i < plagas.length(); i++) {
                        JSONObject plaga = plagas.getJSONObject(i);
                        if (plaga.has("nombre_clase") && plaga.getString("nombre_clase").equals(info.getClassName())) {

                            nombreCientifico = plaga.getString("nombre_cientifico");
                            nombresComunes = plaga.getString("nombre_comun");
                        }
                    }
                    result.setText("Nombre común: "+ nombresComunes);
                    txtscienfic_name.setText("Nombre científico: "+nombreCientifico);
                }
                double score = info.getScore() * 100;
                String formattedScore = String.format("%.2f", score);
                txtscore.setText("Confianza: " + formattedScore+ " %");
                imageViewPest.setImageBitmap(drawCoordinatesOnImage(image,info.getBoundingBox()));
            }
            if(detectionInfoList.size()==0){
                ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) btnVerdetalle.getLayoutParams();
                params.topToBottom = R.id.txtcommon_name;
                btnVerdetalle.setLayoutParams(params);
            }

            objectDetector.close();
        } catch (Exception e) {
            // TODO Handle the exception
            System.out.println(e.toString());
        }
    }
    public Bitmap drawCoordinatesOnImage(Bitmap originalImage, RectF result) {
         //Crear una copia mutable de la imagen original
        Bitmap imageWithCoordinates = originalImage.copy(Bitmap.Config.ARGB_8888, true);

        // Obtener el canvas para dibujar sobre la imagen
        Canvas canvas = new Canvas(imageWithCoordinates);

        // Crear un Paint para las líneas y texto
        Paint linePaint = new Paint();
        linePaint.setColor(Color.RED);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(4);

        // Obtener las coordenadas del objeto detectado
        float x = result.left;
        float y = result.top;
        float w = result.width();
        float h = result.height();

        // Dibujar el rectángulo delimitador
        canvas.drawRect(x, y, x + w, y + h, linePaint);


        // Devolver la imagen modificada
        return imageWithCoordinates;
    }

}