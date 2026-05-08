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

public class MainActivity extends AppCompatActivity {

    private final String TAG = "MainActivity";
    private ActivityMainBinding binding;
    private MainViewModel mainViewModel;
    private String currentUserId = "Eugenius";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mainViewModel = new ViewModelProvider(this).get(MainViewModel.class);

        mainViewModel.isLoading().observe(this, isLoading -> {
            if (isLoading != null && isLoading) {
                binding.progressBar.setVisibility(android.view.View.VISIBLE);
            } else {
                binding.progressBar.setVisibility(android.view.View.GONE);
            }
        });

        mainViewModel.getError().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Log.e(TAG, "Error: " + error);
                Toast.makeText(this, "Ошибка: " + error, Toast.LENGTH_LONG).show();
            }
        });

        mainViewModel.isInitialized().observe(this, isInitialized -> {
            if (isInitialized != null && isInitialized) {
                Log.d(TAG, "Data initialized successfully");
                navigateToMenuActivity();
            }
        });

        mainViewModel.initializeReferenceData();
    }

    private void navigateToMenuActivity() {
        Intent intent = new Intent(this, MenuActivity.class);
        intent.putExtra("USER_ID", currentUserId);
        startActivity(intent);
        finish();
    }
}