package com.lostpawsconnect.dogmobileapp;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.google.firebase.Firebase;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.lostpawsconnect.dogmobileapp.databinding.ActivityDashboardBinding;
import com.lostpawsconnect.dogmobileapp.interfaces.ReportListener;
import com.lostpawsconnect.dogmobileapp.interfaces.SettingsListener;

public class DashboardActivity extends AppCompatActivity implements SettingsListener {

    private NavController navController;
    ActivityDashboardBinding binding;

    private FirebaseApp app;
    private FirebaseAuth auth;

    private FirebaseUser user;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        navController = Navigation.findNavController(DashboardActivity.this, R.id.my_navigation);
        NavigationUI.setupWithNavController(binding.bottomNav, navController);

        app = FirebaseApp.getInstance();
        auth = FirebaseAuth.getInstance(app);
        user = auth.getCurrentUser();

        if(user == null){
            finish();
        }
    }

    @Override
    public void onLogoutClickListener() {
        auth.signOut();
        Intent intent = new Intent(DashboardActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onClickListener() {

    }

}
