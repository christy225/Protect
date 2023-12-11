package com.money.protect.fragment_superviseur

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.toObject
import com.google.firebase.ktx.Firebase
import com.money.protect.R
import com.money.protect.SuperviseurActivity
import com.money.protect.adapter.CumulAdapter
import com.money.protect.models.TransactionModel
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class CumulTransactionSuperviseur(
    private val context: SuperviseurActivity
) : Fragment() {
    private var db = Firebase.firestore
    private lateinit var cumulArrayList: ArrayList<TransactionModel>
    private lateinit var recyclerView: RecyclerView
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_superviseur_cumul_transaction, container, false)
        db = FirebaseFirestore.getInstance()
        recyclerView = view.findViewById(R.id.recyclerViewCumul)
        cumulArrayList = arrayListOf()

        // Recupérer le point du jour

        val current = LocalDateTime.now()
        val formatterDate = DateTimeFormatter.ofPattern("d-M-yyyy")
        val dateFormatted = current.format(formatterDate)


        return view
    }

}