package com.money.protect.fragment_superviseur

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatImageButton
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.money.protect.R
import com.money.protect.SuperviseurActivity
import com.money.protect.adapter.LePointAdapter
import com.money.protect.models.PointModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.money.protect.fragment_assistant.checkInternet.checkForInternet
import java.text.DecimalFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar

class HomeSuperviseurFragment(private val context: SuperviseurActivity) : Fragment() {
    private var db = Firebase.firestore
    private lateinit var auth: FirebaseAuth
    private lateinit var resultat: TextView
    private lateinit var button: Button

    private lateinit var adapter: LePointAdapter

    private lateinit var currencyLib: TextView
    private lateinit var capitalArrayList: ArrayList<String>
    private lateinit var retrieveArrayList: ArrayList<String>

    private lateinit var datePicker: EditText
    private lateinit var buttonSearchDate: AppCompatImageButton
    private lateinit var recyclerView: RecyclerView
    private lateinit var pointArrayList: ArrayList<PointModel>

    private lateinit var progressBar: ProgressBar
    private lateinit var imageSuccess: ImageView

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
        currencyLib = view.findViewById(R.id.currencyLib)

        val data = arguments
        val id = data?.getString("id")
        val nom = data?.getString("nom")
        val assignation = data?.getString("module")
        val capital = data?.getString("capital")

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
        imageSuccess = view.findViewById(R.id.success_point)

        retrieveArrayList = arrayListOf()
        capitalArrayList = arrayListOf()

        pointArrayList = arrayListOf()

        adapter = LePointAdapter(context, pointArrayList)

        val linkToHome = view.findViewById<ImageView>(R.id.backToHomeSuperviseur)
        linkToHome.setOnClickListener {
            context.supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container_superviseur, ListAssistantHome(context))
                .addToBackStack(null)
                .commit()
        }

        // Open POPUP
        val buttonMenu = view.findViewById<ImageButton>(R.id.menuIconSuperviseur)
        val menuFragment = MenuAssistSuperviseur(context)
        buttonMenu.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("id", id)
            bundle.putString("nom", nom)
            bundle.putString("module", assignation)
            bundle.putString("capital", capital)
            menuFragment.arguments = bundle
            context.supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container_superviseur, menuFragment)
                .addToBackStack(null)
                .commit()
        }

        // Recupérer le point du jour
        
        val current = LocalDateTime.now()
        val formatterDate = DateTimeFormatter.ofPattern("d-M-yyyy")
        val dateFormatted = current.format(formatterDate)

        db.collection("point")
            .whereEqualTo("id", id)
            .whereEqualTo("superviseur", auth.currentUser?.uid)
            .whereEqualTo("date", dateFormatted)
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
                builder.setMessage(R.string.onFailureText)
                builder.show()
            }

        // Récupérer le Capital

        if (capital == "0")
        {
            etatCapital = false
        }else{
            etatCapital = true
            capitalArrayList.add(capital.toString())
        }

        button.isEnabled = true
        button.setOnClickListener {
            if (!etatCapital)
            {
                val builder = AlertDialog.Builder(context)
                builder.setMessage("Vous n'avez pas encore défini le capital.")
                builder.show()
            }else if (!verificationSoldeDefini){
                val builder = AlertDialog.Builder(context)
                builder.setMessage("Le solde du jour n'a pas encore été défini par votre assistant.")
                builder.show()
            }else{
                currencyLib.textSize = 10f
                main()
                button.isEnabled = false
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

                    progressBar.visibility = View.VISIBLE
                    // Initialiser le recyclerView
                    db.collection("point")
                        .whereEqualTo("id", id)
                        .whereEqualTo("superviseur", auth.currentUser?.uid)
                        .whereEqualTo("date", datePicker.text.toString())
                        .get().addOnSuccessListener { documents->
                            progressBar.visibility = View.INVISIBLE
                            for (dataz in documents){
                                val donnee = dataz.toObject(PointModel::class.java)
                                pointArrayList.add(donnee)
                                pointArrayList.sortByDescending { it.date }
                                recyclerView.adapter = adapter
                                recyclerView.layoutManager = LinearLayoutManager(context)
                            }
                        }.addOnFailureListener {
                            Toast.makeText(context, R.string.onFailureText, Toast.LENGTH_SHORT).show()
                        }
                },
                year,
                month,
                day
            )
            datePickerDialog.show()
        }

        return view
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

            resultat.text = DecimalFormat("#,###").format(totalCaisse)
            verdict!!.text = "Perte"
        }else if (totalCaisse < 0)
        {
            val res = -1 * totalCaisse
            resultat.text = DecimalFormat("#,###").format(res)
            verdict!!.text = "Surplus"
        }else{
            resultat.visibility = View.INVISIBLE
            imageSuccess.visibility = View.VISIBLE
            currencyLib.visibility = View.INVISIBLE
            verdict!!.text = "Point OK"
        }
    }
}