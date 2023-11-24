package com.money.protect.fragment_superviseur

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatImageButton
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.money.protect.R
import com.money.protect.SuperviseurActivity
import com.money.protect.adapter.LePointAdapter
import com.money.protect.models.PointModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.money.protect.fragment_assistant.checkInternet.checkForInternet
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Calendar
import java.util.Locale

class HomeSuperviseurFragment(private val context: SuperviseurActivity) : Fragment() {
    private var db = Firebase.firestore
    lateinit var auth: FirebaseAuth
    lateinit var resultat: TextView
    lateinit var button: Button

    lateinit var adapter: LePointAdapter

    lateinit var capitalArrayList: ArrayList<String>
    lateinit var retrieveArrayList: ArrayList<String>

    lateinit var datePicker: EditText
    lateinit var buttonSearchDate: AppCompatImageButton
    lateinit var recyclerView: RecyclerView
    lateinit var pointArrayList: ArrayList<PointModel>

    lateinit var progressBar: ProgressBar

    private var verificationSoldeDefini: Boolean = false
    private var etatCapital: Boolean = false

    @SuppressLint("MissingInflatedId")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_superviseur_home, container, false)

        if (!checkForInternet(context)) {
            Toast.makeText(context, "Aucune connexion internet", Toast.LENGTH_SHORT).show()
        }

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        val data = arguments
        val id = data?.getString("id")
        val nom = data?.getString("nom")
        val assignation = data?.getString("module")

        val nomAssistant = view.findViewById<TextView>(R.id.nom_assistant)
        nomAssistant.text = nom

        val module = view.findViewById<TextView>(R.id.module_assistant)
        module.text = assignation

        resultat = view.findViewById(R.id.superviseur_point_resultat_Vw)
        button = view.findViewById(R.id.superviseur_point_btn_upload)

        recyclerView = view.findViewById(R.id.superviseur_point_recyclerView)
        datePicker = view.findViewById(R.id.superviseur_point_datePicker)
        buttonSearchDate = view.findViewById(R.id.superviseur_point_btn_search)
        progressBar = view.findViewById(R.id.superviseur_point_progressbar)
        val card: CardView = view.findViewById(R.id.affiche_delai)
        val libelleDelai: TextView = view.findViewById(R.id.libelle_delai)

        retrieveArrayList = arrayListOf()
        capitalArrayList = arrayListOf()

        pointArrayList = arrayListOf()

        adapter = LePointAdapter(context, pointArrayList)

        progressBar.visibility = View.VISIBLE
        card.visibility = View.INVISIBLE

        val linkToCapital = view.findViewById<TextView>(R.id.linkToCapital)
        val capitalFragment = CapitalSuperviseurFragment(context)
        linkToCapital.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("id", id)
            bundle.putString("nom", nom)
            bundle.putString("module", assignation)
            capitalFragment.arguments = bundle
            context.supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container_superviseur, capitalFragment)
                .addToBackStack(null)
                .commit()
        }

        val linkToHome = view.findViewById<ImageView>(R.id.backToHomeSuperviseur)
        linkToHome.setOnClickListener {
            context.supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container_superviseur, ListAssistantHome(context))
                .addToBackStack(null)
                .commit()
        }


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
                Toast.makeText(context, R.string.onFailureText, Toast.LENGTH_SHORT).show()
            }

        // VERIFIER L'ETAT D'ABONNEMENT DU SUPERVISEUR

        // Recupérer le point du jour
        
        val current = LocalDateTime.now()
        val formatterDate = DateTimeFormatter.ofPattern("d-M-yyyy")
        val dateFormatted = current.format(formatterDate)

        db.collection("point")
            .whereEqualTo("id", id)
            .get()
            .addOnSuccessListener { documents->
                    for (datas in documents){
                        val montant = datas.data["total"].toString()
                        val date = datas.data["date"].toString()
                        if (date == dateFormatted)
                        {
                            retrieveArrayList.add(montant)
                            verificationSoldeDefini = true
                        }else{
                            verificationSoldeDefini = false
                        }
                    }
            }.addOnFailureListener {
                val builder = AlertDialog.Builder(context)
                builder.setTitle("Erreur")
                Toast.makeText(context, R.string.onFailureText, Toast.LENGTH_SHORT).show()
                builder.show()
            }

        // Récupérer le Capital



        db.collection("capital")
            .whereEqualTo("id", id)
            .get().addOnSuccessListener { documents->
                for (data in documents){
                    val idAssistant = data.data["id"].toString()
                    if (idAssistant == id)
                    {
                        etatCapital = true
                        capitalArrayList.add(data!!.data["montant"].toString())
                    }else{
                        etatCapital = false
                    }
                }
            }.addOnFailureListener {
                Toast.makeText(context, R.string.onFailureText, Toast.LENGTH_SHORT).show()
            }

        button.setOnClickListener {
            if (verificationSoldeDefini == false)
            {
                val builder = AlertDialog.Builder(context)
                builder.setMessage("Le solde du jour n'a pas encore été défini par votre assistant.")
                builder.show()
            }else if (etatCapital == false){
                val builder = AlertDialog.Builder(context)
                builder.setMessage("Vous n'avez pas encore défini le capital.")
                builder.show()
            }else{
                main()
            }
        }

        ////////////////////////////////////////////////////////////////////////////////////////////

        // RECHERCHER UN POINT A UNE DATE PRECISE

        datePicker.setOnClickListener {
            val c = Calendar.getInstance()

            val year = c.get(Calendar.YEAR)
            val month = c.get(Calendar.MONTH)
            val day = c.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(
                context, { view, year, monthOfYear, dayOfMonth ->
                    val dat = (dayOfMonth.toString() + "-" + (monthOfYear + 1) + "-" + year)
                    datePicker.setText(dat)
                    filterList(datePicker.text.toString())
                    recyclerView.adapter = adapter
                    recyclerView.layoutManager = LinearLayoutManager(context)
                },
                year,
                month,
                day
            )
            datePickerDialog.show()
        }

        // Initialiser le recyclerView
        db.collection("point")
            .whereEqualTo("id", id)
            .get().addOnSuccessListener { documents->
                progressBar.visibility = View.INVISIBLE
                for (data in documents){
                    val donnee = data.toObject(PointModel::class.java)
                    if (donnee != null) {
                        pointArrayList.add(donnee)
                    }
                    recyclerView.adapter = adapter
                    recyclerView.layoutManager = LinearLayoutManager(context)
                }
            }.addOnFailureListener {
                Toast.makeText(context, R.string.onFailureText, Toast.LENGTH_SHORT).show()
            }

        return view
    }

    private fun filterList(query: String) {
        if (query != null)
        {
            val filteredList = ArrayList<PointModel>()
            for (i in pointArrayList)
            {
                if (i.date.lowercase(Locale.ROOT).contains(query))
                {
                    filteredList.add(i)
                }
            }
            if (filteredList.isNotEmpty())
            {
                adapter.setFilteredList(filteredList)
            }else{
                Toast.makeText(context, "Aucun résultat", Toast.LENGTH_SHORT).show()
            }
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////
    // SAVOIR SI LE POINT DU JOUR DE LA CAISSE EST POSITIF OU NEGATION

    @SuppressLint("ResourceAsColor")
    private fun main() {
        // Total du point
        val array: Array<String> = retrieveArrayList.toTypedArray()
        val ints = array.map { it.toInt() }.toTypedArray()
        val sum = ints.sum()

        // Total Capital
        val array1: Array<String> = capitalArrayList.toTypedArray()
        val ints1 = array1.map { it.toInt() }.toTypedArray()
        val sum1 = ints1.sum()

        val verdict = view?.findViewById<TextView>(R.id.superviseur_point_verdict)

        val totalCaisse = sum1 - sum

        if (totalCaisse > 0)
        {
            resultat.text = totalCaisse.toString()
            verdict!!.text = "Perte"
        }else if (totalCaisse < 0)
        {
            resultat.text = (-1 * totalCaisse).toString()
            verdict!!.text = "Surplus"
        }else{
            resultat.text = totalCaisse.toString()
            verdict!!.text = "Compte OK"
        }
    }
}