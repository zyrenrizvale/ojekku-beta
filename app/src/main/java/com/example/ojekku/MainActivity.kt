package com.example.ojekku

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        // Setup klik untuk layanan utama
        val btnMenuRide = findViewById<LinearLayout>(R.id.btnMenuRide)
        val btnMenuCar = findViewById<LinearLayout>(R.id.btnMenuCar)
        val btnMenuSend = findViewById<LinearLayout>(R.id.btnMenuSend)
        val btnMenuFood = findViewById<LinearLayout>(R.id.btnMenuFood)

        btnMenuRide.setOnClickListener { showToast("OjekKuy Ride segera hadir!") }
        btnMenuCar.setOnClickListener { showToast("OjekKuy Car segera hadir!") }
        btnMenuSend.setOnClickListener { showToast("OjekKuy Send segera hadir!") }
        btnMenuFood.setOnClickListener { showToast("OjekKuy Food segera hadir!") }

        // Setup klik navigasi bawah bergaya kapsul putih
        val navActivity = findViewById<LinearLayout>(R.id.navActivity)
        val navProfile = findViewById<LinearLayout>(R.id.navProfile)

        navActivity.setOnClickListener {
            val intent = Intent(this, HistoryActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            startActivity(intent)
        }

        navProfile.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            startActivity(intent)
        }
        
        // Setup top up
        val btnTopUp = findViewById<android.widget.Button>(R.id.btnTopUp)
        btnTopUp.setOnClickListener { showToast("Fitur Top Up sedang dalam pemeliharaan") }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
