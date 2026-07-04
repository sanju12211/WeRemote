package com.weremote.app.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.weremote.app.databinding.FragmentMeBinding

class MeFragment : Fragment() {

    private var _b: FragmentMeBinding? = null
    private val b get() = _b!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _b = FragmentMeBinding.inflate(inflater, container, false)
        b.rowMessages.setOnClickListener {
            Toast.makeText(requireContext(), "No new messages", Toast.LENGTH_SHORT).show()
        }
        b.rowAbout.setOnClickListener {
            Toast.makeText(requireContext(), "WeRemote v1.0", Toast.LENGTH_SHORT).show()
        }
        return b.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _b = null
    }
}
