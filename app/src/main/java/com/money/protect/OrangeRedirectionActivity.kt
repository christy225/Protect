package com.money.protect

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.RequiresApi

class OrangeRedirectionActivity : AppCompatActivity() {
    private lateinit var button: Button
    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_orange_redirection)

        button = findViewById(R.id.buttonLaunchOrange)

        val intents = intent

        val nomCom = intents.getStringExtra("nomcommercial")
        val creation = intents.getStringExtra("creation")
        val module = intents.getStringExtra("module")
        val duration = intents.getStringExtra("duration")

        val link1 = findViewById<ImageView>(R.id.assistant_link_tresor_redirection)
        link1.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("fragment_to_show", "tresor")
            intent.putExtra("compte", "compte2")
            intent.putExtra("nomcommercial", nomCom)
            intent.putExtra("creation", creation)
            intent.putExtra("module", module)
            intent.putExtra("duration", duration)
            startActivity(intent)
        }
        val link2 = findViewById<ImageView>(R.id.assistant_link_moov_redirection)
        link2.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("fragment_to_show", "moov")
            intent.putExtra("compte", "compte2")
            intent.putExtra("nomcommercial", nomCom)
            intent.putExtra("creation", creation)
            intent.putExtra("module", module)
            intent.putExtra("duration", duration)
            startActivity(intent)
        }

        val homeButton = findViewById<ImageView>(R.id.backButtonToHome)
        homeButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("compte", "compte2")
            intent.putExtra("nomcommercial", nomCom)
            intent.putExtra("creation", creation)
            intent.putExtra("module", module)
            intent.putExtra("duration", duration)
            startActivity(intent)
        }

        button.setOnClickListener {

            val packageName = "com.orange.ci.ompdv"  // Package name de Facebook
            val className = "com.orange.ci.ompdv.MainActivity"

            val intent = Intent()
            intent.component = ComponentName(packageName, className)
            try {
                val intent1 = Intent(this, OrangeSmsListActivity::class.java)
                startActivity(intent1)
                this.startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                // Gérez le cas où L'Application Orange n'est pas installé ou ne permet pas d'ouvrir cette activité
                Toast.makeText(this, "Veuillez installer l'application Point de Vente Orange Money", Toast.LENGTH_LONG).show()
                val intent2 = Intent(this, this::class.java)
                startActivity(intent2)
            }
        }

    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        val back = false
        if (!back) {
            // ne rien faire
        }else{
            super.onBackPressed()
        }
    }
}