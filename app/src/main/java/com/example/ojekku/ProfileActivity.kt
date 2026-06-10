package com.example.ojekku

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class ProfileActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // Setup navigasi bawah
        val navHome = findViewById<LinearLayout>(R.id.navHome)
        val navActivity = findViewById<LinearLayout>(R.id.navActivity)

        navHome.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            startActivity(intent)
        }

        navActivity.setOnClickListener {
            val intent = Intent(this, HistoryActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            startActivity(intent)
        }

        // Menu klik
        findViewById<LinearLayout>(R.id.menuEditProfile).setOnClickListener {
            Toast.makeText(this, "Fitur Edit Profil segera hadir!", Toast.LENGTH_SHORT).show()
        }
        findViewById<LinearLayout>(R.id.menuSavedLocations).setOnClickListener {
            Toast.makeText(this, "Fitur Lokasi Tersimpan segera hadir!", Toast.LENGTH_SHORT).show()
        }
        findViewById<LinearLayout>(R.id.menuHelp).setOnClickListener {
            Toast.makeText(this, "Fitur Pusat Bantuan segera hadir!", Toast.LENGTH_SHORT).show()
        }
        findViewById<LinearLayout>(R.id.menuAbout).setOnClickListener {
            Toast.makeText(this, "OjekKuy! v1.0 — Dibuat dengan ❤️", Toast.LENGTH_SHORT).show()
        }

        // Logout
        val btnLogout = findViewById<Button>(R.id.btnLogout)
        btnLogout.setOnClickListener {
            Toast.makeText(this, "Berhasil keluar dari akun", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, WelcomeActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
            finish()
        }
    }
}
