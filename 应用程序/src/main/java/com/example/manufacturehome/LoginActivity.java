package com.example.manufacturehome;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.manufacturehome.databinding.ActivityLoginBinding;
import com.example.manufacturehome.utils.Data;
import com.example.manufacturehome.utils.HttpsUtils;
import com.example.manufacturehome.utils.MD5encryption;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity {
    private ActivityLoginBinding binding;
    private boolean loginMode;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        loginMode = true;
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        //设置视图绑定 不再用findViewById

    }

    public void switchRegLog(View view){
        loginMode =!loginMode;

        if(loginMode){
            binding.reglogswitch.setText(R.string.no_account_reg);
            binding.login.setVisibility(View.VISIBLE);
            binding.reg.setVisibility(View.GONE);
        }
        else{
            binding.reglogswitch.setText(R.string.have_account_log);
            binding.login.setVisibility(View.GONE);
            binding.reg.setVisibility(View.VISIBLE);
        }
        binding.loading.setVisibility(View.GONE);
    }

    public void onLoginClick (View view){
        setServerURL();
        String username = binding.username.getText().toString().trim();
        String password = binding.password.getText().toString().trim();
        if (!TextUtils.isEmpty(username) && !TextUtils.isEmpty(password)) {
            OkHttpClient okHttpClient = HttpsUtils.getTrustClient();
            RequestBody requestBody = new FormBody.Builder()
                    .add("name",username)
                    .add("password", MD5encryption.md52(password))
                    .build();
            final Request request = new Request.Builder()
                    .url(Data.serverURL+"/api/login")
                    .post(requestBody)
                    .build();

            Call call = okHttpClient.newCall(request);
            binding.login.setVisibility(View.GONE);
            binding.reg.setVisibility(View.GONE);
            binding.reglogswitch.setVisibility(View.GONE);
            binding.loading.setVisibility(View.VISIBLE);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(final Call call, IOException e) {
                    //在UI线程才能更新UI
                    LoginActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            binding.login.setVisibility(View.VISIBLE);
                            binding.reglogswitch.setVisibility(View.VISIBLE);
                            binding.loading.setVisibility(View.GONE);
                            Toast.makeText(LoginActivity.this,R.string.net_error,Toast.LENGTH_SHORT).show();
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
                        //获得SESSION
                        Headers headers = response.headers();
                        List<String> cookies = headers.values("Set-Cookie");
                        String session = cookies.get(0);
                        //在Data类中共享变量
                        Data.Jsessionid = session.substring(0, session.indexOf(";"));
                        Data.username=username;
                        LoginActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(LoginActivity.this, R.string.login_success, Toast.LENGTH_SHORT).show();
                            }});
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    }
                    else if(responseCodeLogin == 400 ) {
                        LoginActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                binding.login.setVisibility(View.VISIBLE);
                                binding.reglogswitch.setVisibility(View.VISIBLE);                    //binding.loading.setVisibility(View.GONE);
                                binding.loading.setVisibility(View.GONE);
                                Toast.makeText(LoginActivity.this, R.string.login_failed, Toast.LENGTH_SHORT).show();
                            }});
                    }
                }
            });
        }
    }

    public void onRegClick (View view){
        setServerURL();
        String username = binding.username.getText().toString().trim();
        String password = binding.password.getText().toString().trim();
        if (!TextUtils.isEmpty(username) && !TextUtils.isEmpty(password)) {
            binding.reg.setVisibility(View.GONE);
            binding.reglogswitch.setVisibility(View.GONE);
            binding.loading.setVisibility(View.VISIBLE);
            OkHttpClient okHttpClient = HttpsUtils.getTrustClient();
            RequestBody requestBody = new FormBody.Builder()
                    .add("name",username)
                    .add("password", password)
                    .build();
            final Request request = new Request.Builder()
                    .url(Data.serverURL+"/api/register")
                    .post(requestBody)
                    .build();

            Call call = okHttpClient.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(final Call call, IOException e) {
                    //在UI线程才能更新UI

                    LoginActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            binding.reg.setVisibility(View.VISIBLE);
                            binding.reglogswitch.setVisibility(View.VISIBLE);
                            binding.loading.setVisibility(View.GONE);
                            Toast.makeText(LoginActivity.this,R.string.net_error,Toast.LENGTH_SHORT).show();
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
                        LoginActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                binding.reglogswitch.setVisibility(View.VISIBLE);
                                binding.loading.setVisibility(View.GONE);
                                binding.reglogswitch.callOnClick();
                                Toast.makeText(LoginActivity.this, R.string.reg_success, Toast.LENGTH_SHORT).show();
                            }});
                    }
                    else if(responseCodeLogin == 400 ) {
                        LoginActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                binding.reg.setVisibility(View.VISIBLE);
                                binding.reglogswitch.setVisibility(View.VISIBLE);
                                binding.loading.setVisibility(View.GONE);
                                Toast.makeText(LoginActivity.this, R.string.reg_failed, Toast.LENGTH_SHORT).show();
                            }});
                    }
                }
            });
        }
    }

    public void setServerURL(){
        if(binding.servreurl.getVisibility()==View.VISIBLE){
            Data.serverURL= binding.servreurl.getText().toString().trim();
        }
    }


    public void onLocalModeClick(View view) {
        Intent intent = new Intent(LoginActivity.this, LocalModeActivity.class);
        startActivity(intent);
        finish();
    }
}