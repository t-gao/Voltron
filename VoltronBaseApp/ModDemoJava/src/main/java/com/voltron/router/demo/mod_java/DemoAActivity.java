package com.voltron.router.demo.mod_java;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.voltron.router.annotation.EndPoint;

@EndPoint(group = "df", value = "/modulea/demoa")
public class DemoAActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo_a);

        findViewById(R.id.btn_test).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_test) {
        }
    }
}
