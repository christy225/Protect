package com.money.protect

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class DoubleAccountActivity : AppCompatActivity() {
    companion object {
        const val REQUEST_CALL_PERMISSION = 456
        const val READ_SMS_PERMISSION_REQUEST = 1
    }
    private lateinit var compte1: CardView
    private lateinit var compte2: CardView
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_double_account)

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED) {

        } else {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.READ_SMS), READ_SMS_PERMISSION_REQUEST)
        }

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {

        } else {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.CALL_PHONE), REQUEST_CALL_PERMISSION)
        }

        compte1 = findViewById(R.id.lienCompte1)
        compte2 = findViewById(R.id.lienCompte2)

        compte1.setOnClickListener {
            val intent1 = Intent(this, MainActivity::class.java)
            intent1.putExtra("compte", "compte1")
            startActivity(intent1)
            finish()
        }
        compte2.setOnClickListener {
            val intent2 = Intent(this, MainActivity::class.java)
            intent2.putExtra("compte", "compte2")
            startActivity(intent2)
            finish()
        }
    }
}