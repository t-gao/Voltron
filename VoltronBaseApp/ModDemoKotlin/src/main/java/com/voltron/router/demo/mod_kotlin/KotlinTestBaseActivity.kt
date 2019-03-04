package com.voltron.router.demo.mod_kotlin

import android.os.Bundle
import android.util.Log

abstract class KotlinTestBaseActivity: BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("KotlinTestBaseActivity", "onCreate, " + this.javaClass)
    }

}