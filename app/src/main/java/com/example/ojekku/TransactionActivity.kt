package com.example.ojekku

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.webkit.GeolocationPermissions
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class TransactionActivity : AppCompatActivity() {

    private lateinit var mapWebView: WebView
    
    // UI native lainnya
    private lateinit var ivCenterPin: ImageView
    private lateinit var ivFoodBanner: ImageView
    private lateinit var searchBarLayout: LinearLayout
    
    private lateinit var layoutStateSelect: LinearLayout
    private lateinit var layoutStateConfirm: LinearLayout
    private lateinit var layoutStateTracking: LinearLayout
    
    private lateinit var tvSelectHint: TextView
    private lateinit var btnSetLocation: Button
    
    private lateinit var tvLabelA: TextView
    private lateinit var tvValueA: TextView
    private lateinit var tvLabelB: TextView
    private lateinit var tvValueB: TextView
    private lateinit var tvPrice: TextView
    private lateinit var btnOrder: Button
    
    private lateinit var pbLoading: ProgressBar
    private lateinit var tvTrackingStatus: TextView
    private lateinit var driverInfoPanel: LinearLayout

    private var currentState = 0 
    private var serviceType = "RIDE"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transaction)

        serviceType = intent.getStringExtra("SERVICE_TYPE") ?: "RIDE"

        // Binding semua UI Component
        mapWebView = findViewById(R.id.mapWebView)
        ivCenterPin = findViewById(R.id.ivCenterPin)
        ivFoodBanner = findViewById(R.id.ivFoodBanner)
        searchBarLayout = findViewById(R.id.searchBarLayout)
        
        layoutStateSelect = findViewById(R.id.layoutStateSelect)
        layoutStateConfirm = findViewById(R.id.layoutStateConfirm)
        layoutStateTracking = findViewById(R.id.layoutStateTracking)
        
        tvSelectHint = findViewById(R.id.tvSelectHint)
        btnSetLocation = findViewById(R.id.btnSetLocation)
        
        tvLabelA = findViewById(R.id.tvLabelA)
        tvValueA = findViewById(R.id.tvValueA)
        tvLabelB = findViewById(R.id.tvLabelB)
        tvValueB = findViewById(R.id.tvValueB)
        tvPrice = findViewById(R.id.tvPrice)
        btnOrder = findViewById(R.id.btnOrder)
        
        pbLoading = findViewById(R.id.pbLoading)
        tvTrackingStatus = findViewById(R.id.tvTrackingStatus)
        driverInfoPanel = findViewById(R.id.driverInfoPanel)

        findViewById<TextView>(R.id.tvTransactionTitle).text = "OjekKuy $serviceType"
        findViewById<ImageView>(R.id.btnBack).setOnClickListener { finish() }

        if (serviceType == "FOOD") {
            setupFoodMode()
        } else {
            checkPermissionsAndSetupWebView()
        }
    }

    private fun setupFoodMode() {
        mapWebView.visibility = View.GONE
        ivCenterPin.visibility = View.GONE
        searchBarLayout.visibility = View.GONE
        ivFoodBanner.visibility = View.VISIBLE
        layoutStateSelect.visibility = View.GONE
        layoutStateConfirm.visibility = View.VISIBLE
        layoutStateTracking.visibility = View.GONE

        tvLabelA.text = "Restoran"
        tvValueA.text = "Ayam Geprek Spesial Mas Rizki"
        tvLabelB.text = "Lokasi Pengantaran"
        tvValueB.text = "Rumah"
        tvPrice.text = "Rp 45.000"

        btnOrder.setOnClickListener {
            Toast.makeText(this, "Pesanan Food berhasil dibuat!", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun checkPermissionsAndSetupWebView() {
        val permissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        val missing = permissions.filter { ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED }
        if (missing.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, missing.toTypedArray(), 100)
        } else {
            setupWebView()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100) {
            setupWebView()
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        // Konfigurasi Web Map Anda
        mapWebView.settings.javaScriptEnabled = true
        mapWebView.settings.domStorageEnabled = true
        mapWebView.settings.setGeolocationEnabled(true)

        mapWebView.webViewClient = WebViewClient()
        mapWebView.webChromeClient = object : WebChromeClient() {
            override fun onGeolocationPermissionsShowPrompt(origin: String?, callback: GeolocationPermissions.Callback?) {
                // Memberikan izin GPS ke Web Map Anda
                callback?.invoke(origin, true, false)
            }
        }

        // Memuat URL Web Map Vercel Anda, ditambah parameter layanan
        mapWebView.loadUrl("https://mapsojekkuy.vercel.app?service=$serviceType")

        // Memulai UI simulasi native
        changeState(0)

        btnSetLocation.setOnClickListener {
            if (currentState == 0) {
                tvValueA.text = "Lokasi Jemput (Diatur di Web)"
                changeState(1)
            } else if (currentState == 1) {
                tvValueB.text = "Lokasi Tujuan (Diatur di Web)"
                changeState(2)
            }
        }

        btnOrder.setOnClickListener {
            changeState(3)
            startDummyDriverSimulation()
        }
    }

    private fun changeState(state: Int) {
        currentState = state
        when (state) {
            0 -> {
                ivCenterPin.visibility = View.GONE // Disembunyikan (Ditangani oleh Web)
                searchBarLayout.visibility = View.GONE // Disembunyikan (Ditangani oleh Web)
                layoutStateSelect.visibility = View.VISIBLE
                layoutStateConfirm.visibility = View.GONE
                layoutStateTracking.visibility = View.GONE
                tvSelectHint.text = "Pilih Lokasi di Web Map Anda"
            }
            1 -> {
                tvSelectHint.text = "Pilih Tujuan di Web Map Anda"
            }
            2 -> {
                layoutStateSelect.visibility = View.GONE
                layoutStateConfirm.visibility = View.VISIBLE
                tvPrice.text = if (serviceType == "CAR") "Rp 35.000" else "Rp 15.000"
            }
            3 -> {
                layoutStateConfirm.visibility = View.GONE
                layoutStateTracking.visibility = View.VISIBLE
                pbLoading.visibility = View.VISIBLE
                driverInfoPanel.visibility = View.GONE
                tvTrackingStatus.text = "Mencari Driver via Web System..."
            }
        }
    }

    private fun startDummyDriverSimulation() {
        // Karena animasi rute aslinya ditangani oleh JavaScript di WebView,
        // Native app hanya perlu menampilkan delay simulasi sederhana.
        Handler(Looper.getMainLooper()).postDelayed({
            pbLoading.visibility = View.GONE
            driverInfoPanel.visibility = View.VISIBLE
            tvTrackingStatus.text = "Driver sedang menuju ke lokasi Anda"

            Handler(Looper.getMainLooper()).postDelayed({
                tvTrackingStatus.text = "Menuju ke Tujuan"
                Toast.makeText(this@TransactionActivity, "Driver telah tiba di lokasi jemput!", Toast.LENGTH_SHORT).show()
                
                Handler(Looper.getMainLooper()).postDelayed({
                    Toast.makeText(this@TransactionActivity, "Pesanan Selesai! Terima kasih.", Toast.LENGTH_LONG).show()
                    finish()
                }, 4000)
            }, 4000)
        }, 2000)
    }
}
