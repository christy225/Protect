package com.example.protect.fragment_assistant

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatImageButton
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.protect.MainActivity
import com.example.protect.R
import com.example.protect.models.TransactionModel
import com.example.protect.adapter.OperationAdapter
import com.example.protect.fragment_assistant.checkInternet.checkForInternet
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.Calendar
import java.util.Locale

class SearchDateFragment(private val context: MainActivity) : Fragment() {
    private var db = Firebase.firestore
    lateinit var datePicker: EditText
    lateinit var button: AppCompatImageButton
    lateinit var linkToNumberSearch: TextView
    lateinit var transactionArrayList: ArrayList<TransactionModel>
    lateinit var adapter: OperationAdapter
    lateinit var recyclerView: RecyclerView
    @SuppressLint("MissingInflatedId")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_assistant_search_date, container, false)
        if (!checkForInternet(context)) {
            Toast.makeText(context, "Impossible de se connecter à internet", Toast.LENGTH_SHORT).show()
        }

        db = FirebaseFirestore.getInstance()
        datePicker = view.findViewById(R.id.datePicker)
        button = view.findViewById(R.id.button_search_date)
        transactionArrayList = arrayListOf()
        recyclerView = view.findViewById(R.id.recyclerViewSearchDate)
        adapter = OperationAdapter(context, transactionArrayList)
        linkToNumberSearch = view.findViewById(R.id.number_page_link)

        linkToNumberSearch.setOnClickListener {
            context.supportFragmentManager
                .beginTransaction()
                .replace(R.id.fragment_container, SearchPhoneFragment(context))
                .addToBackStack(null)
                .commit()
        }

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

                    // On met à jour automatiquement le tableau de recherche
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

        return view
    }

    private fun filterList(query: String) {
        if (query != null)
        {
            val filteredList = ArrayList<TransactionModel>()
            for (i in transactionArrayList)
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
                adapter.setFilteredList(filteredList)
                val builder = AlertDialog.Builder(context)
                builder.setMessage("Aucun résultat pour cette date")
                builder.show()
            }
        }
    }

}