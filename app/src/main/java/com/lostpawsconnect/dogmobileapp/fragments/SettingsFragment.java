package com.lostpawsconnect.dogmobileapp.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.lostpawsconnect.dogmobileapp.adapters.SettingsAdapter;
import com.lostpawsconnect.dogmobileapp.databinding.FragmentSettingsBinding;
import com.lostpawsconnect.dogmobileapp.interfaces.SettingsListener;
import com.lostpawsconnect.dogmobileapp.preference.UserPref;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SettingsFragment extends Fragment {

    FragmentSettingsBinding binding;
    SettingsAdapter adapter;
    List<String> list = new ArrayList<>();
    SettingsListener listener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(LayoutInflater.from(requireContext()), container, false);
        setList();
        return binding.getRoot();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        // Ensure the parent activity implements the LogoutListener interface
        if (context instanceof SettingsListener) {
            listener = (SettingsListener) context;
        } else {
            throw new ClassCastException(context.toString() + " must implement SettingsListener");
        }
    }

    private void setList() {
        list.add("Logout");
        adapter = new SettingsAdapter(requireContext(), list);

        binding.lv.setAdapter(adapter);
        binding.lv.setOnItemClickListener((parent, view, position, id) -> {
            String str = list.get(position);
            switch(str){
                case "Logout":
                    AlertDialog.Builder mBuilder = new AlertDialog.Builder(requireContext());
                    DialogInterface.OnClickListener dListener = (dialog, which) -> {
                        switch (which) {
                            case DialogInterface.BUTTON_NEGATIVE -> {
//                                new UserPref(requireContext()).clearLogin();
                                //TODO logout auth
                                listener.onLogoutClickListener();
                            }
                            default -> {
                                dialog.dismiss();
                            }
                        }
                    };
                    mBuilder.setMessage("Are You Sure You Want Logout?")
                            .setNegativeButton("Yes", dListener)
                            .setPositiveButton("Cancel", dListener)
                            .setCancelable(false)
                            .show();
                    break;
            }
        });
    }
}
