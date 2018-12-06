package com.example.myapp;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
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
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class MyService extends Service {

    private final static String LOG_TAG = "mylog";
    DBHelper dbHelper;
    MyThread mythread;
    String request;
    int notification_id = 0;

    public void onCreate() {
        super.onCreate();
        Log.d(LOG_TAG, "Service - onCreate");
    }
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(LOG_TAG, "onStartCommand");
        dbHelper = new DBHelper(this);
        request = intent.getStringExtra("City");
        mythread = new MyThread();
        mythread.start();
        Log.d(LOG_TAG, "Service Thread started");
        return super.onStartCommand(intent, flags, startId);
    }

    public void onDestroy() {
        mythread.kill();
        super.onDestroy();
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
        notification_id = 0;
        Log.d(LOG_TAG, "Service - onDestroy");
    }

    public IBinder onBind(Intent intent) {
        Log.d(LOG_TAG, "Service - onBind");
        return null;
    }

    private void Response_Proc(JSONObject json)
    {
        ContentValues cv = new ContentValues();
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try {
            JSONObject main = json.getJSONObject("main");
            JSONObject sys = json.getJSONObject("sys");
            JSONObject wind = json.getJSONObject("wind");
            DecimalFormat df = new DecimalFormat("#.#");
            df.setRoundingMode(RoundingMode.CEILING);
            Double degree = wind.getDouble("deg");
            Double windSpeed = wind.getDouble("speed");
            String windDirection = defineDir(degree);
            String Wind = windSpeed.toString() + " м/с, " + windDirection;
            Double temp = main.getDouble("temp");
            String Pressure = df.format(main.getDouble("pressure")/1.333);
            String Humidity = main.getString("humidity");
            String CityName = json.getString("name"); //город
            String Country = sys.getString("country"); //страна
            DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            Date date = new Date();
            String Date = dateFormat.format(date).toString();

            cv.put("request", request);
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

                NotificationCompat.Builder builder =
                        new NotificationCompat.Builder(this)
                                .setSmallIcon(R.mipmap.ic_launcher)
                                .setContentTitle(notification_id + ": Weather in " + CityName)
                                .setContentText("Tempterature: " + df.format(temp) + " K");

                Notification notification = builder.build();

                NotificationManager notificationManager =
                        (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                notificationManager.notify(notification_id, notification);
                notification_id++;

                db.close();
                Toast.makeText(this, "Entry from service added", Toast.LENGTH_SHORT).show();
            }
            catch (Exception e)
            {
                Toast.makeText(this, "Can't add entry from service", Toast.LENGTH_SHORT).show();
                db.close();
            }
        }
        catch (Exception e)
        {
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

    class MyThread extends Thread
    {
        boolean alive = true;
        @Override
        public void run()
        {
            while (true)
            {
                if(alive)
                {
                    Log.d(LOG_TAG, "Service Thread working....");
                    SQLiteDatabase db = dbHelper.getWritableDatabase();
                    ContentValues cv = new ContentValues();
                    DecimalFormat df = new DecimalFormat("#.#");
                    df.setRoundingMode(RoundingMode.CEILING);
                    try
                    {
                        Log.d(LOG_TAG, "Service - Try to make request");
                        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
                        String url = "http://api.openweathermap.org/data/2.5/weather?q=";
                        String APIKey = "&APPID=e400f5493d915484cc024f9f90143ae6";

                        String MyUrl = url + request + APIKey;

                        JsonObjectRequest JsonRequest = new JsonObjectRequest(Request.Method.GET, MyUrl, null,
                                new Response.Listener<JSONObject>() {
                                    @Override
                                    public void onResponse(JSONObject response) {
                                        Log.d(LOG_TAG, "Service - RESPONSE SUCCESS");
                                        Response_Proc(response);
                                    }
                                },
                                new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        Log.d(LOG_TAG, "Service - ERROR ON RESPONSE");
                                    }
                                });

                        queue.add(JsonRequest);
                        dbHelper.close();

                    } catch (Exception e) {
                        Log.d(LOG_TAG, "JSON GETTING ERROR " + e.getMessage());
                        dbHelper.close();
                    }
                    try {
                        TimeUnit.SECONDS.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                else
                {
                    Log.d(LOG_TAG, "Service Thread killed");
                    return;
                }
            }
        }

        public void kill()
        {
            alive = false;
        }

    }
}
