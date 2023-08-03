package com.example.protect.fragment_superviseur

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
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatImageButton
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.protect.LoginActivity
import com.example.protect.R
import com.example.protect.SuperviseurActivity
import com.example.protect.adapter.LePointAdapter
import com.example.protect.models.PointModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Calendar
import java.util.Locale

class HomeSuperviseurFragment(private val context: SuperviseurActivity) : Fragment() {
    private var db = Firebase.firestore
    lateinit var auth: FirebaseAuth
    lateinit var capital: TextView
    lateinit var solde: TextView
    lateinit var resultat: TextView
    lateinit var button: Button

    lateinit var adapter: LePointAdapter
    lateinit var capitalArrayList: ArrayList<String>

    lateinit var datePicker: EditText
    lateinit var buttonSearchDate: AppCompatImageButton
    lateinit var recyclerView: RecyclerView
    lateinit var retrieveArrayList: ArrayList<String>
    lateinit var pointArrayList: ArrayList<PointModel>

    lateinit var progressBar: ProgressBar

    private var verificationSoldeDefini: Boolean = false

    @SuppressLint("MissingInflatedId")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_superviseur_home, container, false)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        capital = view.findViewById(R.id.superviseur_point_capital_Vw)
        solde = view.findViewById(R.id.superviseur_point_solde_Vw)
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
                        val builder = AlertDialog.Builder(context)
                        builder.setTitle("Fin Abonnement")
                        builder.setMessage("Votre abonnement a expiré.")
                        builder.show()
                        auth.signOut()
                        val intent = Intent(context, LoginActivity::class.java)
                        startActivity(intent)
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
            .whereEqualTo("date", dateFormatted)
            .orderBy("date", Query.Direction.DESCENDING)
            .limit(30)
            .get()
            .addOnSuccessListener { documents->
                if (!documents.isEmpty)
                {
                    verificationSoldeDefini = true
                    for (data in documents){
                        val superviseurId = data!!.data["superviseur"].toString()
                        if (superviseurId == auth.currentUser?.uid)
                        {
                            retrieveArrayList.add(data!!.data["total"].toString())
                        }
                    }
                }
            }.addOnFailureListener {
                val builder = AlertDialog.Builder(context)
                builder.setTitle("Infos")
                Toast.makeText(context, R.string.onFailureText, Toast.LENGTH_SHORT).show()
                builder.show()
            }

        // Récupérer le Capital

        db.collection("capital")
            .whereEqualTo("id", auth.currentUser?.uid)
            .get().addOnSuccessListener { documents->
                for (data in documents){
                    capitalArrayList.add(data!!.data["montant"].toString())
                }
            }.addOnFailureListener {
                Toast.makeText(context, R.string.onFailureText, Toast.LENGTH_SHORT).show()
            }

        button.setOnClickListener {
            if (verificationSoldeDefini == false)
            {
                val builder = AlertDialog.Builder(context)
                builder.setTitle("Infos")
                builder.setMessage("Le solde du jour n'a pas encore été défini par votre assistant.")
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
            .whereEqualTo("superviseur", auth.currentUser?.uid)
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

        capital.text = sum1.toString()
        solde.text = sum.toString()

        if (totalCaisse > 0)
        {
            resultat.text = totalCaisse.toString()
            verdict!!.text = "Perte"
        }else if (totalCaisse < 0)
        {
            resultat.text = (-1 * totalCaisse).toString()
            verdict!!.text = "Surplus"
        }else{
            resultat.text = "$totalCaisse"
            verdict!!.text = "Compte OK"
        }
    }
}