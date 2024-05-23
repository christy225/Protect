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

class PointFragmentNational(private val context: MainActivity) : Fragment() {

    private var db = Firebase.firestore
    lateinit var auth: FirebaseAuth
    private lateinit var orange: EditText
    private lateinit var mtn: EditText
    private lateinit var moov: EditText
    private lateinit var wave: EditText
    private lateinit var tresor: EditText
    private lateinit var especes: EditText
    private lateinit var divers: EditText
    private lateinit var pointArrayList: ArrayList<PointModel>
    private lateinit var button: AppCompatButton
    private lateinit var progressBar: ProgressBar
    private lateinit var superviseurId: String
    private lateinit var warning: TextView
    private var moovTxt: Int = 0
    private var waveTxt: Int = 0
    private var tresorTxt: Int = 0
    private var diversTxt: Int = 0
    private var module: String = ""
    @SuppressLint("MissingInflatedId")
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_assistant_point_national, container, false)
        auth = FirebaseAuth.getInstance()

        if (!checkForInternet(context)) {
            Toast.makeText(context, "Aucune connexion internet", Toast.LENGTH_SHORT).show()
        }

        requireActivity().onBackPressedDispatcher.addCallback(context) {

        }

        // Récupère les données émises par l'utilisateur
        orange = view.findViewById(R.id.point_nat_orange_edt)
        mtn = view.findViewById(R.id.point_nat_mtn_edt)
        moov = view.findViewById(R.id.point_nat_moov_edt)
        wave = view.findViewById(R.id.point_nat_wave_edt)
        tresor = view.findViewById(R.id.point_nat_tresor_edt)
        especes = view.findViewById(R.id.point_nat_espece_edt)
        divers = view.findViewById(R.id.point_nat_divers_edt)
        button = view.findViewById(R.id.btn_register_point_national)
        warning = view.findViewById(R.id.info_avant_enregistrement_national)
        progressBar = view.findViewById(R.id.progressBarPointNational)
        pointArrayList = arrayListOf()

        val info = view.findViewById<TextView>(R.id.info_text_point_national)

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

        orange.addTextChangedListener(formatLimitation)
        mtn.addTextChangedListener(formatLimitation)
        moov.addTextChangedListener(formatLimitation)
        wave.addTextChangedListener(formatLimitation)
        tresor.addTextChangedListener(formatLimitation)
        especes.addTextChangedListener(formatLimitation)
        divers.addTextChangedListener(formatLimitation)


        val backButton = view.findViewById<ImageView>(R.id.pointbackButtonNational)
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
            if (orange.text.isEmpty() || mtn.text.isEmpty() || especes.text.isEmpty())
            {
                val builder = AlertDialog.Builder(context)
                builder.setTitle("Infos")
                builder.setMessage("Les champs Orange Money, MTN Money et Espèces sont obligatoires.")
                builder.show()
            }else if(!checkForInternet(context)){
                Toast.makeText(context, "Aucune connexion internet", Toast.LENGTH_SHORT).show()
            }else{
                button.setText(R.string.button_loading)
                button.isEnabled = false
                progressBar.visibility = View.VISIBLE

                // VARIABLE DE CONTOURNEMENT D'ERREURS
                if (wave.text.isNotEmpty())
                {
                    waveTxt = wave.text.toString().toInt()
                }else{
                    waveTxt = 0
                }

                if (moov.text.isNotEmpty())
                {
                    moovTxt = moov.text.toString().toInt()
                }else{
                    moovTxt = 0
                }

                if (tresor.text.isNotEmpty())
                {
                    tresorTxt = tresor.text.toString().toInt()
                }else{
                    tresorTxt = 0
                }

                if (divers.text.isNotEmpty())
                {
                    diversTxt = divers.text.toString().toInt()
                }else{
                    diversTxt = 0
                }

                // Calcul du total des données émises par l'utilisateur
                val sum = orange.text.toString().toInt() +
                        mtn.text.toString().toInt() +
                        moovTxt +
                        waveTxt +
                        tresorTxt +
                        especes.text.toString().toInt() +
                        diversTxt
                val pointMap = hashMapOf(
                    "id" to auth.currentUser?.uid,
                    "orange" to orange.text.toString(),
                    "mtn" to mtn.text.toString(),
                    "moov" to moov.text.toString(),
                    "wave" to waveTxt.toString(),
                    "tresor" to tresorTxt.toString(),
                    "divers" to diversTxt.toString(),
                    "especes" to especes.text.toString(),
                    "retrait" to "0",
                    "envoi" to "0",
                    "module" to module,
                    "total" to sum.toString(),
                    "date" to dateFormatted,
                    "superviseur" to superviseurId
                )
                db.collection("point").add(pointMap).addOnSuccessListener {
                    Toast.makeText(context, "Enregistré avec succès", Toast.LENGTH_SHORT).show()
                    progressBar.visibility = View.INVISIBLE

                    orange.text.clear()
                    mtn.text.clear()
                    moov.text.clear()
                    wave.text.clear()
                    tresor.text.clear()
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