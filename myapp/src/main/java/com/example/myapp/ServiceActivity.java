package com.example.myapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class ServiceActivity extends AppCompatActivity {

    private final static String LOG_TAG = "mylog";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service);
    }

    public void onClickStart(View v)
    {
        startService(new Intent(this, MyService.class));
    }

    public void onClickStop(View v)
    {
        stopService(new Intent(this, MyService.class));
    }
}
