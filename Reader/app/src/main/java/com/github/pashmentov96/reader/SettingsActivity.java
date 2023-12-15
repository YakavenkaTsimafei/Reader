package com.github.pashmentov96.reader;

import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.Locale;

public class SettingsActivity extends AppCompatActivity {

    Spinner spinner;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        SomePreferences somePreferences = new SomePreferences(this);

        Log.d("MyLogs", "Locale = " + getResources().getConfiguration().locale);

        setLocale(somePreferences.getVariableLanguage(), true);

        Toolbar childToolbar = findViewById(R.id.my_toolbar_2);
        setSupportActionBar(childToolbar);

        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);

        spinner = findViewById(R.id.spinner);

        ArrayAdapter<?> adapter = ArrayAdapter.createFromResource(this, R.array.languages_list, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        break;
                    case 1:
                        setLocale("ru", false);
                        break;
                    case 2:
                        setLocale("en", false);
                        break;
                    default:
                        //nothing
                }
            }
        });
    }

    public void setLocale(String localeName, boolean isFirstBoot) {
        SomePreferences somePreferences = new SomePreferences(this);
        String prevLocale = getResources().getConfiguration().locale.toString();
        if (prevLocale.contains("en")) {
            prevLocale = "en";
        }
        if ((isFirstBoot && !prevLocale.equals(localeName)) || !localeName.equals(somePreferences.getVariableLanguage())) {
            Locale myLocale = new Locale(localeName);
            Configuration conf = new Configuration();
            conf.setLocale(myLocale);
            getResources().updateConfiguration(conf, getResources().getDisplayMetrics());
            somePreferences.setVariableLanguage(localeName);
            recreate();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
