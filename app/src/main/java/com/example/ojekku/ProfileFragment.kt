package com.example.ojekku

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment

class ProfileFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<LinearLayout>(R.id.menuEditProfile).setOnClickListener { toast("Fitur Edit Profil segera hadir!") }
        view.findViewById<LinearLayout>(R.id.menuSavedLocations).setOnClickListener { toast("Fitur Lokasi Tersimpan segera hadir!") }
        view.findViewById<LinearLayout>(R.id.menuHelp).setOnClickListener { toast("Fitur Pusat Bantuan segera hadir!") }
        view.findViewById<LinearLayout>(R.id.menuAbout).setOnClickListener { toast("OjekKuy! v1.0 — Dibuat dengan ❤️") }

        view.findViewById<android.widget.Button>(R.id.btnLogout).setOnClickListener {
            toast("Berhasil keluar dari akun")
            val intent = Intent(requireContext(), WelcomeActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
        }
    }

    private fun toast(msg: String) = Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
}
