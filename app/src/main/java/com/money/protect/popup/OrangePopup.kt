package com.money.protect.popup

import android.app.Dialog
import android.os.Bundle
import com.money.protect.R
import com.money.protect.fragment_assistant.national.OrangeCompte2Fragment

class OrangePopup(
    private val fragment: OrangeCompte2Fragment
) : Dialog(fragment.requireContext()) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.item_orange_form)
    }
}