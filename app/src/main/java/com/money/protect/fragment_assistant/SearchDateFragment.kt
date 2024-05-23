package com.money.protect.fragment_assistant

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatImageButton
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.money.protect.MainActivity
import com.money.protect.R
import com.money.protect.models.TransactionModel
import com.money.protect.adapter.OperationAdapter
import com.money.protect.fragment_assistant.checkInternet.checkForInternet
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.Calendar

class SearchDateFragment(private val context: MainActivity) : Fragment() {
    private var db = Firebase.firestore
    private lateinit var auth : FirebaseAuth
    private lateinit var datePicker: EditText
    private lateinit var btnSearch: AppCompatImageButton
    private lateinit var transactionArrayList: ArrayList<TransactionModel>
    lateinit var adapter: OperationAdapter
    lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var libellNoResult: TextView
    @SuppressLint("MissingInflatedId")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_assistant_search_date, container, false)
        auth = FirebaseAuth.getInstance()

        if (!checkForInternet(context)) {
            Toast.makeText(context, "Aucune connexion internet", Toast.LENGTH_SHORT).show()
        }

        requireActivity().onBackPressedDispatcher.addCallback(context) {

        }

        db = FirebaseFirestore.getInstance()
        datePicker = view.findViewById(R.id.datePicker)
        btnSearch = view.findViewById(R.id.searchDateButton)
        recyclerView = view.findViewById(R.id.recyclerViewSearchDate)
        progressBar = view.findViewById(R.id.progressBarSearchdate)
        libellNoResult = view.findViewById(R.id.searchDateNoResult)

        // Récupérer la date entrée par l'utilistauer
        datePicker.setOnClickListener {
            val c = Calendar.getInstance()

            val year = c.get(Calendar.YEAR)
            val month = c.get(Calendar.MONTH)
            val day = c.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(
                context, { view, year, monthOfYear, dayOfMonth ->
                    val dat = (dayOfMonth.toString() + "-" + (monthOfYear + 1) + "-" + year)
                    datePicker.setText(dat)

                },
                year,
                month,
                day
            )
            datePickerDialog.show()
        }

        btnSearch.setOnClickListener {
            progressBar.visibility = View.VISIBLE
            transactionArrayList = arrayListOf()
            // Initialiser le recyclerView
            db.collection("operation")
                .whereEqualTo("date", datePicker.text.toString())
                .whereEqualTo("id", auth.currentUser?.uid)
                .get().addOnSuccessListener { documents->
                    if (documents.isEmpty) {
                        libellNoResult.visibility = View.VISIBLE
                        progressBar.visibility = View.INVISIBLE
                    }else{
                        for (data in documents){
                            val transaction = data.toObject(TransactionModel::class.java)
                            transactionArrayList.add(transaction)
                            // On met à jour automatiquement le tableau de recherche
                            transactionArrayList.sortByDescending { it.heure }
                            progressBar.visibility = View.INVISIBLE
                            adapter = OperationAdapter(context, transactionArrayList)
                            recyclerView.adapter = adapter
                            recyclerView.layoutManager = LinearLayoutManager(context)
                        }
                    }
                }.addOnFailureListener {
                    Toast.makeText(context, R.string.onFailureText, Toast.LENGTH_SHORT).show()
                }

        }

        return view
    }

}