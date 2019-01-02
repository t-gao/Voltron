package com.voltron.router.demo.mod_java;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.voltron.router.annotation.EndPoint;

@EndPoint("voltron://demo.com/scheme")
public class DemoSchemeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo_scheme);
    }
}
