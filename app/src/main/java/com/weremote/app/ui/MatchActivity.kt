package com.weremote.app.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.weremote.app.data.Fn
import com.weremote.app.data.IrDatabase
import com.weremote.app.data.RemoteStore
import com.weremote.app.data.SavedRemote
import com.weremote.app.ir.AcState
import com.weremote.app.ir.GreeAc
import com.weremote.app.ir.IrBlaster
import com.weremote.app.databinding.ActivityMatchBinding

class MatchActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMatchBinding
    private lateinit var blaster: IrBlaster
    private lateinit var typeId: String
    private lateinit var brandName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMatchBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.back.setOnClickListener { finish() }

        typeId = intent.getStringExtra(BrandActivity.EXTRA_TYPE) ?: "tv"
        brandName = intent.getStringExtra(BrandActivity.EXTRA_BRAND) ?: ""
        blaster = IrBlaster(this)

        if (!blaster.hasIrEmitter) {
            binding.hint.text = "This phone has no IR blaster, so signals can't be sent. " +
                "Install WeRemote on a phone with an IR emitter."
        }

        binding.testButton.setOnClickListener { sendPower() }
        binding.btnYes.setOnClickListener { saveAndOpen() }
        binding.btnNo.setOnClickListener {
            Toast.makeText(
                this,
                "That's the only codeset we have for $brandName right now.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun sendPower() {
        val brand = IrDatabase.brand(typeId, brandName)
        val ok = if (brand?.isAc == true) {
            blaster.transmit(GreeAc.signal(AcState(power = true)))
        } else {
            val code = brand?.code(Fn.POWER)
            if (code == null) {
                Toast.makeText(this, "No POWER code for $brandName", Toast.LENGTH_SHORT).show()
                return
            }
            blaster.transmit(code.toSignal())
        }
        binding.testHint.visibility = View.VISIBLE
        if (!ok) Toast.makeText(this, "Could not transmit IR", Toast.LENGTH_SHORT).show()
    }

    private fun saveAndOpen() {
        val store = RemoteStore(this)
        val typeName = IrDatabase.type(typeId)?.displayName ?: "Remote"
        val name = "$brandName $typeName"
        val id = System.currentTimeMillis().toString()
        store.add(SavedRemote(id, name, typeId, brandName))

        val isAc = IrDatabase.brand(typeId, brandName)?.isAc == true
        val next = if (isAc) {
            Intent(this, AcRemoteActivity::class.java)
                .putExtra(AcRemoteActivity.EXTRA_NAME, name)
        } else {
            Intent(this, RemoteActivity::class.java)
                .putExtra(RemoteActivity.EXTRA_TYPE, typeId)
                .putExtra(RemoteActivity.EXTRA_BRAND, brandName)
                .putExtra(RemoteActivity.EXTRA_NAME, name)
        }
        startActivity(next.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
        finish()
    }
}
