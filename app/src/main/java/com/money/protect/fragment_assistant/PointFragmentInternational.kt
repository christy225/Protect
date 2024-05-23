package com.money.protect.fragment_assistant

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.money.protect.MainActivity
import com.money.protect.R
import com.money.protect.fragment_assistant.checkInternet.checkForInternet
import com.money.protect.models.PointModel
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class PointFragmentInternational(private val context: MainActivity) : Fragment() {
    private var db = Firebase.firestore
    lateinit var auth: FirebaseAuth
    private lateinit var retraitInter: EditText
    private lateinit var envoiInter: EditText
    private lateinit var pointArrayList: ArrayList<PointModel>
    private lateinit var button: AppCompatButton
    private lateinit var progressBar: ProgressBar
    private lateinit var superviseurId: String
    private lateinit var warning: TextView
    private lateinit var especes: EditText
    private lateinit var divers: EditText
    private var envoiTxt: Int = 0
    private var diversTxt: Int = 0
    private var module: String = ""

    @SuppressLint("MissingInflatedId")
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_assistant_point_international, container, false)
        auth = FirebaseAuth.getInstance()

        if (!checkForInternet(context)) {
            Toast.makeText(context, "Aucune connexion internet", Toast.LENGTH_SHORT).show()
        }

        requireActivity().onBackPressedDispatcher.addCallback(context) {

        }

        // Récupère les données émises par l'utilisateur
        retraitInter = view.findViewById(R.id.point_retrait_international_edt)
        envoiInter = view.findViewById(R.id.point_envoi_international_edt)
        especes = view.findViewById(R.id.point_inter_espece_edt)
        divers = view.findViewById(R.id.point_inter_divers_edt)
        button = view.findViewById(R.id.btn_register_point_international)
        warning = view.findViewById(R.id.info_avant_enregistrement_international)
        progressBar = view.findViewById(R.id.progressBarPoint_international)
        pointArrayList = arrayListOf()

        val info = view.findViewById<TextView>(R.id.info_text_point_international)

        info.visibility = View.INVISIBLE
        button.visibility = View.VISIBLE
        warning.visibility = View.VISIBLE

        val formatLimitation = object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, start: Int, count: Int, after: Int) {
                // Avant que le texte change
            }

            override fun onTextChanged(charSequence: CharSequence, start: Int, before: Int, count: Int) {
                // Pendant que le texte change
            }

            override fun afterTextChanged(editable: Editable) {
                // Après que le texte a changé

                // Vérifier si la longueur du texte est supérieure à la limite
                val maxLength = 8
                if (editable.length > maxLength) {
                    // Supprimer les caractères excédentaires
                    editable.delete(maxLength, editable.length)
                }
            }
        }

        retraitInter.addTextChangedListener(formatLimitation)
        envoiInter.addTextChangedListener(formatLimitation)
        especes.addTextChangedListener(formatLimitation)
        divers.addTextChangedListener(formatLimitation)


        val backButton = view.findViewById<ImageView>(R.id.pointbackButtonInternational)
        backButton.setOnClickListener {
            context.supportFragmentManager
                .beginTransaction()
                .replace(R.id.fragment_container, HomeFragment(context))
                .addToBackStack(null)
                .commit()
        }

        progressBar.visibility = View.INVISIBLE

        // Générer la date
        val current = LocalDateTime.now()
        val formatterDate = DateTimeFormatter.ofPattern("d-M-yyyy")
        val dateFormatted = current.format(formatterDate)

        // RECUPERER L'ID SUPERVISEUR POUR L'ENREGISTREMENT
        db.collection("account")
            .whereEqualTo("id", auth.currentUser?.uid)
            .get()
            .addOnSuccessListener { document->
                for (data in document)
                {
                    superviseurId = data!!.data["superviseur"].toString()
                    module = data.data["module"].toString()
                }
            }

        // VERIFIER QUE L'UTILISATEUR N'A PAS DEJA ENVOYE UN POINT
        db.collection("point")
            .whereEqualTo("id", auth.currentUser?.uid)
            .whereEqualTo("superviseur", superviseurId)
            .whereEqualTo("date", dateFormatted)
            .get()
            .addOnSuccessListener {docs->
                for (datax in docs)
                {
                    val assistantId = datax!!.data["id"].toString()
                    if (assistantId == auth.currentUser?.uid.toString())
                    {
                        info.visibility = View.VISIBLE
                        button.visibility = View.INVISIBLE
                        warning.visibility = View.INVISIBLE
                    }
                }
            }

        button.setOnClickListener {

            // SI IL N'YA PAS ENCORE EU DE POINT CE JOUR ALORS ON VERIFIE QUE TOUS LES CHAMPS OBLIGATOIRES SONT REMPLIS
            if (retraitInter.text.isEmpty() || especes.text.isEmpty())
            {
                val builder = AlertDialog.Builder(context)
                builder.setTitle("Infos")
                builder.setMessage("Les champs Retrait et Espèces sont obligatoires.")
                builder.show()
            }else if(!checkForInternet(context)){
                Toast.makeText(context, "Aucune connexion internet", Toast.LENGTH_SHORT).show()
            }else{
                button.setText(R.string.button_loading)
                button.isEnabled = false
                progressBar.visibility = View.VISIBLE

                // VARIABLE DE CONTOURNEMENT D'ERREURS
                if (envoiInter.text.isNotEmpty())
                {
                    envoiTxt = envoiInter.text.toString().toInt()
                }else{
                    envoiTxt = 0
                }
                if (divers.text.isNotEmpty())
                {
                    diversTxt = divers.text.toString().toInt()
                }else{
                    diversTxt = 0
                }

                // Calcul du total des données émises par l'utilisateur
                val sum = retraitInter.text.toString().toInt() + especes.text.toString().toInt() + diversTxt

                val pointMap = hashMapOf(
                    "id" to auth.currentUser?.uid,
                    "orange" to "0",
                    "mtn" to "0",
                    "moov" to "0",
                    "wave" to "0",
                    "tresor" to "0",
                    "divers" to diversTxt.toString(),
                    "especes" to especes.text.toString(),
                    "retrait" to retraitInter.text.toString(),
                    "envoi" to envoiTxt.toString(),
                    "module" to module,
                    "total" to sum.toString(),
                    "superviseur" to superviseurId,
                    "date" to dateFormatted
                )
                db.collection("point").add(pointMap).addOnSuccessListener {
                    Toast.makeText(context, "Enregistré avec succès", Toast.LENGTH_SHORT).show()
                    progressBar.visibility = View.INVISIBLE

                    retraitInter.text.clear()
                    envoiInter.text.clear()
                    especes.text.clear()
                    divers.text.clear()
                    button.isEnabled = true
                    button.setText(R.string.register_operation)
                    context.supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, HomeFragment(context))
                        .addToBackStack(null)
                        .commit()

                }.addOnFailureListener {
                    Toast.makeText(context, R.string.onFailureText, Toast.LENGTH_SHORT).show()
                    progressBar.visibility = View.INVISIBLE
                }
            }
        }

        return view
    }
}