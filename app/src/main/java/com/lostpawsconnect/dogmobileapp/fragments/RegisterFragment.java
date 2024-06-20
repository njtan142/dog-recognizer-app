package com.lostpawsconnect.dogmobileapp.fragments;

import static android.app.Activity.RESULT_OK;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.github.MakMoinee.library.interfaces.LocalVolleyRequestListener;
import com.github.MakMoinee.library.models.LocalVolleyRequestBody;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.lostpawsconnect.dogmobileapp.R;
import com.lostpawsconnect.dogmobileapp.databinding.FragmentRegisterBinding;
import com.lostpawsconnect.dogmobileapp.model.Dogs;
import com.lostpawsconnect.dogmobileapp.preference.UserPref;
import com.lostpawsconnect.dogmobileapp.services.VRequest;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.UUID;

public class RegisterFragment extends Fragment {

    FragmentRegisterBinding binding;
    String dogGender = "";

    private static final int REQUEST_PERMISSIONS = 100;

    private Boolean isPermitted = false;

    private ProgressDialog pdOpenFiles, pdSending;
    String filePath = "";
    String imagePath = "";
    Bitmap bitmap;

    VRequest request;

    private ActivityResultLauncher<Intent> browser = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if (result.getResultCode() == RESULT_OK) {
                Uri picUri = result.getData().getData();
                if (picUri == null) {
                    return;
                }
                filePath = getPath(picUri);
                if (filePath != null) {
                    try {

                        Toast.makeText(requireContext(), "File Selected", Toast.LENGTH_SHORT).show();
                        Log.d("filePath", String.valueOf(filePath));
                        bitmap = MediaStore.Images.Media.getBitmap(requireActivity().getContentResolver(), picUri);


                        binding.imgDog.setImageBitmap(bitmap);
//                        addProductBinding.btnRemove.setEnabled(true);
                        pdOpenFiles.dismiss();
                        onResume();
                    } catch (IOException e) {
                        pdOpenFiles.dismiss();
                        Toast.makeText(requireContext(), "browser -->>> " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    pdOpenFiles.dismiss();
                    Toast.makeText(
                            requireContext(), "no image selected",
                            Toast.LENGTH_LONG).show();
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

        cursor = requireActivity().getContentResolver().query(
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                null, MediaStore.Images.Media._ID + " = ? ", new String[]{document_id}, null);
        cursor.moveToFirst();
        @SuppressLint("Range")
        String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
        cursor.close();

        return path;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentRegisterBinding.inflate(LayoutInflater.from(requireContext()), container, false);
        pdOpenFiles = new ProgressDialog(requireContext());
        pdOpenFiles.setMessage("Opening Files ...");
        pdSending = new ProgressDialog(requireContext());
        pdSending.setMessage("Sending Request ...");
        pdSending.setCancelable(false);
        request = new VRequest(requireContext());
        setListeners();
        return binding.getRoot();
    }

//    private void setListeners() {
//        binding.btnSave.setOnClickListener(v -> {
//            String dogName = binding.editDogName.getText().toString();
//            String dogBreed = binding.editDogBreed.getText().toString();
//            String dogColor = binding.editDogColor.getText().toString();
//
//            if (dogName.isEmpty() || dogBreed.isEmpty() || dogColor.isEmpty() || dogGender.equals("") || bitmap.equals(null)) {
//                Toast.makeText(requireContext(), "Please Don't Leave Empty Fields", Toast.LENGTH_SHORT).show();
//            } else {
//                int userID = new UserPref(requireContext()).getUserID();
//                Dogs dogs = new Dogs.DogBuilder()
//                        .setUserID(userID)
//                        .setDogName(dogName)
//                        .setDogBreed(dogBreed)
//                        .setDogGender(dogGender)
//                        .setDogColor(dogColor)
//                        .setImagePath(bitmap)
//                        .buildMultipartRequestBody(true)
//                        .build();
//                LocalVolleyRequestBody body = request.generateDogRequestBody(dogs);
//                pdSending.show();
//                request.sendMultipartJSONPostRequest(body, new LocalVolleyRequestListener() {
//                    @Override
//                    public void onSuccessString(String response) {
//                        pdSending.dismiss();
//                        if (response.isEmpty()) {
//                            Toast.makeText(requireContext(), "Failed to register dog, Please Try Again Later", Toast.LENGTH_SHORT).show();
//                        } else {
//                            Toast.makeText(requireContext(), "Successfully Registered Dog", Toast.LENGTH_SHORT).show();
//                            clearFields();
//                        }
//                    }
//
//                    @Override
//                    public void onError(Error error) {
//                        pdSending.dismiss();
//                        if (error != null) {
//                            Log.e("multipart_err", error.getLocalizedMessage());
//                        }
//                        Toast.makeText(requireContext(), "Failed to register dog, Please Try Again Later", Toast.LENGTH_SHORT).show();
//                    }
//                });
//            }
//
//        });

    private void setListeners() {
        binding.btnSave.setOnClickListener(v -> {
            String dogName = binding.editDogName.getText().toString();
            String dogBreed = binding.editDogBreed.getText().toString();
            String dogColor = binding.editDogColor.getText().toString();

            if (dogName.isEmpty() || dogBreed.isEmpty() || dogColor.isEmpty() || dogGender.equals("") || filePath.isEmpty()) {
                Toast.makeText(requireContext(), "Please Don't Leave Empty Fields", Toast.LENGTH_SHORT).show();
            } else {
                uploadImageToFirebaseStorage(bitmap);

            }
        });

        binding.genderGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.btnMale) {
                dogGender = "Male";
            } else if (checkedId == R.id.btnFemale) {
                dogGender = "Female";
            }
        });

        binding.btnBrowse.setOnClickListener(v -> {
            isPermitted = askPermission();
            if (isPermitted) {
                browseImage();
            } else {
                Toast.makeText(requireContext(), "Please Allow Storage Permission", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void uploadImageToFirebaseStorage(Bitmap imageBitmap) {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();

        // Generate a unique name for the image
        String imageFileName = UUID.randomUUID().toString() + ".jpg";
        StorageReference imageRef = storageRef.child("dogs/" + imageFileName);

        // Convert the Bitmap to a byte array
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageData = baos.toByteArray();

        // Upload the image
        UploadTask uploadTask = imageRef.putBytes(imageData);

        uploadTask.addOnSuccessListener(taskSnapshot -> {
            // Image uploaded successfully, get the download URL
            imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                String downloadUrl = uri.toString();

                // Update the imagePathString in the Dogs object
                imagePath = downloadUrl;

                String dogName = binding.editDogName.getText().toString();
                String dogBreed = binding.editDogBreed.getText().toString();
                String dogColor = binding.editDogColor.getText().toString();
                String userID = UserPref.getInstance().getUserID();
                Dogs dogs = new Dogs(userID, dogName, dogBreed, dogColor, dogGender, imagePath);
                saveDogToFirestore(dogs);
            }).addOnFailureListener(e -> {
                // Handle failure to get the download URL
            });
        }).addOnFailureListener(e -> {
            // Handle failure to upload the image
        });
    }

    private void saveDogToFirestore(Dogs dogs) {
        // Assuming you have a reference to your Firestore database
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Create a new document in the "dogs" collection with an auto-generated ID
        CollectionReference dogsCollection = db.collection("dogs_registered");

        // You can use the `add` method to let Firestore generate an ID for the document
        dogsCollection.document().set(dogs.toMap())
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(requireContext(), "Successfully Registered Dog", Toast.LENGTH_SHORT).show();
                    clearFields();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Failed to register dog, Please Try Again Later", Toast.LENGTH_SHORT).show();
                    Log.e("FirestoreError", e.getMessage());
                });
    }

    private void clearFields() {
        binding.editDogBreed.setText("");
        binding.editDogColor.setText("");
        binding.editDogName.setText("");
        binding.imgDog.setImageResource(android.R.color.transparent);
        bitmap = null;
        dogGender = "";
        binding.genderGroup.clearCheck();
    }

    private void browseImage() {
        pdOpenFiles.show();
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        browser.launch(intent);
    }

    private boolean askPermission() {
        boolean permit = false;
        if ((ContextCompat.checkSelfPermission(requireContext(),
                android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
                || (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
        ) {
                ActivityCompat.requestPermissions(requireActivity(),
                        new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE},
                        REQUEST_PERMISSIONS);
                permit = false;
        } else {
            permit = true;
        }
        return permit;
    }
}
