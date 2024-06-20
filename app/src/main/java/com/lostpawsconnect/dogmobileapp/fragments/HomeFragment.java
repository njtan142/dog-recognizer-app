package com.lostpawsconnect.dogmobileapp.fragments;


import static android.app.Activity.RESULT_OK;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.chaquo.python.Kwarg;
import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;
import com.lostpawsconnect.dogmobileapp.ImageClassifier;
import com.lostpawsconnect.dogmobileapp.MissingDogDetailsActivity;
import com.lostpawsconnect.dogmobileapp.adapters.ReportAdapter;
import com.lostpawsconnect.dogmobileapp.databinding.FragmentHomeBinding;
import com.lostpawsconnect.dogmobileapp.interfaces.ReportListener;
import com.lostpawsconnect.dogmobileapp.model.Dogs;
import com.lostpawsconnect.dogmobileapp.preference.UserPref;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment {

    FragmentHomeBinding binding;

    ReportAdapter adapter;
    List<Dogs> dogsList = new ArrayList<>();

    private static final int REQUEST_PERMISSIONS = 100;
    private Boolean isPermitted = false;

    private ProgressDialog pdOpenFiles, pdSending;
    String filePath = "";
//    String imagePath = "";
    Bitmap bitmap;

    String searchImageURL = "";
    UploadTask.TaskSnapshot uploadSnapshot;
    String classifyResult = "";

    ImageClassifier imageClassifier;



    ActivityResultLauncher<Intent> launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        dogsList.clear();
        binding.recycler.removeAllViews();
        loadDataFirestore();
    });


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(LayoutInflater.from(requireContext()), container, false);
        pdOpenFiles = new ProgressDialog(requireContext());
        pdOpenFiles.setMessage("Opening Files ...");
        pdSending = new ProgressDialog(requireContext());
        pdSending.setMessage("Sending Request ...");
        pdSending.setCancelable(false);
        loadDataFirestore();
        setListeners();
        imageClassifier = new ImageClassifier(this.requireContext(), "breed.tflite", "labels.txt");
        Log.d("ImageClassifierResult", String.valueOf(imageClassifier.labels.size()));
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
                    Intent intent = new Intent(mContext, MissingDogDetailsActivity.class);
                    intent.putExtra("dogs", new Gson().toJson(dogs));

                    launcher.launch(intent);
                }
            });

            binding.recycler.setLayoutManager(new LinearLayoutManager(requireContext()));
            binding.recycler.setAdapter(adapter);
            adapter.notifyDataSetChanged();
        }else{
            binding.recycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        }
    }

    private void loadDataFirestore() {
        String userID = UserPref.getInstance().getUserID();
        dogsList.clear();
        FirebaseFirestore.getInstance().collection("dogs_registered").whereEqualTo("status", "missing").get().addOnSuccessListener(queryDocumentSnapshots -> {
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

    private void loadDataFirestoreWithBreed() {
        String userID = UserPref.getInstance().getUserID();
        dogsList.clear();
        FirebaseFirestore.getInstance().collection("dogs_registered").whereEqualTo("status", "missing").get().addOnSuccessListener(queryDocumentSnapshots -> {
            for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                Dogs dog = Dogs.fromMap(documentSnapshot.getData());
                dog.setDogID(documentSnapshot.getId());
                Log.d("ImageClassifierResult",
                        dog.getDogBreed() +"=="+ classifyResult + " ->" +String.valueOf(classifyResult.equals(dog.getDogBreed())));

                if(classifyResult.equals(dog.getDogBreed())){
                    dogsList.add(dog);
                }
            }
            bindDataToView();
        }).addOnFailureListener(e -> {
            String message = e.getMessage();
            Toast.makeText(this.getContext(), message, Toast.LENGTH_SHORT).show();
        });

    }

    private boolean askPermission() {
        boolean permit = false;
        if ((ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) || (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)) {
            if ((ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) && (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), android.Manifest.permission.READ_EXTERNAL_STORAGE))) {
                permit = true;
            } else {
                ActivityCompat.requestPermissions(requireActivity(), new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_PERMISSIONS);
                permit = false;
            }
        } else {
            permit = true;
        }
        return permit;
    }

    private static final int REQUEST_CAMERA_PERMISSION = 3;
    private boolean askCameraPermission() {
        boolean permit = false;
        if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), android.Manifest.permission.CAMERA)) {
                permit = true;
            } else {
                ActivityCompat.requestPermissions(requireActivity(), new String[]{android.Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
                permit = false;
            }
        } else {
            permit = true;
        }
        return permit;
    }

    private void imageSimilarityPy() { // eto yung function sa pag run ng python code
        Log.d("firebase storage", String.valueOf("loading similarity"));
        Python py = Python.getInstance();
        PyObject imsim = py.getModule("plot");
        Log.d("firebase storage", String.valueOf("loaded similarity"));

        List<Dogs> new_dogsList = new ArrayList<>();

        dogsList.forEach(dog -> {
            Log.d("firebase storage", String.valueOf(dog.getImageurl()));
            PyObject result = imsim.callAttr("compare_similarity", searchImageURL, dog.getImageurl());
            dog.similarity = result.toFloat();
            if(dog.similarity > 0.8){ // dito yung treshhold settings, change to 0.9 if gusto nyo na dapat 90% yung similarity
                new_dogsList.add(dog);
            }
            Log.d("firebase storage", String.valueOf(result.toString()));
        });

        Comparator<Dogs> similarityComparator = (p1, p2) -> Float.compare(p2.similarity, p1.similarity);

        Collections.sort(new_dogsList, similarityComparator);

        dogsList = new_dogsList;
        // Print the sorted list
        for (Dogs dogs : dogsList) {
            Log.d("firebase storage", dogs.getDogName() + " | " + String.valueOf(dogs.similarity.toString()));
        }

        bindDataToView();
    }

    private void tempToFirebaseStorage(Uri fileUri) {
        Log.d("firebase storage", String.valueOf(filePath));
        Toast.makeText(this.getContext(), "loading python", Toast.LENGTH_SHORT).show();

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();

        // Create a reference to the file you want to upload
        StorageReference fileRef = storageRef.child("your_directory_name/" + "your_file_name");

        // Assuming 'filePath' is the local file path you want to upload
//        Uri fileUri = Uri.fromFile(new File(filePath));
        Toast.makeText(this.getContext(), "uploading", Toast.LENGTH_SHORT).show();
        Log.d("firebase storage", String.valueOf("uploading"));

        // Upload the file to Firebase Storage
        fileRef.putFile(fileUri).addOnSuccessListener(taskSnapshot -> {
            uploadSnapshot = taskSnapshot;
            uploadSnapshot.getStorage().getDownloadUrl().addOnSuccessListener(t -> {
                searchImageURL = t.toString();
                Log.d("firebase storage", String.valueOf("checking similarity"));
                imageSimilarityPy();
            });
        }).addOnFailureListener(exception -> {
            Toast.makeText(this.getContext(), "error", Toast.LENGTH_SHORT).show();
        });

//        Toast.makeText(requireContext(), "Breed is " + classifyResult, Toast.LENGTH_SHORT).show();

    }


    private ActivityResultLauncher<Intent> browser = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if (result.getResultCode() == RESULT_OK) {
                Uri picUri = result.getData().getData();
                if (picUri == null) {
                    return;
                }
                filePath = getPath(picUri);
                try {
                    classifyResult = imageClassifier.classifyImage(picUri, requireActivity().getContentResolver());
                    Toast.makeText(requireContext(), classifyResult, Toast.LENGTH_SHORT).show();
                    Log.d("ImageClassifierResult", classifyResult);
                    loadDataFirestoreWithBreed();
                } catch (IOException e) {
                    Toast.makeText(requireContext(), "browser -->>> " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
                tempToFirebaseStorage(picUri);
                if (filePath != null) {
                    try {
//                        Toast.makeText(requireContext(), "File Selecteds", Toast.LENGTH_SHORT).show();
                        Log.d("filePath", String.valueOf(filePath));
                        bitmap = MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(), picUri);
                        pdOpenFiles.dismiss();
                        onResume();
                    } catch (IOException e) {
                        pdOpenFiles.dismiss();
                        Toast.makeText(requireContext(), "browser -->>> " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    pdOpenFiles.dismiss();
                    Toast.makeText(requireContext(), "no image selected", Toast.LENGTH_LONG).show();
                }
            }
        }
    });

    private String getPath(Uri uri) {
        Cursor cursor = requireActivity().getContentResolver().query(uri, null, null, null, null);
        cursor.moveToFirst();
        String document_id = cursor.getString(0);
        document_id = document_id.substring(document_id.lastIndexOf(":") + 1);
        cursor.close();

        cursor = requireActivity().getContentResolver().query(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, MediaStore.Images.Media._ID + " = ? ", new String[]{document_id}, null);
        cursor.moveToFirst();
        @SuppressLint("Range") String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
        cursor.close();

        return path;
    }

    private void setListeners() {
        binding.imgSearch.setOnClickListener(v -> {
            isPermitted = askPermission();
            isPermitted = askCameraPermission();
            if (isPermitted) {
                showImageSourceDialog();
            } else {
                Toast.makeText(requireContext(), "Please Allow Storage Permission", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void browseImage() {
        pdOpenFiles.show();
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        browser.launch(intent);
    }

    private void showImageSourceDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Select Image Source");
        CharSequence[] options = {"Choose from Gallery", "Capture Photo"};
        builder.setItems(options, (dialog, which) -> {
            if (which == 0) {
                // Choose from Gallery
                browseImage();
            } else if (which == 1) {
                // Capture Photo
                capturePhoto();
            }
        });
        builder.show();
    }

    private static final int REQUEST_IMAGE_CAPTURE = 101;

    private void capturePhoto() {
        // Create an intent to open the camera app
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // Check if there is a camera app available to handle the request
//        if (takePictureIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
            // Start the camera intent
            takePictureLauncher.launch(takePictureIntent);
//        } else {
            // Handle the case where there is no camera app available
//            Toast.makeText(requireContext(), "No camera app found on your device", Toast.LENGTH_SHORT).show();
//        }

    }

    private ActivityResultLauncher<Intent> takePictureLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == RESULT_OK) {
            Intent data = result.getData();
            if (data != null) {
                Bundle extras = data.getExtras();
                Bitmap imageBitmap = (Bitmap) extras.get("data");
                ContentResolver resolver = this.requireContext().getContentResolver();

                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
                String currentDateAndTime = dateFormat.format(new Date());
                String title = "Image_" + currentDateAndTime;

                String path = MediaStore.Images.Media.insertImage(resolver, imageBitmap, title, null);

                Uri imageUri = Uri.parse(path);
                try {
                    classifyResult = imageClassifier.classifyImage(imageUri, requireActivity().getContentResolver());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                Toast.makeText(requireContext(), classifyResult, Toast.LENGTH_SHORT).show();
                Log.d("ImageClassifierResult", classifyResult);
                loadDataFirestoreWithBreed();
                tempToFirebaseStorage(imageUri);
            }
        }
    });



}
