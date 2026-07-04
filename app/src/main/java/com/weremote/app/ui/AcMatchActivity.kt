package com.weremote.app.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.weremote.app.data.AcProto
import com.weremote.app.data.IrDatabase
import com.weremote.app.data.RemoteStore
import com.weremote.app.data.SavedRemote
import com.weremote.app.ir.AcEngine
import com.weremote.app.ir.AcState
import com.weremote.app.ir.IrBlaster
import com.weremote.app.databinding.ActivityAcMatchBinding

class AcMatchActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAcMatchBinding
    private lateinit var blaster: IrBlaster
    private lateinit var typeId: String
    private lateinit var brandName: String
    private lateinit var candidates: List<AcProto>
    private var index = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAcMatchBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.back.setOnClickListener { finish() }

        typeId = intent.getStringExtra(BrandActivity.EXTRA_TYPE) ?: "ac"
        brandName = intent.getStringExtra(BrandActivity.EXTRA_BRAND) ?: ""
        candidates = IrDatabase.brand(typeId, brandName)?.acCandidates ?: AcEngine.candidates()
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
        val p = candidates[index]
        binding.tvCount.text = "Codeset ${index + 1} / ${candidates.size}"
        binding.tvLabel.text = "Power (${AcEngine.label(p)})"
    }

    private fun testPower() {
        val ok = blaster.transmit(AcEngine.signal(candidates[index], AcState(power = true)))
        if (!ok) Toast.makeText(this, "Could not transmit IR", Toast.LENGTH_SHORT).show()
    }

    private fun useCurrent() {
        val proto = candidates[index]
        val store = RemoteStore(this)
        val typeName = IrDatabase.type(typeId)?.displayName ?: "AC"
        val name = "$brandName $typeName"
        store.add(
            SavedRemote(
                id = System.currentTimeMillis().toString(),
                name = name,
                typeId = typeId,
                brandName = brandName,
                codeset = proto.name
            )
        )
        startActivity(
            Intent(this, AcRemoteActivity::class.java)
                .putExtra(AcRemoteActivity.EXTRA_NAME, name)
                .putExtra(AcRemoteActivity.EXTRA_PROTO, proto.name)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        )
        finish()
    }
}
