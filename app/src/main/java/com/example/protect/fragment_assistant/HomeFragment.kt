package com.example.protect.fragment_assistant

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
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.protect.AbonnementActivity
import com.example.protect.LoginActivity
import com.example.protect.MainActivity
import com.example.protect.popup.MenuPopupAssistant
import com.example.protect.R
import com.example.protect.models.TransactionModel
import com.example.protect.adapter.OperationAdapter
import com.example.protect.fragment_assistant.checkInternet.checkForInternet
import com.example.protect.popup.MenuPopupAssistantInter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class HomeFragment(private val context: MainActivity) : Fragment() {
    private var db = Firebase.firestore
    lateinit var auth: FirebaseAuth
    lateinit var nomCom: TextView

    lateinit var settingsLink: ImageView
    lateinit var internationalLink: ConstraintLayout
    lateinit var nationalLink: ConstraintLayout
    lateinit var comptabiliteLink: ConstraintLayout
    lateinit var transactionArrayList: ArrayList<TransactionModel>
    lateinit var recyclerView: RecyclerView
    lateinit var progressBar: ProgressBar
    lateinit var infoFailedRetrieveData: TextView

    @SuppressLint("MissingInflatedId")
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
            Toast.makeText(context, "Impossible de se connecter à internet", Toast.LENGTH_SHORT).show()
        }

        nomCom = view.findViewById(R.id.assistant_home_nomCommercial)
        internationalLink = view.findViewById(R.id.assistant_home_internationLink)
        nationalLink = view.findViewById(R.id.assistant_home_nationLink)
        comptabiliteLink = view.findViewById(R.id.assistant_home_comptabiliteLink)
        settingsLink = view.findViewById(R.id.assistant_home_logout)

        recyclerView = view.findViewById(R.id.assistant_home_recyclerView)
        progressBar = view.findViewById(R.id.assistant_home_progressbar)
        infoFailedRetrieveData = view.findViewById(R.id.assistant_home_failed_retrieve_data)
        val cardAbonnement: LinearLayout = view.findViewById(R.id.cardAbonnement)
        val dayAbonnement: TextView = view.findViewById(R.id.dayAbonnement)

        cardAbonnement.visibility = View.INVISIBLE
        progressBar.visibility = View.VISIBLE
        infoFailedRetrieveData.visibility = View.INVISIBLE
        transactionArrayList = arrayListOf()

        // RECUPERER LE NOM COMMERCIAL
        db.collection("account")
            .whereEqualTo("id", auth.currentUser?.uid)
            .get()
            .addOnSuccessListener { document->
                for (data in document)
                {
                    nomCom.text = data!!.data["nomcommercial"].toString()
                }
            }

        settingsLink.setOnClickListener {
            context.supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, SettingsFragment(context))
                .addToBackStack(null)
                .commit()
        }
        nationalLink.setOnClickListener {
            MenuPopupAssistant(context,this).show()
        }
        internationalLink.setOnClickListener {
            MenuPopupAssistantInter(context,this).show()
        }
        comptabiliteLink.setOnClickListener {
            context.supportFragmentManager
                .beginTransaction()
                .replace(R.id.fragment_container, PointFragment(context))
                .addToBackStack(null)
                .commit()
        }

        // RECHERCHER LE SUPERVISEUR POUR L'ABONNEMENT

        db.collection("account")
            .whereEqualTo("id", auth.currentUser?.uid)
            .get()
            .addOnSuccessListener {document->
                for (data in document)
                {
                    val superviseurId = data!!.data["superviseur"].toString()

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
                            Toast.makeText(context, "$it", Toast.LENGTH_SHORT).show()
                        }

                    // VERIFIER L'ETAT D'ABONNEMENT DU SUPERVISEUR
                }
            }.addOnFailureListener {
                Toast.makeText(context, R.string.onFailureText, Toast.LENGTH_SHORT).show()
            }

        // RECHERCHER LE SUPERVISEUR POUR L'ABOONEMENT
        
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