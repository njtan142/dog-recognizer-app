package com.lostpawsconnect.dogmobileapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;
import com.github.MakMoinee.library.common.MapForm;
import com.github.MakMoinee.library.interfaces.LocalVolleyRequestListener;
import com.github.MakMoinee.library.models.LocalVolleyRequestBody;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.lostpawsconnect.dogmobileapp.databinding.ActivityMainBinding;
import com.lostpawsconnect.dogmobileapp.model.Users;
import com.lostpawsconnect.dogmobileapp.parser.ResponseParser;
import com.lostpawsconnect.dogmobileapp.preference.UserPref;
import com.lostpawsconnect.dogmobileapp.services.VRequest;


import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity{

    private ActivityMainBinding binding;
    //    private VRequest request;
    private UserPref pref;
    private ProgressDialog pd;

    private FirebaseApp app;
    private FirebaseAuth auth;

    private FirebaseUser user;

//    private ImageClassifierHelper imgClassHelper;
//    private ImageClassifierHelper.ClassifierListener imgTFListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setListeners();

        app = FirebaseApp.initializeApp(this);
        auth = FirebaseAuth.getInstance(app);
        user = auth.getCurrentUser();

        Python.start(new AndroidPlatform(this.getBaseContext()));

//        imgClassHelper = ImageClassifierHelper.create(this.getBaseContext(), this);

        if (user != null) {
            UserPref.getInstance().setUserID(user.getUid());
            var intent = new Intent(MainActivity.this, DashboardActivity.class);
            startActivity(intent);
        }

    }

    private void setListeners() {
        binding.btnLogin.setOnClickListener(v -> {
            String username = binding.editUsername.getText().toString();
            String password = binding.editPassword.getText().toString();


            if (username.equals("") || password.equals("")) {
                Toast.makeText(MainActivity.this, "Please Don't Leave Empty Fields", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, "Signing In", Toast.LENGTH_SHORT).show();
                auth.signInWithEmailAndPassword(username, password).addOnCompleteListener(
                        new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if(task.isSuccessful()){
                                    user = auth.getCurrentUser();
                                    Toast.makeText(MainActivity.this, "Authentication Successful", Toast.LENGTH_SHORT).show();
                                    var intent = new Intent(MainActivity.this, DashboardActivity.class);
                                    startActivity(intent);
                                }else{
                                    Toast.makeText(getApplicationContext(), "Authentication failed: ",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                );
            }
        });
    }


}