package com.voltron.router.demo.mod_kotlin

import com.voltron.router.annotation.Autowired
import com.voltron.router.annotation.EndPoint
import kotlinx.android.synthetic.main.activity_kotlin_test.*

@EndPoint(scheme = "voltron", host = "kotlin.com", path = "/test")
class KotlinTestActivity : KotlinTestBaseActivity() {

    @Autowired
    @JvmField
    var testInt: Int = 0

    override fun layoutResId(): Int = R.layout.activity_kotlin_test

    override fun findView() {
        intParam.text = testInt.toString()
    }
}
