package com.weremote.app.ui

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.weremote.app.data.AcProto
import com.weremote.app.ir.AcEngine
import com.weremote.app.ir.AcState
import com.weremote.app.ir.IrBlaster
import com.weremote.app.databinding.ActivityAcRemoteBinding

class AcRemoteActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAcRemoteBinding
    private lateinit var blaster: IrBlaster
    private val state = AcState()
    private var proto = AcProto.GREE

    private val modeNames = arrayOf("Auto", "Cool", "Dry", "Fan", "Heat")
    private val fanNames = arrayOf("Auto", "Low", "Med", "High")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAcRemoteBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.back.setOnClickListener { finish() }

        binding.title.text = intent.getStringExtra(EXTRA_NAME) ?: "Air Conditioner"
        proto = runCatching {
            AcProto.valueOf(intent.getStringExtra(EXTRA_PROTO) ?: "GREE")
        }.getOrDefault(AcProto.GREE)
        blaster = IrBlaster(this)
        if (!blaster.hasIrEmitter) {
            Toast.makeText(this, "No IR blaster on this phone", Toast.LENGTH_LONG).show()
        }

        binding.btnPower.setOnClickListener { state.power = !state.power; send() }
        binding.btnTempUp.setOnClickListener { state.temp++; send() }
        binding.btnTempDown.setOnClickListener { state.temp--; send() }
        binding.btnMode.setOnClickListener { state.mode = (state.mode + 1) % 5; send() }
        binding.btnFan.setOnClickListener { state.fan = (state.fan + 1) % 4; send() }
        binding.btnSwing.setOnClickListener { state.swing = !state.swing; send() }

        render()
    }

    private fun send() {
        state.clampTemp()
        render()
        blaster.transmit(AcEngine.signal(proto, state))
    }

    private fun render() {
        state.clampTemp()
        binding.tvTemp.text = "${state.temp}°C"
        binding.tvMode.text = "Mode: ${modeNames[state.mode]}"
        binding.tvFan.text = "Fan: ${fanNames[state.fan]}"
        binding.tvSwing.text = if (state.swing) "Swing: On" else "Swing: Off"
        binding.tvPower.text = if (state.power) "ON" else "OFF"
        binding.tvPower.alpha = if (state.power) 1f else 0.4f
    }

    companion object {
        const val EXTRA_NAME = "name"
        const val EXTRA_PROTO = "proto"
    }
}
