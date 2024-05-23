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

class SuperviseurDetailPointInterActivity : AppCompatActivity() {
    private var db = Firebase.firestore
    lateinit var auth: FirebaseAuth
    private lateinit var resultat: TextView
    private lateinit var backButton: ImageView
    private lateinit var succes: ImageView
    @SuppressLint("MissingInflatedId", "SetTextI18n")
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

        succes = findViewById(R.id.successPointInter)
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

        dateX.text = date
        retraitX.text = DecimalFormat("#,###").format(retrait!!.toInt())
        envoiX.text = DecimalFormat("#,###").format(envoi!!.toInt())
        especesX.text = DecimalFormat("#,###").format(especes!!.toInt())
        diversX.text = DecimalFormat("#,###").format(divers!!.toInt())

        // Montant du point à la date sélectionné

        val pointValeur = retrait.toString().toInt() + especes.toString().toInt() + divers.toString().toInt()

        // ON RECUPERE LE CAPITAL POUR LA COMPARAISON

        db.collection("account")
            .whereEqualTo("id", auth.currentUser?.uid)
            .get()
            .addOnSuccessListener { document->
                for (data in document)
                {
                    val capital = data!!.data["capital"].toString().toInt()
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