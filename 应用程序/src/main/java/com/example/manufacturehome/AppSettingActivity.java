package com.example.manufacturehome;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.LocaleList;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextClock;
import android.widget.Toast;

import java.util.Locale;

public class AppSettingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(R.string.setting);
        actionBar.setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.activity_app_setting);
    }

    public void onLangSettingClick(View view){
    findViewById(R.id.lang_switch_text).setVisibility(View.GONE);
    findViewById(R.id.chs_card).setVisibility(View.VISIBLE);
    findViewById(R.id.en_card).setVisibility(View.VISIBLE);
    }


    public void updateLocaleClick(View view) {
        Locale newLocale;
        if (view.getId()==R.id.chs_card){
            newLocale = new Locale("zh");
        }
        else {
            newLocale = new Locale("en");
        }

        updateLocale(BaseApplication.getAppContext(),newLocale);
        updateLocale(AppSettingActivity.this,newLocale);
        Toast.makeText(AppSettingActivity.this,
                R.string.Success,
                Toast.LENGTH_SHORT).show();
    }

    private void updateLocale(Context context,Locale newLocale) {
        Resources resources = context.getResources();
        Configuration configuration = resources.getConfiguration();
        configuration.setLocales(new LocaleList(newLocale));
        resources.updateConfiguration(configuration, null);
    }

    //toolbar返回键
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            Intent intent = new Intent(AppSettingActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(AppSettingActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}