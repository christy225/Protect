package com.money.protect.fragment_assistant

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.money.protect.MainActivity
import com.money.protect.R

class FactureFragment(private val context: MainActivity) : Fragment() {
    lateinit var auth: FirebaseAuth
    private lateinit var mtn: ImageView
    private lateinit var moov: ImageView

    @SuppressLint("MissingInflatedId")
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_facture, container, false)
        auth = FirebaseAuth.getInstance()

        mtn = view.findViewById(R.id.lienMTNFacture)
        moov = view.findViewById(R.id.lienMoovFacture)

        mtn.setOnClickListener{
            val syntaxe = "*188" + Uri.encode("#")
            val callIntent = Intent(Intent.ACTION_CALL)
            callIntent.data = Uri.parse("tel:$syntaxe")
            startActivity(callIntent)
        }

        moov.setOnClickListener {
            val syntaxe = "*155*4" + Uri.encode("#")
            val callIntent = Intent(Intent.ACTION_CALL)
            callIntent.data = Uri.parse("tel:$syntaxe")
            startActivity(callIntent)
        }

        return view
    }

}