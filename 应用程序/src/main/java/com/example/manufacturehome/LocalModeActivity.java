package com.example.manufacturehome;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.manufacturehome.databinding.ActivityLocalModeMainBinding;
import com.example.manufacturehome.utils.Data;
import com.example.manufacturehome.utils.HttpsUtils;
import com.example.manufacturehome.utils.Message;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
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

public class LocalModeActivity extends AppCompatActivity {
    private static Context context;
    private ActivityLocalModeMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context=LocalModeActivity.this;
        if (getSupportActionBar() != null){
            getSupportActionBar().hide();
        }
        binding = ActivityLocalModeMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        if(!Data.bind){
            bindStatusReq();
        }

        BottomNavigationView navView = findViewById(R.id.nav_view_local);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                //R.id.navigation_local_home, R.id.navigation_local_dashboard, R.id.navigation_local_notifications)
                R.id.navigation_local_home, R.id.navigation_local_dashboard)
                .build();
        NavHostFragment navHostFragment =
                (NavHostFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.nav_host_fragment_activity_local_mode_main);
        NavController navController = navHostFragment.getNavController();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);
        //NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_local_mode_main);
        //NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        //NavigationUI.setupWithNavController(binding.navViewLocal, navController);
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
                    LocalModeActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            View light_card = findViewById(R.id.the_light);
                            light_card.setVisibility(View.VISIBLE);
                        }});
                }
            }
        });
    }

    public void onBackLoginBtnClick(View view) {
        Intent intent = new Intent(LocalModeActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    public void onLocalAddDeviceClick(View view){
        Intent intent = new Intent(LocalModeActivity.this, LocalAddDeviceActivity.class);
        startActivity(intent);
        finish();
    }

    public void onDeviceDetailClick(View view){
        Intent intent = new Intent(LocalModeActivity.this, DeviceDetailActivity.class);
        startActivity(intent);
        finish();
    }

    public void onSettingClick(View view){
        Intent intent = new Intent(LocalModeActivity.this, AppSettingActivity.class);
        startActivity(intent);
        finish();
    }

    public void onAboutClick(View view){
        Intent intent = new Intent(LocalModeActivity.this, AboutActivity.class);
        startActivity(intent);
        finish();
    }
}



