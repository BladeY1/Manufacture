package com.example.manufacturehome.ui.home;

import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.LocaleList;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.manufacturehome.AddDeviceActivity;
import com.example.manufacturehome.LoginActivity;
import com.example.manufacturehome.MainActivity;
import com.example.manufacturehome.R;
import com.example.manufacturehome.databinding.FragmentHomeBinding;
import com.example.manufacturehome.utils.Data;
import com.example.manufacturehome.utils.HttpsUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Locale;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        Resources resources = this.getContext().getResources();
        Configuration configuration = resources.getConfiguration();
        Locale the_locale = configuration.getLocales().get(0);
        if(Objects.equals(the_locale, new Locale("zh"))){
            binding.textClock.setFormat24Hour("M月d日，欢迎！");
        }
        else if(Objects.equals(the_locale, new Locale("en"))){
            binding.textClock.setFormat24Hour("MMdd"+", Welcome!");
        }
        if(Data.bind){
            binding.theLight.setVisibility(View.VISIBLE);
        }
        else {
            binding.theLight.setVisibility(View.GONE);
        }
        return root;
    }



    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}