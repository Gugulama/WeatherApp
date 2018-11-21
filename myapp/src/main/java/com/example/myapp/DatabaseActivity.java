package com.example.myapp;


import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import java.util.ArrayList;

public class DatabaseActivity extends AppCompatActivity
{

    ListView lvMain;
    private final static String LOG_TAG = "mylog";
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_database);

        lvMain = (ListView) findViewById(R.id.lvMain);
        Intent intent = getIntent();
        ArrayList<String> list = intent.getStringArrayListExtra("MyList");

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, list);
        lvMain.setAdapter(adapter);
    }
}


