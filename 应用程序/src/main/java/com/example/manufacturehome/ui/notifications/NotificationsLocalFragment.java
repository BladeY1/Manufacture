package com.example.manufacturehome.ui.notifications;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.manufacturehome.LocalModeActivity;

import com.example.manufacturehome.databinding.FragmentLocalNotificationsBinding;
import com.example.manufacturehome.BaseApplication;
import com.example.manufacturehome.R;
import com.example.manufacturehome.utils.Data;
import com.example.manufacturehome.utils.HttpsUtils;
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
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class NotificationsLocalFragment extends Fragment {
    private OkHttpClient okHttpClient;
    private FragmentLocalNotificationsBinding binding;
    MessageAdapter adapter;

    private List<Message> MessagesList = new ArrayList<>();

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        okHttpClient= HttpsUtils.getTrustClient();
        binding = FragmentLocalNotificationsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        adapter = new MessageAdapter(BaseApplication.getAppContext(), R.layout.message_item, MessagesList);
        binding.messagesListView.setAdapter(adapter);
        binding.swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Request request = new Request.Builder()
                        .addHeader("cookie", Data.Jsessionid)
                        .url(Data.serverURL+"/api/getMessages")
                        .build();
                Call call = okHttpClient.newCall(request);
                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                binding.swipeLayout.setRefreshing(false);
                                Log.d("TSS", e.toString());
                                Toast.makeText(getActivity().getApplicationContext(), "Network error", Toast.LENGTH_SHORT).show();
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
                            MessagesList.clear();
                            binding.swipeLayout.setRefreshing(false);
                            try {
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject object = jsonArray.getJSONObject(i);
                                    Log.i("JSON", object.toString());
                                    Log.i("JSON", "id=" + object.getInt("id"));
                                    Log.i("JSON", "name=" + object.getString("name"));
                                    Log.i("JSON", "apply_id=" + object.getString("apply_id"));
                                    Log.i("JSON", "----------------");
                                    Message message = new Message(object.getString("name"), object.getString("status"), object.getString("R") , object.getInt("apply_id"), object.getInt("id"));
                                    MessagesList.add(message);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Log.d("TSS", "Success");
                                    adapter.notifyDataSetChanged();
                                    Toast.makeText(getActivity().getApplicationContext(), "Success", Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else if (responseCode == 400) {
                            final String MSG = responseMsgMessage;
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Log.d("TSS", "Failed");
                                    Toast.makeText(getActivity().getApplicationContext(), "Error: " + MSG, Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    }
                });

            }
        });
        //进入页面刷新
        refreshList();
        //设置消息点击事件
        setMessageItemOnClick();
        //结束
        return root;
    }

    private void setMessageItemOnClick() {
        binding.messagesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Message message = MessagesList.get(position);
                AlertDialog.Builder builder = new AlertDialog.Builder(LocalModeActivity.getContext());
                builder.setTitle(R.string.apply_process_title)
                        .setMessage(R.string.apply_process_message)//设置显示的内容
                        .setPositiveButton(R.string.accept, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                processMessage(message,true);
                            }
                        })
                        .setNegativeButton(R.string.reject, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                processMessage(message,false);
                            };
                        });
                builder.show();
            }
        });
    }

    public void processMessage(Message message,boolean accept){
        String operate=accept?"accept":"reject";
        String apply_id = String.valueOf(message.getApply_id());
        RequestBody requestBody = new FormBody.Builder()
                .add("apply_id", apply_id)
                .add("operate", operate)
                .build();
        Request request_process = new Request.Builder()
                .addHeader("cookie",Data.Jsessionid)
                .post(requestBody)
                .url(Data.serverURL+"/api/processMessages")
                .build();
        Call call_process = okHttpClient.newCall(request_process);
        Callback callback = new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getActivity().getApplicationContext(), R.string.net_error, Toast.LENGTH_SHORT).show();
                    }
                });
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String ResponseText = response.body().string();
                Integer responseCode = 0;
                String response_msg_processmessage = "NUll";
                try {
                    JSONObject object = new JSONObject(ResponseText);
                    responseCode = object.getInt("code");
                    response_msg_processmessage = object.getString("msg");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (responseCode == 200) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getActivity().getApplicationContext(), R.string.Success, Toast.LENGTH_SHORT).show();
                            refreshList();
                        }
                    });
                } else if (responseCode == 400) {
                    final String MSG = response_msg_processmessage;
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.d("TSS", "Failed");
                            Toast.makeText(getActivity().getApplicationContext(), "Error: " + MSG, Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        };
        call_process.enqueue(callback);
    }

    public void refreshList(){
        binding.swipeLayout.post(new Runnable() {
            @Override
            public void run() {
                binding.swipeLayout.setRefreshing(true);
            }
        });
        Request request = new Request.Builder()
                .addHeader("cookie", Data.Jsessionid)
                .url(Data.serverURL+"/api/getMessages")
                .build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        binding.swipeLayout.setRefreshing(false);
                        Log.d("TSS", e.toString());
                        Toast.makeText(getActivity().getApplicationContext(), R.string.net_error, Toast.LENGTH_SHORT).show();
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
                    MessagesList.clear();
                    binding.swipeLayout.setRefreshing(false);
                    try {
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject object = jsonArray.getJSONObject(i);
                            Log.i("JSON", object.toString());
                            Message message = new Message(object.getString("name"), object.getString("status"), object.getString("R") , object.getInt("apply_id"), object.getInt("id"));
                            MessagesList.add(message);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.d("TSS", "Success");
                            adapter.notifyDataSetChanged();
                            Toast.makeText(getActivity().getApplicationContext(), R.string.Success, Toast.LENGTH_SHORT).show();
                        }
                    });
                } else if (responseCode == 400) {
                    final String MSG = responseMsgMessage;
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.d("TSS", "Failed");
                            Toast.makeText(getActivity().getApplicationContext(), R.string.Fail + MSG, Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        });
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}