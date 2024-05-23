package com.money.protect.fragment_assistant

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
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
import java.util.ArrayList

class SearchAmountFragment(private val context: MainActivity) : Fragment() {
    private var db = Firebase.firestore
    private lateinit var auth : FirebaseAuth
    private lateinit var searchAmount: EditText
    private lateinit var adapter: OperationAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var transactionArrayList: ArrayList<TransactionModel>
    private var textWatcher: TextWatcher? = null
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
        val view = inflater.inflate(R.layout.fragment_assistant_search_amount, container, false)
        auth = FirebaseAuth.getInstance()
        if (!checkForInternet(context)) {
            Toast.makeText(context, "Aucune connexion internet", Toast.LENGTH_SHORT).show()
        }

        requireActivity().onBackPressedDispatcher.addCallback(context) {

        }

        db = FirebaseFirestore.getInstance()
        searchAmount = view.findViewById(R.id.searchAmount)
        recyclerView = view.findViewById(R.id.recyclerSearchMontant)
        progressBar = view.findViewById(R.id.progressBarSearchAmount)
        btnSearch = view.findViewById(R.id.searchAmountButton)
        libellNoResult = view.findViewById(R.id.searchAmountNoResult)

        // PERMET DE FORMATTER LA SAISIE DU MONTANT EN MILLIER
        textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                //
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                //
            }

            override fun afterTextChanged(s: Editable?) {
                //
            }

        }
        searchAmount.addTextChangedListener(textWatcher)

        btnSearch.setOnClickListener{
            if (searchAmount.text.length >= 3)
            {
                progressBar.visibility = View.VISIBLE
                transactionArrayList = arrayListOf()
                db.collection("operation")
                    .whereEqualTo("id", auth.currentUser?.uid)
                    .whereEqualTo("montant", searchAmount.text.toString())
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
                                recyclerView.adapter = adapter
                                recyclerView.layoutManager = LinearLayoutManager(context)
                            }
                        }
                    }.addOnFailureListener {
                        Toast.makeText(context, R.string.onFailureText, Toast.LENGTH_SHORT).show()
                    }
            }else{
                val builder = AlertDialog.Builder(context)
                builder.setMessage("Veuillez saisir un montant valide")
                builder.show()
            }
        }

        return view
    }
}