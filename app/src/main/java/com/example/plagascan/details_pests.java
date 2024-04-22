package com.example.plagascan;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.TextView;

import org.imaginativeworld.whynotimagecarousel.ImageCarousel;
import org.imaginativeworld.whynotimagecarousel.model.CarouselItem;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class details_pests extends AppCompatActivity {
    ImageCarousel carousel;
    TextView common, scientific, descriptor, controlCultural, chemical, controlEtiologic, fountain, link,txttitlee;
    List<CarouselItem> list = new ArrayList<>();
    List<String> imagenes = new ArrayList<>();
    List<String> infoList = new ArrayList<>();
    List<String> returnList = new ArrayList<>();
    String resultString, nombreCientifico, nombresComunes,fuente,enlace;
    JSONArray imagen, descripcion, cultural, quimico, etologico;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details_pests);

        common = findViewById(R.id.txtName_pests);
        scientific = findViewById(R.id.txtvName_scientific);
        descriptor = findViewById(R.id.txtDescripctionC);
        controlCultural = findViewById(R.id.txtcc);
        chemical = findViewById(R.id.txtcq);
        controlEtiologic = findViewById(R.id.txtce);
        fountain = findViewById(R.id.txtvFuente);
        link = findViewById(R.id.txtvEnlace);
        carousel = findViewById(R.id.carousel);
        txttitlee = findViewById(R.id.txtControle);

        carousel.registerLifecycle(getLifecycle());
        Intent intent = getIntent();
        String namexx=intent.getStringExtra("nameClass");

        try {
            json(namexx);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }
    public void flipperImages(List<String> listImagen){
        for (String cadena : listImagen) {
            int resID = getResources().getIdentifier(cadena, "drawable", getPackageName());
            list.add(new CarouselItem(resID));
        }

        carousel.setData(list);
    }
    public void json(String name) throws IOException, JSONException {
        InputStream inputStream = getApplicationContext().getAssets().open("plagas.json");
        int size = inputStream.available();
        byte[] buffer = new byte[size];
        inputStream.read(buffer);
        inputStream.close();

        String jsonString = new String(buffer, StandardCharsets.UTF_8);
        JSONArray plagas = new JSONArray(jsonString);

        for (int i = 0; i < plagas.length(); i++) {
            JSONObject plaga = plagas.getJSONObject(i);
            if (plaga.has("nombre_clase") && plaga.getString("nombre_clase").equals(name)) {

                nombreCientifico = plaga.getString("nombre_cientifico");
                nombresComunes = plaga.getString("nombre_comun");
                imagen = plaga.getJSONArray("imagenes");
                descripcion = plaga.getJSONArray("descripcion");
                cultural = plaga.getJSONArray("control_cultural");
                quimico = plaga.getJSONArray("control_quimico");
                etologico = plaga.getJSONArray("control_etologico");
                fuente = plaga.getString("fuente");
                enlace = plaga.getString("enlace");
                for (int j = 0; j < imagen.length(); j++) {
                    imagenes.add(imagen.getString(j));
                }
                flipperImages(imagenes);
                scientific.setText("Nombre científico: "+nombreCientifico);
                common.setText(nombresComunes);

                returnList = detaildata(descripcion);
                resultString = TextUtils.join(" ", returnList);
                descriptor.setText(resultString);

                returnList.clear();
                returnList = detaildata(cultural);
                resultString = listdata(returnList);
                controlCultural.setText(Html.fromHtml(resultString));

                returnList.clear();
                returnList = detaildata(quimico);
                resultString = listdata(returnList);
                chemical.setText(Html.fromHtml(resultString));

                returnList.clear();
                returnList = detaildata(etologico);
                resultString = listdata(returnList);
                controlEtiologic.setText(Html.fromHtml(resultString));
                System.out.println();
                if(returnList.size()==0)
                    txttitlee.setVisibility(View.GONE);

                SpannableString spannableString = new SpannableString(enlace);
                ClickableSpan clickableSpan = new ClickableSpan() {
                    @Override
                    public void onClick(@NonNull View widget) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(enlace));
                        startActivity(intent);
                    }
                };
                spannableString.setSpan(clickableSpan, 0, enlace.length(), SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);
                link.setText(spannableString);
                link.setMovementMethod(LinkMovementMethod.getInstance());
                fountain.setText("Fuente: "+ fuente);
                break;
            }
        }
    }
    public List<String> detaildata(JSONArray dataList) throws JSONException {

        for (int k = 0; k < dataList.length(); k++) {
            infoList.add(dataList.getString(k));
        }
        return infoList;
    }
    public String listdata(List<String> data){
        String datainfo = "<ul>";
        for (String info : data) {
            datainfo += "<li>&nbsp;" + info + "</li>";
        }
        datainfo += "<ul>";
        return datainfo;
    }

}