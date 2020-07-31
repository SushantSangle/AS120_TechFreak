package com.Techfreaks.SafeTrigger;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class CancelTrigger extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cancel_trigger);
    }
    public void inialize(Context mContext){
        Intent intent = new Intent(mContext,CancelTrigger.class);
        startActivity(intent);
    }
}