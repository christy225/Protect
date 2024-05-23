package com.money.protect.fragment_superviseur

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.money.protect.AbonnementActivity
import com.money.protect.R
import com.money.protect.SuperviseurActivity
import com.money.protect.adapter.AssistantListAdapterHome
import com.money.protect.fragment_assistant.checkInternet.checkForInternet
import com.money.protect.models.AccountModel
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class ListAssistantHome(private val context: SuperviseurActivity) : Fragment() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db : FirebaseFirestore
    private lateinit var assistantArrayList : ArrayList<AccountModel>
    private lateinit var progressBar: ProgressBar
    private lateinit var nomCom: TextView
    @SuppressLint("MissingInflatedId")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_superviseur_list_assistant_home, container, false)

        if (!checkForInternet(context)) {
            Toast.makeText(context, "Aucune connexion internet", Toast.LENGTH_SHORT).show()
        }

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerViewListAssiatntHome)
        progressBar = view.findViewById(R.id.progressBarListAssistant)
        nomCom = view.findViewById(R.id.nomComSuperviseur)
        progressBar.visibility = View.VISIBLE

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        assistantArrayList = arrayListOf()

        val libelleDelai = view.findViewById<TextView>(R.id.libelle_delai_home)

        libelleDelai.visibility = View.INVISIBLE

        db.collection("account")
            .whereEqualTo("id", auth.currentUser?.uid)
            .get()
            .addOnSuccessListener {documents->
                for (datas in documents)
                {
                    nomCom.text = datas.data["nomcommercial"].toString()
                    val creation = datas.data["creation"].toString()
                    val duration = datas.data["duration"].toString()
                    val email = datas.data["email"].toString()
                    val nomcommercial = datas.data["nomcommercial"].toString()
                    val nomcomplet = datas.data["nomcomplet"].toString()
                    val quartier = datas.data["quartier"].toString()
                    val phone = datas.data["telephone"].toString()
                    val ville = datas.data["ville"].toString()
                    val abonnement = datas.data["abonnement"].toString()

                    val formatter = DateTimeFormatter.ofPattern("d-M-yyyy")
                    val current = LocalDateTime.now()
                    val dateFormatted = current.format(formatter)

                    // On formate la date de création réçue de la BDD
                    val date1 = LocalDate.parse(dateFormatted, formatter)
                    val date0 = LocalDate.parse(creation, formatter)
                    // On calcule la différence entre les 2 dates
                    val jourEcoules = ChronoUnit.DAYS.between(date0, date1)
                    val duree = duration.toLong() - jourEcoules

                    if (duree < 0)
                    {
                        val accountMap = hashMapOf(
                            "creation" to creation,
                            "email" to email,
                            "id" to auth.currentUser!!.uid,
                            "nomcommercial" to nomcommercial,
                            "nomcomplet" to nomcomplet,
                            "quartier" to quartier,
                            "role" to "superviseur",
                            "statut" to false,
                            "module" to "null",
                            "superviseur" to "null",
                            "telephone" to phone,
                            "duration" to duration,
                            "ville" to ville,
                            "abonnement" to abonnement,
                            "capital" to "0"
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
                    }
                }
            }

        // Recupérer la liste des assistants SUR LA PAGE D'ACCUEIL
        db.collection("account")
            .whereEqualTo("superviseur", auth.currentUser!!.uid)
            .get().addOnSuccessListener{ documents->
                for (datas in documents)
                {
                    val item = datas.toObject(AccountModel::class.java)
                    progressBar.visibility = View.INVISIBLE
                    assistantArrayList.add(item)
                    recyclerView.adapter = AssistantListAdapterHome(context, assistantArrayList)
                    recyclerView.layoutManager = LinearLayoutManager(context)
                }
            }
        return view
    }
}