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

import com.example.manufacturehome.databinding.ActivityDeviceDetailBinding;
import com.example.manufacturehome.databinding.ActivityLocalDeviceDetailBinding;
import com.example.manufacturehome.utils.Data;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;


public class LocalDeviceDetailActivity extends AppCompatActivity {
    private ActivityLocalDeviceDetailBinding binding;
    private Timer updateLightTimer;
    private Socket socket;
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

        // 创建Socket对象
        new CreateSocketTask(dstAddress, dstPort).execute();

        updateLightTimer = new Timer();
        updateLightTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                LocalDeviceDetailActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateUIFromData();
                    }
                });
            }
        }, 0, 400);
    }

    private class CreateSocketTask extends AsyncTask<Void, Void, Socket> {
        private String dstAddress;
        private int dstPort;

        public CreateSocketTask(String address, int port) {
            dstAddress = address;
            dstPort = port;
        }

        @Override
        protected Socket doInBackground(Void... voids) {
            Socket socket = null;
            try {
                socket = new Socket(dstAddress, dstPort);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return socket;
        }

        @Override
        protected void onPostExecute(Socket result) {
            socket = result;
            // 创建Handler并传递给SocketThread
            Handler handler = new Handler(Looper.getMainLooper());
            updateData(handler);
        }
    }

    public interface Callback {
        void onFailure(String message);

        void onSuccess(String result);

        void onError(String error);
    }

    public class SocketThread implements Runnable {
        private Socket socket;
        private String R;
        private final String  Param;
        private Callback callback;
        private boolean isRunning = false;

        private BufferedReader reader;
        private BufferedWriter writer;

        public SocketThread(Socket socket, String R, String param, Callback callback) {
            this.socket = socket;
            this.callback = callback;
            this.R = R;
            this.Param = param;
            try {
                // 初始化BufferedReader和BufferedWriter对象
                this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                this.writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public boolean isRunning() {
            return isRunning;
        }

        private final Handler handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message message) {
                if (message != null && message.obj instanceof String) {
                    String response = (String) message.obj;
                    if (response.startsWith("suc")) {
                        if (response.length() >= 5) {
                            callback.onSuccess(response.substring(4));
                        } else {
                            callback.onFailure("Invalid response from server: " + response);
                        }
                    } else if (response.startsWith("err")) {
                        if (response.length() >= 5) {
                            callback.onError(response.substring(4));
                        } else {
                            callback.onFailure("Invalid response from server: " + response);
                        }
                    } else if (response.equals("lon\n")) {
                        callback.onFailure("The device is already on");
                    } else if (response.equals("lof\n")) {
                        callback.onFailure("The device is already off");
                    } else if (response.equals("ntf\n")) {
                        callback.onFailure("Unknown error occurred");
                    } else {
                        try {
                            int code = Integer.parseInt(response.trim());
                            if (code >= 0) {
                                callback.onSuccess(response);
                            } else {
                                callback.onFailure("Server returned error code: " + code);
                            }
                        } catch (NumberFormatException e) {
                            callback.onFailure("Invalid response from server: " + response);
                        }
                    }
                } else {
                    callback.onFailure("Unknown error occurred");
                }
                return true;
            }
        });

        public Socket getSocket() {
            return socket;
        }

        public boolean isConnected() {
            return socket != null && socket.isConnected();
        }

        public String receiveData() throws IOException {
            String response = null;
            if (reader != null) {
                response = reader.readLine();
            }
            return response;
        }

        public void cancel() {
            isRunning = false;
            try {
                if( socket != null ) {
                    // 关闭套接字
                    socket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            isRunning = true;
            try {
                if (!socket.isConnected()) {
                    // 如果套接字未连接，则连接服务器
                    socket.connect(new InetSocketAddress(dstAddress, dstPort), CONNECT_TIMEOUT);

                    // 创建 BufferedReader 和 BufferedWriter
                    reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
                }

                // 向服务器发送数据包,格式："R" + "Param" + "Token"
                String data = "R" + "Param" + "Token";
                writer.write(data);
                writer.newLine();
                writer.flush();

                // 接收服务器返回的数据
                String response = reader.readLine();

                if (isRunning && callback != null) {
                    Message message = new Message();
                    message.obj = response;
                    handler.sendMessage(message);
                }
            } catch (IOException e) {
                if (isRunning && callback != null) {
                    callback.onFailure(e.getMessage());
                }
            } catch (Exception e) {
                if (isRunning && callback != null) {
                    callback.onFailure(e.getMessage());
                }
            } finally {
                isRunning = false;
            }
        }
    }

    private class SendDataTask extends AsyncTask<Void, Void, String> {
        private String R;
        private String Param;
        private String token;
        private SocketThread socketThread;

        public SendDataTask(SocketThread socketThread, String R, String Param, String token) {
            this.socketThread = socketThread;
            this.R = R;
            this.Param = Param;
            this.token = token;
        }

        @Override
        protected String doInBackground(Void... voids) {
            try {
                if (!socketThread.isConnected()) {
                    // 如果套接字未连接，则连接服务器
                    socketThread.getSocket().connect(new InetSocketAddress(dstAddress, dstPort), CONNECT_TIMEOUT);

                    // 创建 BufferedReader 和 BufferedWriter
                    socketThread.reader = new BufferedReader(new InputStreamReader(socketThread.getSocket().getInputStream()));
                    socketThread.writer = new BufferedWriter(new OutputStreamWriter(socketThread.getSocket().getOutputStream()));
                }

                // 向服务器发送数据包
                String message = R + Param + token;
                socketThread.writer.write(message);
                socketThread.writer.newLine();
                socketThread.writer.flush();

                // 接收服务器返回的数据
                String response = socketThread.reader.readLine();
                return response;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String response) {
            if (socketThread.isRunning() && socketThread.callback != null) {
                if (response != null) {
                    // 将服务器返回的数据传递给回调方法
                    socketThread.callback.onSuccess(response);
                } else {
                    socketThread.callback.onFailure("Failed to send data to server");
                }
            }
        }
    }

    private void sendData(final String R, String Param, String token) {
        new SendDataTask(socketThread, R, Param, token).execute();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(socket!=null)
        {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        // 当Activity销毁时，关闭SocketThread
        if (socketThread != null) {
            socketThread.cancel();
            while (socketThread.isRunning()) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            socketThread = null;
        }
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
        sendData(R, Param, token);
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
        sendData(R, Param, token);
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


    public void updateData(Handler handler){
        // 判断SocketThread对象是否存在并且其socket对象是否已经连接，如果已经断开连接，就不需要再接收信息
        if (socketThread != null && socketThread.isConnected()) {
            return;
        }

        // 创建SocketThread并启动
        socketThread = new SocketThread(socket, "R","Param" ,  new Callback() {
            @Override
            public void onFailure(String message) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // 在UI线程中显示错误消息
                        Toast.makeText(LocalDeviceDetailActivity.this, message, Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onSuccess(String result) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // 在UI线程中显示成功消息
                        Toast.makeText(LocalDeviceDetailActivity.this, result, Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // 在UI线程中显示错误消息
                        Toast.makeText(LocalDeviceDetailActivity.this, error, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        Thread thread = new Thread(socketThread);
        thread.start();

        // 使用定时器在一定时间间隔内循环接收服务器返回的信息
        Timer timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                try {
                    // 判断SocketThread对象是否存在并且其socket对象是否已经连接，如果已经断开连接，就不需要再接收信息
                    if (socketThread == null || socketThread.getSocket() == null || socketThread.getSocket().isClosed()) {
                        timer.cancel();
                        return;
                    }

                    // 接收服务器返回的信息
                    String responseText = socketThread.receiveData();

                    // 检查 responseText 是否为空
                    if (responseText == null) {
                        return;
                    }
                    // 根据返回的信息进行处理
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
                            // 操作请求处理成功
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(LocalDeviceDetailActivity.this, "Operation request processed successfully.", Toast.LENGTH_SHORT).show();
                                }
                            });
                            break;
                        case "lon":
                            // 已经处于打开状态，无需执行该操作
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(LocalDeviceDetailActivity.this, "The light is already on.", Toast.LENGTH_SHORT).show();
                                }
                            });
                            break;
                        case "lof":
                            // 已经处于关闭状态，无需执行该操作
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(LocalDeviceDetailActivity.this, "The light is already off.", Toast.LENGTH_SHORT).show();
                                }
                            });
                            break;
                        default:
                            // 其他情况下，返回一个数字，数字表示操作请求处理的结果代码
                            final int responseCode = Integer.parseInt(responseText.trim());
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    switch (responseCode) {
                                        case 200:
                                            // 处理成功
                                            Toast.makeText(LocalDeviceDetailActivity.this, "The operation request processed successfully.", Toast.LENGTH_SHORT).show();
                                            break;
                                        case 404:
                                            // 请求的资源不存在
                                            Toast.makeText(LocalDeviceDetailActivity.this, "The requested resource does not exist.", Toast.LENGTH_SHORT).show();
                                            break;
                                        case 500:
                                            // 服务器内部错误
                                            Toast.makeText(LocalDeviceDetailActivity.this, "Internal server error.", Toast.LENGTH_SHORT).show();
                                            break;
                                        default:
                                            // 其他错误
                                            Toast.makeText(LocalDeviceDetailActivity.this, "Error occurred: " + responseCode, Toast.LENGTH_SHORT).show();
                                            break;
                                    }
                                }
                            });
                            break;
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };

        // 每400毫秒执行一次任务
        timer.schedule(timerTask, 0, 400);
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
        sendData(R, Param, token);
    }

    public void onBackBtnClick(View view){
        Intent intent = new Intent(LocalDeviceDetailActivity.this, LocalModeActivity.class);
        startActivity(intent);
        if (updateLightTimer != null) {
            updateLightTimer.cancel();
        }
        finish();
    }
//    public void onChannelSettingBtnClick(View view){
//        Intent intent = new Intent(LocalDeviceDetailActivity.this, LocalChannelControlActivity.class);
//        startActivity(intent);
//        if (updateLightTimer != null) {
//            updateLightTimer.cancel();
//        }
//        finish();
//    }
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(LocalDeviceDetailActivity.this, LocalModeActivity.class);
        startActivity(intent);
        if (updateLightTimer != null) {
            updateLightTimer.cancel();
        }
        finish();
    }
}