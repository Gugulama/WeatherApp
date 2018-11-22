package com.example.myapp;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainScreenActivity extends AppCompatActivity implements OnClickListener
{
    EditText etCity;
    Button btnFind;
    Button btnCheckDB;
    Button btnClear;
    RadioGroup rdgrCheckUnits;
    RadioButton rdbtnCelcius;
    RadioButton rdbtnKelvin;
    RadioButton rdbtnFahrenheit;
    TextView tvHello;
    DBHelper dbHelper;
    String myRequest;
    private final static String LOG_TAG = "mylog";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_screen);
        etCity = (EditText) findViewById(R.id.etServCity);

        btnFind = (Button) findViewById(R.id.btnFind);
        btnFind.setOnClickListener(this);

        btnClear = (Button) findViewById(R.id.btnClear);
        btnClear.setOnClickListener(this);

        btnCheckDB = (Button) findViewById(R.id.btnCheckDB);
        btnCheckDB.setOnClickListener(this);

        rdgrCheckUnits = (RadioGroup) findViewById(R.id.rdgrCheckUnits);

        rdbtnCelcius = (RadioButton) findViewById(R.id.rdbtnCelcius);
        rdbtnFahrenheit = (RadioButton) findViewById(R.id.rdbtnFahrenheit);
        rdbtnKelvin = (RadioButton) findViewById(R.id.rdbtnKelvin);

        rdbtnCelcius.setChecked(true);

        tvHello = (TextView) findViewById(R.id.tvHello);

        dbHelper = new DBHelper(this);
        Log.d(LOG_TAG, "Created");
    }
    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        Log.d(LOG_TAG, "Destroyed");
    }
    @Override
    public void onClick(View v)
    {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        switch (v.getId()) {
            case R.id.btnFind:
                if (etCity.getText().toString().equals("")) {
                    Toast.makeText(this, "Введите город", Toast.LENGTH_SHORT).show();
                    break;
                }
                else {
                    btnFind.setEnabled(false);
                    try {
                        Log.d(LOG_TAG, "Try to make request");
                        myRequest = etCity.getText().toString().toLowerCase();
                        RequestQueue queue = Volley.newRequestQueue(this);
                        String url = "http://api.openweathermap.org/data/2.5/weather?q=";
                        String APIKey = "&APPID=e400f5493d915484cc024f9f90143ae6";

                        String MyUrl = url + myRequest + APIKey;

                        JsonObjectRequest JsonRequest = new JsonObjectRequest(Request.Method.GET, MyUrl, null,
                                new Response.Listener<JSONObject>() {
                                    @Override
                                    public void onResponse(JSONObject response) {
                                        Log.d(LOG_TAG, "RESPONSE SUCCESS");
                                        btnFind.setEnabled(true);
                                        Response_Proc(response);
                                    }
                                },
                                new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        btnFind.setEnabled(true);
                                        Log.d(LOG_TAG, "checking MyDB");
                                        if(getResponseFromDB(myRequest))
                                        {
                                            Log.d(LOG_TAG, "RESPONSE SUCCESS");
                                        }
                                        else
                                        {
                                            CallErrorActivity(0, error.toString());
                                            Log.d(LOG_TAG, "ERROR ON RESPONSE");
                                        }
                                    }
                                });

                        queue.add(JsonRequest);
                        dbHelper.close();

                    } catch (Exception e) {
                        Log.d(LOG_TAG, "JSON GETTING ERROR " + e.getMessage());
                        dbHelper.close();
                    }
                }
                break;
            case R.id.btnCheckDB:
                Cursor c = db.query("MySQLTable", null, null, null, null, null, null);
                String item;
                ArrayList<String> list = new ArrayList<String>();
                if (c.moveToFirst())
                {
                    int idColIndex = c.getColumnIndex("id");
                    int requestColIndex = c.getColumnIndex("request");
                    int cityNameColIndex = c.getColumnIndex("cityName");
                    int countryNameColIndex = c.getColumnIndex("countryName");
                    int temperatureColIndex = c.getColumnIndex("temperature");
                    int windColIndex = c.getColumnIndex("wind");
                    int pressureColIndex = c.getColumnIndex("pressure");
                    int humidityColIndex = c.getColumnIndex("humidity");
                    int timeColIndex = c.getColumnIndex("time");
                    do {
                        item = c.getInt(idColIndex) +": "+ c.getString(cityNameColIndex)+" " +(c.getDouble(temperatureColIndex) - 273) +" "+ c.getString(timeColIndex);
                        list.add(item);

                        // получаем значения по номерам столбцов и пишем все в лог
//                        Log.d(LOG_TAG,
//                                "ID = " + c.getInt(idColIndex) +
//                                        ", request = " + c.getString(requestColIndex) +
//                                        ", cityName = " + c.getString(cityNameColIndex) +
//                                        ", countryName = " + c.getString(countryNameColIndex) +
//                                        ", temperature = " + c.getDouble(temperatureColIndex) +
//                                        ", wind = " + c.getString(windColIndex) +
//                                        ", pressure = " + c.getString(pressureColIndex) +
//                                        ", humidity = " + c.getString(humidityColIndex) +
//                                        ", time = " + c.getString(timeColIndex));
                    } while (c.moveToNext());

                    try{
                        Log.d(LOG_TAG, "Start DB_Activity");
                        Intent intent = new Intent(this, DatabaseActivity.class);
                        intent.putStringArrayListExtra("MyList", list);
                        startActivity(intent);
                    }
                    catch (Exception e)
                    {
                        Log.d(LOG_TAG, "fek");
                        Log.d(LOG_TAG, e.getMessage());
                    }
                }
                else
                {
                    list.add("DATABASE IS EMPTY");
                    try
                    {
                        Log.d(LOG_TAG, "try to start DB_Activity");
                        Intent intent = new Intent(this, DatabaseActivity.class);
                        intent.putStringArrayListExtra("MyList", list);
                        startActivity(intent);
                    } catch (Exception e) {
                        Log.d(LOG_TAG, "fek");
                        Log.d(LOG_TAG, e.getMessage());
                    }
                    Log.d(LOG_TAG, "0 rows");
                }
                c.close();
                break;
            case R.id.btnClear:
                Log.d(LOG_TAG, "--- Clear MySQLTable: ---");
                // удаляем все записи
                int clearCount = db.delete("MySQLTable", null, null);
                Log.d(LOG_TAG, "deleted rows count = " + clearCount);
                Toast.makeText(this, "deleted " + clearCount + " rows", Toast.LENGTH_SHORT).show();
                this.deleteDatabase("myDB");
                break;
        }
    }

    private Boolean getResponseFromDB(String request)
    {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor c = db.query("MySQLTable", null, null, null, null, null, null);
        request = request.toLowerCase();
        if (c.moveToFirst())
        {
            int idColIndex = c.getColumnIndex("id");
            int requestColIndex = c.getColumnIndex("request");
            int cityNameColIndex = c.getColumnIndex("cityName");
            int countryNameColIndex = c.getColumnIndex("countryName");
            int temperatureColIndex = c.getColumnIndex("temperature");
            int windColIndex = c.getColumnIndex("wind");
            int pressureColIndex = c.getColumnIndex("pressure");
            int humidityColIndex = c.getColumnIndex("humidity");
            int timeColIndex = c.getColumnIndex("time");
            do {
                if(request.equals(c.getString(requestColIndex)))
                {
                    DecimalFormat df = new DecimalFormat("#.#");
                    String Temperature;
                    Double temp = c.getDouble(temperatureColIndex);
                    switch (rdgrCheckUnits.getCheckedRadioButtonId())
                    {
                        case R.id.rdbtnCelcius:
                            Temperature = df.format(temp - 273.15) + " C";
                            break;
                        case R.id.rdbtnKelvin:
                            Temperature = df.format(temp) + " K";
                            break;
                        case R.id.rdbtnFahrenheit:
                            Temperature = df.format(temp * 1.8 - 459.67) + " F";
                            break;
                        default:
                            Temperature = df.format(temp) + " K";
                            break;
                    }
                    Intent intent = new Intent(this, ShowweatherActivity.class);
                    intent.putExtra("CityName", c.getString(cityNameColIndex));
                    intent.putExtra("Temperature", Temperature);
                    intent.putExtra("Country", c.getString(countryNameColIndex));
                    intent.putExtra("Pressure", c.getString(pressureColIndex));
                    intent.putExtra("Humidity", c.getString(humidityColIndex));
                    intent.putExtra("Wind", c.getString(windColIndex));
                    intent.putExtra("Activity", "fromDB");
                    startActivity(intent);
                    c.close();
                    Toast.makeText(this, "Weather from DB", Toast.LENGTH_SHORT).show();
                    return true;
                }
            }
            while (c.moveToNext());
            return false;
        }
        else
            return false;
    }

    private void Response_Proc(JSONObject json)
    {
        ContentValues cv = new ContentValues();
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try {
            JSONObject main = json.getJSONObject("main");
            JSONObject sys = json.getJSONObject("sys");
            JSONObject wind = json.getJSONObject("wind");
            DecimalFormat df = new DecimalFormat("#");
            df.setRoundingMode(RoundingMode.CEILING);
            Double degree = wind.getDouble("deg");
            Double windSpeed = wind.getDouble("speed");
            String windDirection = defineDir(degree);
            String Wind = windSpeed.toString() + " м/с, " + windDirection;
            String Temperature;
            Double temp = main.getDouble("temp");
            switch (rdgrCheckUnits.getCheckedRadioButtonId())
            {
                case R.id.rdbtnCelcius:
                    Temperature = df.format(temp - 273.15) + " C";
                    break;
                case R.id.rdbtnKelvin:
                    Temperature = df.format(temp) + " K";
                    break;
                case R.id.rdbtnFahrenheit:
                    Temperature = df.format(temp * 1.8 - 459.67) + " F";
                    break;
                default:
                    Temperature = df.format(temp) + " K";
                    break;
            }
            String Pressure = df.format(main.getDouble("pressure")/1.333); //давление
            String Humidity = main.getString("humidity");
            String CityName = json.getString("name"); //город
            String Country = sys.getString("country"); //страна
            DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            Date date = new Date();
            String Date = dateFormat.format(date).toString();

            //вывоз SHOWACTIVITY
            Intent intent = new Intent(this, ShowweatherActivity.class);
            intent.putExtra("MyRequest", myRequest);
            intent.putExtra("CityName", CityName);
            intent.putExtra("Temperature", Temperature);
            intent.putExtra("temp", temp);
            intent.putExtra("Country", Country);
            intent.putExtra("Pressure", Pressure);
            intent.putExtra("Humidity", Humidity);
            intent.putExtra("Wind", Wind);
            intent.putExtra("Date", Date);
            intent.putExtra("Activity", "fromInternet");
            Toast.makeText(this, "Weather from Internet", Toast.LENGTH_SHORT).show();
            startActivity(intent);
        }
        catch (Exception e)
        {
            CallErrorActivity(1, "");
            Log.d(LOG_TAG, "JSON PARSING ERROR " + e.getMessage());
        }

    }

    private String defineDir(double degree)
    {
        String dir = "C";
        if(degree < 22.5 && degree > 337.5) dir = "С";
        else if(degree < 67.5) dir = "СВ";
        else if(degree < 112.5) dir = "В";
        else if(degree < 157.5) dir = "ЮВ";
        else if(degree < 202.5) dir = "Ю";
        else if(degree < 247.5) dir = "ЮЗ";
        else if(degree < 292.5) dir = "З";
        else if(degree < 337.5) dir = "СЗ";
        return dir;
    }

    private void CallErrorActivity(int a, String text_error)
    {
        Pattern p1 = Pattern.compile("NoConnectionError");
        Pattern p2 = Pattern.compile("ClientError");
        Matcher m1 = p1.matcher(text_error);
        Matcher m2 = p2.matcher(text_error);
        Intent intent = new Intent(getBaseContext(), ErrorActivity.class);
        switch(a)
        {
            case 0:
                intent.putExtra("Error", "Ошибка при получении JSON");
                if(m1.find())
                {
                    intent.putExtra("Text_Error", "Нет интернета :(\n\tВ БД тож ниче нет");
                }
                else
                {
                    if(m2.find())
                        intent.putExtra("Text_Error", "Не удалось найти город :(");
                    else
                        intent.putExtra("Text_Error", text_error);
                }
                break;
            case 1:
                intent.putExtra("Error", "Ошибка при обработке JSON");
                intent.putExtra("Text_Error", "");
                break;
            default:
                intent.putExtra("Error", "Неизвестная ошибка");
                intent.putExtra("Text_Error", "");
                break;
        }
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        menu.add("ServiceSettings");
        menu.add("About");
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getTitle().toString())
        {
            case "About":
                Intent intent = new Intent(this, AboutActivity.class);
                Toast.makeText(this, item.getTitle(), Toast.LENGTH_SHORT).show();
                startActivity(intent);
                break;
            case "ServiceSettings":
                intent = new Intent(this, ServiceActivity.class);
                Toast.makeText(this, item.getTitle(), Toast.LENGTH_SHORT).show();
                startActivity(intent);
                break;
            default:
                Toast.makeText(this, item.getTitle(), Toast.LENGTH_SHORT).show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
