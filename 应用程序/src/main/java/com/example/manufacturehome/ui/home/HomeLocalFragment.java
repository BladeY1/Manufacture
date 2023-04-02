package com.example.manufacturehome.ui.home;

import android.app.Activity;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.manufacturehome.databinding.FragmentLocalHomeBinding;
import com.example.manufacturehome.utils.Data;

import java.util.Locale;
import java.util.Objects;

public class HomeLocalFragment extends Fragment {

    private FragmentLocalHomeBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentLocalHomeBinding.inflate(inflater, container, false);
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
