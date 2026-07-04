package com.weremote.app.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.weremote.app.data.Brand
import com.weremote.app.data.Fn
import com.weremote.app.data.IrDatabase
import com.weremote.app.data.RemoteStore
import com.weremote.app.data.SavedRemote
import com.weremote.app.ir.IrBlaster
import com.weremote.app.databinding.ActivityTvMatchBinding

/** Cycles through every TV codeset so an unknown TV can be matched by testing. */
class TvMatchActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTvMatchBinding
    private lateinit var blaster: IrBlaster
    private lateinit var candidates: List<Brand>
    private var index = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTvMatchBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.back.setOnClickListener { finish() }

        candidates = IrDatabase.tvCodesets()
        blaster = IrBlaster(this)

        binding.btnPrev.setOnClickListener { move(-1) }
        binding.btnNext.setOnClickListener { move(1) }
        binding.btnPower.setOnClickListener { testPower() }
        binding.btnUse.setOnClickListener { useCurrent() }

        render()
    }

    private fun move(delta: Int) {
        index = (index + delta + candidates.size) % candidates.size
        render()
    }

    private fun render() {
        val b = candidates[index]
        binding.tvCount.text = "Codeset ${index + 1} / ${candidates.size}"
        binding.tvLabel.text = "Power (${b.name})"
    }

    private fun testPower() {
        val code = candidates[index].code(Fn.POWER)
        if (code == null) {
            Toast.makeText(this, "No Power code", Toast.LENGTH_SHORT).show()
            return
        }
        if (!blaster.transmit(code.toSignal()))
            Toast.makeText(this, "Could not transmit IR", Toast.LENGTH_SHORT).show()
    }

    private fun useCurrent() {
        val brand = candidates[index]
        val store = RemoteStore(this)
        val name = "${brand.name} TV"
        store.add(
            SavedRemote(
                id = System.currentTimeMillis().toString(),
                name = name,
                typeId = "tv",
                brandName = brand.name
            )
        )
        startActivity(
            Intent(this, RemoteActivity::class.java)
                .putExtra(RemoteActivity.EXTRA_TYPE, "tv")
                .putExtra(RemoteActivity.EXTRA_BRAND, brand.name)
                .putExtra(RemoteActivity.EXTRA_NAME, name)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        )
        finish()
    }
}
