package com.money.protect.fragment_assistant

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import com.money.protect.MainActivity
import com.money.protect.R
import com.money.protect.fragment_assistant.checkInternet.checkForInternet
import com.money.protect.popup.MenuPopupAssistantInter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.money.protect.AbonnementActivity
import com.money.protect.DoubleAccountActivity
import com.money.protect.TikeramaActivity
import com.money.protect.popup.SearchPopup
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class HomeFragment(private val context: MainActivity) : Fragment() {
    private var db = Firebase.firestore
    lateinit var auth: FirebaseAuth
    private lateinit var nomCom: TextView
    private lateinit var searchLink: ImageView
    private lateinit var internationalLink: CardView
    private lateinit var nationalLink: CardView
    private lateinit var comptabiliteLink: CardView
    private lateinit var libelleFinAbonnement: TextView
    private lateinit var cardFinAbonnment: ConstraintLayout
    private var creation: String? = null
    private var module: String? = null
    private var duration: String? = null

    private lateinit var loaderNomCommercial: ProgressBar
    private lateinit var numeroAbonnement: TextView

    private lateinit var cardHome1: CardView
    private lateinit var cardHome2: CardView

    @SuppressLint("MissingInflatedId", "HardwareIds")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_assistant_home, container, false)
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        nomCom = view.findViewById(R.id.assistant_home_nomCommercial)
        internationalLink = view.findViewById(R.id.assistant_home_internationLink)
        nationalLink = view.findViewById(R.id.assistant_home_nationLink)
        comptabiliteLink = view.findViewById(R.id.assistant_home_comptabiliteLink)
        searchLink = view.findViewById(R.id.assistant_home_serach)
        libelleFinAbonnement = view.findViewById(R.id.libFinAbonnement)
        cardFinAbonnment = view.findViewById(R.id.CardFinAbonnement)
        loaderNomCommercial = view.findViewById(R.id.loader_nomCommercial)
        numeroAbonnement = view.findViewById(R.id.numAbonnement)

        cardHome1 = view.findViewById(R.id.cHome1)
        cardHome2 = view.findViewById(R.id.cHome2)

        if (!checkForInternet(context)) {
            Toast.makeText(context, "Aucune connexion internet", Toast.LENGTH_SHORT).show()
        }

        val secondFragment = FactureFragment(context)
        cardHome1.setOnClickListener{
            val compte = context.comptePourFacture()
            val bundle = Bundle()
            bundle.putString("compte", compte)
            secondFragment.arguments = bundle
            context.supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, secondFragment)
                .addToBackStack(null)
                .commit()
        }

        cardHome2.setOnClickListener{
            val intent = Intent(context, TikeramaActivity::class.java)
            startActivity(intent)
        }

        val formatter = DateTimeFormatter.ofPattern("d-M-yyyy")
        val current = LocalDateTime.now()
        val dateFormatted = current.format(formatter)

        // VERIFICATION DE L'ABONNEMENT
        db.collection("account")
            .whereEqualTo("id", auth.currentUser?.uid)
            .get()
            .addOnSuccessListener { document->
                for (datas in document)
                {
                    creation = datas!!.data["creation"].toString()
                    duration = datas.data["duration"].toString()

                    val email = datas.data["email"].toString()
                    val nomcommercial = datas.data["nomcommercial"].toString()
                    val nomcomplet = datas.data["nomcomplet"].toString()
                    val quartier = datas.data["quartier"].toString()
                    val modules = datas.data["module"].toString()
                    val superviseur = datas.data["superviseur"].toString()
                    val phone = datas.data["telephone"].toString()
                    val ville = datas.data["ville"].toString()
                    val abonnement = datas.data["abonnement"].toString()
                    val capital = datas.data["capital"].toString()

                    loaderNomCommercial.visibility = View.GONE
                    numeroAbonnement.text = abonnement

                    // START RECUPERER LE NOM COMMERCIAL
                    nomCom.text = nomcommercial
                    module = modules
                    // END RECUPERER LE NOM COMMERCIAL

                    // On formate la date de création réçue de la BDD
                    val date1 = LocalDate.parse(dateFormatted, formatter)
                    val date0 = LocalDate.parse(creation, formatter)
                    // On calcule la différence entre les 2 dates
                    val jourEcoules = ChronoUnit.DAYS.between(date0, date1)
                    val duree = duration!!.toLong() - jourEcoules

                    if (duree < 0)
                    {
                        val accountMap = hashMapOf(
                            "creation" to creation.toString(),
                            "email" to email,
                            "id" to auth.currentUser!!.uid,
                            "nomcommercial" to nomcommercial,
                            "nomcomplet" to nomcomplet,
                            "quartier" to quartier,
                            "role" to "assistant",
                            "statut" to false,
                            "module" to modules,
                            "superviseur" to superviseur,
                            "telephone" to phone,
                            "duration" to duration.toString(),
                            "ville" to ville,
                            "abonnement" to abonnement,
                            "capital" to capital
                        )
                        db.collection("account")
                            .document(auth.currentUser!!.uid)
                            .set(accountMap)
                            .addOnSuccessListener {
                                auth.signOut()
                                val intents = Intent(context, AbonnementActivity::class.java)
                                startActivity(intents)
                            }.addOnFailureListener{
                                val builder = AlertDialog.Builder(context)
                                builder.setMessage(R.string.onFailureText)
                                builder.show()
                            }
                    }else if (duree in 0..3){
                        cardFinAbonnment.visibility = View.VISIBLE
                        libelleFinAbonnement.text = "$duree jours"
                    }
                }
            }

        val accueil = view.findViewById<ImageView>(R.id.assistant_home_accueil)
        accueil.setOnClickListener {
            val intent = Intent(context, DoubleAccountActivity::class.java)
            intent.putExtra("module", module)
            intent.putExtra("nomcommercial", nomCom.text.toString())
            intent.putExtra("creation", creation)
            intent.putExtra("duration", duration)
            startActivity(intent)
        }

        searchLink.setOnClickListener {
            SearchPopup(context).show()
        }

        nationalLink.setOnClickListener {
            // Cette instruction à cause du double compte qui affiche différent menu en National
                context.Compte()
        }
        internationalLink.setOnClickListener {
                MenuPopupAssistantInter(context).show()
        }

        val separatorInter = view.findViewById<View>(R.id.separator_inter)
        // Rediriger en fonction du module
        if (module == "National-International") {
            internationalLink.visibility = View.VISIBLE
            separatorInter.visibility = View.VISIBLE
        }
        // Rediriger en fonction du module

        comptabiliteLink.setOnClickListener {
            if (module == "National")
            {
                context.supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, PointFragmentNational(context))
                    .addToBackStack(null)
                    .commit()
            }
            if (module == "National-International"){
                context.supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, PointFragment(context))
                    .addToBackStack(null)
                    .commit()
            }
            if (module == "International"){
                context.supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, PointFragmentInternational(context))
                    .addToBackStack(null)
                    .commit()
            }
        }
        
        return view
    }

}