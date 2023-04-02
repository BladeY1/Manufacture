package com.example.manufacturehome;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.manufacturehome.databinding.ActivityMainBinding;
import com.example.manufacturehome.utils.Data;
import com.example.manufacturehome.utils.HttpsUtils;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    private static Context context;
    private ActivityMainBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context=MainActivity.this;
        if (getSupportActionBar() != null){
            getSupportActionBar().hide();
        }
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        if(!Data.bind){
            bindStatusReq();
        }
        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        //NavigationUI.setupWithNavController(navView, navController);
        NavigationUI.setupWithNavController(binding.navView, navController);
    }

    public static Context getContext(){
        return context;
    }

    public void bindStatusReq(){
        //判断是否已经绑定
        String pinInput = "0";
        OkHttpClient okHttpClient = HttpsUtils.getTrustClient();
        RequestBody requestBody = new FormBody.Builder()
                .add("pin",pinInput)
                .build();
        final Request request = new Request.Builder()
                .addHeader("cookie", Data.Jsessionid)
                .url(Data.serverURL+"/api/bindDevice")
                .post(requestBody)
                .build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String ResponseText = response.body().string();
                Integer responseCodeLogin = 0;
                try {
                    JSONObject object = new JSONObject(ResponseText);
                    responseCodeLogin = object.getInt("code");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if(responseCodeLogin == 403 ) {
                    Data.bind=true;
                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            View light_card = findViewById(R.id.the_light);
                            light_card.setVisibility(View.VISIBLE);
                        }});
                }
            }
        });
    }

    public void onAddDeviceClick(View view){
        Intent intent = new Intent(MainActivity.this, AddDeviceActivity.class);
        startActivity(intent);
        finish();
    }

    public void onDeviceDetailClick(View view){
        Intent intent = new Intent(MainActivity.this, DeviceDetailActivity.class);
        startActivity(intent);
        finish();
    }

    public void onSettingClick(View view){
        Intent intent = new Intent(MainActivity.this, AppSettingActivity.class);
        startActivity(intent);
        finish();
    }

    public void onAboutClick(View view){
        Intent intent = new Intent(MainActivity.this, AboutActivity.class);
        startActivity(intent);
        finish();
    }
}