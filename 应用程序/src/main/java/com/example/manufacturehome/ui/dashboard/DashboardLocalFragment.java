package com.example.manufacturehome.ui.dashboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.manufacturehome.databinding.FragmentDashboardBinding;
import com.example.manufacturehome.databinding.FragmentLocalDashboardBinding;
import com.example.manufacturehome.utils.Data;

public class DashboardLocalFragment extends Fragment {

    private FragmentLocalDashboardBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentLocalDashboardBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        binding.usernameText.setText(Data.username);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
