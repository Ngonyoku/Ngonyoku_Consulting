package com.ngonyoku.ngonyokuconsulting;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class RegisterActivity extends AppCompatActivity {
    private static final String TAG = "RegisterActivity";

    public static final String DOMAIN_NAME = "ngonyoku.com";

    private Button mRegister;
    private EditText mEmail, mPassword, mConfirmPassword;
    private ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mRegister = findViewById(R.id.btn_register);
        mEmail = findViewById(R.id.input_email);
        mPassword = findViewById(R.id.input_password);
        mConfirmPassword = findViewById(R.id.input_confirm_password);
        mProgressBar = findViewById(R.id.progressBarRegister);

        mRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: attempting To Register");
                /*Check if Fields are Empty*/
                if (mEmail.getText().toString().trim().isEmpty()
                        || mPassword.getText().toString().trim().isEmpty()
                        || mConfirmPassword.getText().toString().trim().isEmpty()) {
                    feedBack("All inputs are Required");
                    mEmail.requestFocus();

                    /*Check id domain is valid*/
                } else if (!isValidDomain(mEmail.getText().toString().trim())) {

                    /*Check if Passwords Match*/
                    if (mPassword.getText().toString().trim()
                            .equals(mConfirmPassword.getText().toString().trim())) {

                        /*Register New User Account*/
                        registerAccount(
                                mEmail.getText().toString().trim(),
                                mPassword.getText().toString().trim()
                        );
                    } else {
                        feedBack("Passwords Don't match");
                    }
                } else {
                    feedBack("Not a valid Email Address");
                }
            }
        });
    }

    private void registerAccount(String email, String password) {
        showDialog();

        FirebaseAuth
                .getInstance()
                .createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "onComplete: " + task.isSuccessful());
                        if (task.isSuccessful()) {
                            Log.d(TAG, "onComplete: AuthState: "
                                    + FirebaseAuth.getInstance().getCurrentUser()
                            );
                            feedBack("Registration Successful!!");
                            erase();
                            FirebaseAuth.getInstance().signOut();
                        } else {
                            feedBack("OOPS!!, Registration Failed. "
                                    + task.getException().getMessage()
                            );
                        }
                        dismissDialog();
                    }
                })
        ;
    }

    private void dismissDialog() {
        mProgressBar.setVisibility(View.GONE);
    }

    private void showDialog() {
        mProgressBar.setVisibility(View.VISIBLE);
    }

    private boolean isValidDomain(String email) {
        String domain = email.substring(email.indexOf("@") + 1).toLowerCase();
        return domain.equals(DOMAIN_NAME);
    }

    public void goToLogIn(View view) {
        startActivity(new Intent(this, LogInActivity.class));
    }

    private void erase() {
        mEmail.setText(null);
        mPassword.setText(null);
        mConfirmPassword.setText(null);
        mEmail.requestFocus();
    }

    private void feedBack(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private void feedback(String message, boolean length_long) {
        if (!length_long) {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        } else feedBack(message);
    }
}