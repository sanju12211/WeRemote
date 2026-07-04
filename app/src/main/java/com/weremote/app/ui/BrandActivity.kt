package com.weremote.app.ui

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.weremote.app.data.Brand
import com.weremote.app.data.IrDatabase
import com.weremote.app.databinding.ActivityBrandBinding
import com.weremote.app.databinding.ItemBrandBinding

class BrandActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBrandBinding
    private lateinit var typeId: String
    private var allBrands: List<Brand> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBrandBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.back.setOnClickListener { finish() }

        typeId = intent.getStringExtra(EXTRA_TYPE) ?: "tv"
        allBrands = IrDatabase.type(typeId)?.brands?.sortedBy { it.name } ?: emptyList()

        binding.recycler.layoutManager = LinearLayoutManager(this)
        val adapter = Adapter(allBrands)
        binding.recycler.adapter = adapter

        binding.empty.visibility = if (allBrands.isEmpty()) View.VISIBLE else View.GONE

        binding.search.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
            override fun onTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {
                val q = s?.toString()?.trim()?.lowercase().orEmpty()
                adapter.submit(
                    if (q.isEmpty()) allBrands
                    else allBrands.filter { it.name.lowercase().contains(q) }
                )
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private inner class Adapter(private var items: List<Brand>) :
        RecyclerView.Adapter<Adapter.VH>() {

        fun submit(list: List<Brand>) { items = list; notifyDataSetChanged() }

        inner class VH(val v: ItemBrandBinding) : RecyclerView.ViewHolder(v.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
            VH(ItemBrandBinding.inflate(LayoutInflater.from(parent.context), parent, false))

        override fun getItemCount() = items.size

        override fun onBindViewHolder(holder: VH, position: Int) {
            val brand = items[position]
            holder.v.name.text = brand.name
            holder.v.root.setOnClickListener {
                val target = when {
                    brand.isUniversal -> TvMatchActivity::class.java
                    brand.isAc -> AcMatchActivity::class.java
                    else -> MatchActivity::class.java
                }
                startActivity(
                    Intent(this@BrandActivity, target)
                        .putExtra(EXTRA_TYPE, typeId)
                        .putExtra(EXTRA_BRAND, brand.name)
                )
            }
        }
    }

    companion object {
        const val EXTRA_TYPE = "type"
        const val EXTRA_BRAND = "brand"
    }
}
