package com.money.protect.fragment_superviseur

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Switch
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.money.protect.R
import com.money.protect.SuperviseurActivity

class AnnexeCompteFragment(private val context: SuperviseurActivity) : Fragment() {
    private var db = Firebase.firestore
    private var database = Firebase.firestore
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    private lateinit var switch: Switch
    @SuppressLint("MissingInflatedId", "UseSwitchCompatOrMaterialCode")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_superviseur_annexe, container, false)
        database = FirebaseFirestore.getInstance()

        val data = arguments
        val identifiant = data?.getString("id")
        val nom = data?.getString("nom")
        val assignation = data?.getString("module")

        switch = view.findViewById(R.id.switchAnnexeSuperviseur)

        // Vérifier l'état du double compte
        database.collection("annexe")
            .whereEqualTo("id", identifiant)
            .get()
            .addOnSuccessListener { documents->
                for (donnee in documents)
                {
                    if (donnee != null)
                    {
                        switch.isChecked = donnee.data["statut"].toString().toBoolean()
                        // Désactiver le switch si le compte annexe est activé
                        if (switch.isChecked)
                        {
                            switch.isEnabled = false
                        }
                    }
                }
            }.addOnFailureListener {
                Toast.makeText(context, R.string.onFailureText, Toast.LENGTH_SHORT).show()
            }

        // Activer le double-compte
        switch.setOnClickListener {
            val builder = AlertDialog.Builder(context)
            builder.setTitle("Confirmation")
                .setMessage("Cette opération est irréversible. Souhaitez-vous continuer ?")
                .setPositiveButton("Oui"){ dialog, id->
                    val annexeMap = hashMapOf(
                        "id" to identifiant,
                        "statut" to true,
                        "mainprofil" to false
                    )
                    db.collection("annexe").document(identifiant!!).set(annexeMap).addOnCompleteListener { it->
                        if (it.isSuccessful)
                        {
                            Toast.makeText(context, "Enregistré avec succès", Toast.LENGTH_SHORT).show()
                        }else{
                            Toast.makeText(context, R.string.onFailureText, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                .setNegativeButton("Non"){ dialod, id->
                    // Ne rien faire
                }

            builder.create().show()
        }

        // Open MENU
        val menuFragment = MenuAssistSuperviseur(context)
        val buttonMenu = view.findViewById<ImageView>(R.id.backToMenuSuperviseur)
        buttonMenu.setOnClickListener{
            val bundle = Bundle()
            bundle.putString("id", identifiant)
            bundle.putString("nom", nom)
            bundle.putString("module", assignation)
            menuFragment.arguments = bundle
            context.supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container_superviseur, menuFragment)
                .addToBackStack(null)
                .commit()
        }

        return view
    }
}