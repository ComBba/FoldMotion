package com.foldmotion.app

import android.app.Application
import android.content.Context
import android.util.Log
import org.lsposed.hiddenapibypass.HiddenApiBypass

class FoldMotionApp : Application() {
    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        val ok = HiddenApiBypass.setHiddenApiExemptions("")
        Log.d("FoldMotion", "hidden api exemptions=$ok")
    }
}
