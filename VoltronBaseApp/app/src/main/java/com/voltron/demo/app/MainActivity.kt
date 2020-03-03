package com.voltron.demo.app

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import com.voltron.demo.app.inject.TestParcelable
import com.voltron.demo.app.inject.TestSerializable
import com.voltron.demo.app.utils.UrlUtil
import com.voltron.router.api.NavCallback
import com.voltron.router.api.VRouter
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btn_go.setOnClickListener {
            VRouter.with(this)
                    .path("/modulea/demoa")
                    .stringExtra("extParam", "EXT-----VALUE")
                    .forResult(100)
                    .go()
        }

        btn_go_within_module.setOnClickListener {
            VRouter.with(this)
                    .path("/main/second")
                    .serializableExtra("test", TestSerializable("Donald Duck", 100))
                    .parcelableExtra("testParcelable", TestParcelable("Mickey Mouse", 101))
                    .intExtra("testInt", 99)
                    .go()
        }

        btn_go_within_module_kotlin.setOnClickListener {
            VRouter.with(this)
                    .path("/main/third")
                    .serializableExtra("test", TestSerializable("Tom", 100))
                    .parcelableExtra("testParcelable", TestParcelable("Jerry", 101))
                    .intExtra("testInt", 99)
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
                    .intExtra("testInt", 99)
                    .go()
        }

        btn_test_path_only.setOnClickListener {
            VRouter.with(this)
                    .path("/pathonly")
                    .go()
        }

        btn_go_to_frag_container.setOnClickListener {
            startActivity(Intent(this@MainActivity, FragContainerActivity::class.java))
        }

        btn_deeplink_go_activity.setOnClickListener {
            VRouter.with(this)
                    .route("testscheme://m.test.com/modulea/demoa?extParam=json")
                    .addInterceptor {
                        it.postcard().route?.apply {
                            if (this.startsWith("testscheme://")) {
                                val builder = VRouter.with(this@MainActivity)
                                        .route("/main/dispatch")
                                val data = Bundle()
                                if (UrlUtil.checkIsLegalDeepLinkPath(this)) {
                                    //解析需要跳转的参数
                                    data.putString("deeplink", UrlUtil.getRouterPath(this))
                                    UrlUtil.URLRequest(this)?.let {
                                        data.putSerializable("params", it)
                                    }
                                } else {
                                    data.putString("deeplink", "")
                                }
                                builder.setExtra(data)
                                builder.go()
                            }
                        }
                    }
                    .go()
        }

        btn_deeplink_go_webview.setOnClickListener {
            VRouter.with(this@MainActivity)
                    .route("https://www.baidu.com")
                    .addInterceptor {
                        it.postcard().route?.apply {
                            if (this.startsWith("http://") || this.startsWith("https://")) {
                                VRouter.with(this@MainActivity)
                                        .route("/main/webview")
                                        .stringExtra("url", this)
                                        .go()
                            }
                        }
                    }
                    .go()
        }

        btn_navurl.setOnClickListener {
            VRouter.with(this)
                    .route("/main/webview")
                    .stringExtra("url", "file:///android_asset/scheme-test.html")
                    .go()
        }

        btn_start_multiple.setOnClickListener {
            VRouter.startActivities(this,
                    VRouter.with(this).route("/main/second").build(),
                    VRouter.with(this).route("/main/third").build()
            )
        }

        btn_start_service.setOnClickListener {
            VRouter.with(this)
                    .scheme("voltron")
                    .host("kotlin.com")
                    .path("/test/service")
                    .go()
        }

        btn_test_interceptor.setOnClickListener {
            VRouter.with(this)
                    .path("/main/second")
                    .serializableExtra("test", TestSerializable("Donald Duck", 100))
                    .parcelableExtra("testParcelable", TestParcelable("Mickey Mouse", 101))
                    .intExtra("testInt", 99)
                    .addInterceptor {
                        Log.d("MainActivity", "interceptor 1")
                        it.proceed()
                    }
                    .addInterceptor {
                        Log.d("MainActivity", "interceptor 2")
                        it.proceed()
                    }
                    .addInterceptor {
                        it.postcard().mutate()
                                .serializableExtra("test", TestSerializable("Donald Trump", 200))
                                .parcelableExtra("testParcelable", TestParcelable("I'm gonna build a Great Wall!", 201))

                        it.proceed()
                    }
                    .go()
        }

        btn_test_callback.setOnClickListener {
            VRouter.with(this)
//                    .path("/xxx/i-route-to-nothing")
                    .path("/modulea/demoa")
                    .stringExtra("extParam", "EXT-----VALUE")
                    .forResult(100)
                    .callback(object : NavCallback {
                        override fun onPending() {
                            Log.d("MainActivity", "NavCallback onPending")
                        }

                        override fun onCancelled(cancelCode: Int, cancelMessage: CharSequence?) {
                            Log.d("MainActivity", "NavCallback onCancelled, cancelCode:$cancelCode, cancelMessage: ${cancelMessage?.toString()}")
                        }

                        override fun onIntercepted() {
                            Log.d("MainActivity", "NavCallback onIntercepted")
                        }

                        override fun onError(e: Throwable?) {
                            Log.e("MainActivity", "NavCallback onError", e)
                        }

                        override fun onNotFound() {
                            Log.d("MainActivity", "NavCallback onNotFound")
                        }

                        override fun onNavigated() {
                            Log.d("MainActivity", "NavCallback onNavigated")
                        }

                    })
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
