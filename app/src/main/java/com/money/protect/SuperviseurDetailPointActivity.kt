package com.money.protect

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class SuperviseurDetailPointActivity : AppCompatActivity() {
    private var db = Firebase.firestore
    lateinit var auth: FirebaseAuth
    lateinit var resultat: TextView
    lateinit var backButton: ImageView
    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("SetTextI18n", "ResourceAsColor")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        setContentView(R.layout.activity_superviseur_detail_point)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        backButton = findViewById(R.id.superviseur_detail_point_btnBack)
        backButton.setOnClickListener {
            finish()
        }

        resultat = findViewById(R.id.superviseur_detail_point_resultat)

        val intent: Intent = getIntent()

        val dateX = findViewById<TextView>(R.id.superviseur_detail_point_date)
        val orangeX = findViewById<TextView>(R.id.superviseur_detail_point_montant_orange)
        val mtnX = findViewById<TextView>(R.id.superviseur_detail_point_montant_mtn)
        val moovX = findViewById<TextView>(R.id.superviseur_detail_point_montant_moov)
        val waveX = findViewById<TextView>(R.id.superviseur_detail_point_montant_wave)
        val tresorX = findViewById<TextView>(R.id.superviseur_detail_point_montant_tresor)
        val especesX = findViewById<TextView>(R.id.superviseur_detail_point_montant_especes)
        val diversX = findViewById<TextView>(R.id.superviseur_detail_point_montant_divers)

        val date = intent.getStringExtra("date")
        val orange = intent.getStringExtra("orange")
        val mtn = intent.getStringExtra("mtn")
        val moov = intent.getStringExtra("moov")
        val wave = intent.getStringExtra("wave")
        val tresor = intent.getStringExtra("tresor")
        val especes = intent.getStringExtra("especes")
        val divers = intent.getStringExtra("divers")

        val id = intent.getStringExtra("id")

        dateX.text = date
        orangeX.text = orange
        mtnX.text = mtn
        moovX.text = moov
        waveX.text = wave
        tresorX.text = tresor
        especesX.text = especes
        diversX.text = divers

        // Montant du point à la date sélectionné

        val pointValeur = orange.toString().toInt() +
                mtn.toString().toInt() +
                moov.toString().toInt() +
                wave.toString().toInt() +
                tresor.toString().toInt() +
                especes.toString().toInt() +
                divers.toString().toInt()

        // ON RECUPERE LE CAPITAL POUR LA COMPARAISON

        db.collection("capital")
            .whereEqualTo("id", id)
            .get()
            .addOnSuccessListener { document->
                for (data in document)
                {
                    val capital = data!!.data["montant"].toString().toInt()
                    val res = capital - pointValeur
                    if (res < 0) {
                        resultat.text = "Surplus : " + (-1 * res)
                    }else if(res > 0) {
                        resultat.text = "Perte : $res"
                    }else{
                        resultat.text = "COMPTE OK"
                    }
                }
            }.addOnFailureListener {
                val builder = AlertDialog.Builder(this)
                builder.setMessage("Une erreur s'est produite")
                builder.show()
            }

    }

    override fun onStart() {
        super.onStart()
        if (auth.currentUser == null)
        {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }
}