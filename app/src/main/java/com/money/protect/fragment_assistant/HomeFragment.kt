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
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.money.protect.MainActivity
import com.money.protect.R
import com.money.protect.models.TransactionModel
import com.money.protect.adapter.OperationAdapter
import com.money.protect.fragment_assistant.checkInternet.checkForInternet
import com.money.protect.popup.MenuPopupAssistantInter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.money.protect.DoubleAccountActivity
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
    private lateinit var internationalLink: ConstraintLayout
    private lateinit var nationalLink: ConstraintLayout
    private lateinit var comptabiliteLink: ConstraintLayout
    private lateinit var transactionArrayList: ArrayList<TransactionModel>
    private lateinit var recyclerView: RecyclerView
    lateinit var progressBar: ProgressBar
    private lateinit var infoFailedRetrieveData: TextView
    private var module: String? = null

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

        if (!checkForInternet(context)) {
            Toast.makeText(context, "Aucune connexion internet", Toast.LENGTH_SHORT).show()
        }

        val accueil = view.findViewById<ImageView>(R.id.assistant_home_accueil)
        accueil.setOnClickListener {
            val intent = Intent(context, DoubleAccountActivity::class.java)
            startActivity(intent)
        }

        requireActivity().onBackPressedDispatcher.addCallback(context) {

        }

        nomCom = view.findViewById(R.id.assistant_home_nomCommercial)
        internationalLink = view.findViewById(R.id.assistant_home_internationLink)
        nationalLink = view.findViewById(R.id.assistant_home_nationLink)
        comptabiliteLink = view.findViewById(R.id.assistant_home_comptabiliteLink)
        searchLink = view.findViewById(R.id.assistant_home_serach)

        recyclerView = view.findViewById(R.id.assistant_home_recyclerView)
        progressBar = view.findViewById(R.id.assistant_home_progressbar)
        infoFailedRetrieveData = view.findViewById(R.id.assistant_home_failed_retrieve_data)
        val cardAbonnement: LinearLayout = view.findViewById(R.id.cardAbonnement)
        val dayAbonnement: TextView = view.findViewById(R.id.dayAbonnement)

        cardAbonnement.visibility = View.INVISIBLE

        infoFailedRetrieveData.visibility = View.INVISIBLE
        transactionArrayList = arrayListOf()

        progressBar.visibility = View.VISIBLE

        // Appeler la fonction pour afficher le message après 10 secondes

        // RECUPERER LE NOM COMMERCIAL
        db.collection("account")
            .whereEqualTo("id", auth.currentUser?.uid)
            .get()
            .addOnSuccessListener { document->
                for (dons in document)
                {
                    nomCom.text = dons.data["nomcommercial"].toString()
                    module = dons.data["module"].toString()

                    if (module == "International") {
                        internationalLink.visibility = View.VISIBLE
                        comptabiliteLink.visibility = View.VISIBLE
                    }else if (module == "National") {
                        nationalLink.visibility = View.VISIBLE
                        comptabiliteLink.visibility = View.VISIBLE
                    }else{
                        internationalLink.visibility = View.VISIBLE
                        nationalLink.visibility = View.VISIBLE
                        comptabiliteLink.visibility = View.VISIBLE
                    }
                }
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

        // RECHERCHER LE SUPERVISEUR POUR L'ABONNEMENT

        db.collection("account")
            .whereEqualTo("id", auth.currentUser?.uid)
            .get()
            .addOnSuccessListener { document->
                for (data in document)
                {
                    val superviseurId = data!!.data["superviseur"].toString()
                    module = data!!.data["module"].toString()

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
                                if (duree >= 0 && duree <= 3)
                                {
                                    dayAbonnement.text = duree.toString()
                                    cardAbonnement.visibility = View.VISIBLE
                                }else if(duree < 0){

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
                                            val intent = Intent(context, com.money.protect.AbonnementActivity::class.java)
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
                            Toast.makeText(context, "$it", Toast.LENGTH_SHORT).show()
                        }

                    // VERIFIER L'ETAT D'ABONNEMENT DU SUPERVISEUR
                }
            }.addOnFailureListener {
                Toast.makeText(context, R.string.onFailureText, Toast.LENGTH_SHORT).show()
            }

        // FIN RECHERCHER LE SUPERVISEUR POUR L'ABOONEMENT

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

        // Afficher la liste des transactions du jour

        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("d-M-yyyy")
        val dateFormatted = current.format(formatter)

       db.collection("operation")
           .orderBy("heure", Query.Direction.DESCENDING)
           .get().addOnSuccessListener { documents->
                if (documents.isEmpty)
                {
                    progressBar.visibility = View.INVISIBLE
                    infoFailedRetrieveData.visibility = View.VISIBLE
                }else{
                    for (data in documents){
                        val transaction = data.toObject(TransactionModel::class.java)
                        if (transaction != null) {
                            transactionArrayList.add(transaction)
                        }

                        val arrayList = transactionArrayList.filter { it.date == dateFormatted && it.id == auth.currentUser?.uid } as ArrayList<TransactionModel>
                        if (arrayList.isNotEmpty())
                        {
                            progressBar.visibility = View.INVISIBLE
                            infoFailedRetrieveData.visibility = View.INVISIBLE
                        }else{
                            progressBar.visibility = View.INVISIBLE
                            infoFailedRetrieveData.visibility = View.VISIBLE
                        }
                        recyclerView.adapter = OperationAdapter(context, arrayList)
                        recyclerView.layoutManager = LinearLayoutManager(context)
                    }
                }
        }.addOnFailureListener {
               Toast.makeText(context, "$it", Toast.LENGTH_SHORT).show()
        }
        
        return view
    }

}