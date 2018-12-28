package com.voltron.demo.app

import android.app.Activity
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
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
            VRouter.with(this)
                    .group("abc")
                    .path("/modulea/demoa")
                    .stringExtra("EXT_HH", "EXT-----VALUE")
                    .forResult(100)
                    .go()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100 && resultCode == Activity.RESULT_OK) {
            Toast.makeText(this, "onActivityResult", Toast.LENGTH_LONG).show()
        }
    }
}
