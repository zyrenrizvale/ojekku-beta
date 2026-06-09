package com.example.ojekku

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

import android.view.animation.AnimationUtils
import android.view.View

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val rootView = findViewById<View>(android.R.id.content)
        val animFadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        rootView.startAnimation(animFadeIn)

        val btnLogout = findViewById<Button>(R.id.btnLogout)
        val tvUserEmail = findViewById<TextView>(R.id.tvUserEmail)

        btnLogout.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
