package com.money.protect.fragment_assistant

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.money.protect.MainActivity
import com.money.protect.R
import com.money.protect.adapter.OperationAdapter
import com.money.protect.fragment_assistant.checkInternet.checkForInternet
import com.money.protect.models.TransactionModel
import java.util.Calendar

class SearchAdvancedFragment(private val context: MainActivity) : Fragment() {
    private var db = Firebase.firestore
    private lateinit var auth : FirebaseAuth
    private lateinit var numero: EditText
    lateinit var montant: EditText
    private lateinit var datePicker: EditText
    lateinit var recyclerView: RecyclerView
    private lateinit var transactionArrayList: ArrayList<TransactionModel>
    lateinit var adapter: OperationAdapter
    lateinit var button: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var libellNoResult: TextView
    @RequiresApi(Build.VERSION_CODES.M)
    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_assistant_search_advanced, container, false)
        auth = FirebaseAuth.getInstance()
        if (!checkForInternet(context)) {
            Toast.makeText(context, "Aucune connexion internet", Toast.LENGTH_SHORT).show()
        }

        requireActivity().onBackPressedDispatcher.addCallback(context) {

        }

        db = FirebaseFirestore.getInstance()

        progressBar = view.findViewById(R.id.progressBarSearchAdvanced)

        datePicker = view.findViewById(R.id.datePickerAdvanced)
        recyclerView = view.findViewById(R.id.recyclerViewSearchAdvanced)
        numero = view.findViewById(R.id.numberAdvanced)
        montant = view.findViewById(R.id.montantAdvanced)
        button = view.findViewById(R.id.btnSearchAdvanced)
        libellNoResult = view.findViewById(R.id.searchAdvancedNoResult)

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

        button.setOnClickListener {
            if (numero.text.isEmpty() || montant.text.isEmpty() || datePicker.text.isEmpty())
            {
                Toast.makeText(context, "Veuillez saisir tous les champs", Toast.LENGTH_SHORT).show()
            }else{
                progressBar.visibility = View.VISIBLE
                transactionArrayList = arrayListOf()
                // Initialiser le recyclerView
                db.collection("operation")
                    .whereEqualTo("id", auth.currentUser?.uid)
                    .whereEqualTo("telephone", numero.text.toString())
                    .whereEqualTo("montant", montant.text.toString())
                    .whereEqualTo("date", datePicker.text.toString())
                    .get().addOnSuccessListener { documents->
                        if (documents.isEmpty){
                            libellNoResult.visibility = View.VISIBLE
                            progressBar.visibility = View.INVISIBLE
                        }else{
                            for (data in documents){
                                val transaction = data.toObject(TransactionModel::class.java)
                                transactionArrayList.add(transaction)
                                progressBar.visibility = View.INVISIBLE
                                transactionArrayList.sortByDescending { it.heure }
                                adapter = OperationAdapter(context, transactionArrayList)
                                recyclerView.adapter = adapter
                                recyclerView.layoutManager = LinearLayoutManager(context)
                            }
                        }
                    }.addOnFailureListener {
                        Toast.makeText(context, R.string.onFailureText, Toast.LENGTH_SHORT).show()
                    }
            }
        }

        return view
    }

}