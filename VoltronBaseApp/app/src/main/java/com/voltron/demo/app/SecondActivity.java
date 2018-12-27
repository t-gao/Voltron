package com.voltron.demo.app;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.voltron.router.annotation.EndPoint;


@EndPoint("/main/second")
public class SecondActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
    }
}
