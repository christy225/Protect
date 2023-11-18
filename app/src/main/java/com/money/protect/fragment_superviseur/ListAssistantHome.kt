package com.money.protect.fragment_superviseur

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.money.protect.AbonnementActivity
import com.money.protect.R
import com.money.protect.SuperviseurActivity
import com.money.protect.adapter.AssistantListAdapterHome
import com.money.protect.models.AccountModel
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class ListAssistantHome(private val context: SuperviseurActivity) : Fragment() {
    lateinit var auth: FirebaseAuth
    lateinit var db : FirebaseFirestore
    lateinit var assistantArrayList : ArrayList<AccountModel>
    @SuppressLint("MissingInflatedId")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_superviseur_list_assistant_home, container, false)
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerViewListAssiatntHome)
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        assistantArrayList = arrayListOf()

        val libelleDelai = view.findViewById<TextView>(R.id.libelle_delai_home)
        val card = view.findViewById<CardView>(R.id.affiche_delai_home)

        card.visibility = View.INVISIBLE

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
                    val duree = dureeAutorisee.toLong() - jourEcoules

                    // SI ON A ATTEINT LA DUREE AUTORISEE LE COMPTE EST VEROUILLE
                    if (duree >= 0 && duree <= 3)
                    {
                        libelleDelai.text = "Votre abonnement expire dans " + duree + " jour(s)"
                        card.visibility = View.VISIBLE
                    }else if(duree < 0){
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
                Toast.makeText(context, R.string.onFailureText, Toast.LENGTH_SHORT).show()
            }

        // VERIFIER L'ETAT D'ABONNEMENT DU SUPERVISEUR

        // Recupérer la liste des assistants SUR LA PAGE D'ACCUEIL
        db.collection("account")
            .whereEqualTo("superviseur", auth.currentUser!!.uid)
            .get().addOnSuccessListener{ documents->
                for (datas in documents)
                {
                    val item = datas.toObject(AccountModel::class.java)
                    if (item != null)
                    {
                        assistantArrayList.add(item)
                    }
                    recyclerView.adapter = AssistantListAdapterHome(context, assistantArrayList)
                    recyclerView.layoutManager = LinearLayoutManager(context)
                }
            }

        return view
    }
}