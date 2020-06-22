package com.example.assignment;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.example.assignment.databinding.ActivityHomeBinding;
import com.example.assignment.login.LoginActivity;
import com.example.assignment.utility.Utils;
import com.firebase.ui.auth.AuthUI;

public class HomeActivity extends AppCompatActivity {

    private String TAG = HomeActivity.class.getSimpleName();
    private ActivityHomeBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_home);

        binding.signOutButton.setOnClickListener(v -> {
            AuthUI.getInstance()
                    .signOut(this)
                    .addOnCompleteListener(task -> signOut())
                    .addOnFailureListener(e ->
                            Utils.showShortToastMessage(binding.constraintLayout, "" + e.getMessage()));
        });
    }

    private void signOut() {
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }
}