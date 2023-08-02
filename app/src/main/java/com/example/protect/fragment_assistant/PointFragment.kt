package com.example.protect.fragment_assistant

import android.app.AlertDialog
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatButton
import com.example.protect.MainActivity
import com.example.protect.R
import com.example.protect.fragment_assistant.checkInternet.checkForInternet
import com.example.protect.models.PointModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class PointFragment(private val context: MainActivity) : Fragment() {
    private var db = Firebase.firestore
    lateinit var auth: FirebaseAuth
    lateinit var orange: EditText
    lateinit var mtn: EditText
    lateinit var moov: EditText
    lateinit var wave: EditText
    lateinit var tresor: EditText
    lateinit var especes: EditText
    lateinit var divers: EditText
    lateinit var pointArrayList: ArrayList<PointModel>
    lateinit var button: AppCompatButton
    lateinit var progressBar: ProgressBar
    lateinit var superviseur: String
    var waveTxt: Int = 0
    var tresorTxt: Int = 0
    var diversTxt: Int = 0

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_assistant_point, container, false)
        auth = FirebaseAuth.getInstance()

        if (!checkForInternet(context)) {
            Toast.makeText(context, "Impossible de se connecter à internet", Toast.LENGTH_SHORT).show()
        }

        // Récupère les données émises par l'utilisateur
        orange = view.findViewById(R.id.point_orange_edt)
        mtn = view.findViewById(R.id.point_mtn_edt)
        moov = view.findViewById(R.id.point_moov_edt)
        wave = view.findViewById(R.id.point_wave_edt)
        tresor = view.findViewById(R.id.point_tresor_edt)
        especes = view.findViewById(R.id.point_espece_edt)
        divers = view.findViewById(R.id.point_divers_edt)
        button = view.findViewById(R.id.btn_register_point)
        progressBar = view.findViewById(R.id.progressBarPoint)
        pointArrayList = arrayListOf()

        val info = view.findViewById<TextView>(R.id.info_text_point)
        info.visibility = View.INVISIBLE
        button.visibility = View.VISIBLE


        val backButton = view.findViewById<ImageView>(R.id.pointbackButton)
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
                    superviseur = data!!.data["superviseur"].toString()
                }
            }

        // VERIFIER QUE L'UTILISATEUR N'A PAS DEJA ENVOYE UN POINT
        db.collection("point").
        whereEqualTo("date", dateFormatted)
            .get()
            .addOnSuccessListener {docs->
                for (datax in docs)
                {
                    val superviseurId = datax!!.data["superviseur"].toString()
                    if (superviseurId.isNotEmpty())
                    {
                        info.visibility = View.VISIBLE
                        button.visibility = View.INVISIBLE
                    }
                }
            }

        button.setOnClickListener {

            // SI IL N'YA PAS ENCORE EU DE POINT CE JOUR ALORS ON VERIFIE QUE TOUS LES CHAMPS OBLIGATOIRES SONT REMPLIS
            if (orange.text.isEmpty() || mtn.text.isEmpty() || moov.text.isEmpty()|| especes.text.isEmpty())
            {
                val builder = AlertDialog.Builder(context)
                builder.setTitle("Infos")
                builder.setMessage("Les champs Orange Money, MTN Money, Moov Money et Espèces sont obligatoires.")
                builder.show()
            }else{
                button.setText(R.string.button_loading)
                button.isEnabled = false
                progressBar.visibility = View.VISIBLE

                // VARIABLE DE CONTOURNEMENT D'ERREURS
                if (wave.text.isNotEmpty())
                {
                    waveTxt = wave.text.toString().toInt()
                }
                if (tresor.text.isNotEmpty())
                {
                    tresorTxt = tresor.text.toString().toInt()
                }
                if (divers.text.isNotEmpty())
                {
                    diversTxt = divers.text.toString().toInt()
                }

                // Calcul du total des données émises par l'utilisateur
                val sum = orange.text.toString().toInt() +
                        mtn.text.toString().toInt() +
                        moov.text.toString().toInt() +
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
                    "total" to sum.toString(),
                    "superviseur" to superviseur,
                    "date" to dateFormatted
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