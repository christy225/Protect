package com.money.protect

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.ActivityInfo
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class SuperviseurDetailPointInterActivity : AppCompatActivity() {
    private var db = Firebase.firestore
    lateinit var auth: FirebaseAuth
    private lateinit var resultat: TextView
    private lateinit var backButton: ImageView
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        setContentView(R.layout.activity_superviseur_detail_point_inter)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        backButton = findViewById(R.id.superviseur_detail_point_btnBack_inter)
        backButton.setOnClickListener {
            finish()
        }

        resultat = findViewById(R.id.superviseur_detail_point_resultat_inter)
        val progressBar = findViewById<ProgressBar>(R.id.progressBar_superviseur_point_inter)
        progressBar.visibility = View.VISIBLE

        val intent: Intent = getIntent()

        val dateX = findViewById<TextView>(R.id.superviseur_detail_point_date_inter)

        val retraitX = findViewById<TextView>(R.id.superviseur_detail_point_montant_retrait_inter)
        val envoiX = findViewById<TextView>(R.id.superviseur_detail_point_montant_envoi_inter)
        val especesX = findViewById<TextView>(R.id.superviseur_detail_point_montant_especes_inter)
        val diversX = findViewById<TextView>(R.id.superviseur_detail_point_montant_divers_inter)

        val date = intent.getStringExtra("date")
        val retrait = intent.getStringExtra("retrait")
        val envoi = intent.getStringExtra("envoi")
        val especes = intent.getStringExtra("especes")
        val divers = intent.getStringExtra("divers")

        val id = intent.getStringExtra("id")

        dateX.text = date
        retraitX.text = retrait
        envoiX.text = envoi
        especesX.text = especes
        diversX.text = divers

        // Montant du point à la date sélectionné

        val pointValeur = retrait.toString().toInt() + especes.toString().toInt() + divers.toString().toInt()

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
}