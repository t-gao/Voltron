package com.voltron.demo.app

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*
import com.voltron.router.demo.mod_java.DemoAActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        btn_go.setOnClickListener {
//            startActivity(Intent(this@MainActivity, Main2Activity::class.java))
            startActivity(Intent(this@MainActivity, DemoAActivity::class.java))
        }
    }
}
