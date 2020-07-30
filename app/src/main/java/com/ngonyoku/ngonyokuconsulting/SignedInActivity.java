package com.ngonyoku.ngonyokuconsulting;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class SignedInActivity extends AppCompatActivity {
    private static final String TAG = "SignedInActivity";

    private FirebaseAuth.AuthStateListener mAuthStateListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signed_in);

        setUpFirebaseAuth();
//        getUserDetails();
        setUserDetails();
    }

    private void setUserDetails() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            UserProfileChangeRequest profileUpdates =
                    new UserProfileChangeRequest.Builder()
                            .setDisplayName("Ngonyoku Roderick")
                            .setPhotoUri(Uri.parse("https://avatars1.githubusercontent.com/u/47557555?s=460&u=c5d397f15ba99243d77718cb5131d511b3f3dce5&v=4"))
                            .build();

            user.updateProfile(profileUpdates)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "onComplete: User Profile Updated");

                                getUserDetails();
                            }
                        }
                    })
            ;
        }
    }

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

                        startActivity(new Intent(SignedInActivity.this, SignedInActivity.class));
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

    private void checkAuthenticationState() {
        Log.d(TAG, "checkAuthenticationState: Checking Authentication State");

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Log.d(TAG, "checkAuthenticationState: User is Null, Navigate Back to Login Screen!");
            startActivity(new Intent(this, LogInActivity.class)
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK) /*Clear the Activity Stack*/
            );
            finish();
        }
    }

    private void getUserDetails() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String uId = user.getUid();
            String name = user.getDisplayName();
            String email = user.getEmail();
            Uri photoUrl = user.getPhotoUrl();

            String properties = "uId: " + uId + "\n"
                    + "name: " + name + "\n"
                    + "email: " + email + "\n"
                    + "photoUrl: " + photoUrl + "\n";
            Log.d(TAG, "getUserDetails: properties: \n" + properties);

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkAuthenticationState();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_signed_in, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_sign_out:
                FirebaseAuth.getInstance().signOut();
                Toast.makeText(this, "Signed Out", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, LogInActivity.class)
                        .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK) /*Clear the Activity Stack*/
                );
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    private void feedBack(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private void feedBack(String message, boolean length_long) {
        if (!length_long) {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        } else feedBack(message);
    }
}