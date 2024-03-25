package com.money.protect.popup

import android.app.Dialog
import android.os.Bundle
import com.money.protect.MainActivity
import com.money.protect.R

class cgu(private val context: MainActivity) : Dialog(context) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.cgu_frame)
    }
}