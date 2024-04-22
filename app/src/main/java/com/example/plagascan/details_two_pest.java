package com.example.plagascan;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.os.Bundle;

import com.google.android.material.tabs.TabLayout;

public class details_two_pest extends AppCompatActivity {
    TabLayout tabLayout;
    ViewPager viewPager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details_two_pest);
        // Inicializar los objetos tabLayout y viewPager utilizando findViewById
        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);

        // Crear un adaptador para el ViewPager y agregar fragmentos
        VPAdapter vpAdapter = new VPAdapter(getSupportFragmentManager(), FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        vpAdapter.addFragment(new fragment1(), "Cogollero");
        vpAdapter.addFragment(new fragment2(), "Barrenador");
        viewPager.setAdapter(vpAdapter);

        // Configurar el ViewPager y TabLayout después de configurar el adaptador
        tabLayout.setupWithViewPager(viewPager);
    }
}