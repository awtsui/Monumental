package com.example.monument.ui.login;

import android.app.Activity;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import android.content.SharedPreferences;


import android.os.Debug;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.monument.R;
import com.example.monument.ui.login.LoginViewModel;
import com.example.monument.ui.login.LoginViewModelFactory;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity implements  View.OnClickListener{

    private LoginViewModel loginViewModel;
    private FirebaseAuth mAuth;
    private SharedPreferences loginPreferences;
    private SharedPreferences.Editor loginPreferencesEditor;
    private boolean saveLogin;
    private String username, password;
    EditText usernameEditText;
    EditText passwordEditText;
    Button loginButton;
    Button registerButton;
    ProgressBar loadingProgressBar;
    ImageView logoImage;
    CheckBox checkBox;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        loginViewModel = ViewModelProviders.of(this, new LoginViewModelFactory())
                .get(LoginViewModel.class);

        usernameEditText = findViewById(R.id.email);
        passwordEditText = findViewById(R.id.password);
        checkBox = findViewById(R.id.checkBox);
        loginButton = findViewById(R.id.login);
        registerButton = findViewById(R.id.registerButton);
        loadingProgressBar = findViewById(R.id.loading);
        logoImage = findViewById(R.id.monumentLogo);
        loginPreferences = getSharedPreferences("loginPrefs", MODE_PRIVATE);
        loginPreferencesEditor = loginPreferences.edit();
        saveLogin = loginPreferences.getBoolean("saveLogin", false);
        if (saveLogin){
            usernameEditText.setText(loginPreferences.getString("username", ""));
            passwordEditText.setText(loginPreferences.getString("password", ""));
            checkBox.setChecked(true);
        }

        loginButton.setOnClickListener(this);
        registerButton.setOnClickListener(this);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();



        loginViewModel.getLoginFormState().observe(this, new Observer<LoginFormState>() {
            @Override
            public void onChanged(@Nullable LoginFormState loginFormState) {
                if (loginFormState == null) {
                    return;
                }
                loginButton.setEnabled(loginFormState.isDataValid());
                if (loginFormState.getUsernameError() != null) {
                    usernameEditText.setError(getString(loginFormState.getUsernameError()));
                }
                if (loginFormState.getPasswordError() != null) {
                    passwordEditText.setError(getString(loginFormState.getPasswordError()));
                }
            }
        });

        loginViewModel.getLoginResult().observe(this, new Observer<LoginResult>() {
            @Override
            public void onChanged(@Nullable LoginResult loginResult) {
                if (loginResult == null) {
                    return;
                }
                loadingProgressBar.setVisibility(View.GONE);
                if (loginResult.getError() != null) {
                    showLoginFailed(loginResult.getError());
                }
                if (loginResult.getSuccess() != null) {
                    updateUiWithUser(loginResult.getSuccess());
                }
                setResult(Activity.RESULT_OK);

                //Complete and destroy login activity once successful
                finish();
            }
        });

        TextWatcher afterTextChangedListener = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // ignore
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // ignore
            }

            @Override
            public void afterTextChanged(Editable s) {
                loginViewModel.loginDataChanged(usernameEditText.getText().toString(),
                        passwordEditText.getText().toString());
            }
        };
        usernameEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                   // loginViewModel.login(usernameEditText.getText().toString(),
                   //         passwordEditText.getText().toString());
                    //System.out.println("Signing in");
                    signIn(usernameEditText.getText().toString(), passwordEditText.getText().toString());

                }
                return false;
            }
        });


    }

    private void signIn(String email, String password) {

        // [START sign_in_with_email]
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();


                            Toast.makeText(LoginActivity.this, "Authentication Succeeded.",
                                    Toast.LENGTH_SHORT).show();
                            goToMap();
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }

                        // [START_EXCLUDE]
                        if (!task.isSuccessful()) {
                            //mStatusTextView.setText(R.string.auth_failed);
                        }
                        //hideProgressBar();
                        // [END_EXCLUDE]
                    }
                });
        // [END sign_in_with_email]
    }

    private void updateUiWithUser(LoggedInUserView model) {
        String welcome = getString(R.string.welcome) + model.getDisplayName();
        // TODO : initiate successful logged in experience
        Toast.makeText(getApplicationContext(), welcome, Toast.LENGTH_LONG).show();
    }

    private void showLoginFailed(@StringRes Integer errorString) {
        Toast.makeText(getApplicationContext(), errorString, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onClick(View v) {
        //System.out.println("Button Pressed");
        //Toast.makeText(getApplicationContext(), "Button Pressed", Toast.LENGTH_SHORT).show();
        username = usernameEditText.getText().toString();
        password = passwordEditText.getText().toString();
        if (v.getId() == loginButton.getId()) {
            if (checkBox.isChecked()) {
                loginPreferencesEditor.putBoolean("saveLogin", true);
                loginPreferencesEditor.putString("username", username);
                loginPreferencesEditor.putString("password", password);
                loginPreferencesEditor.commit();
            } else {
                loginPreferencesEditor.clear();
                loginPreferencesEditor.commit();
            }

            signIn(usernameEditText.getText().toString(), passwordEditText.getText().toString());
        }


        else if (v.getId() == registerButton.getId()){

            Intent intent = new Intent(this,RegisterActivity.class);
            startActivity(intent);
        }



    }

    private void goToMap(){

        FirebaseUser user = mAuth.getCurrentUser();

        Intent intent = new Intent(this, MapActivity.class);
        intent.putExtra("user", user.getEmail());

        startActivity(intent);
    }
}
