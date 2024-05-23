package com.money.protect.fragment_assistant

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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

class SearchPhoneFragment(private val context: MainActivity) : Fragment() {
    private var db = Firebase.firestore
    private lateinit var auth : FirebaseAuth
    private lateinit var recyclerViewSearch: RecyclerView
    private lateinit var transactionArrayList: ArrayList<TransactionModel>
    private lateinit var adapter: OperationAdapter
    private lateinit var searchPhone: EditText
    private lateinit var progressBar: ProgressBar
    private lateinit var btnSearch: AppCompatImageButton
    private lateinit var libellNoResult: TextView
    @RequiresApi(Build.VERSION_CODES.M)
    @SuppressLint("MissingInflatedId")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_assistant_search_phone, container, false)
        auth = FirebaseAuth.getInstance()

        if (!checkForInternet(context)) {
            Toast.makeText(context, "Aucune connexion internet", Toast.LENGTH_SHORT).show()
        }

        requireActivity().onBackPressedDispatcher.addCallback(context) {

        }

        db = FirebaseFirestore.getInstance()
        recyclerViewSearch = view.findViewById(R.id.recyclerViewSearch)
        searchPhone = view.findViewById(R.id.searchPhone)
        progressBar = view.findViewById(R.id.progressBarSearchNumber)
        libellNoResult = view.findViewById(R.id.searchPhoneNoResult)

        btnSearch = view.findViewById(R.id.searchPhoneButton)

        // BLOQUER LE NOMBRE DE CARACTERES DE SAISIE
        searchPhone.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, start: Int, count: Int, after: Int) {
                // Avant que le texte change
            }

            override fun onTextChanged(charSequence: CharSequence, start: Int, before: Int, count: Int) {
                // Pendant que le texte change
            }

            override fun afterTextChanged(editable: Editable) {
                // Après que le texte a changé

                // Vérifier si la longueur du texte est supérieure à la limite
                val maxLength = 10
                if (editable.length > maxLength) {
                    // Supprimer les caractères excédentaires
                    editable.delete(maxLength, editable.length)
                }
            }
        })


        btnSearch.setOnClickListener {
            if (searchPhone.text.length >= 10){
                progressBar.visibility = View.VISIBLE
                transactionArrayList = arrayListOf()
                db.collection("operation")
                    .whereEqualTo("id", auth.currentUser?.uid)
                    .whereEqualTo("telephone", searchPhone.text.toString())
                    .get()
                    .addOnSuccessListener { documents->
                        if (documents.isEmpty){
                            libellNoResult.visibility = View.VISIBLE
                            progressBar.visibility = View.INVISIBLE
                        }else{
                            for (data in documents){
                                val transaction = data.toObject(TransactionModel::class.java)
                                transactionArrayList.add(transaction)
                                progressBar.visibility = View.INVISIBLE
                                transactionArrayList.sortByDescending { it.date }
                                adapter = OperationAdapter(context, transactionArrayList)
                                recyclerViewSearch.adapter = adapter
                                recyclerViewSearch.layoutManager = LinearLayoutManager(context)
                            }
                        }
                    }.addOnFailureListener {
                        Toast.makeText(context, R.string.onFailureText, Toast.LENGTH_SHORT).show()
                    }
            }else{
                Toast.makeText(context, "Veuillez saisir entièrement le numéro", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }


}