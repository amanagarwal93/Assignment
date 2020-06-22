package com.example.assignment.login;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.example.assignment.R;
import com.example.assignment.utility.Utils;
import com.example.assignment.databinding.ActivityPhoneAuthBinding;

public class PhoneAuthActivity extends AppCompatActivity {

    private ActivityPhoneAuthBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_phone_auth);

        setTitle("Login with Mobile");

        binding.buttonContinue.setOnClickListener(v -> {
            if (binding.mobile.getText().toString().isEmpty()) {
                Utils.showShortToastMessage(binding.relativeLayout, "Please enter the number");
            } else if (binding.mobile.getText().toString().length() < 10) {
                Utils.showShortToastMessage(binding.relativeLayout, "Please enter the correct number");
            } else {
                startActivity(new Intent(this, VerificationActivity.class)
                        .putExtra("mobile", binding.mobile.getText().toString()));
            }
        });
    }
}