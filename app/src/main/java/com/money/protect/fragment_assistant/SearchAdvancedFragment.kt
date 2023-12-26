package com.money.protect.fragment_assistant

import android.annotation.SuppressLint
import android.app.AlertDialog
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
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.money.protect.MainActivity
import com.money.protect.R
import com.money.protect.adapter.OperationAdapter
import com.money.protect.fragment_assistant.checkInternet.checkForInternet
import com.money.protect.models.TransactionModel
import java.util.Calendar
import java.util.Locale

class SearchAdvancedFragment(private val context: MainActivity) : Fragment() {
    private var db = Firebase.firestore
    lateinit var numero: EditText
    lateinit var montant: EditText
    lateinit var datePicker: EditText
    lateinit var recyclerView: RecyclerView
    lateinit var transactionArrayList: ArrayList<TransactionModel>
    lateinit var adapter: OperationAdapter
    lateinit var button: Button
    private lateinit var progressBar: ProgressBar
    @RequiresApi(Build.VERSION_CODES.M)
    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_assistant_search_advanced, container, false)

        if (!checkForInternet(context)) {
            Toast.makeText(context, "Aucune connexion internet", Toast.LENGTH_SHORT).show()
        }

        db = FirebaseFirestore.getInstance()

        transactionArrayList = arrayListOf()

        progressBar = view.findViewById(R.id.progressBarSearchAdvanced)

        datePicker = view.findViewById(R.id.datePickerAdvanced)
        adapter = OperationAdapter(context, transactionArrayList)
        recyclerView = view.findViewById(R.id.recyclerViewSearchAdvanced)
        numero = view.findViewById(R.id.numberAdvanced)
        montant = view.findViewById(R.id.montantAdvanced)
        button = view.findViewById(R.id.btnSearchAdvanced)

        // Initialiser le recyclerView
        db.collection("operation")
            .orderBy("heure", Query.Direction.DESCENDING)
            .get().addOnSuccessListener { documents->
                for (data in documents){
                    val transaction = data.toObject(TransactionModel::class.java)
                    if (transaction != null) {
                        transactionArrayList.add(transaction)
                    }
                }
            }.addOnFailureListener {
                Toast.makeText(context, R.string.onFailureText, Toast.LENGTH_SHORT).show()
            }

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
            progressBar.visibility = View.VISIBLE
            if (numero.text.isEmpty() || montant.text.isEmpty() || datePicker.text.isEmpty())
            {
                Toast.makeText(context, "Veuillez saisir tous les champs", Toast.LENGTH_SHORT).show()
            }else{
                progressBar.visibility = View.INVISIBLE
                filterList(numero.text.toString(), montant.text.toString(), datePicker.text.toString())
                recyclerView.adapter = adapter
                recyclerView.layoutManager = LinearLayoutManager(context)
            }
        }

        return view
    }

    private fun filterList(num: String, mon: String, dat: String) {
        if (num != null && mon != null && dat != null)
        {
            val filteredList = ArrayList<TransactionModel>()
            progressBar.visibility = View.VISIBLE
            for (i in transactionArrayList)
            {
                if (i.telephone.lowercase(Locale.ROOT).contains(num) &&
                    i.montant.lowercase(Locale.ROOT).contains(mon) &&
                    i.date.lowercase(Locale.ROOT).contains(dat))
                {
                    progressBar.visibility = View.INVISIBLE
                    filteredList.add(i)
                }
            }
            if (filteredList.isNotEmpty())
            {
                adapter.setFilteredList(filteredList)
            }else{
                progressBar.visibility = View.INVISIBLE
                adapter.setFilteredList(filteredList)
                val builder = AlertDialog.Builder(context)
                builder.setMessage("Aucun résultat pour cette date")
                builder.show()
            }
        }
    }

}