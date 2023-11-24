package com.money.protect.fragment_assistant

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.money.protect.MainActivity
import com.money.protect.R
import com.money.protect.fragment_assistant.checkInternet.checkForInternet
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class FactureFragment(private val context: MainActivity) : Fragment() {
    private val db = Firebase.firestore
    lateinit var auth: FirebaseAuth
    private lateinit var button: Button
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_facture, container, false)
        button = view.findViewById(R.id.btn_register_input_facture)
        auth = FirebaseAuth.getInstance()

        button.setOnClickListener {
            if (checkForInternet(context))
            {
                val operateur = view.findViewById<Spinner>(R.id.operateur_facture)
                val montant = view.findViewById<EditText>(R.id.montant_input_facture)

                if (montant.text.isEmpty() || operateur.selectedItem.toString() == "Sélectionner")
                {
                    val builder = AlertDialog.Builder(context)
                    builder.setMessage("Veuillez saisir tous les champs")
                    builder.show()
                }else{
                    if (operateur.selectedItem.toString() == "MTN"){
                        val syntaxe = "*188" + Uri.encode("#")
                        val callIntent = Intent(Intent.ACTION_CALL)
                        callIntent.data = Uri.parse("tel:$syntaxe")
                        startActivity(callIntent)
                    }else if (operateur.selectedItem.toString() == "MOOV"){
                        val syntaxe = "*155*4" + Uri.encode("#")
                        val callIntent = Intent(Intent.ACTION_CALL)
                        callIntent.data = Uri.parse("tel:$syntaxe")
                        startActivity(callIntent)
                    }

                    // Générer la date
                    val current = LocalDateTime.now()
                    val formatterDate = DateTimeFormatter.ofPattern("d-M-yyyy")
                    val dateFormatted = current.format(formatterDate)

                    // Générer l'heure
                    val formatterHour = DateTimeFormatter.ofPattern("HH:mm")
                    val hourFormatted = current.format(formatterHour)

                    // Dans le cas où l'utilisateur n'a pas enregistré d'image on met la valeur à NULL
                    val operationMap = hashMapOf(
                        "id" to auth.currentUser?.uid,
                        "date" to dateFormatted,
                        "heure" to hourFormatted,
                        "operateur" to operateur.selectedItem.toString(),
                        "telephone" to "Paiement Facture",
                        "montant" to montant.text.toString().trim(),
                        "typeoperation" to "Facture",
                        "url" to "null"
                    )

                    db.collection("operation").add(operationMap).addOnCompleteListener {
                        // Ne rien faire ici
                    }.addOnFailureListener {
                        val builder = AlertDialog.Builder(context)
                        builder.setTitle("Alerte")
                        builder.setMessage(R.string.onFailureText)
                        builder.show()
                    }
                }
            }else{
                Toast.makeText(context, "Aucune connexion internet", Toast.LENGTH_SHORT).show()
            }
        }
        return view
    }
}