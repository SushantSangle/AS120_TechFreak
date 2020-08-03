//Activity for displaying the complaint completed status.

package com.techfreaks.safetrigger.coptracking;

//Importing all required libraries.

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class ComplaintCompleted extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_complaint_completed);
//
//        //Using Handler class for delaying the execution for redirecting to the Status activity.
//        Handler handler=new Handler();
//        handler.postDelayed(new Runnable() {
//            @Override
//            public void run() {

        //Redirecting to the Status activity.
        Intent intent = new Intent(ComplaintCompleted.this, Status.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
//        }, 5500);   //Delay of 5.5 seconds.
//    }

}
