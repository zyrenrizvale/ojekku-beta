package com.example.ojekku

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

        view.findViewById<LinearLayout>(R.id.btnMenuRide).setOnClickListener { toast("OjekKuy Ride segera hadir!") }
        view.findViewById<LinearLayout>(R.id.btnMenuCar).setOnClickListener { toast("OjekKuy Car segera hadir!") }
        view.findViewById<LinearLayout>(R.id.btnMenuSend).setOnClickListener { toast("OjekKuy Send segera hadir!") }
        view.findViewById<LinearLayout>(R.id.btnMenuFood).setOnClickListener { toast("OjekKuy Food segera hadir!") }
        view.findViewById<android.widget.Button>(R.id.btnTopUp).setOnClickListener { toast("Fitur Top Up sedang dalam pemeliharaan") }
    }

    private fun toast(msg: String) = Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
}
