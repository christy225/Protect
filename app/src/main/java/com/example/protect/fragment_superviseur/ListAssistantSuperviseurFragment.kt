package com.example.protect.fragment_superviseur

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.protect.LoginActivity
import com.example.protect.R
import com.example.protect.SuperviseurActivity
import com.example.protect.adapter.AssistantListAdapter
import com.example.protect.models.AccountModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class ListAssistantSuperviseurFragment(private val context: SuperviseurActivity) : Fragment() {
    private var db = Firebase.firestore
    private lateinit var auth : FirebaseAuth
    lateinit var nomCommercial: TextView
    lateinit var nomComplet: TextView
    lateinit var recyclerView: RecyclerView
    private var assistantArrayList = ArrayList<AccountModel>()
    lateinit var progressBar: ProgressBar
    lateinit var logout: Button
    @SuppressLint("MissingInflatedId", "UseSwitchCompatOrMaterialCode")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_superviseur_list_assistant, container, false)
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        nomCommercial = view.findViewById(R.id.superviseur_list_assistant_nomCommercial)
        nomComplet = view.findViewById(R.id.superviseur_list_assistant_nomcomplet_superviseur)

        recyclerView = view.findViewById(R.id.superviseur_list_assistant_recyclerView)
        progressBar = view.findViewById(R.id.superviseur_list_assistant_progressbar)
        logout = view.findViewById(R.id.superviseur_logout_button)

        // Afficher les infos du superviseur
        db.collection("account")
            .whereEqualTo("id", auth.currentUser?.uid)
            .get()
            .addOnSuccessListener { doc->
                for (dat in doc)
                {
                    nomCommercial.text = dat!!.data["nomcommercial"].toString()
                    nomComplet.text = dat!!.data["nomcomplet"].toString()
                }
            }.addOnFailureListener {
                Toast.makeText(context, R.string.onFailureText, Toast.LENGTH_SHORT).show()
            }

        // Afficher la liste des assistants du superviseur

        db.collection("account")
            .whereEqualTo("superviseur", auth.currentUser!!.uid)
            .get()
            .addOnSuccessListener{ documents->
                progressBar.visibility = View.INVISIBLE
                for (datas in documents){
                    val item = datas.toObject(AccountModel::class.java)
                    if (item != null)
                    {
                        assistantArrayList.add(item)
                    }
                    recyclerView.adapter = AssistantListAdapter(assistantArrayList)
                    recyclerView.layoutManager = LinearLayoutManager(context)
                }
            }.addOnFailureListener {
                Toast.makeText(context, R.string.onFailureText, Toast.LENGTH_SHORT).show()
            }
        // Fin affichage

        logout.setOnClickListener {
            auth.signOut()
            val intent = Intent(context, LoginActivity::class.java)
            startActivity(intent)
        }

        return view
    }
}