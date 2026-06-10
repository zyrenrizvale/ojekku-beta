package com.example.ojekku

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment

class MainActivity : AppCompatActivity() {

    private lateinit var navHomeInner: LinearLayout
    private lateinit var navActivityInner: LinearLayout
    private lateinit var navProfileInner: LinearLayout
    private lateinit var navHomeText: TextView
    private lateinit var navActivityText: TextView
    private lateinit var navProfileText: TextView
    private lateinit var navHomeIcon: ImageView
    private lateinit var navActivityIcon: ImageView
    private lateinit var navProfileIcon: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        navHomeInner = findViewById(R.id.navHomeInner)
        navActivityInner = findViewById(R.id.navActivityInner)
        navProfileInner = findViewById(R.id.navProfileInner)
        navHomeText = findViewById(R.id.navHomeText)
        navActivityText = findViewById(R.id.navActivityText)
        navProfileText = findViewById(R.id.navProfileText)
        navHomeIcon = findViewById(R.id.navHomeIcon)
        navActivityIcon = findViewById(R.id.navActivityIcon)
        navProfileIcon = findViewById(R.id.navProfileIcon)

        // Load tab awal
        if (savedInstanceState == null) {
            loadFragment(HomeFragment(), "HOME")
            setNavActive(0)
        }

        // Klik navigasi
        findViewById<LinearLayout>(R.id.navHome).setOnClickListener {
            loadFragment(HomeFragment(), "HOME")
            setNavActive(0)
        }
        findViewById<LinearLayout>(R.id.navActivity).setOnClickListener {
            loadFragment(HistoryFragment(), "HISTORY")
            setNavActive(1)
        }
        findViewById<LinearLayout>(R.id.navProfile).setOnClickListener {
            loadFragment(ProfileFragment(), "PROFILE")
            setNavActive(2)
        }
    }

    private fun loadFragment(fragment: Fragment, tag: String) {
        val existing = supportFragmentManager.findFragmentByTag(tag)
        val tx = supportFragmentManager.beginTransaction()
        tx.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
        if (existing != null) {
            tx.replace(R.id.fragmentContainer, existing, tag)
        } else {
            tx.replace(R.id.fragmentContainer, fragment, tag)
        }
        tx.commit()
    }

    private fun setNavActive(index: Int) {
        val inactiveColor = 0xFF94A3B8.toInt()
        val activeColor = 0xFF0056D2.toInt()
        val activeBackground = resources.getDrawable(R.drawable.bg_pill_active_light, null)
        val noBackground = null

        // Reset semua
        navHomeInner.background = noBackground
        navActivityInner.background = noBackground
        navProfileInner.background = noBackground
        navHomeIcon.setColorFilter(inactiveColor)
        navActivityIcon.setColorFilter(inactiveColor)
        navProfileIcon.setColorFilter(inactiveColor)
        navHomeText.setTextColor(inactiveColor)
        navActivityText.setTextColor(inactiveColor)
        navProfileText.setTextColor(inactiveColor)

        // Aktifkan yang dipilih
        when (index) {
            0 -> {
                navHomeInner.background = activeBackground
                navHomeIcon.setColorFilter(activeColor)
                navHomeText.setTextColor(activeColor)
            }
            1 -> {
                navActivityInner.background = activeBackground
                navActivityIcon.setColorFilter(activeColor)
                navActivityText.setTextColor(activeColor)
            }
            2 -> {
                navProfileInner.background = activeBackground
                navProfileIcon.setColorFilter(activeColor)
                navProfileText.setTextColor(activeColor)
            }
        }
    }
}
