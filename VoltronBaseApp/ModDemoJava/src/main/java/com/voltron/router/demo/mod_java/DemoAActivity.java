package com.voltron.router.demo.mod_java;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.voltron.router.annotation.Autowired;
import com.voltron.router.annotation.EndPoint;

@EndPoint(value = "/modulea/demoa")
public class DemoAActivity extends AppCompatActivity implements View.OnClickListener {
    @Autowired
    public String EXT_HH;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_demo_a);

        findViewById(R.id.btn_test).setOnClickListener(this);

        TextView ext = findViewById(R.id.ext);
        ext.setText("string param : " + EXT_HH);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_test) {
            setResult(RESULT_OK);
            finish();
        }
    }
}
