package com.example.protect.popup

import android.app.Dialog
import android.os.Bundle
import android.view.Window
import androidx.cardview.widget.CardView
import com.example.protect.MainActivity
import com.example.protect.R
import com.example.protect.fragment_assistant.HomeFragment
import com.example.protect.fragment_assistant.international.MoneygramFragment
import com.example.protect.fragment_assistant.international.RiaFragment
import com.example.protect.fragment_assistant.international.WesternFragment
import com.example.protect.fragment_assistant.national.MoovFragment
import com.example.protect.fragment_assistant.national.MtnFragment
import com.example.protect.fragment_assistant.national.OrangeFragment
import com.example.protect.fragment_assistant.national.TresorFragment
import com.example.protect.fragment_assistant.national.WaveFragment

class MenuPopupAssistantInter(
    private val context: MainActivity,
    private val homeFragment: HomeFragment
    ) : Dialog(homeFragment.requireContext()) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.menu_popup_inter)

        openWestern()
        openMoneyGram()
        openRia()
    }

    private fun openWestern() {
        val btn = findViewById<CardView>(R.id.linkTo_western_popup)
        btn.setOnClickListener {
            context.supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, WesternFragment(context))
                .addToBackStack(null)
                .commit()
            this.dismiss()
        }
    }
    private fun openMoneyGram() {
        val btn = findViewById<CardView>(R.id.linkTo_moneygram_popup)
        btn.setOnClickListener {
            context.supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, MoneygramFragment(context))
                .addToBackStack(null)
                .commit()
            this.dismiss()
        }
    }
    private fun openRia() {
        val btn = findViewById<CardView>(R.id.linkTo_ria_popup)
        btn.setOnClickListener {
            context.supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, RiaFragment(context))
                .addToBackStack(null)
                .commit()
            this.dismiss()
        }
    }
}