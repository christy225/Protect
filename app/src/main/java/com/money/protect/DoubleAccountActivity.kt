package com.money.protect

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.cardview.widget.CardView

class DoubleAccountActivity : AppCompatActivity() {
    private lateinit var compte1: CardView
    private lateinit var compte2: CardView
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_double_account)

        compte1 = findViewById(R.id.lienCompte1)
        compte2 = findViewById(R.id.lienCompte2)

        compte1.setOnClickListener {
            val intent1 = Intent(this, MainActivity::class.java)
            intent1.putExtra("compte", "compte1")
            startActivity(intent1)
        }
        compte2.setOnClickListener {
            val intent2 = Intent(this, MainActivity::class.java)
            intent2.putExtra("compte", "compte2")
            startActivity(intent2)
        }
    }
}