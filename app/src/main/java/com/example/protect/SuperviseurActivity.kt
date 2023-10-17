package com.example.protect

import android.app.AlertDialog
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.example.protect.fragment_superviseur.CapitalSuperviseurFragment
import com.example.protect.fragment_superviseur.HomeSuperviseurFragment
import com.example.protect.fragment_superviseur.ListAssistantSuperviseurFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class SuperviseurActivity : AppCompatActivity() {
    private var db = Firebase.firestore
    lateinit var auth: FirebaseAuth
    lateinit var bottomNavigationView: BottomNavigationView
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_superviseur)
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        bottomNavigationView = findViewById(R.id.superviseur_bottomNavigationView)

        // VERIFIER L'ETAT D'ABONNEMENT

        db.collection("abonnement")
            .whereEqualTo("id", auth.currentUser?.uid)
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
                        loadFragment(HomeSuperviseurFragment(this))
                        bottomNavigationView.setOnItemSelectedListener {
                            when(it.itemId)
                            {
                                R.id.homeMenuSuperviseur -> loadFragment(HomeSuperviseurFragment(this))
                                R.id.capitalMenuSuperviseur -> loadFragment(CapitalSuperviseurFragment(this))
                                R.id.listAssistantMenuSuperviseur -> loadFragment(ListAssistantSuperviseurFragment(this))
                            }
                            false
                        }
                    } else if (dureeAutorisee.toLong() < jourEcoules){
                        // APPLIQUER EXPIRATION DE L'ABONNEMENT

                        val abonnementMap = hashMapOf(
                            "creation" to creation,
                            "duration" to "30",
                            "id" to auth.currentUser?.uid,
                            "statut" to false
                        )
                        db.collection("abonnement")
                            .document(auth.currentUser?.uid!!)
                            .set(abonnementMap)
                            .addOnSuccessListener {
                                auth.signOut()
                                val intent = Intent(this, AbonnementActivity::class.java)
                                startActivity(intent)
                            }.addOnFailureListener {
                                val builder = AlertDialog.Builder(this)
                                builder.setTitle("Erreur")
                                builder.setMessage("Une erreur s'est produite.")
                                builder.show()
                            }

                        // FIN APPLIQUER EXPIRATION DE L'ABONNEMENT
                    }
                }
            }.addOnFailureListener {
                Toast.makeText(this, R.string.onFailureText, Toast.LENGTH_SHORT).show()
            }

        // VERIFIER L'ETAT D'ABONNEMENT DU SUPERVISEUR

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
            .replace(R.id.fragment_container_superviseur, fragment)
            .addToBackStack(null)
            .commit()
    }
}