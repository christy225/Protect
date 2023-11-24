package com.money.protect

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
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
    private lateinit var resultat: TextView
    private lateinit var backButton: ImageView
    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("SetTextI18n", "ResourceAsColor", "MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        setContentView(R.layout.activity_superviseur_detail_point)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        backButton = findViewById(R.id.superviseur_detail_point_btnBack_all)
        backButton.setOnClickListener {
            finish()
        }

        resultat = findViewById(R.id.superviseur_detail_point_resultat_all)
        val progressBar = findViewById<ProgressBar>(R.id.progressBar_superviseur_point_all)
        progressBar.visibility = View.VISIBLE

        val intent: Intent = getIntent()

        val dateX = findViewById<TextView>(R.id.superviseur_detail_point_date_all)
        val orangeX = findViewById<TextView>(R.id.superviseur_detail_point_montant_orange_all)
        val mtnX = findViewById<TextView>(R.id.superviseur_detail_point_montant_mtn_all)
        val moovX = findViewById<TextView>(R.id.superviseur_detail_point_montant_moov_all)
        val waveX = findViewById<TextView>(R.id.superviseur_detail_point_montant_wave_all)
        val tresorX = findViewById<TextView>(R.id.superviseur_detail_point_montant_tresor_all)
        val retraitX = findViewById<TextView>(R.id.superviseur_detail_point_montant_retrait_inter_all)
        val envoiX = findViewById<TextView>(R.id.superviseur_detail_point_montant_envoi_inter_all)
        val especesX = findViewById<TextView>(R.id.superviseur_detail_point_montant_especes_all)
        val diversX = findViewById<TextView>(R.id.superviseur_detail_point_montant_divers_all)

        val date = intent.getStringExtra("date")
        val orange = intent.getStringExtra("orange")
        val mtn = intent.getStringExtra("mtn")
        val moov = intent.getStringExtra("moov")
        val wave = intent.getStringExtra("wave")
        val tresor = intent.getStringExtra("tresor")
        val retrait = intent.getStringExtra("retrait")
        val envoi = intent.getStringExtra("envoi")
        val especes = intent.getStringExtra("especes")
        val divers = intent.getStringExtra("divers")

        val id = intent.getStringExtra("id")

        dateX.text = date
        orangeX.text = orange
        mtnX.text = mtn
        moovX.text = moov
        waveX.text = wave
        tresorX.text = tresor
        retraitX.text = retrait
        envoiX.text = envoi
        especesX.text = especes
        diversX.text = divers

        // Montant du point à la date sélectionné

        val pointValeur = orange.toString().toInt() + mtn.toString().toInt() + moov.toString().toInt() +
                wave.toString().toInt() +
                tresor.toString().toInt() +
                retrait.toString().toInt() +
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
                        progressBar.visibility = View.INVISIBLE
                        resultat.text = "Surplus : " + (-1 * res)
                    }else if(res > 0) {
                        progressBar.visibility = View.INVISIBLE
                        resultat.text = "Perte : $res"
                    }else{
                        progressBar.visibility = View.INVISIBLE
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