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
import java.text.DecimalFormat

class SuperviseurDetailPointNational : AppCompatActivity() {
    private var db = Firebase.firestore
    lateinit var auth: FirebaseAuth
    private lateinit var resultat: TextView
    private lateinit var backButton: ImageView
    private lateinit var succes: ImageView
    @SuppressLint("MissingInflatedId", "SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        setContentView(R.layout.activity_superviseur_detail_point_national)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        backButton = findViewById(R.id.superviseur_detail_point_btnBack_national)
        backButton.setOnClickListener {
            finish()
        }

        succes = findViewById(R.id.successPointNational)
        resultat = findViewById(R.id.superviseur_detail_point_resultat_national)
        val progressBar = findViewById<ProgressBar>(R.id.progressBar_superviseur_point_national)
        progressBar.visibility = View.VISIBLE

        val intent: Intent = getIntent()

        val dateX = findViewById<TextView>(R.id.superviseur_detail_point_date_national)
        val orangeX = findViewById<TextView>(R.id.superviseur_detail_point_montant_orange_national)
        val mtnX = findViewById<TextView>(R.id.superviseur_detail_point_montant_mtn_national)
        val moovX = findViewById<TextView>(R.id.superviseur_detail_point_montant_moov_national)
        val waveX = findViewById<TextView>(R.id.superviseur_detail_point_montant_wave_national)
        val tresorX = findViewById<TextView>(R.id.superviseur_detail_point_montant_tresor_national)
        val especesX = findViewById<TextView>(R.id.superviseur_detail_point_montant_especes_national)
        val diversX = findViewById<TextView>(R.id.superviseur_detail_point_montant_divers_national)

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
        orangeX.text = DecimalFormat("#,###").format(orange!!.toInt())
        mtnX.text = DecimalFormat("#,###").format(mtn!!.toInt())
        moovX.text = DecimalFormat("#,###").format(moov!!.toInt())
        waveX.text = DecimalFormat("#,###").format(wave!!.toInt())
        tresorX.text = DecimalFormat("#,###").format(tresor!!.toInt())
        especesX.text = DecimalFormat("#,###").format(especes!!.toInt())
        diversX.text = DecimalFormat("#,###").format(divers!!.toInt())

        // Montant du point à la date sélectionné

        val pointValeur = orange.toString().toInt() + mtn.toString().toInt() + moov.toString().toInt() +
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
                        progressBar.visibility = View.INVISIBLE
                        val calcul = -1 * res
                        resultat.text = "Surplus : " + DecimalFormat("#,###").format(calcul)
                    }else if(res > 0) {
                        progressBar.visibility = View.INVISIBLE
                        resultat.text = "Perte : " + DecimalFormat("#,###").format(res)
                    }else{
                        progressBar.visibility = View.INVISIBLE
                        resultat.visibility = View.INVISIBLE
                        succes.visibility = View.VISIBLE
                    }
                }
            }.addOnFailureListener {
                val builder = AlertDialog.Builder(this)
                builder.setMessage("Une erreur s'est produite")
                builder.show()
            }
    }
}