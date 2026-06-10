package com.example.ojekku

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        // Logika dashboard seperti fetching saldo, nama user, dsb
        // akan diimplementasikan setelah setup Firebase
    }
}
