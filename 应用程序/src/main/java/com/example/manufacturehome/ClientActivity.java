package com.example.manufacturehome;

import static com.example.manufacturehome.LocalDeviceDetailActivity.CONNECT_TIMEOUT;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.manufacturehome.databinding.ActivityClientBinding;
import com.example.manufacturehome.databinding.ActivityMainBinding;
import com.example.manufacturehome.utils.Data;
import com.example.manufacturehome.utils.HttpsUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.InetSocketAddress;
import java.net.Socket;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ClientActivity extends AppCompatActivity {
    private static ActivityClientBinding binding;

    private static Context context;
    ClientHandler clientHandler;
    private static String socketInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context=ClientActivity.this;
        if (getSupportActionBar() != null){
            getSupportActionBar().hide();
        }
        binding = ActivityClientBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.disconnect.setEnabled(false);
        binding.send.setEnabled(false);


        binding.connect.setOnClickListener(buttonConnectOnClickListener);
        binding.disconnect.setOnClickListener(buttonDisConnectOnClickListener);
        binding.send.setOnClickListener(buttonSendOnClickListener);

        clientHandler = new ClientHandler(this);
    }

    public static Context getContext(){
        return context;
    }

    static class ConnectTask extends AsyncTask<Void, Void, Boolean> {
        private final String ipAddress;
        private final int port;
        private final WeakReference<ClientActivity> activityRef;

        public ConnectTask(String ipAddress, int port, ClientActivity activity) {
            this.ipAddress = ipAddress;
            this.port = port;
            this.activityRef = new WeakReference<>(activity);
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            boolean isConnected = false;
            // Connect to server
            try {
                // Create a new socket
                Socket socket = new Socket(ipAddress, port);

                // Set the isConnected flag to true if the socket connection is successful
                isConnected = socket.isConnected();

                // Close the socket connection
                if( socket != null ) {
                    // 关闭套接字
                    socket.close();
                }
            } catch (IOException e) {
                // Handle the IOException
            }
            //...
            return isConnected;
        }

        @Override
        protected void onPostExecute(Boolean isConnected) {
            ClientActivity activity = activityRef.get();
            if (activity == null || activity.isFinishing()) {
                return;
            }

            if (isConnected) {
                socketInfo = ipAddress + ":" + port;
                Intent newIntent = new Intent(context, LocalDeviceDetailActivity.class);
                newIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                newIntent.putExtra("socketInfo", socketInfo);
                context.startActivity(newIntent);
                binding.connect.setEnabled(false);
                binding.disconnect.setEnabled(true);
                binding.send.setEnabled(true);
            } else {
                Toast.makeText(activity, R.string.connection_fail,Toast.LENGTH_SHORT).show();
            }
        }
    }

    View.OnClickListener buttonConnectOnClickListener =
            new View.OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    String ipAddress = binding.address.getText().toString();
                    int portNumber = Integer.parseInt(binding.port.getText().toString());

                    // Start a new instance of ConnectTask AsyncTask
                    new ConnectTask(ipAddress, portNumber, ClientActivity.this).execute();

                }
            };

    View.OnClickListener buttonDisConnectOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            clientEnd();
            binding.connect.setEnabled(true);
            binding.disconnect.setEnabled(false);
            binding.send.setEnabled(false);
        }
    };

    View.OnClickListener buttonSendOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
        }
    };



    private void updateState(String state){
        binding.state.setText(state);
    }

    @SuppressLint("SetTextI18n")
    private void updateRxMsg(String rxmsg){
        binding.receivedMsg.setText(rxmsg + "\n");
    }

    @SuppressLint("SetTextI18n")
    private void clientEnd(){
        binding.state.setText("clientEnd()");
        binding.connect.setEnabled(true);
        binding.disconnect.setEnabled(false);
        binding.send.setEnabled(false);
    }

    public static class ClientHandler extends Handler {
        public static final int UPDATE_STATE = 0;
        public static final int UPDATE_MSG = 1;
        public static final int UPDATE_END = 2;
        /* 修改：加final */
        //private ClientActivity parent;
        private final ClientActivity parent;

        public ClientHandler(ClientActivity parent) {
            super();
            this.parent = parent;
        }

        @Override
        public void handleMessage(Message msg) {

            switch (msg.what){
                case UPDATE_STATE:
                    parent.updateState((String)msg.obj);
                    break;
                case UPDATE_MSG:
                    parent.updateRxMsg((String)msg.obj);
                    break;
                case UPDATE_END:
                    parent.clientEnd();
                    break;
                default:
                    super.handleMessage(msg);
            }

        }

    }



    //toolbar返回键
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            Intent intent = new Intent(ClientActivity.this, LocalModeActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(ClientActivity.this, LocalModeActivity.class);
        startActivity(intent);
        finish();
    }
}