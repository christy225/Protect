package com.example.protect

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.ortiz.touchview.TouchImageView

class MainDetailOperationActivity : AppCompatActivity() {
    lateinit var auth: FirebaseAuth
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_detail_operation)

        val buttonBack = findViewById<ImageView>(R.id.backButtonDetailtransactionVw)
        auth = FirebaseAuth.getInstance()

        buttonBack.setOnClickListener {
            finish()
        }

        val intent: Intent = getIntent()

        val operateurX = findViewById<TextView>(R.id.operateurDetailTransactionVw)
        val telX = findViewById<TextView>(R.id.phoneDetailTransactionVw)
        val typeX = findViewById<TextView>(R.id.typeOpDetailTransactionVw)
        val montantX = findViewById<TextView>(R.id.montantDetailTransactionVw)
        val dateX = findViewById<TextView>(R.id.dateDetailTransactionVw)
        val heureX = findViewById<TextView>(R.id.heureDetailTransactionVw)
        val previewImage = findViewById<TouchImageView>(R.id.preview_details_operation)
        val info = findViewById<TextView>(R.id.text_info_details_operation)

        info.visibility = View.INVISIBLE

        val operateur = intent.getStringExtra("operateur")
        val tel = intent.getStringExtra("telephone")
        val type = intent.getStringExtra("typeoperation")
        val montant = intent.getStringExtra("montant")
        val date = intent.getStringExtra("date")
        val heure = intent.getStringExtra("heure")
        val url = intent.getStringExtra("url")

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
            Glide.with(this).load(Uri.parse(url)).into(previewImage)
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