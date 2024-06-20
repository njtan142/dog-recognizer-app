package com.lostpawsconnect.dogmobileapp.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.gson.Gson;
import com.lostpawsconnect.dogmobileapp.FoundDogDetailsActivity;
import com.lostpawsconnect.dogmobileapp.ReportDogDetailsActivity;
import com.lostpawsconnect.dogmobileapp.adapters.ReportAdapter;
import com.lostpawsconnect.dogmobileapp.databinding.FragmentNotificationBinding;
import com.lostpawsconnect.dogmobileapp.databinding.FragmentReportBinding;
import com.lostpawsconnect.dogmobileapp.interfaces.ReportListener;
import com.lostpawsconnect.dogmobileapp.model.Dogs;
import com.lostpawsconnect.dogmobileapp.preference.UserPref;
import com.lostpawsconnect.dogmobileapp.services.VRequest;

import java.util.ArrayList;
import java.util.List;

public class NotificationFragment extends Fragment {

    FragmentNotificationBinding binding;

//    FragmentReportBinding binding;
    ReportAdapter adapter;
    VRequest request;
    List<Dogs> dogsList = new ArrayList<>();


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentNotificationBinding.inflate(LayoutInflater.from(requireContext()), container, false);
        loadDataFirestore();
        return binding.getRoot();
    }

    private void bindDataToView() {
        if (dogsList.size() > 0) {
            adapter = new ReportAdapter(requireContext(), dogsList, new ReportListener() {
                @Override
                public void onClickListener() {

                }

                @Override
                public void onClickListener(int position) {
                    Dogs dogs = dogsList.get(position);
                    Context mContext = requireContext();
                    Intent intent = new Intent(mContext, FoundDogDetailsActivity.class);
                    intent.putExtra("dogs", new Gson().toJson(dogs));
                    mContext.startActivity(intent);
                }
            });

            binding.recycler.setLayoutManager(new LinearLayoutManager(requireContext()));
            binding.recycler.setAdapter(adapter);
            adapter.notifyDataSetChanged();
        }
    }

    private void loadDataFirestore() {
        String userID = UserPref.getInstance().getUserID();
        FirebaseFirestore.getInstance().collection("dogs_registered").whereEqualTo("status", "found").get().addOnSuccessListener(queryDocumentSnapshots -> {
            for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                Dogs dog = Dogs.fromMap(documentSnapshot.getData());
                dog.setDogID(documentSnapshot.getId());
                dogsList.add(dog);
            }
            bindDataToView();
        }).addOnFailureListener(e -> {
            String message = e.getMessage();
            Toast.makeText(this.getContext(), message, Toast.LENGTH_SHORT).show();
        });

    }
}
