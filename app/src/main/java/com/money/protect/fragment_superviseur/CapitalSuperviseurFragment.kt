package com.money.protect.fragment_superviseur

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.Fragment
import com.money.protect.R
import com.money.protect.SuperviseurActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.money.protect.fragment_assistant.checkInternet.checkForInternet
import java.text.DecimalFormat

class CapitalSuperviseurFragment(private val context: SuperviseurActivity) : Fragment() {
    private var db = Firebase.firestore
    private var database = Firebase.firestore
    lateinit var auth: FirebaseAuth
    private lateinit var valeurAcuelleCapital: TextView
    lateinit var montant: EditText
    private lateinit var checkBox: CheckBox
    lateinit var button: AppCompatButton
    lateinit var progressBar: ProgressBar
    @RequiresApi(Build.VERSION_CODES.M)
    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_superviseur_capital, container, false)
        auth = FirebaseAuth.getInstance()
        database = FirebaseFirestore.getInstance()

        val data = arguments
        val id = data?.getString("id")
        val nom = data?.getString("nom")
        val module = data?.getString("module")

        val nameX = view.findViewById<TextView>(R.id.nom_assistant_capital)
        nameX.text = nom

        var capitalInit = 0

        valeurAcuelleCapital = view.findViewById(R.id.superviseur_capital_valeur_actuel)
        montant = view.findViewById(R.id.superviseur_capital_montant)
        checkBox = view.findViewById(R.id.reduireCapitalSuperviseur)
        button = view.findViewById(R.id.superviseur_capital_btn_register)
        progressBar = view.findViewById(R.id.superviseur_capital_progressbar)

        progressBar.visibility = View.INVISIBLE

        montant.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, start: Int, count: Int, after: Int) {
                // Avant que le texte change
            }

            override fun onTextChanged(charSequence: CharSequence, start: Int, before: Int, count: Int) {
                // Pendant que le texte change
            }

            override fun afterTextChanged(editable: Editable) {
                // Après que le texte a changé

                // Vérifier si la longueur du texte est supérieure à la limite
                val maxLength = 9
                if (editable.length > maxLength) {
                    // Supprimer les caractères excédentaires
                    editable.delete(maxLength, editable.length)
                }
            }
        })

        val linkToHomeListPoint = view.findViewById<ImageView>(R.id.backToListHomeSuperviseur)
        val menuFragment = MenuAssistSuperviseur(context)
        linkToHomeListPoint.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("id", id)
            bundle.putString("nom", nom)
            bundle.putString("module", module)
            menuFragment.arguments = bundle
            context.supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container_superviseur, menuFragment)
                .addToBackStack(null)
                .commit()
        }

        // ON RECUPERE LA VALEUR ACTUELLE DU CAPITAL

        database.collection("capital")
            .whereEqualTo("id", id)
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
            if (checkForInternet(context)) {
            if (montant.text.isEmpty()) {
                val builder = AlertDialog.Builder(context)
                builder.setMessage("Veuillez entrer le montant du capital SVP.")
                builder.show()
            } else {

                // ON RECUPERE LA NOUVELLE VALEUR DU CAPITAL AVANT L'ENVOI EN BASE DE DONNEES

                val valeur1 = montant.text.toString().toInt() + valeurAcuelleCapital.text.toString().toInt()
                val valeur2 = valeurAcuelleCapital.text.toString().toInt() - montant.text.toString().toInt()

                var nouvelleValeurCapital: Int

                if (checkBox.isChecked)
                {
                    nouvelleValeurCapital = valeur2
                }else{
                    nouvelleValeurCapital = valeur1
                }

                button.isEnabled = false
                progressBar.visibility = View.VISIBLE
                val capitalMap = hashMapOf(
                    "id" to id,
                    "montant" to nouvelleValeurCapital.toString()
                )
                db.collection("capital")
                    .document(id!!)
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
        }else{
                Toast.makeText(context, "Aucune connexion internet", Toast.LENGTH_SHORT).show()
            }
        }
        return view
    }
}