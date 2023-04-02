package com.example.manufacturehome;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.example.manufacturehome.databinding.ActivityChannelControlBinding;
import com.example.manufacturehome.utils.Data;
import com.example.manufacturehome.utils.HttpsUtils;
import com.example.manufacturehome.utils.Message;
import com.example.manufacturehome.utils.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ChannelControlActivity extends AppCompatActivity {
    private ActivityChannelControlBinding binding;
    private Timer updateChannelTimer;
    private List<String> userNameList = new ArrayList<String>();
    private List<User> userList = new ArrayList<>();
    private String user_selected;
    // 然后的话创建一个我们的一个数组适配器并且的话这个数组适配器使我们的字符串类型的
    ArrayAdapter<String> adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("设备管理");
        actionBar.setDisplayHomeAsUpEnabled(true);
        binding = ActivityChannelControlBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        userNameList.add("等待数据更新");
        adapter = new ArrayAdapter<String>
                (this,android.R.layout.simple_spinner_item,userNameList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.userSpinner.setAdapter(adapter);
        setUserSelected();
        //更新channel状态
        updateChannelTimer = new Timer();
        updateChannelTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                ChannelControlActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateData();
                        updateUIFromData();
                    }
                });
            }
        }, 0, 500);

        //获取用户及对应token
        updateUser();
    }


    public void onDelTokenClick(View view){
        int id = view.getId();
        String R_toDel="-1";
        String user_toDel=user_selected;
        if(id==R.id.R0){R_toDel="0";}
        if(id==R.id.R1){R_toDel="1";}
        if(id==R.id.R2){R_toDel="2";}
        if(id==R.id.R3){R_toDel="3";}
        if(id==R.id.R4){R_toDel="4";}
        if(id==R.id.R5){R_toDel="5";}
        OkHttpClient okHttpClient = HttpsUtils.getTrustClient();
        RequestBody requestBody = new FormBody.Builder()
                .add("name",user_toDel)
                .add("R",R_toDel)
                .build();
        final Request request = new Request.Builder()
                .url(Data.serverURL+"/api/delToken")
                .addHeader("cookie", Data.Jsessionid)
                .post(requestBody)
                .build();
        Callback getStatusCallback = new Callback() {
            @Override
            public void onFailure(final Call call, IOException e) {
                //在UI线程才能更新UI
                ChannelControlActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(ChannelControlActivity.this, R.string.net_error, Toast.LENGTH_SHORT).show();
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
                    ChannelControlActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(ChannelControlActivity.this, R.string.Success, Toast.LENGTH_SHORT).show();
                            view.setVisibility(View.GONE);
                        }
                    });
                } else if (responseCodeLogin == 400) {
                    ChannelControlActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(ChannelControlActivity.this, R.string.Fail, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        };
        Call call = okHttpClient.newCall(request);
        call.enqueue(getStatusCallback);
    }

    public void setUserSelected(){
        binding.userSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                String user_name = userNameList.get(position);
                for(User i:userList){
                    if(i.name.equals(user_name)){
                        user_selected=i.name;
                        binding.R0.setVisibility(i.R0?View.VISIBLE:View.GONE);
                        binding.R1.setVisibility(i.R1?View.VISIBLE:View.GONE);
                        binding.R2.setVisibility(i.R2?View.VISIBLE:View.GONE);
                        binding.R3.setVisibility(i.R3?View.VISIBLE:View.GONE);
                        binding.R4.setVisibility(i.R4?View.VISIBLE:View.GONE);
                        binding.R5.setVisibility(i.R5?View.VISIBLE:View.GONE);
                        break;
                    }
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                binding.R0.setVisibility(View.GONE);
                binding.R1.setVisibility(View.GONE);
                binding.R2.setVisibility(View.GONE);
                binding.R3.setVisibility(View.GONE);
                binding.R4.setVisibility(View.GONE);
                binding.R5.setVisibility(View.GONE);
            }
        });
    }

    private void updateUser() {
        OkHttpClient okHttpClient = HttpsUtils.getTrustClient();
        Request request = new Request.Builder()
                .addHeader("cookie", Data.Jsessionid)
                .url(Data.serverURL+"/api/getDeviceUser")
                .build();
        Call call = okHttpClient.newCall(request);
        Callback callback = new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                ChannelControlActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(ChannelControlActivity.this, R.string.net_error, Toast.LENGTH_SHORT).show();
                    }
                });
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String ResponseText = response.body().string();
                Integer responseCode = 0;
                String responseMsgMessage = "NUll";
                JSONArray jsonArray = null;
                try {
                    JSONObject object = new JSONObject(ResponseText);
                    responseCode = object.getInt("code");
                    if (responseCode == 400) {
                        responseMsgMessage = object.getString("msg");
                    } else {
                        jsonArray = object.getJSONArray("msg");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if (responseCode == 200) {
                    userNameList.clear();
                    userNameList.add("请选择用户");
                    userList.clear();
                    userList.add(new User("请选择用户","0","0","0","0","0","0"));
                    try {
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject object = jsonArray.getJSONObject(i);
                            Log.d("JSON", object.toString());
                            User user = new User(object.getString("name"),
                                    object.getString("R0"),
                                    object.getString("R1"),
                                    object.getString("R2"),
                                    object.getString("R3"),
                                    object.getString("R4"),
                                    object.getString("R5")
                            );
                            userList.add(user);
                            Log.d("JSON", user.toString());
                            userNameList.add(user.name);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.d("JSON", e.toString());

                    }

                    ChannelControlActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            adapter.notifyDataSetChanged();
                            //Toast.makeText(ChannelControlActivity.this, R.string.Success, Toast.LENGTH_SHORT).show();
                        }
                    });
                } else if (responseCode == 400) {
                    final String MSG = responseMsgMessage;
                    ChannelControlActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(ChannelControlActivity.this, R.string.Fail + MSG, Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        };
        call.enqueue(callback);
    }

    private void updateUIFromData() {
        binding.switchMqtt.setChecked(Data.channel_mqtt);
        binding.switchZigbee.setChecked(Data.channel_zigbee);
        binding.switchSpeaker.setChecked(Data.channel_speaker);
        setTextStatus(binding.mqttText,Data.channel_mqtt);
        setTextStatus(binding.zigbeeText,Data.channel_zigbee);
        setTextStatus(binding.speakerText,Data.channel_speaker);
    }

    public void setTextStatus(TextView view, boolean status){
        Drawable drawableLeft_on = getResources().getDrawable((android.R.drawable.presence_online));
        Drawable drawableLeft_off = getResources().getDrawable((android.R.drawable.presence_invisible));
        view.setCompoundDrawablePadding(4);
        if(status){
            view.setCompoundDrawablesWithIntrinsicBounds(drawableLeft_on,
                    null, null, null);
        }
        else{
            view.setCompoundDrawablesWithIntrinsicBounds(drawableLeft_off,
                    null, null, null);
        }
    }

    private void updateData() {
        OkHttpClient okHttpClient = HttpsUtils.getTrustClient();
        RequestBody requestBody = new FormBody.Builder()
                .add("operate","get")
                .build();
        final Request request = new Request.Builder()
                .url(Data.serverURL+"/api/channel")
                .addHeader("cookie", Data.Jsessionid)
                .post(requestBody)
                .build();
        Callback getStatusCallback = new Callback() {
            @Override
            public void onFailure(final Call call, IOException e) {
                //在UI线程才能更新UI
                ChannelControlActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(ChannelControlActivity.this, R.string.net_error, Toast.LENGTH_SHORT).show();
                    }
                });
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String ResponseText = response.body().string();
                Integer responseCodeLogin = 0;
                String msg="";
                int mqtt=1;
                int zigbee=1;
                int speaker=1;
                int local=1;
                try {
                    JSONObject object = new JSONObject(ResponseText);
                    responseCodeLogin = object.getInt("code");
                    msg = object.getString("msg");
                    try {
                        JSONObject msgObject = new JSONObject(msg);
                        mqtt = msgObject.getInt("mqtt");
                        zigbee = msgObject.getInt("zigbee");
                        speaker = msgObject.getInt("speaker");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (responseCodeLogin == 200) {
                Data.channel_mqtt = (mqtt==1);
                Data.channel_zigbee = (zigbee==1);
                Data.channel_speaker = (speaker==1);
                Data.channel_local = (local==1);
                }
            }
        };
        Call call = okHttpClient.newCall(request);
        call.enqueue(getStatusCallback);
    }

    public void onSwitchBtnClick(View view){
        SwitchCompat switcher= (SwitchCompat) view;
        onSwitchChannel(view,switcher.isChecked());
    }
    public void onSwitchChannel(View view, boolean isChecked){
        String operate="set";
        String target="";
        String param="";
        param = isChecked?"on":"off";
        if(view.getId()==R.id.switch_mqtt){
            target="mqtt";
        }
        else if(view.getId()==R.id.switch_zigbee){
            target="zigbee";
        }
        else if(view.getId()==R.id.switch_speaker){
            target="speaker";
        }
        OkHttpClient okHttpClient = HttpsUtils.getTrustClient();
        RequestBody requestBody = new FormBody.Builder()
                .add("operate",operate)
                .add("target",target)
                .add("param",param)
                .build();
        final Request request = new Request.Builder()
                .url(Data.serverURL+"/api/channel")
                .addHeader("cookie", Data.Jsessionid)
                .post(requestBody)
                .build();
        Callback getStatusCallback = new Callback() {
            @Override
            public void onFailure(final Call call, IOException e) {
                //在UI线程才能更新UI
                ChannelControlActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(ChannelControlActivity.this, R.string.net_error, Toast.LENGTH_SHORT).show();
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
                    ChannelControlActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(ChannelControlActivity.this, R.string.Success, Toast.LENGTH_SHORT).show();
                        }
                    });
                } else if (responseCodeLogin == 400) {
                    ChannelControlActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(ChannelControlActivity.this, R.string.Fail, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        };
        Call call = okHttpClient.newCall(request);
        call.enqueue(getStatusCallback);
    }



    public void onLogBtnClick(View view){
        Intent intent = new Intent(ChannelControlActivity.this, DeviceLogActivity.class);
        startActivity(intent);
        updateChannelTimer.cancel();
        finish();
    }
    //toolbar返回键
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            Intent intent = new Intent(ChannelControlActivity.this, MainActivity.class);
            startActivity(intent);
            updateChannelTimer.cancel();
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(ChannelControlActivity.this, DeviceDetailActivity.class);
        startActivity(intent);
        updateChannelTimer.cancel();
        finish();
    }
}