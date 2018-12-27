package com.voltron.demo.app

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.voltron.router.api.VRouter
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        btn_go.setOnClickListener {

            // SecondActivity
//            VRouter.go(this, "main", "/main/second")

            // DemoAActivity
            VRouter.go(this, "df", "/modulea/demoa")
        }
    }
}
