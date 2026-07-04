package com.weremote.app.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.weremote.app.R
import com.weremote.app.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val home = HomeFragment()
    private val me = MeFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        show(home)
        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> { show(home); true }
                R.id.nav_me -> { show(me); true }
                else -> false
            }
        }
    }

    private fun show(f: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, f)
            .commit()
    }
}
