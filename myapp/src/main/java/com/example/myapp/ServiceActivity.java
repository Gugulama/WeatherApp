package com.example.myapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

public class ServiceActivity extends AppCompatActivity
{
    Button btnStart;
    Button btnStop;
    ProgressBar prbar;
    EditText etServCity;

    private final static String LOG_TAG = "mylog";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service);

        btnStart = (Button) findViewById(R.id.btnStart);
        btnStop = (Button) findViewById(R.id.btnStop);
        prbar = (ProgressBar) findViewById(R.id.prbar);
        etServCity = (EditText) findViewById(R.id.etServCity);
    }

    public void onClickStart(View v)
    {
        if(!etServCity.getText().toString().equals(""))
        {
            startService(new Intent(this, MyService.class).putExtra("City", etServCity.getText().toString()));
            prbar.setVisibility(View.VISIBLE);
            btnStart.setEnabled(false);
        }
        else Toast.makeText(this, "Empty ET", Toast.LENGTH_SHORT).show();
    }

    public void onClickStop(View v)
    {
        stopService(new Intent(this, MyService.class));
        prbar.setVisibility(View.INVISIBLE);
        btnStop.setEnabled(true);
    }
}
