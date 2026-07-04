package com.weremote.app.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.weremote.app.data.RemoteStore
import com.weremote.app.data.SavedRemote
import com.weremote.app.databinding.FragmentHomeBinding
import com.weremote.app.databinding.ItemRemoteCardBinding

class HomeFragment : Fragment() {

    private var _b: FragmentHomeBinding? = null
    private val b get() = _b!!
    private lateinit var store: RemoteStore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _b = FragmentHomeBinding.inflate(inflater, container, false)
        store = RemoteStore(requireContext())
        b.recycler.layoutManager = LinearLayoutManager(requireContext())

        val add = View.OnClickListener {
            startActivity(Intent(requireContext(), DeviceTypeActivity::class.java))
        }
        b.fabAdd.setOnClickListener(add)
        b.emptyAdd.setOnClickListener(add)
        return b.root
    }

    override fun onResume() {
        super.onResume()
        val remotes = store.all()
        b.empty.visibility = if (remotes.isEmpty()) View.VISIBLE else View.GONE
        b.recycler.visibility = if (remotes.isEmpty()) View.GONE else View.VISIBLE
        b.recycler.adapter = Adapter(remotes)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _b = null
    }

    private inner class Adapter(val items: List<SavedRemote>) :
        RecyclerView.Adapter<Adapter.VH>() {

        inner class VH(val v: ItemRemoteCardBinding) : RecyclerView.ViewHolder(v.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
            VH(ItemRemoteCardBinding.inflate(LayoutInflater.from(parent.context), parent, false))

        override fun getItemCount() = items.size

        override fun onBindViewHolder(holder: VH, position: Int) {
            val r = items[position]
            holder.v.title.text = r.name
            holder.v.subtitle.text = r.brandName
            holder.v.root.setOnClickListener {
                val intent = if (r.typeId == "ac") {
                    Intent(requireContext(), AcRemoteActivity::class.java)
                        .putExtra(AcRemoteActivity.EXTRA_NAME, r.name)
                        .putExtra(AcRemoteActivity.EXTRA_PROTO, r.codeset)
                } else {
                    Intent(requireContext(), RemoteActivity::class.java)
                        .putExtra(RemoteActivity.EXTRA_TYPE, r.typeId)
                        .putExtra(RemoteActivity.EXTRA_BRAND, r.brandName)
                        .putExtra(RemoteActivity.EXTRA_NAME, r.name)
                }
                startActivity(intent)
            }
            holder.v.root.setOnLongClickListener {
                store.remove(r.id)
                onResume()
                true
            }
        }
    }
}
