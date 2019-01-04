package com.voltron.demo.app;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.voltron.demo.app.inject.TestParcelable;
import com.voltron.demo.app.inject.TestSerializable;
import com.voltron.router.annotation.Autowired;
import com.voltron.router.annotation.EndPoint;
import com.voltron.router.api.VRouter;


@EndPoint("/main/second")
public class SecondActivity extends AppCompatActivity {

    @Autowired(name = "test")
    public TestSerializable testSerializable ; //取key优先级，指定的name优先级比较高，然后是变量名称

    @Autowired
    public TestParcelable testParcelable ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        VRouter.inject(this); //TODO 目前只支持手动注入
        setContentView(R.layout.activity_second);

        TextView serializableParam = findViewById(R.id.serializableParam) ;
        serializableParam.setText("serializable param :"+(testSerializable != null? testSerializable.toString(): "null"));

        TextView parcelableParam = findViewById(R.id.parcelableParam) ;
        parcelableParam.setText("parcelable param :"+(testParcelable != null? testParcelable.toString(): "null"));
    }
}
