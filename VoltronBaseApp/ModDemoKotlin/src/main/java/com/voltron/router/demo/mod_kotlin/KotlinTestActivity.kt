package com.voltron.router.demo.mod_kotlin

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.voltron.router.annotation.Autowired
import com.voltron.router.annotation.EndPoint
import kotlinx.android.synthetic.main.activity_kotlin_test.*

@EndPoint(scheme = "voltron", host = "kotlin.com", path = "/test")
class KotlinTestActivity : AppCompatActivity() {

    @Autowired
    var testInt: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_kotlin_test)
        intParam.text = testInt.toString()
    }
}
