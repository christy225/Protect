package com.example.protect

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.example.protect.fragment_assistant.HomeFragment
import com.example.protect.fragment_assistant.SearchPhoneFragment
import com.example.protect.fragment_assistant.checkInternet.checkForInternet
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class MainActivity : AppCompatActivity() {
    lateinit var auth: FirebaseAuth
    private var db = Firebase.firestore
    lateinit var bottomNavigationView: BottomNavigationView
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        bottomNavigationView = findViewById(R.id.bottomNavigation)

        // RECHERCHER LE COMPTE ASSISTANT POUR RECUPERER L'ID SUPERVISEUR

        db.collection("account")
            .whereEqualTo("id", auth.currentUser!!.uid)
            .get()
            .addOnSuccessListener {document->
                for (data in document)
                {
                    val superviseurId = data!!.data["superviseur"].toString()

                    // VERIFIER L'ETAT D'ABONNEMENT DE SON SUPERVISEUR

                    db.collection("abonnement")
                        .whereEqualTo("id", superviseurId)
                        .get()
                        .addOnSuccessListener {document->
                            for (data in document)
                            {
                                val dureeAutorisee = data!!.data["duration"].toString()
                                val creation = data!!.data["creation"].toString()

                                // On formate la date de création réçue de la BDD
                                val formatter = DateTimeFormatter.ofPattern("d-M-yyyy")
                                val date0 = LocalDate.parse(creation, formatter)

                                // On récupère la date du jour
                                val current = LocalDateTime.now()
                                val dateFormatted = current.format(formatter)
                                val date1 = LocalDate.parse(dateFormatted, formatter)

                                val jourEcoules = ChronoUnit.DAYS.between(date0, date1)

                                // SI ON A ATTEINT LA DUREE AUTORISEE LE COMPTE EST VEROUILLE

                                if (dureeAutorisee.toLong() >= jourEcoules)
                                {
                                    loadFragment(HomeFragment(this))
                                    bottomNavigationView.setOnItemSelectedListener {
                                        when(it.itemId)
                                        {
                                            R.id.home -> loadFragment(HomeFragment(this))
                                            R.id.search -> loadFragment(SearchPhoneFragment(this))
                                        }
                                        false
                                    }
                                } else if (dureeAutorisee.toLong() < jourEcoules){
                                    val builder = AlertDialog.Builder(this)
                                    builder.setTitle("Fin Abonnement")
                                    builder.setMessage("Votre abonnement a expiré")
                                    builder.show()
                                    auth.signOut()
                                    val intent = Intent(this, LoginActivity::class.java)
                                    startActivity(intent)
                                }
                            }
                        }.addOnFailureListener {
                            Toast.makeText(this, R.string.onFailureText, Toast.LENGTH_SHORT).show()
                        }

                    // VERIFIER L'ETAT D'ABONNEMENT DU SUPERVISEUR
                }
            }.addOnFailureListener {
                Toast.makeText(this, R.string.onFailureText, Toast.LENGTH_SHORT).show()
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

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }
}