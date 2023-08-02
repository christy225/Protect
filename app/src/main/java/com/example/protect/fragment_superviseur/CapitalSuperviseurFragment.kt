package com.example.protect.fragment_superviseur

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.Fragment
import com.example.protect.R
import com.example.protect.SuperviseurActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class CapitalSuperviseurFragment(private val context: SuperviseurActivity) : Fragment() {
    private var db = Firebase.firestore
    private var database = Firebase.firestore
    lateinit var auth: FirebaseAuth
    lateinit var valeurAcuelleCapital: TextView
    lateinit var montant: EditText
    lateinit var button: AppCompatButton
    lateinit var progressBar: ProgressBar
    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_superviseur_capital, container, false)
        auth = FirebaseAuth.getInstance()
        database = FirebaseFirestore.getInstance()

        var capitalInit = 0

        valeurAcuelleCapital = view.findViewById(R.id.superviseur_capital_valeur_actuel)
        montant = view.findViewById(R.id.superviseur_capital_montant)
        button = view.findViewById(R.id.superviseur_capital_btn_register)
        progressBar = view.findViewById(R.id.superviseur_capital_progressbar)

        progressBar.visibility = View.INVISIBLE

        // ON RECUPERE LA VALEUR ACTUELLE DU CAPITAL

        database.collection("capital")
            .whereEqualTo("id", auth.currentUser?.uid)
            .get()
            .addOnSuccessListener {document->
                if (document.isEmpty)
                {
                    valeurAcuelleCapital.text = capitalInit.toString()
                }else{
                    for (data in document)
                    {
                        capitalInit = data!!.data["montant"].toString().toInt()
                        valeurAcuelleCapital.text = capitalInit.toString()
                    }
                }
            }.addOnFailureListener {
                val builder = AlertDialog.Builder(context)
                builder.setTitle("Alerte").setIcon(R.drawable.icone)
                builder.setMessage(R.string.onFailureText)
                builder.show()
            }

        button.setOnClickListener {
            if (montant.text.isEmpty())
            {
                val builder = AlertDialog.Builder(context)
                builder.setTitle("Alerte").setIcon(R.drawable.icone)
                builder.setMessage("Veuillez entrer le montant du capital SVP.")
                builder.show()
            }else{

                // ON RECUPERE LA NOUVELLE VALEUR DU CAPITAL AVANT L'ENVOI EN BASE DE DONNEES

                val nouvelleValeurCapital = montant.text.toString().toInt() + valeurAcuelleCapital.text.toString().toInt()

                button.isEnabled = false
                progressBar.visibility = View.VISIBLE
                val capitalMap = hashMapOf(
                    "id" to auth.currentUser?.uid,
                    "montant" to nouvelleValeurCapital.toString()
                )
                db.collection("capital")
                    .document(auth.currentUser!!.uid)
                    .set(capitalMap)
                    .addOnSuccessListener {
                        Toast.makeText(context, "Enregistré avec succès", Toast.LENGTH_SHORT).show()
                        montant.text.clear()
                        progressBar.visibility = View.INVISIBLE
                        button.isEnabled = true
                    }.addOnFailureListener {
                        val builder = AlertDialog.Builder(context)
                        builder.setTitle("Alerte").setIcon(R.drawable.icone)
                        builder.setMessage(R.string.onFailureText)
                        builder.show()
                        progressBar.visibility = View.INVISIBLE
                        button.isEnabled = true
                    }
            }
        }
        return view
    }
}