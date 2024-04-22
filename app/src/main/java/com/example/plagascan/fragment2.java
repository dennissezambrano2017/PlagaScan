package com.example.plagascan;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.text.Html;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

public class fragment2 extends Fragment {

    ImageCarousel carousel;
    TextView common, scientific, descriptor, controlCultural, chemical,fountain, link;
    List<CarouselItem> list = new ArrayList<>();
    List<String> imagenes = new ArrayList<>();
    List<String> infoList = new ArrayList<>();
    List<String> returnList = new ArrayList<>();
    String resultString, nombreCientifico,fuente,enlace, packageName;
    JSONArray imagen, descripcion, cultural, quimico,nombresComunes;
    Context appContext;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_fragment2, container, false);
        common = rootView.findViewById(R.id.txtName_pests);
        scientific = rootView.findViewById(R.id.txtvName_scientific);
        descriptor = rootView.findViewById(R.id.txtDescripctionC);
        controlCultural = rootView.findViewById(R.id.txtcc);
        chemical = rootView.findViewById(R.id.txtcq);
        fountain = rootView.findViewById(R.id.txtvFuente);
        link = rootView.findViewById(R.id.txtvEnlace);
        carousel = rootView.findViewById(R.id.carousel);

        packageName = getContext().getPackageName();
        appContext = requireContext();
        InputStream inputStream;
        try {
            inputStream = appContext.getAssets().open("plagas.json");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            json("worm-lepidoptera", inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        carousel.registerLifecycle(getLifecycle());
        // Inflate the layout for this fragment
        return rootView;
    }
    public void flipperImages(List<String> listImagen){
        for (String cadena : listImagen) {
            int resID = getResources().getIdentifier(cadena, "drawable", packageName);
            list.add(new CarouselItem(resID));
        }

        carousel.setData(list);
    }
    public void json(String name,InputStream inputStream) throws IOException, JSONException {
        int size = inputStream.available();
        byte[] buffer = new byte[size];
        inputStream.read(buffer);
        inputStream.close();

        String jsonString = new String(buffer, StandardCharsets.UTF_8);
        JSONArray plagas = new JSONArray(jsonString);
        JSONObject barrenador = null;
        for (int i = 0; i < plagas.length(); i++) {
            JSONObject plaga = plagas.getJSONObject(i);
            if (plaga.has("nombre_clase") && plaga.getString("nombre_clase").equals(name)) {
                cultural = plaga.getJSONArray("control_cultural");
                barrenador = plaga.getJSONObject("barrenador");

                returnList.clear();
                returnList = detaildata(cultural);
                resultString = listdata(returnList);
                controlCultural.setText(Html.fromHtml(resultString));
                break;
            }
        }

        if (barrenador != null) {
            nombreCientifico = barrenador.getJSONArray("nombre_cientifico").getString(0);
            scientific.setText("Nombre científico: " + nombreCientifico);
            returnList.clear();
            nombresComunes = barrenador.getJSONArray("nombre_comun");
            returnList = detaildata(nombresComunes);
            resultString = TextUtils.join(" / ", returnList);
            common.setText(resultString);

            imagen = barrenador.getJSONArray("imagenes");
            descripcion = barrenador.getJSONArray("descripcion");
            quimico = barrenador.getJSONArray("control_quimico");
            fuente = barrenador.getString("fuente");
            enlace = barrenador.getString("enlace");
            for (int j = 0; j < imagen.length(); j++) {
                imagenes.add(imagen.getString(j));
            }
            flipperImages(imagenes);

            returnList.clear();
            returnList = detaildata(descripcion);
            resultString = TextUtils.join(" ", returnList);
            descriptor.setText(resultString);

            returnList.clear();
            returnList = detaildata(quimico);
            resultString = listdata(returnList);
            chemical.setText(Html.fromHtml(resultString));

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
            fountain.setText("Fuente: " + fuente);
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