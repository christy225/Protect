package com.example.protect.popup

import android.app.Dialog
import android.os.Bundle
import android.view.Window
import androidx.cardview.widget.CardView
import com.example.protect.MainActivity
import com.example.protect.R
import com.example.protect.fragment_assistant.HomeFragment
import com.example.protect.fragment_assistant.national.MoovFragment
import com.example.protect.fragment_assistant.national.MtnFragment
import com.example.protect.fragment_assistant.national.OrangeFragment
import com.example.protect.fragment_assistant.national.TresorFragment
import com.example.protect.fragment_assistant.national.WaveFragment

class MenuPopupAssistant(
    private val context: MainActivity,
    private val homeFragment: HomeFragment
    ) : Dialog(homeFragment.requireContext()) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.menu_popup)

        openOrange()
        openMtn()
        openMoov()
        openWave()
        openTresor()
    }

    private fun openOrange() {
        val btn = findViewById<CardView>(R.id.linkTo_orange_popup)
        btn.setOnClickListener {
            context.supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, OrangeFragment(context))
                .addToBackStack(null)
                .commit()
            this.dismiss()
        }
    }
    private fun openMtn() {
        val btn = findViewById<CardView>(R.id.linkTo_mtn_popup)
        btn.setOnClickListener {
            context.supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, MtnFragment(context))
                .addToBackStack(null)
                .commit()
            this.dismiss()
        }
    }
    private fun openMoov() {
        val btn = findViewById<CardView>(R.id.linkTo_moov_popup)
        btn.setOnClickListener {
            context.supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, MoovFragment(context))
                .addToBackStack(null)
                .commit()
            this.dismiss()
        }
    }
    private fun openWave() {
        val btn = findViewById<CardView>(R.id.linkTo_wave_popup)
        btn.setOnClickListener {
            context.supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, WaveFragment(context))
                .addToBackStack(null)
                .commit()
            this.dismiss()
        }
    }
    private fun openTresor() {
        val btn = findViewById<CardView>(R.id.linkTo_tresor_popup)
        btn.setOnClickListener {
            context.supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, TresorFragment(context))
                .addToBackStack(null)
                .commit()
            this.dismiss()
        }
    }
}