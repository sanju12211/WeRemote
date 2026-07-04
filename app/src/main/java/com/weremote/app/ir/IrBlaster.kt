package com.weremote.app.ir

import android.content.Context
import android.hardware.ConsumerIrManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log

/**
 * Thin wrapper over Android's [ConsumerIrManager] (the built-in IR blaster).
 * No extra hardware is required on phones that ship an IR emitter.
 */
class IrBlaster(context: Context) {

    private val appContext = context.applicationContext
    private val irManager =
        appContext.getSystemService(Context.CONSUMER_IR_SERVICE) as? ConsumerIrManager
    private val vibrator =
        appContext.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator

    val hasIrEmitter: Boolean
        get() = irManager?.hasIrEmitter() == true

    /** Transmit a prepared [IrSignal]. Returns false if no emitter is available. */
    fun transmit(signal: IrSignal): Boolean {
        val mgr = irManager ?: return false
        if (!mgr.hasIrEmitter()) return false
        return try {
            mgr.transmit(signal.frequency, signal.pattern)
            buzz()
            true
        } catch (t: Throwable) {
            Log.e("IrBlaster", "transmit failed", t)
            false
        }
    }

    private fun buzz() {
        val v = vibrator ?: return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v.vibrate(VibrationEffect.createOneShot(30, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                v.vibrate(30)
            }
        } catch (_: Throwable) { /* ignore */ }
    }
}
