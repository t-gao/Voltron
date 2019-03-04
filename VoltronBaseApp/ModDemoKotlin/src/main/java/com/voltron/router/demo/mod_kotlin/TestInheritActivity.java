package com.voltron.router.demo.mod_kotlin;

import android.os.Bundle;
import android.widget.TextView;

import com.voltron.router.annotation.EndPoint;

import com.voltron.router.annotation.Autowired;

@EndPoint("testinherit")
public class TestInheritActivity extends BaseActivity {

    @Autowired
    public int testInt = 0;

    @Override
    protected int layoutResId() {
        return R.layout.activity_kotlin_test;
    }

    @Override
    protected void findView() {
        TextView intParamTv = findViewById(R.id.intParam);
        intParamTv.setText(String.valueOf(testInt));
    }

}
