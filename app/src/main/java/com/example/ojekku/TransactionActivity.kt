package com.example.ojekku

import android.Manifest
import android.animation.ValueAnimator
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import org.json.JSONArray
import org.osmdroid.config.Configuration
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import org.osmdroid.tileprovider.tilesource.XYTileSource
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import kotlin.concurrent.thread

class TransactionActivity : AppCompatActivity() {

    private lateinit var map: MapView
    private lateinit var ivCenterPin: ImageView
    private lateinit var ivFoodBanner: ImageView
    private lateinit var searchBarLayout: LinearLayout
    private lateinit var etSearchLocation: EditText
    private lateinit var btnSearchLocation: ImageView
    private lateinit var myLocationOverlay: MyLocationNewOverlay

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

    // Cache untuk nama alamat dari pencarian
    private var cachedLocationName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // FIX FORCE CLOSE: Set User-Agent wajib dari OSMDroid sebelum meload map
        val ctx: Context = applicationContext
        Configuration.getInstance().userAgentValue = packageName
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx))
        
        setContentView(R.layout.activity_transaction)

        serviceType = intent.getStringExtra("SERVICE_TYPE") ?: "RIDE"

        // Initialize Views
        map = findViewById(R.id.mapView)
        ivCenterPin = findViewById(R.id.ivCenterPin)
        ivFoodBanner = findViewById(R.id.ivFoodBanner)
        searchBarLayout = findViewById(R.id.searchBarLayout)
        etSearchLocation = findViewById(R.id.etSearchLocation)
        btnSearchLocation = findViewById(R.id.btnSearchLocation)
        
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
            checkPermissionsAndSetupMap()
        }
    }

    private fun setupFoodMode() {
        map.visibility = View.GONE
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

    private fun checkPermissionsAndSetupMap() {
        val permissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        val missing = permissions.filter { ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED }
        if (missing.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, missing.toTypedArray(), 100)
        } else {
            setupMapMode()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100) {
            setupMapMode() // Setup even if denied (fallback to Monas)
        }
    }

    private fun setupMapMode() {
        map.setMultiTouchControls(true)
        
        // Custom Tile Source untuk OjekKuy Maps (CartoDB Positron)
        val cartoTileSource = XYTileSource(
            "CartoPositron",
            0, 20, 256, ".png",
            arrayOf("https://basemaps.cartocdn.com/light_all/")
        )
        map.setTileSource(cartoTileSource)

        val mapController = map.controller
        mapController.setZoom(16.0)
        
        // GPS Tracker
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            myLocationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(this), map)
            myLocationOverlay.enableMyLocation()
            map.overlays.add(myLocationOverlay)
            
            // Pan to GPS Location on first fix
            myLocationOverlay.runOnFirstFix {
                runOnUiThread {
                    mapController.animateTo(myLocationOverlay.myLocation)
                }
            }
        }

        // Default Center: Monas, Jakarta if GPS not ready
        val startPoint = GeoPoint(-6.175392, 106.827153)
        mapController.setCenter(startPoint)

        setupSearchFeature()
        changeState(0)

        btnSetLocation.setOnClickListener {
            val centerGeoPoint = map.mapCenter as GeoPoint
            if (currentState == 0) {
                pickupPoint = centerGeoPoint
                addMarker(pickupPoint!!, "Jemput", org.osmdroid.library.R.drawable.marker_default)
                tvValueA.text = cachedLocationName ?: "Titik Koordinat: ${pickupPoint!!.latitude.toString().take(7)}, ${pickupPoint!!.longitude.toString().take(8)}"
                cachedLocationName = null
                etSearchLocation.setText("")
                changeState(1)
            } else if (currentState == 1) {
                dropoffPoint = centerGeoPoint
                addMarker(dropoffPoint!!, "Tujuan", org.osmdroid.library.R.drawable.marker_default)
                tvValueB.text = cachedLocationName ?: "Titik Koordinat: ${dropoffPoint!!.latitude.toString().take(7)}, ${dropoffPoint!!.longitude.toString().take(8)}"
                changeState(2)
            }
        }

        btnOrder.setOnClickListener {
            changeState(3)
            startDriverSimulation()
        }
    }

    private fun setupSearchFeature() {
        btnSearchLocation.setOnClickListener { performSearch() }
        etSearchLocation.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch()
                true
            } else false
        }
    }

    private fun performSearch() {
        val query = etSearchLocation.text.toString()
        if (query.isEmpty()) return

        // Hide keyboard
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(etSearchLocation.windowToken, 0)

        Toast.makeText(this, "Mencari lokasi...", Toast.LENGTH_SHORT).show()

        thread {
            try {
                val encodedQuery = URLEncoder.encode(query, "UTF-8")
                // Menggunakan Nominatim API gratis (OSM)
                val url = URL("https://nominatim.openstreetmap.org/search?q=$encodedQuery&format=json&limit=1")
                val connection = url.openConnection() as HttpURLConnection
                connection.setRequestProperty("User-Agent", packageName)
                
                if (connection.responseCode == 200) {
                    val response = connection.inputStream.bufferedReader().readText()
                    val jsonArray = JSONArray(response)
                    if (jsonArray.length() > 0) {
                        val firstResult = jsonArray.getJSONObject(0)
                        val lat = firstResult.getDouble("lat")
                        val lon = firstResult.getDouble("lon")
                        val displayName = firstResult.getString("display_name")
                        
                        runOnUiThread {
                            map.controller.animateTo(GeoPoint(lat, lon), 18.0, 1000)
                            Toast.makeText(this@TransactionActivity, "Ditemukan!", Toast.LENGTH_SHORT).show()
                            // Simpan nama tempat untuk ditampilkan saat tombol "Pilih Lokasi" ditekan
                            cachedLocationName = displayName.split(",").take(2).joinToString(",")
                        }
                    } else {
                        runOnUiThread { Toast.makeText(this@TransactionActivity, "Lokasi tidak ditemukan", Toast.LENGTH_SHORT).show() }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread { Toast.makeText(this@TransactionActivity, "Error pencarian lokasi", Toast.LENGTH_SHORT).show() }
            }
        }
    }

    private fun changeState(state: Int) {
        currentState = state
        when (state) {
            0 -> {
                ivCenterPin.visibility = View.VISIBLE
                searchBarLayout.visibility = View.VISIBLE
                layoutStateSelect.visibility = View.VISIBLE
                layoutStateConfirm.visibility = View.GONE
                layoutStateTracking.visibility = View.GONE
                tvSelectHint.text = "Geser peta untuk memilih Lokasi Jemput"
                etSearchLocation.hint = "Cari Lokasi Jemput..."
            }
            1 -> {
                tvSelectHint.text = "Geser peta untuk memilih Lokasi Tujuan"
                etSearchLocation.hint = "Cari Lokasi Tujuan..."
            }
            2 -> {
                ivCenterPin.visibility = View.GONE
                searchBarLayout.visibility = View.GONE
                layoutStateSelect.visibility = View.GONE
                layoutStateConfirm.visibility = View.VISIBLE
                
                // Draw Polyline (OjekKuy Route Style)
                val line = Polyline()
                line.addPoint(pickupPoint)
                line.addPoint(dropoffPoint)
                line.color = 0xFF0056D2.toInt() // Biru OjekKuy
                line.width = 15f // Lebih tebal
                map.overlays.add(line)

                // Zoom to bounding box
                val boundingBox = BoundingBox(
                    Math.max(pickupPoint!!.latitude, dropoffPoint!!.latitude),
                    Math.max(pickupPoint!!.longitude, dropoffPoint!!.longitude),
                    Math.min(pickupPoint!!.latitude, dropoffPoint!!.latitude),
                    Math.min(pickupPoint!!.longitude, dropoffPoint!!.longitude)
                )
                // Add padding and zoom
                map.zoomToBoundingBox(boundingBox, true, 200)

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
        Handler(Looper.getMainLooper()).postDelayed({
            pbLoading.visibility = View.GONE
            driverInfoPanel.visibility = View.VISIBLE
            tvTrackingStatus.text = "Driver sedang menuju ke lokasi Anda"

            val driverStartGeo = GeoPoint(pickupPoint!!.latitude + 0.005, pickupPoint!!.longitude - 0.005)
            driverMarker = addMarker(driverStartGeo, "Driver", android.R.drawable.ic_menu_directions)
            map.controller.animateTo(driverStartGeo, 16.0, 1000)

            animateMarker(driverStartGeo, pickupPoint!!, 4000) {
                tvTrackingStatus.text = "Menuju ke Tujuan"
                Toast.makeText(this@TransactionActivity, "Driver telah tiba di lokasi jemput!", Toast.LENGTH_SHORT).show()
                
                Handler(Looper.getMainLooper()).postDelayed({
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
