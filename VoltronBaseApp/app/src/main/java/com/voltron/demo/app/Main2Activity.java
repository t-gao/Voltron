package com.voltron.demo.app;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.voltron.router.annotation.EndPoint;

import com.voltron.router.api.VRouter;


@EndPoint("/main/main2")
public class Main2Activity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        VRouter.init(this);
    }
}
