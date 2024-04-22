package com.example.plagascan;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.Manifest;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class identify_pests extends AppCompatActivity {

    ConstraintLayout camera, gallery;
    public static int REQUEST_CAMERA = 111;
    public static int REQUEST_GALLERY = 222;
    private ProgressBar progressBar;
    private LayoutInflater inflater;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_identify_pests);
        camera = findViewById(R.id.clnewcapture);
        gallery = findViewById(R.id.clgallery);
        inflater = getLayoutInflater();
        if(checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
            requestPermissions(new String[]{Manifest.permission.CAMERA},100);

        View progressBarLayout = inflater.inflate(R.layout.loading_layout, null);
        progressBar = progressBarLayout.findViewById(R.id.progressBar);
    }
    public void abrirGaleria (View view){

        Intent i = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, REQUEST_GALLERY);
    }

    public void abrirCamera (View view){
        /*/Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, REQUEST_CAMERA);*/
        Intent intent = new Intent(identify_pests.this, camera.class);
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && null != data) {
            Bitmap image = null;

            if (requestCode == REQUEST_CAMERA){
                // Obtener la imagen de la cámara
                image = (Bitmap) data.getExtras().get("data");
            } else{
                // Obtener la imagen de la galería
                try {
                    image = MediaStore.Images.Media.getBitmap(this.getContentResolver(), data.getData());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (image != null) {
                int imageSize = 256;
                image = Bitmap.createScaledBitmap(image, imageSize, imageSize, false);

                // Muestra el ProgressBar
                progressBar.setVisibility(View.VISIBLE);

                // Envía la imagen a la siguiente actividad
                Intent intent = new Intent(identify_pests.this, probability_pests.class);

                // Convertir Bitmap a byte array
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                image.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byte[] byteArray = stream.toByteArray();

                intent.putExtra("imageBytes", byteArray);

                // Inicia la actividad
                startActivity(intent);

                // Oculta el ProgressBar después de iniciar la actividad
                progressBar.setVisibility(View.GONE);
            }

        }

    }
}