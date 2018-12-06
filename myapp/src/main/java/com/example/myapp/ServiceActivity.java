package com.example.myapp;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
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
    SharedPreferences sPref;
    final String SAVED_City = "saved_city";
    final String SAVED_btnStartState = "saved_btnstate";
    final int ENABLED = 1;
    final int DISABLED = 0;

    private final static String LOG_TAG = "mylog";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service);

        btnStart = (Button) findViewById(R.id.btnStart);
        btnStop = (Button) findViewById(R.id.btnStop);
        prbar = (ProgressBar) findViewById(R.id.prbar);
        etServCity = (EditText) findViewById(R.id.etServCity);
        if(isMyServiceRunning(MyService.class))
        {
            loadCityName();
            prbar.setVisibility(View.VISIBLE);
        }
    }
    public void onClickStart(View v)
    {
        //скрыть клавиатуру
        v = getCurrentFocus();
        if (v instanceof EditText) {
            v.clearFocus();
        InputMethodManager imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

        }
        if(!etServCity.getText().toString().equals(""))
        {
            startService(new Intent(this, MyService.class).putExtra("City", etServCity.getText().toString()));
            prbar.setVisibility(View.VISIBLE);
            btnStart.setEnabled(false);
            saveCityName();
        }
        else Toast.makeText(this, "Empty ET", Toast.LENGTH_SHORT).show();
    }

    public void onClickStop(View v)
    {
        stopService(new Intent(this, MyService.class));
        prbar.setVisibility(View.INVISIBLE);
        btnStart.setEnabled(true);
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    void saveCityName() {
        sPref = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor ed = sPref.edit();
        ed.putString(SAVED_City, etServCity.getText().toString());
        if(btnStart.isEnabled()) ed.putInt(SAVED_btnStartState, ENABLED);
        else ed.putInt(SAVED_btnStartState, DISABLED);
        ed.commit();
    }

    void loadCityName() {
        sPref = getPreferences(MODE_PRIVATE);
        String savedText = sPref.getString(SAVED_City, "");
        int btnstate = sPref.getInt(SAVED_btnStartState, 1);
        if(btnstate == ENABLED) btnStart.setEnabled(true);
        else btnStart.setEnabled(false);
        etServCity.setText(savedText);
    }
}
