package com.money.protect.fragment_superviseur

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.money.protect.R
import com.money.protect.SuperviseurActivity
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class CumulTransactionSuperviseur(
    private val context: SuperviseurActivity
) : Fragment() {
    private var db = Firebase.firestore
    private lateinit var orange1: TextView
    private lateinit var orange2: TextView
    private lateinit var mtn1: TextView
    private lateinit var mtn2: TextView
    private lateinit var moov1: TextView
    private lateinit var moov2: TextView
    private lateinit var wave1: TextView
    private lateinit var wave2: TextView
    private lateinit var tresor1: TextView
    private lateinit var tresor2: TextView
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_superviseur_cumul_transaction, container, false)
        db = FirebaseFirestore.getInstance()

        // Recupérer le point du jour

        val data = arguments
        val identifiant = data?.getString("id")
        val nom = data?.getString("nom")
        val assignation = data?.getString("module")

        val current = LocalDateTime.now()
        val formatterDate = DateTimeFormatter.ofPattern("d-M-yyyy")
        val dateFormatted = current.format(formatterDate)

        orange1 = view.findViewById(R.id.ma1)
        orange2 = view.findViewById(R.id.maa1)
        mtn1 = view.findViewById(R.id.ma2)
        mtn2 = view.findViewById(R.id.maa2)
        moov1 = view.findViewById(R.id.ma3)
        moov2 = view.findViewById(R.id.maa3)
        wave1 = view.findViewById(R.id.ma4)
        wave2 = view.findViewById(R.id.maa4)
        tresor1 = view.findViewById(R.id.ma5)
        tresor2 = view.findViewById(R.id.maa5)


        db.collection("operation")
            .whereEqualTo("id", identifiant)
            .get()
            .addOnSuccessListener { documents->
                var totalOrange1 = 0
                var totalOrange2 = 0
                var totalMtn1 = 0
                var totalMtn2 = 0
                var totalMoov1 = 0
                var totalMoov2 = 0
                var totalWave1 = 0
                var totalWave2 = 0
                var totalTresor1 = 0
                var totalTresor2 = 0
                for (donnee in documents)
                {
                    if (donnee != null)
                    {
                        val operateur = donnee.data["operateur"].toString()
                        val typeOperateur = donnee.data["typeoperation"].toString()
                        val date = donnee.data["date"].toString()

                        if (operateur == "orange" && date == dateFormatted.toString())
                        {
                            if (typeOperateur == "Dépôt")
                            {
                                val sum = donnee.data["montant"].toString()
                                val caractere = ','
                                val newValue = sum.filter { it != caractere }
                                totalOrange1 += newValue.toInt()
                            }else if (typeOperateur == "Retrait") {
                                val sum = donnee.data["montant"].toString()
                                val caractere = ','
                                val newValue = sum.filter { it != caractere }
                                totalOrange2 += newValue.toInt()
                            }
                        }
                        if (operateur == "mtn" && date == dateFormatted.toString())
                        {
                            if (typeOperateur == "Dépôt")
                            {
                                val sum = donnee.data["montant"].toString()
                                val caractere = ','
                                val newValue = sum.filter { it != caractere }
                                totalMtn1 += newValue.toInt()
                            }else if (typeOperateur == "Retrait") {
                                val sum = donnee.data["montant"].toString()
                                val caractere = ','
                                val newValue = sum.filter { it != caractere }
                                totalMtn2 += newValue.toInt()
                            }
                        }
                        if (operateur == "moov" && date == dateFormatted.toString())
                        {
                            if (typeOperateur == "Dépôt")
                            {
                                val sum = donnee.data["montant"].toString()
                                val caractere = ','
                                val newValue = sum.filter { it != caractere }
                                totalMoov1 += newValue.toInt()
                            }else if (typeOperateur == "Retrait") {
                                val sum = donnee.data["montant"].toString()
                                val caractere = ','
                                val newValue = sum.filter { it != caractere }
                                totalMoov2 += newValue.toInt()
                            }
                        }
                        if (operateur == "wave" && date == dateFormatted.toString())
                        {
                            if (typeOperateur == "Dépôt")
                            {
                                val sum = donnee.data["montant"].toString()
                                val caractere = ','
                                val newValue = sum.filter { it != caractere }
                                totalWave1 += newValue.toInt()
                            }else if (typeOperateur == "Retrait") {
                                val sum = donnee.data["montant"].toString()
                                val caractere = ','
                                val newValue = sum.filter { it != caractere }
                                totalWave2 += newValue.toInt()
                            }
                        }
                        if (operateur == "tresor" && date == dateFormatted.toString())
                        {
                            if (typeOperateur == "Dépôt")
                            {
                                val sum = donnee.data["montant"].toString()
                                val caractere = ','
                                val newValue = sum.filter { it != caractere }
                                totalTresor1 += newValue.toInt()
                            }else if (typeOperateur == "Retrait") {
                                val sum = donnee.data["montant"].toString()
                                val caractere = ','
                                val newValue = sum.filter { it != caractere }
                                totalTresor2 += newValue.toInt()
                            }
                        }
                    }
                }

                // Utilisation de la locale par défaut pour obtenir le séparateur de milliers correct
                val format = DecimalFormat("#,###", DecimalFormatSymbols.getInstance(Locale.getDefault()))

                // Appliquer le format au nombre
                val nombreFormateOrange1 = format.format(totalOrange1)
                val nombreFormateOrange2 = format.format(totalOrange2)
                val nombreFormateMtn1 = format.format(totalMtn1)
                val nombreFormateMtn2 = format.format(totalMtn2)
                val nombreFormateMoov1 = format.format(totalMoov1)
                val nombreFormateMoov2 = format.format(totalMoov2)
                val nombreFormateWave1 = format.format(totalWave1)
                val nombreFormateWave2 = format.format(totalWave2)
                val nombreFormateTresor1 = format.format(totalTresor1)
                val nombreFormateTresor2 = format.format(totalTresor2)

                orange1.text = nombreFormateOrange1.toString()
                orange2.text = nombreFormateOrange2.toString()
                mtn1.text = nombreFormateMtn1.toString()
                mtn2.text = nombreFormateMtn2.toString()
                moov1.text = nombreFormateMoov1.toString()
                moov2.text = nombreFormateMoov2.toString()
                wave1.text = nombreFormateWave1.toString()
                wave2.text = nombreFormateWave2.toString()
                tresor1.text = nombreFormateTresor1.toString()
                tresor2.text = nombreFormateTresor2.toString()
            }

        return view
    }

}