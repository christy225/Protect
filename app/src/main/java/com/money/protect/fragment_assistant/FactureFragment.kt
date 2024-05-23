package com.money.protect.fragment_assistant

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.activity.addCallback
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.money.protect.MainActivity
import com.money.protect.R

class FactureFragment(private val context: MainActivity) : Fragment() {
    lateinit var auth: FirebaseAuth
    private var db = Firebase.firestore
    private lateinit var libelle: TextView
    private lateinit var btn: Button

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

        val bundle = arguments

        val compte = bundle?.getString("compte")

        btn = view.findViewById(R.id.btnFacture)
        libelle = view.findViewById(R.id.libelleFacture)
        //moov = view.findViewById(R.id.lienMoovFacture)
        //tikerama = view.findViewById(R.id.lienTikeramaFacture)

        requireActivity().onBackPressedDispatcher.addCallback(context) {

        }

        if (compte == "compte1") {
            libelle.text = "Payer avec Mtn Money"
            btn.visibility = View.VISIBLE
            btn.setOnClickListener{
                val syntaxe = "*188" + Uri.encode("#")
                val callIntent = Intent(Intent.ACTION_CALL)
                callIntent.data = Uri.parse("tel:$syntaxe")
                startActivity(callIntent)
            }
        }else if (compte == "compte2") {
            libelle.text = "Payer avec Moov Money"
            btn.visibility = View.VISIBLE
            btn.setOnClickListener{
                val syntaxe = "*156*4" + Uri.encode("#")
                val callIntent = Intent(Intent.ACTION_CALL)
                callIntent.data = Uri.parse("tel:$syntaxe")
                startActivity(callIntent)
            }
        }

        return view
    }

}