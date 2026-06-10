package com.example.ojekku

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class TransactionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transaction)

        val tvTitle = findViewById<TextView>(R.id.tvTransactionTitle)
        val tvLabelA = findViewById<TextView>(R.id.tvLabelA)
        val tvValueA = findViewById<TextView>(R.id.tvValueA)
        val tvLabelB = findViewById<TextView>(R.id.tvLabelB)
        val tvValueB = findViewById<TextView>(R.id.tvValueB)
        val tvPrice = findViewById<TextView>(R.id.tvPrice)
        val btnOrder = findViewById<Button>(R.id.btnOrder)
        val btnBack = findViewById<ImageView>(R.id.btnBack)
        val ivHeader = findViewById<ImageView>(R.id.ivHeaderImage)

        val serviceType = intent.getStringExtra("SERVICE_TYPE") ?: "RIDE"

        when (serviceType) {
            "RIDE" -> {
                tvTitle.text = "OjekKuy Ride"
                tvPrice.text = "Rp 15.000"
            }
            "CAR" -> {
                tvTitle.text = "OjekKuy Car"
                tvPrice.text = "Rp 35.000"
                tvValueB.text = "Bandara Soekarno Hatta"
            }
            "SEND" -> {
                tvTitle.text = "OjekKuy Send"
                tvLabelA.text = "Lokasi Pengambilan"
                tvLabelB.text = "Lokasi Pengiriman"
                tvValueB.text = "Kantor Pos Pusat"
                tvPrice.text = "Rp 20.000"
            }
            "FOOD" -> {
                tvTitle.text = "OjekKuy Food"
                tvLabelA.text = "Restoran"
                tvValueA.text = "Ayam Geprek Mas Rizki"
                tvLabelB.text = "Lokasi Pengantaran"
                tvPrice.text = "Rp 45.000"
            }
        }

        btnBack.setOnClickListener { finish() }

        btnOrder.setOnClickListener {
            Toast.makeText(this, "Pesanan $serviceType berhasil dibuat! Mencari driver...", Toast.LENGTH_LONG).show()
            finish()
        }
    }
}
