package com.example.ojekku

import android.animation.ValueAnimator
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import org.osmdroid.config.Configuration
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline

class TransactionActivity : AppCompatActivity() {

    private lateinit var map: MapView
    private lateinit var ivCenterPin: ImageView
    private lateinit var ivFoodBanner: ImageView

    // Layout Containers
    private lateinit var layoutStateSelect: LinearLayout
    private lateinit var layoutStateConfirm: LinearLayout
    private lateinit var layoutStateTracking: LinearLayout

    // State 1: Select UI
    private lateinit var tvSelectHint: TextView
    private lateinit var btnSetLocation: Button

    // State 2: Confirm UI
    private lateinit var tvLabelA: TextView
    private lateinit var tvValueA: TextView
    private lateinit var tvLabelB: TextView
    private lateinit var tvValueB: TextView
    private lateinit var tvPrice: TextView
    private lateinit var btnOrder: Button

    // State 3+: Tracking UI
    private lateinit var pbLoading: ProgressBar
    private lateinit var tvTrackingStatus: TextView
    private lateinit var driverInfoPanel: LinearLayout

    private var currentState = 0 // 0=Pickup, 1=Dropoff, 2=Confirm, 3=Tracking
    private var serviceType = "RIDE"

    private var pickupPoint: GeoPoint? = null
    private var dropoffPoint: GeoPoint? = null
    private var driverMarker: Marker? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Init OSMDroid Configuration BEFORE setting content view
        val ctx: Context = applicationContext
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx))
        setContentView(R.layout.activity_transaction)

        serviceType = intent.getStringExtra("SERVICE_TYPE") ?: "RIDE"

        // Initialize Views
        map = findViewById(R.id.mapView)
        ivCenterPin = findViewById(R.id.ivCenterPin)
        ivFoodBanner = findViewById(R.id.ivFoodBanner)
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
            setupMapMode()
        }
    }

    private fun setupFoodMode() {
        map.visibility = View.GONE
        ivCenterPin.visibility = View.GONE
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

    private fun setupMapMode() {
        map.setMultiTouchControls(true)
        val mapController = map.controller
        mapController.setZoom(16.0)
        // Default Center: Monas, Jakarta
        val startPoint = GeoPoint(-6.175392, 106.827153)
        mapController.setCenter(startPoint)

        changeState(0)

        btnSetLocation.setOnClickListener {
            val centerGeoPoint = map.mapCenter as GeoPoint
            if (currentState == 0) {
                pickupPoint = centerGeoPoint
                addMarker(pickupPoint!!, "Jemput", org.osmdroid.library.R.drawable.marker_default)
                changeState(1)
            } else if (currentState == 1) {
                dropoffPoint = centerGeoPoint
                addMarker(dropoffPoint!!, "Tujuan", org.osmdroid.library.R.drawable.marker_default)
                changeState(2)
            }
        }

        btnOrder.setOnClickListener {
            changeState(3)
            startDriverSimulation()
        }
    }

    private fun changeState(state: Int) {
        currentState = state
        when (state) {
            0 -> {
                ivCenterPin.visibility = View.VISIBLE
                layoutStateSelect.visibility = View.VISIBLE
                layoutStateConfirm.visibility = View.GONE
                layoutStateTracking.visibility = View.GONE
                tvSelectHint.text = "Geser peta untuk memilih Lokasi Jemput"
            }
            1 -> {
                tvSelectHint.text = "Geser peta untuk memilih Lokasi Tujuan"
            }
            2 -> {
                ivCenterPin.visibility = View.GONE
                layoutStateSelect.visibility = View.GONE
                layoutStateConfirm.visibility = View.VISIBLE
                
                // Draw Polyline
                val line = Polyline()
                line.addPoint(pickupPoint)
                line.addPoint(dropoffPoint)
                line.color = 0xFF0056D2.toInt()
                line.width = 10f
                map.overlays.add(line)

                // Zoom to bounding box
                val boundingBox = BoundingBox(
                    Math.max(pickupPoint!!.latitude, dropoffPoint!!.latitude),
                    Math.max(pickupPoint!!.longitude, dropoffPoint!!.longitude),
                    Math.min(pickupPoint!!.latitude, dropoffPoint!!.latitude),
                    Math.min(pickupPoint!!.longitude, dropoffPoint!!.longitude)
                )
                map.zoomToBoundingBox(boundingBox, true, 100)

                tvValueA.text = "Titik Koordinat Terpilih"
                tvValueB.text = "Titik Tujuan Terpilih"
                tvPrice.text = if (serviceType == "CAR") "Rp 35.000" else "Rp 15.000"
            }
            3 -> {
                layoutStateConfirm.visibility = View.GONE
                layoutStateTracking.visibility = View.VISIBLE
                pbLoading.visibility = View.VISIBLE
                driverInfoPanel.visibility = View.GONE
                tvTrackingStatus.text = "Mencari Driver di sekitarmu..."
            }
        }
    }

    private fun startDriverSimulation() {
        // Step 1: Simulate searching (3 seconds)
        Handler(Looper.getMainLooper()).postDelayed({
            pbLoading.visibility = View.GONE
            driverInfoPanel.visibility = View.VISIBLE
            tvTrackingStatus.text = "Driver sedang menuju ke lokasi Anda"

            // Spawn driver roughly 1km away
            val driverStartGeo = GeoPoint(pickupPoint!!.latitude + 0.005, pickupPoint!!.longitude - 0.005)
            driverMarker = addMarker(driverStartGeo, "Driver", android.R.drawable.ic_menu_directions) // Placeholder icon
            map.controller.animateTo(driverStartGeo, 16.0, 1000)

            // Step 2: Animate Driver to Pickup
            animateMarker(driverStartGeo, pickupPoint!!, 4000) {
                // Step 3: At Pickup
                tvTrackingStatus.text = "Menuju ke Tujuan"
                Toast.makeText(this@TransactionActivity, "Driver telah tiba di lokasi jemput!", Toast.LENGTH_SHORT).show()
                
                Handler(Looper.getMainLooper()).postDelayed({
                    // Step 4: Animate to Dropoff
                    animateMarker(pickupPoint!!, dropoffPoint!!, 6000) {
                        Toast.makeText(this@TransactionActivity, "Pesanan Selesai! Terima kasih.", Toast.LENGTH_LONG).show()
                        finish()
                    }
                }, 1000)
            }
        }, 3000)
    }

    private fun addMarker(point: GeoPoint, title: String, iconRes: Int): Marker {
        val marker = Marker(map)
        marker.position = point
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        marker.title = title
        val icon = resources.getDrawable(iconRes, null)
        marker.icon = icon
        map.overlays.add(marker)
        map.invalidate()
        return marker
    }

    private fun animateMarker(start: GeoPoint, end: GeoPoint, durationMs: Long, onFinish: () -> Unit) {
        val animator = ValueAnimator.ofFloat(0f, 1f)
        animator.duration = durationMs
        animator.addUpdateListener { animation ->
            val fraction = animation.animatedFraction
            val lat = start.latitude + (end.latitude - start.latitude) * fraction
            val lon = start.longitude + (end.longitude - start.longitude) * fraction
            val currentGeo = GeoPoint(lat, lon)
            driverMarker?.position = currentGeo
            map.controller.setCenter(currentGeo)
            map.invalidate()
        }
        animator.addListener(object : android.animation.AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: android.animation.Animator) {
                onFinish()
            }
        })
        animator.start()
    }

    override fun onResume() {
        super.onResume()
        map.onResume()
    }

    override fun onPause() {
        super.onPause()
        map.onPause()
    }
}
