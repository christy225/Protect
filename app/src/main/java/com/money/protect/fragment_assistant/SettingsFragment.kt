package com.money.protect.fragment_assistant

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
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
import com.money.protect.LoginActivity
import com.money.protect.MainActivity
import com.money.protect.R
import com.money.protect.fragment_assistant.checkInternet.checkForInternet
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.money.protect.AbonnementActivity
import com.money.protect.UpdatePasswordActivity
import com.money.protect.popup.cgu
import com.money.protect.popup.confidentialite
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class SettingsFragment(private val context: MainActivity) : Fragment() {
    var db = Firebase.firestore
    lateinit var auth : FirebaseAuth
    private val database = Firebase.firestore
    private var capit: String? = null
    @SuppressLint("UseSwitchCompatOrMaterialCode", "MissingInflatedId")
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
            Toast.makeText(context, "Aucune connexion internet", Toast.LENGTH_SHORT).show()
        }

        val dayAbonnement = view.findViewById<TextView>(R.id.assistant_settings_nbJours_abonnement)
        val nomComplet = view.findViewById<TextView>(R.id.assistant_settings_nomcomplet)
        val localite = view.findViewById<TextView>(R.id.assistant_settings_superviseur_nom)
        val btnLogout = view.findViewById<Button>(R.id.assistant_settings_logout)
        val numAbonnement = view.findViewById<TextView>(R.id.assistant_settings_num_abonnement)
        val identifiant = view.findViewById<TextView>(R.id.assistant_identifiant)

        val cgu = view.findViewById<TextView>(R.id.cgu_link)

        cgu.setOnClickListener {
            cgu(context).show()
        }

        val confidence = view.findViewById<TextView>(R.id.confidentialite_link)
        confidence.setOnClickListener {
            confidentialite(context).show()
        }


        val linkReabonnement = view.findViewById<Button>(R.id.assistant_settings_reabonnement_link)

        linkReabonnement.setOnClickListener {
            val intent = Intent(context, AbonnementActivity::class.java)
            startActivity(intent)
        }

        val linkToUpdatePassword = view.findViewById<Button>(R.id.assistant_update_password)

        linkToUpdatePassword.setOnClickListener {
            val intent = Intent(context, UpdatePasswordActivity::class.java)
            startActivity(intent)
        }

        val openWhatsapp = view.findViewById<Button>(R.id.btnOpenWhatsapp)
        openWhatsapp.setOnClickListener {
            val tel = "+2250767093131"
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse("https://api.whatsapp.com/send/?phone=$tel")
            startActivity(intent)
        }

        // RECHERCHER LE SUPERVISEUR POUR L'ABONNEMENT

        db.collection("account")
            .whereEqualTo("id", auth.currentUser?.uid)
            .get()
            .addOnSuccessListener {document->
                for (data in document)
                {
                    val role = data.data["role"].toString()
                    val email = data.data["email"].toString()
                    val nomcommercial = data.data["nomcommercial"].toString()
                    val nomcomplet = data.data["nomcomplet"].toString()
                    val quartier = data.data["quartier"].toString()
                    val creation = data.data["creation"].toString()
                    val module = data.data["module"].toString()
                    val superviseur = data.data["superviseur"].toString()
                    val phone = data.data["telephone"].toString()
                    val ville = data.data["ville"].toString()
                    val abonnement = data.data["abonnement"].toString()
                    val duration = data.data["duration"].toString()
                    val capital = data.data["capital"].toString()

                    nomComplet.text = nomcomplet
                    numAbonnement.text = abonnement
                    identifiant.text = phone
                    localite.text = quartier

                    // On formate la date de création réçue de la BDD
                    val formatter = DateTimeFormatter.ofPattern("d-M-yyyy")
                    val date0 = LocalDate.parse(creation, formatter)

                    // On récupère la date du jour
                    val current = LocalDateTime.now()
                    val dateFormatted = current.format(formatter)
                    val date1 = LocalDate.parse(dateFormatted, formatter)

                    // On calcule la différence entre les 2 dates
                    val jourEcoules = ChronoUnit.DAYS.between(date0, date1)
                    val duree = duration.toLong() - jourEcoules

                    if (duree >= 0)
                    {
                        //
                        dayAbonnement.text = duree.toString() + " jour(s)"
                    }else{
                        if(superviseur !== "null"){
                            capit = capital
                        }else{
                            capit = "0"
                        }
                        val accountMap = hashMapOf(
                            "creation" to creation,
                            "email" to email,
                            "id" to auth.currentUser!!.uid,
                            "nomcommercial" to nomcommercial,
                            "nomcomplet" to nomcomplet,
                            "quartier" to quartier,
                            "role" to role,
                            "statut" to false,
                            "module" to module,
                            "superviseur" to superviseur,
                            "telephone" to phone,
                            "duration" to "60",
                            "ville" to ville,
                            "abonnement" to abonnement,
                            "capital" to capit // a revoir si il s'agit du superviseur
                        )
                        database.collection("account")
                            .document(auth.currentUser!!.uid)
                            .set(accountMap)
                            .addOnSuccessListener {
                                auth.signOut()
                                val intent = Intent(context, AbonnementActivity::class.java)
                                startActivity(intent)
                            }.addOnFailureListener{
                                val builder = AlertDialog.Builder(context)
                                builder.setMessage(R.string.onFailureText)
                                builder.show()
                            }
                    }
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