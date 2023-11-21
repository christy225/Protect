package com.money.protect

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.money.protect.popup.AfficheIdentite


class MainDetailOperationActivity : AppCompatActivity() {
    lateinit var auth: FirebaseAuth
    private lateinit var intents: Intent
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        setContentView(R.layout.activity_main_detail_operation)
        auth = FirebaseAuth.getInstance()

        window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)

        val buttonBack = findViewById<ImageView>(R.id.backButtonDetailtransactionVw)

        buttonBack.setOnClickListener {
            finish()
        }

        intents = intent
        val textAfficher = findViewById<TextView>(R.id.affiche_image_DetailTransactionVw)

        val operateurX = findViewById<TextView>(R.id.operateurDetailTransactionVw)
        val telX = findViewById<TextView>(R.id.phoneDetailTransactionVw)
        val typeX = findViewById<TextView>(R.id.typeOpDetailTransactionVw)
        val montantX = findViewById<TextView>(R.id.montantDetailTransactionVw)
        val dateX = findViewById<TextView>(R.id.dateDetailTransactionVw)
        val heureX = findViewById<TextView>(R.id.heureDetailTransactionVw)
        val info = findViewById<TextView>(R.id.text_info_DetailTransactionVw)

        info.visibility = View.INVISIBLE

        val operateur = intents.getStringExtra("operateur")
        val tel = intents.getStringExtra("telephone")
        val type = intents.getStringExtra("typeoperation")
        val montant = intents.getStringExtra("montant")
        val date = intents.getStringExtra("date")
        val heure = intents.getStringExtra("heure")
        val url = intents.getStringExtra("url")

        operateurX.text = operateur
        telX.text = tel
        typeX.text = type
        montantX.text = montant
        dateX.text = date
        heureX.text = heure

        if (url == "null")
        {
            info.visibility = View.VISIBLE
        }else{
            textAfficher.text = "Afficher l'image"
            textAfficher.setOnClickListener {
                AfficheIdentite(this).show()
            }
        }

    }

    fun lienUrl(): String? {
        return intent!!.getStringExtra("url")
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