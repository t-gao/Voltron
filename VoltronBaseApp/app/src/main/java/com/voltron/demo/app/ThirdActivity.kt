package com.voltron.demo.app

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.voltron.demo.app.inject.TestParcelable
import com.voltron.demo.app.inject.TestSerializable
import com.voltron.router.annotation.Autowired
import com.voltron.router.annotation.EndPoint
import kotlinx.android.synthetic.main.activity_third.*

@EndPoint("/main/third")
class ThirdActivity : AppCompatActivity() {

    @Autowired(name = "test")
    var testSerializable: TestSerializable? = null //取key优先级，指定的name优先级比较高，然后是变量名称

    @Autowired
    var testParcelable: TestParcelable? = null

    @Autowired
    @JvmField
    var testInt: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_third)

        serializableParam.text = "serializable param: " + if (testSerializable != null) testSerializable.toString() else "null"

        parcelableParam.text = "parcelable param: " + if (testParcelable != null) testParcelable.toString() else "null"

        intParam.text = "int param: $testInt"
    }
}
