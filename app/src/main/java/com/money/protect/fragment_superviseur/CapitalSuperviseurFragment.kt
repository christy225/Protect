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
import java.text.NumberFormat
import java.util.Locale

class CapitalSuperviseurFragment(private val context: SuperviseurActivity) : Fragment() {
    private var db = Firebase.firestore
    private var database = Firebase.firestore
    lateinit var auth: FirebaseAuth
    private lateinit var valeurAcuelleCapital: TextView
    private lateinit var montant: EditText
    private lateinit var checkBox: CheckBox
    private lateinit var button: AppCompatButton
    private lateinit var progressBar: ProgressBar
    private var textWatcher: TextWatcher? = null


    private var nomcomplet: String? = null
    private var email: String? = null
    private var telephone: String? = null
    private var role: String? = null
    private var statut: String? = null
    private var nomcommercial: String? = null
    private var ville: String? = null
    private var quartier: String? = null
    private var creation: String? = null
    private var duration: String? = null
    private var abonnement: String? = null

    private var valeurCapitalDB: String? = null
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
        val capital = data?.getString("capital")

        val nameX = view.findViewById<TextView>(R.id.nom_assistant_capital)
        nameX.text = nom

        var capitalInit = 0

        valeurAcuelleCapital = view.findViewById(R.id.superviseur_capital_valeur_actuel)
        montant = view.findViewById(R.id.superviseur_capital_montant)
        checkBox = view.findViewById(R.id.reduireCapitalSuperviseur)
        button = view.findViewById(R.id.superviseur_capital_btn_register)
        progressBar = view.findViewById(R.id.superviseur_capital_progressbar)

        progressBar.visibility = View.INVISIBLE

        // PERMET DE FORMATTER LA SAISIE DU MONTANT EN MILLIER
        textWatcher = object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                //
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                //
            }

            override fun afterTextChanged(s: Editable?) {
                this@CapitalSuperviseurFragment.formatEditext(s)
            }

        }
        montant.addTextChangedListener(textWatcher)

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
            bundle.putString("capital", capital)
            menuFragment.arguments = bundle
            context.supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container_superviseur, menuFragment)
                .addToBackStack(null)
                .commit()
        }

        // ON RECUPERE LA VALEUR ACTUELLE DU CAPITAL

        database.collection("account")
            .whereEqualTo("id", id)
            .whereEqualTo("superviseur", auth.currentUser?.uid)
            .get()
            .addOnSuccessListener {document->
                if (document.isEmpty)
                {
                    valeurAcuelleCapital.text = capitalInit.toString()

                    // on récupère les données qui nous permettront de faire la mise à jour du capital sans affecter les autres propriétés
                }else{
                    for (datas in document)
                    {
                        capitalInit = datas!!.data["capital"].toString().toInt()
                        valeurCapitalDB = capitalInit.toString()

                        valeurAcuelleCapital.text = DecimalFormat("#,###").format(capitalInit)

                        nomcomplet = datas.data["nomcomplet"].toString()
                        email = datas.data["email"].toString()
                        telephone = datas.data["telephone"].toString()
                        role = datas.data["role"].toString()
                        statut = datas.data["statut"].toString()
                        nomcommercial = datas.data["nomcommercial"].toString()
                        ville = datas.data["ville"].toString()
                        quartier = datas.data["quartier"].toString()
                        creation = datas.data["creation"].toString()
                        duration = datas.data["duration"].toString()
                        abonnement = datas.data["abonnement"].toString()
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
                val builder = AlertDialog.Builder(context)
                builder.setMessage("Souhaitez-vous vraiment modifier votre capital ?")
                builder.setPositiveButton(android.R.string.yes){dialog, which ->
                    // ON RECUPERE LA NOUVELLE VALEUR DU CAPITAL AVANT L'ENVOI EN BASE DE DONNEES
                    //formater le montant
                    val theAmount = montant.text.toString()
                    val caractere = ','
                    val theNewAmount = theAmount.filter { it != caractere }

                    val valeur1 = theNewAmount.toInt() + valeurCapitalDB!!.toInt()
                    val valeur2 = valeurCapitalDB!!.toInt() - theNewAmount.toInt()

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
                        "id" to  id,
                        "nomcomplet" to  nomcomplet,
                        "email" to  email,
                        "superviseur" to  auth.currentUser?.uid,
                        "telephone" to  telephone,
                        "role" to  role,
                        "statut" to  statut.toBoolean(),
                        "module" to  module,
                        "nomcommercial" to  nomcommercial,
                        "ville" to  ville,
                        "quartier" to  quartier,
                        "creation" to  creation,
                        "duration" to  duration,
                        "abonnement" to  abonnement,
                        "capital" to  nouvelleValeurCapital.toString()
                    )
                    db.collection("account")
                        .document(id!!)
                        .set(capitalMap)
                        .addOnSuccessListener {
                            Toast.makeText(context, "Enregistré avec succès", Toast.LENGTH_SHORT).show()
                            montant.text.clear()
                            progressBar.visibility = View.INVISIBLE
                            button.isEnabled = true
                        }.addOnFailureListener {
                            val build = AlertDialog.Builder(context)
                            build.setMessage(R.string.onFailureText)
                            build.show()
                            progressBar.visibility = View.INVISIBLE
                            button.isEnabled = true
                        }
                }
                builder.setNegativeButton(android.R.string.no){dialog, which ->
                    // ne rien faire
                }
                builder.show()
            }
        }else{
                Toast.makeText(context, "Aucune connexion internet", Toast.LENGTH_SHORT).show()
            }
        }
        return view
    }

    // PERMET DE FORMATTER LA SAISIE DU MONTANT EN MILLIER
    private fun formatEditext(s: Editable?) {
        if (!s.isNullOrBlank())
        {
            val originalText = s.toString().replace(",","")
            val number = originalText.toBigDecimalOrNull()

            if (number != null)
            {
                val formattedText = NumberFormat.getNumberInstance(Locale.US).format(number)
                montant.removeTextChangedListener(textWatcher)
                montant.setText(formattedText)
                montant.setSelection(formattedText.length)
                montant.addTextChangedListener(textWatcher)
            }
        }
    }
}