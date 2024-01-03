package com.money.protect.popup

import android.app.AlertDialog
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.view.Window
import androidx.cardview.widget.CardView
import com.money.protect.MainActivity
import com.money.protect.R
import com.money.protect.fragment_assistant.national.MoovFragment
import com.money.protect.fragment_assistant.national.OrangeFragment
import com.money.protect.fragment_assistant.national.OrangeRedirectionFragment
import com.money.protect.fragment_assistant.national.TresorFragment

class MenuPopupCompte2Assistant(
    private val context: MainActivity,
    ) : Dialog(context) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.menu_popup_compte2)

        openOrange()
        openTresor()
        openMoov()
    }

    private fun openMoov() {
        val btn = findViewById<CardView>(R.id.linkTo_moov_popup_compte2)
        btn.setOnClickListener {
            context.supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, MoovFragment(context))
                .addToBackStack(null)
                .commit()
            this.dismiss()
        }
    }

    private fun openOrange() {
        val btn = findViewById<CardView>(R.id.linkTo_orange_popup_compte2)
        btn.setOnClickListener {
            context.supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, OrangeRedirectionFragment(context))
                .addToBackStack(null)
                .commit()
            this.dismiss()
        }
    }
    private fun openTresor() {
        val btn = findViewById<CardView>(R.id.linkTo_tresor_popup_compte2)
        btn.setOnClickListener {
            context.supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, TresorFragment(context))
                .addToBackStack(null)
                .commit()
            this.dismiss()
        }
    }

}