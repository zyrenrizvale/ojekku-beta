package com.example.ojekku

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment

class HomeFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Menu Utama -> Buka Transaction Activity
        view.findViewById<LinearLayout>(R.id.btnMenuRide).setOnClickListener { openTransaction("RIDE") }
        view.findViewById<LinearLayout>(R.id.btnMenuCar).setOnClickListener { openTransaction("CAR") }
        view.findViewById<LinearLayout>(R.id.btnMenuSend).setOnClickListener { openTransaction("SEND") }
        view.findViewById<LinearLayout>(R.id.btnMenuFood).setOnClickListener { openTransaction("FOOD") }
        
        view.findViewById<android.widget.Button>(R.id.btnTopUp).setOnClickListener { toast("Fitur Top Up sedang dalam pemeliharaan") }

        // Menu Sekunder -> Dummy Toast
        view.findViewById<LinearLayout>(R.id.btnMenuPulsa).setOnClickListener { toast("Layanan Pulsa & Data segera hadir!") }
        view.findViewById<LinearLayout>(R.id.btnMenuTagihan).setOnClickListener { toast("Layanan Bayar Tagihan segera hadir!") }
        view.findViewById<LinearLayout>(R.id.btnMenuTiket).setOnClickListener { toast("Layanan Tiket Hiburan segera hadir!") }
        view.findViewById<LinearLayout>(R.id.btnMenuLainnya).setOnClickListener { toast("Menampilkan semua layanan...") }
    }

    private fun openTransaction(type: String) {
        val intent = Intent(requireContext(), TransactionActivity::class.java)
        intent.putExtra("SERVICE_TYPE", type)
        startActivity(intent)
    }

    private fun toast(msg: String) = Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
}
