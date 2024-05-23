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

        val intent = intent

        val nomcommercial = intent.getStringExtra("nomcommercial")
        val creation = intent.getStringExtra("creation")
        val module = intent.getStringExtra("module")
        val duration = intent.getStringExtra("duration")

        compte1.setOnClickListener {
            val intent1 = Intent(this, MainActivity::class.java)
            intent1.putExtra("compte", "compte1")
            intent1.putExtra("module", module)
            intent1.putExtra("nomcommercial", nomcommercial)
            intent1.putExtra("creation", creation)
            intent1.putExtra("duration", duration)
            startActivity(intent1)
            finish()
        }
        compte2.setOnClickListener {
            val intent2 = Intent(this, MainActivity::class.java)
            intent2.putExtra("compte", "compte2")
            intent2.putExtra("module", module)
            intent2.putExtra("nomcommercial", nomcommercial)
            intent2.putExtra("creation", creation)
            intent2.putExtra("duration", duration)
            startActivity(intent2)
            finish()
        }
    }
}