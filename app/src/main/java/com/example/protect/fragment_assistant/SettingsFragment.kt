package com.example.protect.fragment_assistant

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.example.protect.AbonnementActivity
import com.example.protect.LoginActivity
import com.example.protect.MainActivity
import com.example.protect.R
import com.example.protect.fragment_assistant.checkInternet.checkForInternet
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class SettingsFragment(private val context: MainActivity) : Fragment() {
    var db = Firebase.firestore
    lateinit var auth : FirebaseAuth
    @SuppressLint("MissingInflatedId")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_assistant_settings, container, false)
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        if (!checkForInternet(context)) {
            Toast.makeText(context, "Impossible de se connecter à internet", Toast.LENGTH_SHORT).show()
        }

        val dayAbonnement = view.findViewById<TextView>(R.id.assistant_settings_nbJours_abonnement)
        val nomComplet = view.findViewById<TextView>(R.id.assistant_settings_nomcomplet)
        val nomSuperviseur = view.findViewById<TextView>(R.id.assistant_settings_superviseur_nom)
        val btnLogout = view.findViewById<Button>(R.id.assistant_settings_logout)
        val numAbonnement = view.findViewById<TextView>(R.id.assistant_settings_num_abonnement)

        val linkReabonnement = view.findViewById<TextView>(R.id.assistant_settings_reabonnement_link)

        linkReabonnement.setOnClickListener {
            val intent = Intent(context, AbonnementActivity::class.java)
            startActivity(intent)
        }

        // RECHERCHER LE SUPERVISEUR POUR L'ABONNEMENT

        db.collection("account")
            .whereEqualTo("id", auth.currentUser?.uid)
            .get()
            .addOnSuccessListener {document->
                for (data in document)
                {
                    val superviseurId = data!!.data["superviseur"].toString()
                    // Afficher le nom de l'assistant
                    nomComplet.text = data!!.data["nomcomplet"].toString()

                    // Afficher le nom du superviseur
                    db.collection("account")
                        .whereEqualTo("id", superviseurId)
                        .get()
                        .addOnSuccessListener {doc->
                            for (datas in doc)
                            {
                                nomSuperviseur.text = datas!!.data["nomcomplet"].toString()
                                numAbonnement.text = datas!!.data["abonnement"].toString()
                            }
                        }.addOnFailureListener {
                            nomSuperviseur.text = R.string.onFailureText.toString()
                        }

                    // VERIFIER L'ETAT D'ABONNEMENT
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
                                val duree = dureeAutorisee.toLong() - jourEcoules

                                // SI ON A ATTEINT LA DUREE AUTORISEE LE COMPTE EST VEROUILLE
                                if (duree >= 0)
                                {
                                    dayAbonnement.text = duree.toString() + " jour(s)"
                                }else{

                                    // APPLIQUER EXPIRATION DE L'ABONNEMENT

                                    val abonnementMap = hashMapOf(
                                        "creation" to creation,
                                        "duration" to "30",
                                        "id" to superviseurId,
                                        "statut" to false
                                    )
                                    db.collection("abonnement")
                                        .document(superviseurId!!)
                                        .set(abonnementMap)
                                        .addOnSuccessListener {
                                            auth.signOut()
                                            val intent = Intent(context, AbonnementActivity::class.java)
                                            startActivity(intent)
                                        }.addOnFailureListener {
                                            val builder = AlertDialog.Builder(context)
                                            builder.setTitle("Erreur")
                                            builder.setMessage("Une erreur s'est produite.")
                                            builder.show()
                                        }

                                    // FIN APPLIQUER EXPIRATION DE L'ABONNEMENT
                                }
                            }
                        }.addOnFailureListener {
                            dayAbonnement.text = R.string.onFailureText.toString()
                        }

                    // VERIFIER L'ETAT D'ABONNEMENT DU SUPERVISEUR
                }
            }.addOnFailureListener {
                Toast.makeText(context, R.string.onFailureText, Toast.LENGTH_SHORT).show()
            }
        // RECHERCHER LE SUPERVISEUR

        btnLogout.setOnClickListener {
            auth.signOut()
            val intent = Intent(context, LoginActivity::class.java)
            startActivity(intent)
        }
        return view
    }
}