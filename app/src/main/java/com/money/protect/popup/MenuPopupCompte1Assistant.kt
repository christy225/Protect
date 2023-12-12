package com.money.protect.popup

import android.app.Dialog
import android.os.Bundle
import android.view.Window
import androidx.cardview.widget.CardView
import com.money.protect.MainActivity
import com.money.protect.R
import com.money.protect.fragment_assistant.HomeFragment
import com.money.protect.fragment_assistant.national.MoovFragment
import com.money.protect.fragment_assistant.national.MtnFragment
import com.money.protect.fragment_assistant.national.OrangeFragment
import com.money.protect.fragment_assistant.national.TresorFragment
import com.money.protect.fragment_assistant.national.WaveFragment

class MenuPopupCompte1Assistant(
    private val context: MainActivity,
    ) : Dialog(context) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.menu_popup_compte1)

        openOrange()
        openMtn()
        openMoov()
        openWave()
    }

    private fun openOrange() {
        val btn = findViewById<CardView>(R.id.linkTo_orange_popup_compte1)
        btn.setOnClickListener {
            context.supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, OrangeFragment(context))
                .addToBackStack(null)
                .commit()
            this.dismiss()
        }
    }
    private fun openMtn() {
        val btn = findViewById<CardView>(R.id.linkTo_mtn_popup_compte1)
        btn.setOnClickListener {
            context.supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, MtnFragment(context))
                .addToBackStack(null)
                .commit()
            this.dismiss()
        }
    }
    private fun openMoov() {
        val btn = findViewById<CardView>(R.id.linkTo_moov_popup_compte1)
        btn.setOnClickListener {
            context.supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, MoovFragment(context))
                .addToBackStack(null)
                .commit()
            this.dismiss()
        }
    }
    private fun openWave() {
        val btn = findViewById<CardView>(R.id.linkTo_wave_popup_compte1)
        btn.setOnClickListener {
            context.supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, WaveFragment(context))
                .addToBackStack(null)
                .commit()
            this.dismiss()
        }
    }
}