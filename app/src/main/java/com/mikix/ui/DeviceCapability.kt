package com.mikix.ui

import android.app.ActivityManager
import android.content.Context

object DeviceCapability {
    fun supportsRealtime3D(context: Context): Boolean {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryClass = am.memoryClass
        val lowRam = am.isLowRamDevice
        return !lowRam && memoryClass >= 192
    }
}
