package com.example.myapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class ErrorActivity extends AppCompatActivity implements View.OnClickListener {

    Button btnErrBack;
    TextView tvError, tvTextError;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_error);
        btnErrBack = (Button) findViewById(R.id.btnErrBack);
        btnErrBack.setOnClickListener(this);

        tvError = (TextView) findViewById(R.id.tvError);
        tvTextError = (TextView) findViewById(R.id.tvTextError);

        Intent intent = getIntent();
        String Error = intent.getStringExtra("Error");
        String Text_Error = intent.getStringExtra("Text_Error");
        tvError.setText(Error);
        tvTextError.setText(Text_Error);

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
        switch(v.getId())
        {
            case R.id.btnErrBack:
                ErrorActivity.this.finish();
                break;
        }
    }
}
