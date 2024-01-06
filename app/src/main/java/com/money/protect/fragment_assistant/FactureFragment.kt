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
import android.widget.TextView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.annotation.RequiresApi
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.money.protect.MainActivity
import com.money.protect.R
import com.money.protect.TikeramaActivity

class FactureFragment(private val context: MainActivity) : Fragment() {
    lateinit var auth: FirebaseAuth
    private var db = Firebase.firestore
    private lateinit var titleFacture: TextView
    private lateinit var mtn: CardView
    private lateinit var moov: CardView
    private lateinit var tikerama: CardView

    @SuppressLint("MissingInflatedId")
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_facture, container, false)
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        titleFacture = view.findViewById(R.id.titleFacture)
        mtn = view.findViewById(R.id.lienMTNFacture)
        moov = view.findViewById(R.id.lienMoovFacture)
        tikerama = view.findViewById(R.id.lienTikeramaFacture)

        requireActivity().onBackPressedDispatcher.addCallback(context) {

        }

        if (context.comptePourFacture() == "compte1") {
            moov.visibility = View.GONE
        }else if (context.comptePourFacture() == "compte2"){
            mtn.visibility = View.GONE
        }

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

        tikerama.setOnClickListener{
            val intent = Intent(context, TikeramaActivity::class.java)
            startActivity(intent)
        }

        return view
    }

}