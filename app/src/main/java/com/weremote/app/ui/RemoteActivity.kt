package com.weremote.app.ui

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.weremote.app.data.Brand
import com.weremote.app.data.Fn
import com.weremote.app.data.IrDatabase
import com.weremote.app.ir.IrBlaster
import com.weremote.app.databinding.ActivityRemoteBinding

class RemoteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRemoteBinding
    private lateinit var blaster: IrBlaster
    private var brand: Brand? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRemoteBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.back.setOnClickListener { finish() }

        val typeId = intent.getStringExtra(EXTRA_TYPE) ?: "tv"
        val brandName = intent.getStringExtra(EXTRA_BRAND) ?: ""
        binding.title.text = intent.getStringExtra(EXTRA_NAME) ?: brandName

        blaster = IrBlaster(this)
        brand = IrDatabase.brand(typeId, brandName)

        if (!blaster.hasIrEmitter) {
            Toast.makeText(this, "No IR blaster on this phone", Toast.LENGTH_LONG).show()
        }

        wire(binding.btnPower, Fn.POWER)
        wire(binding.btnSource, Fn.SOURCE)
        wire(binding.btnMute, Fn.MUTE)
        wire(binding.btnMenu, Fn.MENU)
        wire(binding.btnHome, Fn.HOME)
        wire(binding.btnBackKey, Fn.BACK)
        wire(binding.btnExit, Fn.EXIT)
        wire(binding.btnInfo, Fn.INFO)
        wire(binding.btnVolUp, Fn.VOL_UP)
        wire(binding.btnVolDown, Fn.VOL_DOWN)
        wire(binding.btnChUp, Fn.CH_UP)
        wire(binding.btnChDown, Fn.CH_DOWN)
        wire(binding.btnUp, Fn.UP)
        wire(binding.btnDown, Fn.DOWN)
        wire(binding.btnLeft, Fn.LEFT)
        wire(binding.btnRight, Fn.RIGHT)
        wire(binding.btnOk, Fn.OK)
        wire(binding.btn0, Fn.D0)
        wire(binding.btn1, Fn.D1)
        wire(binding.btn2, Fn.D2)
        wire(binding.btn3, Fn.D3)
        wire(binding.btn4, Fn.D4)
        wire(binding.btn5, Fn.D5)
        wire(binding.btn6, Fn.D6)
        wire(binding.btn7, Fn.D7)
        wire(binding.btn8, Fn.D8)
        wire(binding.btn9, Fn.D9)
    }

    private fun wire(view: View, fn: String) {
        val code = brand?.code(fn)
        if (code == null) {
            view.isEnabled = false
            view.alpha = 0.35f
            return
        }
        view.setOnClickListener { blaster.transmit(code.toSignal()) }
    }

    companion object {
        const val EXTRA_TYPE = "type"
        const val EXTRA_BRAND = "brand"
        const val EXTRA_NAME = "name"
    }
}
