package com.weremote.app.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.weremote.app.data.DeviceType
import com.weremote.app.data.IrDatabase
import com.weremote.app.databinding.ActivityDeviceTypeBinding
import com.weremote.app.databinding.ItemDeviceTypeBinding

class DeviceTypeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityDeviceTypeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.back.setOnClickListener { finish() }

        binding.recycler.layoutManager = GridLayoutManager(this, 2)
        binding.recycler.adapter = Adapter(IrDatabase.types)
    }

    private inner class Adapter(val items: List<DeviceType>) :
        RecyclerView.Adapter<Adapter.VH>() {

        inner class VH(val v: ItemDeviceTypeBinding) : RecyclerView.ViewHolder(v.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
            VH(ItemDeviceTypeBinding.inflate(LayoutInflater.from(parent.context), parent, false))

        override fun getItemCount() = items.size

        override fun onBindViewHolder(holder: VH, position: Int) {
            val t = items[position]
            holder.v.label.text = t.displayName
            holder.v.icon.setImageResource(t.iconRes)
            holder.v.root.setOnClickListener {
                startActivity(
                    Intent(this@DeviceTypeActivity, BrandActivity::class.java)
                        .putExtra(BrandActivity.EXTRA_TYPE, t.id)
                )
            }
        }
    }
}
