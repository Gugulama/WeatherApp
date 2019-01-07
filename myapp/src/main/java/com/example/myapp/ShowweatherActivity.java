package com.example.myapp;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.example.myapp.MainScreenActivity;

public class ShowweatherActivity extends AppCompatActivity implements OnClickListener
{
    Button btnBack;
    Button btnSave;
    TextView tvCity;
    TextView tvTemp;
    TextView tvWind;
    TextView tvPressure;
    TextView tvHumidity;
    DBHelper dbHelper;

    String myRequest;
    String CityName;
    String Temperature;
    String Country;
    String Wind;
    String Pressure;
    String Humidity;
    Double temp;
    String Date;
    String Activity;

    private final static String LOG_TAG = "mylog";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.showweather);
        btnBack = (Button) findViewById(R.id.btnBack);
        btnBack.setOnClickListener(this);
        btnSave = (Button) findViewById(R.id.btnSave);
        btnSave.setOnClickListener(this);

        tvCity = (TextView) findViewById(R.id.tvCity);
        tvTemp = (TextView) findViewById(R.id.tvTemp);
        tvWind = (TextView) findViewById(R.id.tvWind);
        tvPressure = (TextView) findViewById(R.id.tvPressure);
        tvHumidity = (TextView) findViewById(R.id.tvHumidity);

        dbHelper = new DBHelper(this);


        Intent intent = getIntent();
        //получение и подготовка данных
        myRequest = intent.getStringExtra("MyRequest");
        CityName = intent.getStringExtra("CityName");
        Temperature = intent.getStringExtra("Temperature");
        Country = intent.getStringExtra("Country");
        temp = intent.getDoubleExtra("temp", 0.0);
        Wind = intent.getStringExtra("Wind");
        Pressure = intent.getStringExtra("Pressure");
        Humidity = intent.getStringExtra("Humidity");
        Date = intent.getStringExtra("Date");
        Activity = intent.getStringExtra("Activity");

        if (Activity.equals("fromDB"))
        {
            btnSave.setVisibility(View.INVISIBLE);
            setTitle("Прогноз из БД");
        }
        else {
            try {
                Log.d(LOG_TAG, "Take country fullname");
                Country = getString(this.getResources().getIdentifier(Country, "string", this.getPackageName()));
            } catch (Exception e) {
                Log.d(LOG_TAG, "Can't find this country fullname");
            }
        }


        tvCity.setText(CityName + ", " + Country);
        tvTemp.setText(Temperature);
        tvWind.setText(Wind);
        tvPressure.setText(Pressure + " мм рт.ст.");
        tvHumidity.setText(Humidity + " %");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v)
    {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        DecimalFormat df = new DecimalFormat("#.#");
        df.setRoundingMode(RoundingMode.CEILING);
        switch(v.getId())
        {
            case R.id.btnBack:
                ShowweatherActivity.this.finish();
                db.close();
                Log.d(LOG_TAG, "showweather close");
                break;
            case R.id.btnSave:
                btnSave.setEnabled(false);
                Log.d(LOG_TAG, "--- Insert in MySQLTable: ---");
                cv.put("request", myRequest);
                cv.put("cityName", CityName);
                cv.put("countryName", Country);
                cv.put("temperature", df.format(temp));
                cv.put("wind", Wind);
                cv.put("pressure", Pressure);
                cv.put("humidity", Humidity);
                cv.put("time", Date);
                try {
                    long rowID = db.insert("MySQLTable", null, cv);
                    Log.d(LOG_TAG, "row inserted, ID = " + rowID + " City = " + CityName);
                    db.close();
                    Toast.makeText(this, "Запись добавлена", Toast.LENGTH_SHORT).show();
                }
                catch (Exception e)
                {
                    Toast.makeText(this, "Не удалось добавить запись", Toast.LENGTH_SHORT).show();
                    btnSave.setEnabled(true);
                    db.close();
                }

                //обновление записей с совпадающими городами
//                int Count;
//                try {
//                    Count = db.update("MySQLTable", cv, "request = ?", new String[] { myRequest });
//                    if (Count == 0)
//                    {
//                        long rowID = db.insert("MySQLTable", null, cv);
//                        Log.d(LOG_TAG, "row inserted, ID = " + rowID + " City = " + CityName);
//                        cv.clear();
//                        cv.put("cityName", CityName);
//                        cv.put("countryName", Country);
//                        cv.put("temperature", df.format(temp));
//                        cv.put("wind", Wind);
//                        cv.put("pressure", Pressure);
//                        cv.put("humidity", Humidity);
//                        cv.put("time", Date);
//                        db.update("MySQLTable", cv, "CityName = ?", new String[] { CityName });
//                    }
//                    else
//                    {
//                        cv.clear();
//                        cv.put("cityName", CityName);
//                        cv.put("countryName", Country);
//                        cv.put("temperature", df.format(temp));
//                        cv.put("wind", Wind);
//                        cv.put("pressure", Pressure);
//                        cv.put("humidity", Humidity);
//                        cv.put("time", Date);
//                        db.update("MySQLTable", cv, "CityName = ?", new String[] { CityName });
//                        Log.d(LOG_TAG, "Row with " + CityName + " updated");
//                    }
//                    db.close();
//                }
//                catch (Exception e)
//                { db.close(); }

        }
    }
}
