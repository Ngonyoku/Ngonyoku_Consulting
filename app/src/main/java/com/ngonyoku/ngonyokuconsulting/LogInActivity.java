package com.ngonyoku.ngonyokuconsulting;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LogInActivity extends AppCompatActivity {
    private static final String TAG = "LogInActivity";
    //Firebase
    private FirebaseAuth.AuthStateListener mAuthStateListener; /*Listens for changes in the Authentication State*/

    private Button mLogIn;
    private EditText mEmail, mPassword;
    private ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        setUpFirebaseAuth();

        mEmail = findViewById(R.id.input_email);
        mPassword = findViewById(R.id.input_password);
        mLogIn = findViewById(R.id.btn_logIn);
        mProgressBar = findViewById(R.id.progressBarLogIn);

        mLogIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = mEmail.getText().toString().trim();
                String password = mPassword.getText().toString().trim();

                if (email.isEmpty() || password.isEmpty()) {
                    feedBack("Email and Password is Required!");
                    mEmail.requestFocus();
                } else {
                    showDialog();
                    FirebaseAuth
                            .getInstance()
                            .signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    dismissDialog();
                                    feedBack("Sign In Successful");
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    feedBack("OOps, FAILED TO SIGN IN!!!");
                                    dismissDialog();
                                }
                            })
                    ;
                }
            }
        });
    }

    public void goToCreateAccount(View view) {
        startActivity(new Intent(this, RegisterActivity.class));
    }

    private void erase() {
        mEmail.setText(null);
        mPassword.setText(null);
        mEmail.requestFocus();
    }

    private void feedBack(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private void feedBack(String message, boolean length_long) {
        if (!length_long) {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        } else feedBack(message);
    }

    private void dismissDialog() {
        mProgressBar.setVisibility(View.GONE);
    }

    private void showDialog() {
        mProgressBar.setVisibility(View.VISIBLE);
    }

    /*-----------------Firebase SetUp ---------------------------------*/
    private void setUpFirebaseAuth() {
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();

                if (user != null) {
                    /*Confirm that the Verification Link has been sent*/
                    if (user.isEmailVerified()) {
                        Log.d(TAG, "onAuthStateChanged: Sign In:" + user.getUid());
                        feedBack("Authenticated with " + user.getEmail(), false);

                        startActivity(new Intent(LogInActivity.this, SignedInActivity.class));
                        finish();
                    } else {
                        feedBack("Check your Email Inbox for Verification Link");
                        FirebaseAuth.getInstance().signOut();
                    }
                } else {
                    Log.d(TAG, "onAuthStateChanged: Signed Out");
                }
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseAuth.getInstance().addAuthStateListener(mAuthStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mAuthStateListener != null) {
            FirebaseAuth.getInstance().removeAuthStateListener(mAuthStateListener); /*Detach AuthStateListener*/
        }
    }

    private void resendVerificationEmail(String email, String password) {
        AuthCredential credential = EmailAuthProvider.getCredential(email, password);
        FirebaseAuth
                .getInstance()
                .signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "onComplete: ReAuthenticate Success");
                            sendVerificationEmail();
                            FirebaseAuth.getInstance().signOut();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        feedBack("Incorrect Credentials! Please Try agin");
                        erase();
                    }
                })
        ;
    }

    public void resendVerificationEmail(View view) {
        String email = mEmail.getText().toString().trim();
        String password = mPassword.getText().toString().trim();

        if (!email.isEmpty() || !password.isEmpty()) {
            resendVerificationEmail(email, password);
        } else {
            feedBack("Email and Password is Required!");
        }
    }

    private void sendVerificationEmail() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        assert user != null;
        user.sendEmailVerification()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            feedBack("Verification Email Sent", false);
                        } else {
                            feedBack(task.getException().getMessage());
                        }
                    }
                })
        ;
    }
}