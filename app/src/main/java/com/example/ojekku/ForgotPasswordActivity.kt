package com.example.ojekku

import android.os.Bundle
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class ForgotPasswordActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        val cardForgot = findViewById<LinearLayout>(R.id.cardForgot)
        val animSlideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up)
        cardForgot.startAnimation(animSlideUp)

        val ivBack = findViewById<ImageView>(R.id.ivBack)
        ivBack.setOnClickListener {
            finish()
        }

        val etEmail = findViewById<EditText>(R.id.etEmail)
        val btnReset = findViewById<Button>(R.id.btnReset)

        btnReset.setOnClickListener {
            val email = etEmail.text.toString()
            if (email.isNotEmpty()) {
                Toast.makeText(this, "Tautan reset password telah dikirim ke $email", Toast.LENGTH_LONG).show()
                finish() // Kembali ke halaman Login
            } else {
                Toast.makeText(this, "Mohon masukkan email yang terdaftar", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
