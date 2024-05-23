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
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.money.protect.R
import com.money.protect.SuperviseurActivity
import com.money.protect.adapter.OperationAdapterSuperviseur
import com.money.protect.models.TransactionModel
import java.util.Calendar

class TransactionAssistantFragment(private val context: SuperviseurActivity) : Fragment() {
    private var db = Firebase.firestore
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: OperationAdapterSuperviseur
    private lateinit var operationArrayList: ArrayList<TransactionModel>
    private lateinit var datePicker: EditText
    private lateinit var linkToHomeListPoint: ImageView
    private lateinit var progressBar: ProgressBar
    private lateinit var identifiant: String
    private lateinit var info: TextView
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
        info = view.findViewById(R.id.progressBarMsgSuperviseurTransac)

        info.visibility = View.GONE

        val data = arguments

        identifiant = data?.getString("id").toString()
        val nom = data?.getString("nom")
        val module = data?.getString("module")
        val capital = data?.getString("capital")

        datePicker = view.findViewById(R.id.superviseur_transac_datePicker)
        linkToHomeListPoint = view.findViewById(R.id.backToListHomeSuperviseur11)
        progressBar = view.findViewById(R.id.progressBarTransacSuperviseur)

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
                    info.visibility = View.INVISIBLE
                    progressBar.visibility = View.VISIBLE
                    operationArrayList = arrayListOf()
                    db.collection("operation")
                        .whereEqualTo("id", identifiant)
                        .whereEqualTo("date", datePicker.text.toString())
                        .get()
                        .addOnSuccessListener { documents->
                            if (documents.isEmpty){
                                info.visibility = View.VISIBLE
                                progressBar.visibility = View.INVISIBLE
                            }else{
                                progressBar.visibility = View.INVISIBLE
                                info.visibility = View.INVISIBLE
                                for (datas in documents) {
                                    val transaction = datas.toObject(TransactionModel::class.java)
                                    operationArrayList.add(transaction)
                                    operationArrayList.sortByDescending { it.heure }
                                    adapter = OperationAdapterSuperviseur(context, operationArrayList)
                                    recyclerView.adapter = OperationAdapterSuperviseur(context, operationArrayList)
                                    recyclerView.layoutManager = LinearLayoutManager(context)
                                }
                            }
                        }.addOnFailureListener {
                            Toast.makeText(context, "Une erreur s'est produite", Toast.LENGTH_SHORT).show()
                        }
                },
                year,
                month,
                day
            )
            datePickerDialog.show()
        }

        val menuFragment = MenuAssistSuperviseur(context)
        linkToHomeListPoint.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("id", identifiant)
            bundle.putString("nom", nom)
            bundle.putString("module", module)
            bundle.putString("capital", capital)
            menuFragment.arguments = bundle
            context.supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container_superviseur, menuFragment)
                .addToBackStack(null)
                .commit()
        }

        return view
    }

}