package com.example.manufacturehome;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import com.example.manufacturehome.databinding.ActivityDeviceLogBinding;
import com.example.manufacturehome.utils.Data;
import com.example.manufacturehome.utils.HttpsUtils;
import com.example.manufacturehome.utils.Log;
import com.example.manufacturehome.utils.LogAdapter;
import com.example.manufacturehome.utils.Message;
import com.example.manufacturehome.utils.MessageAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DeviceLogActivity extends AppCompatActivity {
    private ActivityDeviceLogBinding binding;
    private OkHttpClient okHttpClient;
    private List<Log> LogList = new ArrayList<>();
    LogAdapter adapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        okHttpClient = HttpsUtils.getTrustClient();
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("设备日志");
        actionBar.setDisplayHomeAsUpEnabled(true);
        binding = ActivityDeviceLogBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        adapter = new LogAdapter(this, R.layout.message_item, LogList);
        binding.logs.setAdapter(adapter);
        binding.logSwipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshLogs();
            }
        });
        refreshLogs();
    }


    public void refreshLogs(){
        Request request = new Request.Builder()
                .addHeader("cookie", Data.Jsessionid)
                .url(Data.serverURL+"/api/getLogs")
                .build();
        Call call = okHttpClient.newCall(request);
        Callback callback = new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                DeviceLogActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        binding.logSwipeLayout.setRefreshing(false);
                        Toast.makeText(DeviceLogActivity.this, R.string.net_error, Toast.LENGTH_SHORT).show();
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
                    LogList.clear();
                    binding.logSwipeLayout.setRefreshing(false);
                    try {
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject object = jsonArray.getJSONObject(i);
                            android.util.Log.i("JSON", object.toString());
                            Log log = new Log(object.getString("time"), object.getString("type"), object.getString("name"), object.getString("channel"), object.getString("Param"),
                                    object.getString("R"), object.getString("status"),
                                    object.getString("topic"), object.getString("msg"));
                            LogList.add(log);
                            android.util.Log.d("log", String.valueOf(log));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    DeviceLogActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            adapter.notifyDataSetChanged();
                            //Toast.makeText(DeviceLogActivity.this, R.string.Success, Toast.LENGTH_SHORT).show();
                        }
                    });
                } else if (responseCode == 400) {
                    final String MSG = responseMsgMessage;
                    DeviceLogActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(DeviceLogActivity.this, R.string.Fail + MSG, Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        };
        call.enqueue(callback);
    }

    //toolbar返回键
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            Intent intent = new Intent(DeviceLogActivity.this, ChannelControlActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(DeviceLogActivity.this, ChannelControlActivity.class);
        startActivity(intent);
        finish();
    }
}