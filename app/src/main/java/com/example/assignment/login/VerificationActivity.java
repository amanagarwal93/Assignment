package com.example.assignment.login;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.databinding.DataBindingUtil;

import com.example.assignment.R;
import com.example.assignment.utility.Utils;
import com.example.assignment.databinding.ActivityVerificationBinding;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class VerificationActivity extends AppCompatActivity {

    private String TAG = VerificationActivity.class.getSimpleName();

    private ActivityVerificationBinding binding;
    private String mVerificationId = "";
    private FirebaseAuth mAuth;
    private String mobile;
    private ProgressDialog progressDialog;
    private PhoneAuthProvider.ForceResendingToken resendToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_verification);
    }

    @Override
    protected void onStart() {
        super.onStart();

        /*
         * initialization of objects
         */
        mAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);
        mobile = getIntent().getStringExtra("mobile");
    }

    @Override
    protected void onResume() {
        super.onResume();
        setTitle("OTP Verification");
        sendVerificationCode(mobile);
        getPinFromPinView1();


        /*
         * verify button click
         */
        binding.verifyBtn.setOnClickListener(v -> {
            if (Utils.getInstance().isNetworkAvailable(this)) {
                if (binding.pinView1.getText().toString().length() == 6) {
                    showLoading("Verifying");
                    String userOTP = binding.pinView1.getText().toString();
                    if (!userOTP.isEmpty()) {
                        verifyCode(userOTP);
                    } else {
                        hideLoading();
                        Utils.showShortToastMessage(binding.relativeLayout, "Invalid OTP");
                    }
                }
            } else {
                Utils.showShortToastMessage(binding.relativeLayout, "No Internet Connection");
            }
        });


        /*
         * resend button click
         */
        binding.resendBtn.setOnClickListener(v -> {
            binding.pinView1.getText().clear();
            resendVerificationCode(mobile);
        });
    }

    /**
     * this will be called first time automatically as well as on button click for sending the code
     */

    private void sendVerificationCode(String mobile) {
        PhoneAuthProvider
                .getInstance()
                .verifyPhoneNumber("+91" + mobile, 60, TimeUnit.SECONDS, this, callbacks);
    }

    /**
     * set pin view to UI
     */

    private void getPinFromPinView1() {

        binding.pinView1.setTextColor(
                ResourcesCompat.getColor(getResources(), R.color.colorBlack, getTheme()));
        binding.pinView1.setTextColor(
                ResourcesCompat.getColorStateList(getResources(), R.color.colorBlack, getTheme()));
        binding.pinView1.setLineColor(
                ResourcesCompat.getColor(getResources(), R.color.colorBlack, getTheme()));
        binding.pinView1.setLineColor(
                Objects.requireNonNull(ResourcesCompat.getColorStateList(getResources(), R.color.colorBlack, getTheme())));
        binding.pinView1.setItemCount(6);
        binding.pinView1.setItemHeight(getResources().getDimensionPixelSize(R.dimen.pv_pin_view_item_size));
        binding.pinView1.setItemWidth(getResources().getDimensionPixelSize(R.dimen.pv_pin_view_item_size));
        binding.pinView1.setItemRadius(getResources().getDimensionPixelSize(R.dimen.pv_pin_view_item_radius));
        binding.pinView1.setItemSpacing(getResources().getDimensionPixelSize(R.dimen.pv_pin_view_item_spacing));
        binding.pinView1.setLineWidth(getResources().getDimensionPixelSize(R.dimen.pv_pin_view_item_line_width));
        binding.pinView1.setAnimationEnable(true);// start animation when adding text
        binding.pinView1.setCursorVisible(false);
        binding.pinView1.setCursorColor(
                ResourcesCompat.getColor(getResources(), R.color.lightGrey, getTheme()));
        binding.pinView1.setCursorWidth(getResources().getDimensionPixelSize(R.dimen.pv_pin_view_cursor_width));
        binding.pinView1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.d(TAG, "onTextChanged() called with: s = [" + s + "], start = [" + start + "], before = [" + before + "], count = [" + count + "]");

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (Objects.requireNonNull(binding.pinView1.getText()).toString().length() == 6) {
                    sendVerificationCode(mobile);
                }
            }
        });

        binding.pinView1.setItemBackgroundColor(Color.BLACK);
        binding.pinView1.setItemBackground(getResources().getDrawable(R.drawable.item_background));
        binding.pinView1.setItemBackgroundResources(R.drawable.item_background);
        binding.pinView1.setHideLineWhenFilled(false);
    }

    /**
     * verification callbacks
     */

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks
            callbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        @Override
        public void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider.ForceResendingToken token) {
            Log.d(TAG, "onCodeSent:" + verificationId);

            hideLoading();
            mVerificationId = verificationId;
            resendToken = token;
            binding.resendBtn.setVisibility(View.VISIBLE);
            binding.verifyBtn.setVisibility(View.VISIBLE);
            binding.pinView1.requestFocus();
        }

        @Override
        public void onVerificationCompleted(PhoneAuthCredential credential) {
            // This callback will be invoked in two situations:
            // 1 - Instant verification. In some cases the phone number can be instantly
            //     verified without needing to send or enter a verification code.
            // 2 - Auto-retrieval. On some devices Google Play services can automatically
            //     detect the incoming verification SMS and perform verification without
            //     user action.
            Log.d(TAG, "onVerificationCompleted:" + credential);
            if (Utils.getInstance().isNetworkAvailable(VerificationActivity.this)) {
                Log.d(TAG, "onVerificationCompleted: " + credential.getSmsCode());
                binding.pinView1.setText(credential.getSmsCode());
                verifyCode(credential.getSmsCode());
            } else {
                Utils.showShortToastMessage(binding.relativeLayout, "No Internet Connection");
            }
        }

        @Override
        public void onVerificationFailed(FirebaseException e) {
            Log.d(TAG, "onVerificationFailed", e);
            hideLoading();
            if (e instanceof FirebaseAuthInvalidCredentialsException) {
                Utils.showShortToastMessage(binding.relativeLayout, e.getMessage());
            } else if (e instanceof FirebaseTooManyRequestsException) {
                Utils.showShortToastMessage(binding.relativeLayout, e.getMessage());
            }
        }
    };

    /**
     * @param code verification code is sent here to match credentials
     */

    private void verifyCode(String code) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, code);
        if (Utils.getInstance().isNetworkAvailable(this)) {
            signInWithCredential(credential);
        } else {
            Utils.showShortToastMessage(binding.relativeLayout, "No Internet Connection");
        }
    }

    private void signInWithCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        hideLoading();
                        Log.d(TAG, "signInWithCredential:success");
                        Utils.showShortToastMessage(binding.relativeLayout, "Validation Success");
                    } else {
                        if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                            Log.d(TAG, "task exception = " + task.getException().getMessage());
                        } else {
                            Log.d(TAG, "task exception = " + task.getException().getMessage());
                        }
                        hideLoading();
                    }
                });
    }

    /**
     * display progress bar
     */
    private void showLoading(String message) {
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage(message);
        progressDialog.setCancelable(false);
        progressDialog.show();
    }

    /**
     * hide progress bar
     */
    private void hideLoading() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    /**
     * @param phoneNumber resend code to the specified mobile number
     */

    private void resendVerificationCode(String phoneNumber) {
        showLoading("OTP sending again..");
        PhoneAuthProvider
                .getInstance()
                .verifyPhoneNumber("+91" + phoneNumber, 60, TimeUnit.SECONDS, this, callbacks, resendToken);
    }
}