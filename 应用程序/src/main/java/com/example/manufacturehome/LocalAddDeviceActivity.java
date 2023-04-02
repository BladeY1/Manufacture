package com.example.manufacturehome;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.manufacturehome.databinding.ActivityLocalAddDeviceBinding;
import com.example.manufacturehome.utils.Data;
import com.example.manufacturehome.utils.HttpsUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LocalAddDeviceActivity extends AppCompatActivity {
    private ActivityLocalAddDeviceBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLocalAddDeviceBinding.inflate(getLayoutInflater());
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(R.string.addDevice);
        actionBar.setDisplayHomeAsUpEnabled(true);
        setContentView(binding.getRoot());
    }

    public void onEnterPinCardClick(View view){
        binding.pinInputCard.setVisibility(View.VISIBLE);
        binding.enterDeviceCateCard.setVisibility(View.GONE);
        binding.enterPinCard.setVisibility(View.GONE);
    }

    public void onPinConfirm(View view){
        String pinInput = binding.pinedit.getText().toString().trim();
        if (!TextUtils.isEmpty(pinInput)) {
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
                public void onFailure(final Call call, IOException e) {
                    //在UI线程才能更新UI
                    LocalAddDeviceActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(LocalAddDeviceActivity.this,R.string.net_error,Toast.LENGTH_SHORT).show();
                        }
                    });
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

                    if (responseCodeLogin == 200) {
                        LocalAddDeviceActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(LocalAddDeviceActivity.this, R.string.bind_success, Toast.LENGTH_SHORT).show();
                            }});
                        Data.bind=true;
                        Intent intent = new Intent(LocalAddDeviceActivity.this, LocalModeActivity.class);
                        startActivity(intent);
                        finish();
                    }
                    else if(responseCodeLogin == 400 ) {
                        LocalAddDeviceActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(LocalAddDeviceActivity.this, R.string.bind_failed_pin, Toast.LENGTH_SHORT).show();
                            }});
                    }
                    else if(responseCodeLogin == 403 ) {
                        LocalAddDeviceActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(LocalAddDeviceActivity.this, R.string.bind_already, Toast.LENGTH_SHORT).show();
                            }});
                    }
                }
            });
        }
    }




    //toolbar返回键
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            Intent intent = new Intent(LocalAddDeviceActivity.this, LocalModeActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(LocalAddDeviceActivity.this, LocalModeActivity.class);
        startActivity(intent);
        finish();
    }

    public void onEnterIPActivityClick(View view) {
        Intent intent = new Intent(LocalAddDeviceActivity.this, ClientActivity.class);
        startActivity(intent);
        finish();
    }
}