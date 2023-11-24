package com.money.protect.fragment_assistant

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.money.protect.MainActivity
import com.money.protect.R
import com.money.protect.models.TransactionModel
import com.money.protect.adapter.OperationAdapter
import com.money.protect.fragment_assistant.checkInternet.checkForInternet
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class SearchPhoneFragment(private val context: MainActivity) : Fragment() {
    private var db = Firebase.firestore
    lateinit var searchView: SearchView
    lateinit var recyclerViewSearch: RecyclerView
    lateinit var transactionArrayList: ArrayList<TransactionModel>
    lateinit var adapter: OperationAdapter
    @RequiresApi(Build.VERSION_CODES.M)
    @SuppressLint("MissingInflatedId")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_assistant_search_phone, container, false)
        if (!checkForInternet(context)) {
            Toast.makeText(context, "Aucune connexion internet", Toast.LENGTH_SHORT).show()
        }

        db = FirebaseFirestore.getInstance()
        recyclerViewSearch = view.findViewById(R.id.recyclerViewSearch)
        searchView = view.findViewById(R.id.searchView)

        transactionArrayList = arrayListOf()
        adapter = OperationAdapter(context, transactionArrayList)

        db.collection("operation")
            .orderBy("date", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents->
            for (data in documents){
                val transaction = data.toObject(TransactionModel::class.java)
                if (transaction != null) {
                    transactionArrayList.add(transaction)
                }
            }
        }.addOnFailureListener {
            Toast.makeText(context, R.string.onFailureText, Toast.LENGTH_SHORT).show()
        }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterList(newText)
                recyclerViewSearch.adapter = adapter
                recyclerViewSearch.layoutManager = LinearLayoutManager(context)
                return true
            }

        })

        return view
    }

    private fun filterList(query: String?) {
        if (query != null)
        {
            val filteredList = ArrayList<TransactionModel>()
            for (i in transactionArrayList)
            {
               if (i.telephone.contains(query))
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
                builder.setMessage("Aucun résultat pour n° téléphone")
                builder.show()
            }
        }
    }

}