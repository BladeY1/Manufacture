
package com.example.manufacturehome;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.manufacturehome.databinding.ActivityLocalDeviceDetailBinding;
import com.example.manufacturehome.utils.Data;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;


public class LocalDeviceDetailActivity extends AppCompatActivity {
    private ActivityLocalDeviceDetailBinding binding;
    private SocketThread socketThread;

    static final int CONNECT_TIMEOUT = 5000; // 5秒钟超时

    private Message message;
    private Intent intent;
    private String socketInfo = null;
    private String dstAddress;
    private int dstPort;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getSupportActionBar() != null){
            getSupportActionBar().hide();
        }

        // 在目标Activity中获取传递过来的Intent
        intent = getIntent();

        // 从Intent中提取socketInfo字符串
        if(intent != null && intent.hasExtra("socketInfo")){
            socketInfo = intent.getStringExtra("socketInfo");
        }

        if(socketInfo == null){
            Toast.makeText(this, "Error: socket info is null", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 使用split()方法分离地址和端口号
        String[] parts = socketInfo.split(":");
        dstAddress = parts[0];
        dstPort = Integer.parseInt(parts[1]);

        binding = ActivityLocalDeviceDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Connect to the server
        socketThread = new SocketThread(dstAddress, dstPort);
        socketThread.start();

    }

    private class SocketThread extends Thread {

        private Socket socket;
        private BufferedReader input;
        private PrintWriter output;
        private String serverIP;
        private int serverPort;

        SocketThread(String serverIP, int serverPort) {
            this.serverIP = serverIP;
            this.serverPort = serverPort;
        }

        @Override
        public void run() {
            try {
                socket = new Socket(serverIP, serverPort);
                input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                output = new PrintWriter(socket.getOutputStream(), true);

//                StringBuilder requestData = new StringBuilder();
//                requestData.append("R");
//                requestData.append("Param");
//                requestData.append("token");
//
//                String requestDataString = requestData.toString();
//                socketThread.output.println(requestDataString);
                while (!socket.isClosed()) {
                    receiveData();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            updateUIFromData();
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void sendData(final String R, String Param, String token) {
        try {
            StringBuilder requestData = new StringBuilder();
            requestData.append(R);
            requestData.append(Param);
            requestData.append(token);

            String requestDataString = requestData.toString();
            socketThread.output.println(requestDataString);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendDataAsync(final String R, final String Param, final String token) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                sendData(R, Param, token);
            }
        }).start();
    }

    private void receiveData() {
        try {
            String responseText = socketThread.input.readLine();
            String res_brightness = "60";

            if (responseText == null) {
                return;
            }

            switch (responseText.trim()) {
                case "ntf":
                    // 未知错误
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(LocalDeviceDetailActivity.this, "Unknown error.", Toast.LENGTH_SHORT).show();
                        }
                    });
                    break;
                case "err":
                    // 操作请求处理出错
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(LocalDeviceDetailActivity.this, "Error processing the operation request.", Toast.LENGTH_SHORT).show();
                        }
                    });
                    break;
                case "suc":
                    // 操作请求处理成功,再发一次读操作更新Data
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(LocalDeviceDetailActivity.this, "Operation request processed successfully.", Toast.LENGTH_SHORT).show();
                        }
                    });
                    System.out.printf("接收：%s%n",responseText);
                    String R="4";
                    String Param="0";//0为开
                    String token = "LocalModeChannel_2023_03_17_0000";
                    sendDataAsync(R, Param, token);
                    receiveData();
                    break;
                case "lon":
                    // 处于打开状态
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(LocalDeviceDetailActivity.this, "The light is on.", Toast.LENGTH_SHORT).show();
                        }
                    });
                    Data.Light_Color = "red";
                    Data.Light_Status = true;
                    Data.Light_Brightness = Float.parseFloat("0." + res_brightness);
                    break;
                case "lof":
                    // 处于关闭状态
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(LocalDeviceDetailActivity.this, "The light is off.", Toast.LENGTH_SHORT).show();
                        }
                    });
                    Data.Light_Color = "grey";
                    Data.Light_Status = false;
                    Data.Light_Brightness = 0f;
                    break;
                case "lrd":
                    Data.Light_Color = "red";
                    Data.Light_Status = true;
                    Data.Light_Brightness = Float.parseFloat("0." + res_brightness);
                    break;
                case "lyl":
                    Data.Light_Color = "yellow";
                    Data.Light_Status = true;
                    Data.Light_Brightness = Float.parseFloat("0." + res_brightness);
                    break;
                case "lgn":
                    Data.Light_Color = "green";
                    Data.Light_Status = true;
                    Data.Light_Brightness = Float.parseFloat("0." + res_brightness);
                    break;
                default:
                    int responseCode = Integer.parseInt(responseText.trim());
                    handleResponseCode(responseCode);
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleResponseCode(final int responseCode) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                switch (responseCode) {
                    case 200:
                        // Success
                        Toast.makeText(LocalDeviceDetailActivity.this, "The operation request processed successfully.", Toast.LENGTH_SHORT).show();
                        break;
                    case 404:
                        // Requested resource does not exist
                        Toast.makeText(LocalDeviceDetailActivity.this, "The requested resource does not exist.", Toast.LENGTH_SHORT).show();
                        break;
                    case 500:
                        // Internal server error
                        Toast.makeText(LocalDeviceDetailActivity.this, "Internal server error.", Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        // Other errors
                        Toast.makeText(LocalDeviceDetailActivity.this, "Error occurred: " + responseCode, Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if (socketThread.socket != null) {
                socketThread.socket.close();
            }
            if (socketThread.input != null) {
                socketThread.input.close();
            }
            if (socketThread.output != null) {
                socketThread.output.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    public void updateUIFromData(){
        binding.switchLight.setChecked(Data.Light_Status);
        if(!Data.Light_Status){
            binding.lightFill.setVisibility(View.INVISIBLE);
            binding.brightnessText.setText("0%");
            binding.brightUp.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(LocalDeviceDetailActivity.this,R.color.grey)));
            binding.brightDown.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(LocalDeviceDetailActivity.this,R.color.grey)));
            binding.lightOutline.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(LocalDeviceDetailActivity.this,R.color.grey)));
            binding.switchLight.setThumbTintList(ColorStateList.valueOf(ContextCompat.getColor(LocalDeviceDetailActivity.this,R.color.grey)));
            binding.switchLight.setTrackTintList(ColorStateList.valueOf(ContextCompat.getColor(LocalDeviceDetailActivity.this,R.color.grey)));
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
        binding.brightUp.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(LocalDeviceDetailActivity.this,current_color)));
        binding.brightDown.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(LocalDeviceDetailActivity.this,current_color)));
        binding.lightOutline.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(LocalDeviceDetailActivity.this,current_color)));
        binding.lightFill.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(LocalDeviceDetailActivity.this,current_color)));
        binding.switchLight.setThumbTintList(ColorStateList.valueOf(ContextCompat.getColor(LocalDeviceDetailActivity.this,current_color)));
        binding.switchLight.setTrackTintList(ColorStateList.valueOf(ContextCompat.getColor(LocalDeviceDetailActivity.this,current_color)));
        return;
    }


    public void onSwitchBtnClick(View view){
        onSwitchLight((CompoundButton)view,((CompoundButton)view).isChecked());
    }

    private void onSwitchLight(CompoundButton buttonView, boolean isChecked) {
        String R="1";
        String Param=isChecked?"0":"1";//0为开1为关

        //这个token包含一个描述性前缀“LocalModeChannel”来表示本地模式通道，接着是当前日期（年、月、日）
        //最后是4位数字（可以将其视为一个版本号或其他标识符, 未来使用）。
        String token = "LocalModeChannel_2023_03_17_0000";
        //sendData(R, Param, token);
        sendDataAsync(R, Param, token);
    }

    public void onBrightnessClick(View view) {
        final String Param;
        if (view.getId() == R.id.bright_up) {
            Param = "0"; //+20
        } else if (view.getId() == R.id.bright_down) {
            Param = "1"; //-20
        } else {
            return; // handle invalid view id
        }
        String R="3";
        String token = "LocalModeChannel_2023_03_17_0000";
        //sendData(R, Param, token);
        sendDataAsync(R, Param, token);
    }

    public void onColorSwitchClick(View view){
        int id=view.getId();
        final String Param;
        if(id == R.id.to_red_btn) {
            Param = "4";
        }
        else if(id == R.id.to_yellow_btn) {
            Param = "5";
        }
        else if(id == R.id.to_green_btn) {
            Param = "6";
        }
        else{
            Param = "4";
        }

        String R="5";
        String token = "LocalModeChannel_2023_03_17_0000";
        //sendData(R, Param, token);
        sendDataAsync(R, Param, token);
    }

    public void onBackBtnClick(View view){
        Intent intent = new Intent(LocalDeviceDetailActivity.this, LocalModeActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(LocalDeviceDetailActivity.this, LocalModeActivity.class);
        startActivity(intent);
        finish();
    }
}
