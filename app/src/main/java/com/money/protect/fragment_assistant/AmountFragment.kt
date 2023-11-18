package com.money.protect.fragment_assistant

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.money.protect.MainActivity
import com.money.protect.R
import com.money.protect.adapter.OperationAdapter
import com.money.protect.models.TransactionModel
import java.util.ArrayList

class AmountFragment(private val context: MainActivity) : Fragment() {
    private var db = Firebase.firestore
    lateinit var searchView: SearchView
    lateinit var adapter: OperationAdapter
    lateinit var recyclerView: RecyclerView
    lateinit var resultArrayList: ArrayList<TransactionModel>
    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_assistant_search_amount, container, false)

        db = FirebaseFirestore.getInstance()
        searchView = view.findViewById(R.id.searchAmount)
        recyclerView = view.findViewById(R.id.recyclerSearchMontant)
        resultArrayList = arrayListOf()
        adapter = OperationAdapter(context, resultArrayList)

        db.collection("operation")
            .orderBy("date", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents->
                for (data in documents){
                    val transaction = data.toObject(TransactionModel::class.java)
                    if (transaction != null) {
                        resultArrayList.add(transaction)
                    }
                }
            }.addOnFailureListener {
                Toast.makeText(context, R.string.onFailureText, Toast.LENGTH_SHORT).show()
            }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterList(newText)
                recyclerView.adapter = adapter
                recyclerView.layoutManager = LinearLayoutManager(context)
                return true
            }

        })
        return view
    }

    private fun filterList(query: String?) {
        if (query != null)
        {
            val filteredList = ArrayList<TransactionModel>()
            for (i in resultArrayList)
            {
                if (i.montant.startsWith(query))
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
                builder.setMessage("Aucun résultat")
                builder.show()
            }
        }
    }
}