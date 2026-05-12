package com.example.tasktracker.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.tasktracker.R;
import com.example.tasktracker.components.MainViewModel;
import com.example.tasktracker.databinding.ActivityMainBinding;
import com.example.tasktracker.utils.SPHelper;

public class MainActivity extends AppCompatActivity {

    private final String TAG = "MainActivity";
    private ActivityMainBinding binding;
    private MainViewModel mainViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        SPHelper spHelper = SPHelper.getInstance(this);

        if (spHelper.isLoggedIn()) {
            mainViewModel = new ViewModelProvider(this).get(MainViewModel.class);

            setupLoadingObserve();
            setupErrorObserve();
            setupInitializeObserve();

            mainViewModel.initializeReferenceData();
        } else {
            navigateToAuthenticationActivity();
        }
    }

    private void setupLoadingObserve() {
        mainViewModel.isLoading().observe(this, isLoading -> {
            if (isLoading != null && isLoading) {
                binding.progressBar.setVisibility(android.view.View.VISIBLE);
            } else {
                binding.progressBar.setVisibility(android.view.View.GONE);
            }
        });
    }

    private void setupErrorObserve() {
        mainViewModel.getError().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Log.e(TAG, "Error: " + error);
                Toast.makeText(this, "Ошибка: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setupInitializeObserve() {
        mainViewModel.isInitialized().observe(this, isInitialized -> {
            if (isInitialized != null && isInitialized) {
                navigateToMenuActivity();
            }
        });
    }

    private void navigateToMenuActivity() {
        Intent intent = new Intent(this, MenuActivity.class);
        startActivity(intent);
        finish();
    }

    private void navigateToAuthenticationActivity() {
        Intent intent = new Intent(this, AuthenticationActivity.class);
        startActivity(intent);
        finish();
    }
}