package com.voltron.router.demo.mod_java;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import com.voltron.router.annotation.EndPoint;

@EndPoint(scheme = "voltronTest", host = "test.net", path = "/scheme_host_path")
public class DemoSchemeHostPathActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo_scheme_host_path);
    }
}
