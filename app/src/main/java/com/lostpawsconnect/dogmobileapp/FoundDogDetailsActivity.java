package com.lostpawsconnect.dogmobileapp;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lostpawsconnect.dogmobileapp.databinding.ActivityFoundDogDetailsBinding;
import com.lostpawsconnect.dogmobileapp.databinding.ActivityMissingDogDetailsBinding;
import com.lostpawsconnect.dogmobileapp.model.Dogs;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

public class FoundDogDetailsActivity extends AppCompatActivity {

    Dogs dog = null;
    ActivityFoundDogDetailsBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFoundDogDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        getData();
        setListeners();
    }

    private void setListeners() {
        //Report Missing Button
//        binding.btnReport.setOnClickListener(
//                view -> {
//                    showConfirmationDialog();
//                }
//        );

    }

    private void showConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        final EditText remarksInput = new EditText(this);
        remarksInput.setHint("Remarks (required)");
        builder.setView(remarksInput);

        builder.setTitle("Confirmation");
        builder.setMessage("Are you sure you want to report this dog as missing?");
        builder.setPositiveButton("Yes", (dialog, which) -> {
            // User confirmed, perform the action here
            String remarks = remarksInput.getText().toString().trim();
            if (!remarks.isEmpty()) {
                dog.setRemarks(remarks);
                dog.setStatus("found");
                reportMissing();
                setResult(Activity.RESULT_OK);
            } else {
                Toast.makeText(FoundDogDetailsActivity.this, "Operation cancelled due to no remarks provided", Toast.LENGTH_SHORT).show();
            }
            dialog.dismiss();
        });
        builder.setNegativeButton("No", (dialog, which) -> {
            // User canceled the action
            dialog.dismiss();
        });
        builder.create().show();
    }

    private void reportMissing() {
        String dogID = dog.getDogID();
        FirebaseFirestore.getInstance()
                .collection("dogs_registered")
                .document(dogID).update(
                        dog.toMap()
                ).addOnSuccessListener(documentReference -> {
                    Toast.makeText(FoundDogDetailsActivity.this, "Reported as found", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(FoundDogDetailsActivity.this, "Failed to report as found, Please Try Again Later", Toast.LENGTH_SHORT).show();
                    Log.e("FirestoreError", e.getMessage());
                });
    }

    private void getData() {
        String rawDog = getIntent().getStringExtra("dogs");
        try {
            Dogs dogs = new Gson().fromJson(rawDog, new TypeToken<Dogs>() {
            }.getType());
            dog = dogs;

            if (dogs != null) {
                if (dogs.getImageurl() != null && !dogs.getImageurl().equals("")) {
                    Picasso.get()
                            .load(dogs.getImageurl())
                            .into(binding.dogPic, new Callback() {
                                @Override
                                public void onSuccess() {

                                }

                                @Override
                                public void onError(Exception e) {
                                    binding.dogPic.setImageResource(R.drawable.dog);
                                }
                            });
                }
                binding.editDogName.setText(dogs.getDogName());
                binding.editColor.setText(dogs.getDogColor());
                binding.editDogBreed.setText(dogs.getDogBreed());
                binding.txtDogGender.setText(dogs.getDogGender());
            }
        } catch (Exception e) {

        }
    }
}
