package com.voltron.demo.app

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import com.voltron.demo.app.inject.TestParcelable
import com.voltron.demo.app.inject.TestSerializable
import com.voltron.router.api.VRouter
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btn_go.setOnClickListener {
            VRouter.with(this)
                    .path("/modulea/demoa")
                    .stringExtra("EXT_HH", "EXT-----VALUE")
                    .forResult(100)
                    .go()
        }

        btn_go_within_module.setOnClickListener {
            VRouter.with(this)
                    .path("/main/second")
                    .serializableExtra("test", TestSerializable("Tom" , 100) )
                    .parcelableExtra("testParcelable", TestParcelable("Jerry" , 101))
                    .go()
        }

        btn_scheme.setOnClickListener {
            VRouter.with(this)
                    .route("voltron://demo.com/scheme")
                    .go()
        }

        btn_scheme_host_path.setOnClickListener {
            VRouter.with(this)
                    .scheme("voltronTest")
                    .host("test.net")
                    .path("/scheme_host_path")
                    .go()
        }

        btn_go_to_kotlin.setOnClickListener {
            VRouter.with(this)
                    .scheme("voltron")
                    .host("kotlin.com")
                    .path("/test")
                    .go()
        }

        btn_test_path_only.setOnClickListener {
            VRouter.with(this)
                    .path("/pathonly")
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
