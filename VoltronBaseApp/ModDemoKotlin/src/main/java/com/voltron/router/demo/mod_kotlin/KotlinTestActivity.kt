package com.voltron.router.demo.mod_kotlin

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.voltron.router.annotation.EndPoint

@EndPoint(scheme = "voltron", host = "kotlin.com", path = "/test")
class KotlinTestActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_kotlin_test)
    }
}
