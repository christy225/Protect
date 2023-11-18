package com.money.protect.popup

import android.app.Dialog
import android.os.Bundle
import androidx.cardview.widget.CardView
import com.money.protect.MainActivity
import com.money.protect.R
import com.money.protect.fragment_assistant.AdvancedFragment
import com.money.protect.fragment_assistant.AmountFragment
import com.money.protect.fragment_assistant.HomeFragment
import com.money.protect.fragment_assistant.SearchDateFragment
import com.money.protect.fragment_assistant.SearchPhoneFragment

class SearchPopup(
    private val context: MainActivity,
) : Dialog(context) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.menu_popup_recherche)

        datesearch()
        numbersearch()
        montantsearch()
        advancedsearch()
    }

    private fun advancedsearch() {
        val btn = findViewById<CardView>(R.id.linkToSearchAdvance)
        btn.setOnClickListener {
            context.supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, AdvancedFragment(context))
                .addToBackStack(null)
                .commit()
            dismiss()
        }
    }

    private fun montantsearch() {
        val btn = findViewById<CardView>(R.id.linkToSearchMontant)
        btn.setOnClickListener {
            context.supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, AmountFragment(context))
                .addToBackStack(null)
                .commit()
            dismiss()
        }
    }

    private fun numbersearch() {
        val btn = findViewById<CardView>(R.id.linkToSearchNumber)
        btn.setOnClickListener {
            context.supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, SearchPhoneFragment(context))
                .addToBackStack(null)
                .commit()
            dismiss()
        }
    }

    private fun datesearch() {
        val btn = findViewById<CardView>(R.id.linkToSearchDate)
        btn.setOnClickListener {
            context.supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, SearchDateFragment(context))
                .addToBackStack(null)
                .commit()
            dismiss()
        }
    }
}