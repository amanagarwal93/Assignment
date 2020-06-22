package com.example.assignment.login;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.example.assignment.HomeActivity;
import com.example.assignment.R;
import com.example.assignment.utility.Utils;
import com.example.assignment.databinding.ActivityLoginBinding;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.login.LoginResult;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONException;

import java.util.Collections;
import java.util.List;

public class LoginActivity extends AppCompatActivity {

    private String TAG = LoginActivity.class.getSimpleName();

    private ActivityLoginBinding binding;

    private static final int MY_REQUEST_CODE = 123;
    List<AuthUI.IdpConfig> providers;

    private CallbackManager mCallbackManager;
    private FirebaseAuth mFirebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login);
    }

    @Override
    protected void onStart() {
        super.onStart();

        providers = Collections.singletonList(
                new AuthUI.IdpConfig.GoogleBuilder().build());

        mFirebaseAuth = FirebaseAuth.getInstance();

        mCallbackManager = CallbackManager.Factory.create();

        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mFirebaseAuth.getCurrentUser();
        if(Utils.getInstance().isNetworkAvailable(this)) {
            if (currentUser != null) {
                startActivity(new Intent(this, HomeActivity.class));
                finish();
            }
        }else{
            Utils.showShortToastMessage(binding.rootLayout,"No Internet Connection");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        binding.facebookLoginBtn.setPermissions("public_profile", "email", "user_birthday");
        binding.facebookLoginBtn.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                //Sign in completed
                Log.d(TAG, "onSuccess: logged in successfully");

                //handling the token for Firebase Auth
                handleFacebookAccessToken(loginResult.getAccessToken());

                //Getting the user information
                GraphRequest request = GraphRequest.newMeRequest(loginResult.getAccessToken(), (object, response) -> {
                    // Application code
                    Log.d(TAG, "onCompleted: response: " + response.toString());
                    try {
                        String email = object.getString("email");
                        String birthday = object.getString("birthday");

                        Log.d(TAG, "onCompleted: Email: " + email);
                        Log.d(TAG, "onCompleted: Birthday: " + birthday);

                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.d(TAG, "onCompleted: JSON exception");
                    }
                });

                Bundle parameters = new Bundle();
                parameters.putString("fields", "id,name,email,gender,birthday");
                request.setParameters(parameters);
                request.executeAsync();
            }

            @Override
            public void onCancel() {
                Log.d(TAG, "facebook:onCancel");
            }

            @Override
            public void onError(FacebookException error) {
                Log.d(TAG, "facebook:onError", error);
            }
        });

        binding.gmailLoginBtn.setOnClickListener(v -> showSignInOptions());
        binding.phoneLoginBtn.setOnClickListener(v -> {
            startActivity(new Intent(this, PhoneAuthActivity.class));
        });
    }

    private void showSignInOptions() {
        startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .setTheme(R.style.MyTheme)
                .setLogo(R.drawable.common_google_signin_btn_icon_light)
                .build(), MY_REQUEST_CODE);
    }

    private void handleFacebookAccessToken(AccessToken token) {
        Log.d(TAG, "handleFacebookAccessToken:" + token);

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mFirebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithCredential:success");
                        FirebaseUser user = mFirebaseAuth.getCurrentUser();
                        Log.d(TAG, "onComplete: login completed with user: " + user.getDisplayName());

                    } else {
                        // If sign in fails, display a message to the user.
                        Log.d(TAG, "signInWithCredential:failure", task.getException());
                        Log.d(TAG, "handleFacebookAccessToken: Authentication Failed ");
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == MY_REQUEST_CODE) {
            IdpResponse response = IdpResponse.fromResultIntent(data);
            Log.d(TAG, "onActivityResult: " + response.getEmail());
            if (resultCode == RESULT_OK) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                startActivity(new Intent(this, HomeActivity.class));
                finish();
                Toast.makeText(this, "Welcome! " + user.getDisplayName(), Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "" + response.getError().getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }
}