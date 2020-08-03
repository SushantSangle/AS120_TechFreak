package com.Techfreaks.SafeTrigger;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.Techfreaks.SafeTrigger.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class splash extends AppCompatActivity {

    private FirebaseUser firebaseUser;
    private Intent intent;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        //Getting the current Firebase user from FirebaseAuth service.
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        introDone();
        finish();
    }
    private void introDone(){
        //Handler object for delaying the execution.
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                //Checking if the user is logged in.
                if (firebaseUser != null) {

                    //If user logged in, then check the current complaint status, if any.
                    checkUserComplaintStatus();
                } else {
                    //If user is not logged in, then redirect to Login activity.
                    intent = new Intent(splash.this, Login.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    finish();
                    startActivity(intent);
                }

            }
        }, 500);  //Execution after .5 seconds.
    }
    //Method to check the user's current activity status.
    private void checkUserComplaintStatus() {
        intent = new Intent(splash.this, MainActivity.class);

        //Starting the assigned activity.
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        finish();
        startActivity(intent);
    }

}
