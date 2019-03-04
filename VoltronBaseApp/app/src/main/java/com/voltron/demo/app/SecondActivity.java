package com.voltron.demo.app;

import android.widget.TextView;

import com.voltron.demo.app.inject.TestParcelable;
import com.voltron.demo.app.inject.TestSerializable;
import com.voltron.router.annotation.Autowired;
import com.voltron.router.annotation.EndPoint;

@EndPoint("/main/second")
public class SecondActivity extends IntermediateBaseActivity {

    @Autowired(name = "test")
    public TestSerializable testSerializable; //取key优先级，指定的name优先级比较高，然后是变量名称

    @Autowired
    public TestParcelable testParcelable;

    @Autowired
    int testInt;

    @Override
    protected int layoutResId() {
        return R.layout.activity_second;
    }

    @Override
    protected void findView() {
        TextView serializableParam = findViewById(R.id.serializableParam);
        serializableParam.setText("serializable param :"+(testSerializable != null? testSerializable.toString(): "null"));

        TextView parcelableParam = findViewById(R.id.parcelableParam);
        parcelableParam.setText("parcelable param :"+(testParcelable != null? testParcelable.toString(): "null"));

        TextView inParamView = findViewById(R.id.intParam);
        inParamView.setText("int param: " + testInt);
    }
}
