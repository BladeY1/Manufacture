package com.example.manufacturehome;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;

import com.example.manufacturehome.utils.Data;
import com.example.manufacturehome.utils.HttpsUtils;
import com.example.manufacturehome.utils.MD5encryption;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Toast;

import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.manufacturehome.databinding.ActivityDeviceDetailBinding;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class DeviceDetailActivity extends AppCompatActivity {
    private ActivityDeviceDetailBinding binding;
    private Timer updateLightTimer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null){
            getSupportActionBar().hide();
        }
        binding = ActivityDeviceDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //间隔0.4s更新ui
        updateLightTimer = new Timer();
        updateLightTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                DeviceDetailActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateData();
                        updateUIFromData();
                    }
                });
            }
        }, 0, 400);
    }

    public void onSwitchBtnClick(View view){
        onSwitchLight((CompoundButton)view,((CompoundButton)view).isChecked());
    }

    private void onSwitchLight(CompoundButton buttonView, boolean isChecked) {
        String R="1";
        String Param=isChecked?"0":"1";//0为开1为关
        OkHttpClient okHttpClient = HttpsUtils.getTrustClient();
        RequestBody requestBody = new FormBody.Builder()
                .add("R",R)
                .add("Param",Param)
                .build();
        final Request request = new Request.Builder()
                .url(Data.serverURL+"/api/operate")
                .addHeader("cookie", Data.Jsessionid)
                .post(requestBody)
                .build();
        Callback getStatusCallback = new Callback() {
            @Override
            public void onFailure(final Call call, IOException e) {
                //在UI线程才能更新UI
                DeviceDetailActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(DeviceDetailActivity.this, com.example.manufacturehome.R.string.net_error, Toast.LENGTH_SHORT).show();
                    }
                });
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String ResponseText = response.body().string();
                Integer responseCode = 0;
                try {
                    JSONObject object = new JSONObject(ResponseText);
                    responseCode = object.getInt("code");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Integer finalResponseCode = responseCode;
                DeviceDetailActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (finalResponseCode == 200) {
                            Log.d("res",ResponseText);
                            Toast.makeText(DeviceDetailActivity.this,com.example.manufacturehome.R.string.Success, Toast.LENGTH_SHORT).show();
                        } else if (finalResponseCode == 400){
                            Log.d("res",ResponseText);
                            Toast.makeText(DeviceDetailActivity.this, com.example.manufacturehome.R.string.Fail, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        };
        Call call = okHttpClient.newCall(request);
        call.enqueue(getStatusCallback);
    }

    public void onBrightnessClick(View view) {
        String Param="";
        if(view.getId()==R.id.bright_up){
            Param="0";//+20
        }
        else if(view.getId()==R.id.bright_down){
            Param="1";//-20
        }
        String R="3";
        OkHttpClient okHttpClient = HttpsUtils.getTrustClient();
        RequestBody requestBody = new FormBody.Builder()
                .add("R",R)
                .add("Param",Param)
                .build();
        final Request request = new Request.Builder()
                .url(Data.serverURL+"/api/operate")
                .addHeader("cookie", Data.Jsessionid)
                .post(requestBody)
                .build();
        Callback getStatusCallback = new Callback() {
            @Override
            public void onFailure(final Call call, IOException e) {
                //在UI线程才能更新UI
                DeviceDetailActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(DeviceDetailActivity.this, com.example.manufacturehome.R.string.net_error, Toast.LENGTH_SHORT).show();
                    }
                });
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String ResponseText = response.body().string();
                Integer responseCode = 0;
                try {
                    JSONObject object = new JSONObject(ResponseText);
                    responseCode = object.getInt("code");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Integer finalResponseCode = responseCode;
                DeviceDetailActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (finalResponseCode == 200) {
                            Log.d("res",ResponseText);
                            Toast.makeText(DeviceDetailActivity.this,com.example.manufacturehome.R.string.Success, Toast.LENGTH_SHORT).show();
                        } else if (finalResponseCode == 400){
                            Log.d("res",ResponseText);
                            Toast.makeText(DeviceDetailActivity.this, com.example.manufacturehome.R.string.Fail, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        };
        Call call = okHttpClient.newCall(request);
        call.enqueue(getStatusCallback);
    }

    public void updateUIFromData(){
        binding.switchLight.setChecked(Data.Light_Status);
        if(!Data.Light_Status){
            binding.lightFill.setVisibility(View.INVISIBLE);
            binding.brightnessText.setText("0%");
            binding.brightUp.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(DeviceDetailActivity.this,R.color.grey)));
            binding.brightDown.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(DeviceDetailActivity.this,R.color.grey)));
            binding.lightOutline.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(DeviceDetailActivity.this,R.color.grey)));
            binding.switchLight.setThumbTintList(ColorStateList.valueOf(ContextCompat.getColor(DeviceDetailActivity.this,R.color.grey)));
            binding.switchLight.setTrackTintList(ColorStateList.valueOf(ContextCompat.getColor(DeviceDetailActivity.this,R.color.grey)));
            return;
        }
        binding.lightFill.setVisibility(View.VISIBLE);
        binding.lightFill.setAlpha(Data.Light_Brightness);
        int i= (int) (Data.Light_Brightness*100);
        String p=String.valueOf(i);
        binding.brightnessText.setText(p+"%");
        int current_color = R.color.grey;
        switch (Data.Light_Color){
            case "red":
                current_color=R.color.red;
                break;
            case "yellow":
                current_color=R.color.yellow;
                break;
            case "green":
                current_color=R.color.green;
                break;
        }
        binding.brightUp.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(DeviceDetailActivity.this,current_color)));
        binding.brightDown.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(DeviceDetailActivity.this,current_color)));
        binding.lightOutline.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(DeviceDetailActivity.this,current_color)));
        binding.lightFill.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(DeviceDetailActivity.this,current_color)));
        binding.switchLight.setThumbTintList(ColorStateList.valueOf(ContextCompat.getColor(DeviceDetailActivity.this,current_color)));
        binding.switchLight.setTrackTintList(ColorStateList.valueOf(ContextCompat.getColor(DeviceDetailActivity.this,current_color)));
        return;
    }

    public void updateData(){
        OkHttpClient okHttpClient = HttpsUtils.getTrustClient();
        RequestBody requestBody = new FormBody.Builder()
                .build();
        final Request request = new Request.Builder()
                .url(Data.serverURL+"/api/getstatus")
                .addHeader("cookie", Data.Jsessionid)
                .post(requestBody)
                .build();
        Callback getStatusCallback = new Callback() {
            @Override
            public void onFailure(final Call call, IOException e) {
                //在UI线程才能更新UI
                DeviceDetailActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(DeviceDetailActivity.this, R.string.net_error, Toast.LENGTH_SHORT).show();
                    }
                });
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String ResponseText = response.body().string();
                Integer responseCodeLogin = 0;
                String res_color = "";
                String res_brightness = "";
                String res_status = "";
                try {
                    JSONObject object = new JSONObject(ResponseText);
                    responseCodeLogin = object.getInt("code");
                    res_color = object.getString("Light_Color");
                    res_brightness = object.getString("Light_Brightness");
                    res_status = object.getString("Light_Status");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if (responseCodeLogin == 200) {
                    //从object获取key_value更新Data中亮度数据
                    Data.Light_Color =res_color;
                    Data.Light_Status = res_status.equals("on");
                    Data.Light_Brightness=Float.parseFloat("0."+res_brightness);
                    //Log.d("a",Data.Light_Color);
                    //Log.d("a", String.valueOf(Data.Light_Status));
                    //Log.d("a", String.valueOf(Data.Light_Brightness));
                } else if (responseCodeLogin == 400) {
                    DeviceDetailActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                           Toast.makeText(DeviceDetailActivity.this, R.string.getstatus_fail, Toast.LENGTH_SHORT).show();
                            updateLightTimer.cancel();
                        }
                    });
                }
            }
        };
        Call call = okHttpClient.newCall(request);
        call.enqueue(getStatusCallback);
    }

    public void onColorSwitchClick(View view){
        int id=view.getId();
        String Param="4";
        if(id == R.id.to_red_btn) {
            Param = "4";
        }
        if(id == R.id.to_yellow_btn) {
            Param = "5";
        }
        if(id == R.id.to_green_btn) {
            Param = "6";
        }
        OkHttpClient okHttpClient = HttpsUtils.getTrustClient();
        RequestBody requestBody = new FormBody.Builder()
                .add("R","5")
                .add("Param", Param)
                .build();
        final Request request = new Request.Builder()
                .url(Data.serverURL+"/api/operate")
                .addHeader("cookie", Data.Jsessionid)
                .post(requestBody)
                .build();
        Callback setColorCallback = new Callback() {
            @Override
            public void onFailure(final Call call, IOException e) {
                //在UI线程才能更新UI
                DeviceDetailActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(DeviceDetailActivity.this, R.string.net_error, Toast.LENGTH_SHORT).show();
                    }
                });
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String ResponseText = response.body().string();
                Integer responseCode = 0;
                try {
                    JSONObject object = new JSONObject(ResponseText);
                    responseCode = object.getInt("code");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Integer finalResponseCode = responseCode;
                DeviceDetailActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (finalResponseCode == 200) {
                            Log.d("res",ResponseText);
                            Toast.makeText(DeviceDetailActivity.this, R.string.color_set_success, Toast.LENGTH_SHORT).show();
                            //todo
                        } else if (finalResponseCode == 400){
                            Log.d("res",ResponseText);
                            Toast.makeText(DeviceDetailActivity.this, R.string.color_set_fail, Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            }
        };
        Call call = okHttpClient.newCall(request);
        call.enqueue(setColorCallback);
    }

    public void onBackBtnClick(View view){
        Intent intent = new Intent(DeviceDetailActivity.this, MainActivity.class);
        startActivity(intent);
        updateLightTimer.cancel();
        finish();
    }
    public void onChannelSettingBtnClick(View view){
        Intent intent = new Intent(DeviceDetailActivity.this, ChannelControlActivity.class);
        startActivity(intent);
        updateLightTimer.cancel();
        finish();
    }
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(DeviceDetailActivity.this, MainActivity.class);
        startActivity(intent);
        updateLightTimer.cancel();
        finish();
    }
}