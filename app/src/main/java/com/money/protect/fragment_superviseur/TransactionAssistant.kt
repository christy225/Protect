package com.money.protect.fragment_superviseur

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.money.protect.R
import com.money.protect.SuperviseurActivity
import com.money.protect.adapter.OperationAdapter
import com.money.protect.adapter.OperationAdapterSuperviseur
import com.money.protect.models.PointModel
import com.money.protect.models.TransactionModel
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale

class TransactionAssistant(private val context: SuperviseurActivity) : Fragment() {
    private var db = Firebase.firestore
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: OperationAdapterSuperviseur
    private lateinit var operationArrayList: ArrayList<TransactionModel>
    private lateinit var datePicker: EditText
    private lateinit var linkToHomeListPoint: ImageView
    private lateinit var progressBar: ProgressBar
    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_superviseur_transation_assistant, container, false)
        db = FirebaseFirestore.getInstance()
        recyclerView = view.findViewById(R.id.recyclerViewSuperviseurTransaction)
        operationArrayList = arrayListOf()

        val data = arguments

        val id = data?.getString("id")
        val nom = data?.getString("nom")
        val module = data?.getString("module")

        datePicker = view.findViewById(R.id.superviseur_transac_datePicker)
        linkToHomeListPoint = view.findViewById(R.id.backToListHomeSuperviseur11)
        progressBar = view.findViewById(R.id.progressBarTransacSuperviseur)

        progressBar.visibility = View.VISIBLE

        adapter = OperationAdapterSuperviseur(context, operationArrayList)

        val info = view.findViewById<TextView>(R.id.progressBarMsgSuperviseurTransac)

        // Recupérer le point du jour

        val current = LocalDateTime.now()
        val formatterDate = DateTimeFormatter.ofPattern("d-M-yyyy")
        val dateFormatted = current.format(formatterDate)

        db.collection("operation")
            .orderBy("heure", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents->
                progressBar.visibility = View.INVISIBLE
                for (data in documents) {
                    val transaction = data.toObject(TransactionModel::class.java)
                    if (transaction != null) {
                        operationArrayList.add(transaction)
                    }else{
                        info.text = "Aucun enregistrement"
                    }
                    val arrayList = operationArrayList.filter { it.date == dateFormatted && it.id == id} as ArrayList<TransactionModel>
                    recyclerView.adapter = OperationAdapterSuperviseur(context, arrayList)
                    recyclerView.layoutManager = LinearLayoutManager(context)
                }
            }.addOnFailureListener {
                Toast.makeText(context, "Une erreur s'est produite", Toast.LENGTH_SHORT).show()
            }

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

        val homeFragment = HomeSuperviseurFragment(context)
        linkToHomeListPoint.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("id", id)
            bundle.putString("nom", nom)
            bundle.putString("module", module)
            homeFragment.arguments = bundle
            context.supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container_superviseur, homeFragment)
                .addToBackStack(null)
                .commit()
        }

        return view
    }

    private fun filterList(query: String) {
        if (query != null)
        {
            val filteredList = ArrayList<TransactionModel>()
            for (i in operationArrayList)
            {
                if (i.date.lowercase(Locale.ROOT).contains(query))
                {
                    filteredList.add(i)
                }
            }
            if (filteredList.isNotEmpty())
            {
                adapter.setFilterList(filteredList)
            }else{
                Toast.makeText(context, "Aucun résultat", Toast.LENGTH_SHORT).show()
            }
        }
    }

}