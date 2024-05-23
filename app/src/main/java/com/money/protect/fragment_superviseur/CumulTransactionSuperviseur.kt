package com.money.protect.fragment_superviseur

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
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
import java.util.Calendar
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

    private lateinit var sumDepot1: TextView
    private lateinit var sumRetrait1: TextView
    private lateinit var sumDepot2: TextView
    private lateinit var sumRetrait2: TextView
    private lateinit var sumDepot3: TextView
    private lateinit var sumRetrait3: TextView
    private lateinit var sumDepot4: TextView
    private lateinit var sumRetrait4: TextView
    private lateinit var sumDepot5: TextView
    private lateinit var sumRetrait5: TextView

    private lateinit var datePicker: EditText
    private lateinit var button: ImageButton

    private lateinit var loader: TextView
    @SuppressLint("MissingInflatedId", "SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_superviseur_cumul_transaction, container, false)
        db = FirebaseFirestore.getInstance()

        // Recupérer le point du jour

        datePicker = view.findViewById(R.id.dateSelectCumulSuperviseur)
        button = view.findViewById(R.id.buttonDatepickerCumulSuperviseur)
        loader = view.findViewById(R.id.loaderCumulSuperviseur)

        val data = arguments
        val identifiant = data?.getString("id")
        val nom = data?.getString("nom")
        val module = data?.getString("module")
        val capital = data?.getString("capital")

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
        // RECHERCHER UN POINT A UNE DATE PRECISE

        sumDepot1 = view.findViewById(R.id.tdep1)
        sumRetrait1 = view.findViewById(R.id.tret1)
        sumDepot2 = view.findViewById(R.id.tdep2)
        sumRetrait2 = view.findViewById(R.id.tret2)
        sumDepot3 = view.findViewById(R.id.tdep3)
        sumRetrait3 = view.findViewById(R.id.tret3)
        sumDepot4 = view.findViewById(R.id.tdep4)
        sumRetrait4 = view.findViewById(R.id.tret4)
        sumDepot5 = view.findViewById(R.id.tdep5)
        sumRetrait5 = view.findViewById(R.id.tret5)

        datePicker.setOnClickListener {
            val c = Calendar.getInstance()

            val year = c.get(Calendar.YEAR)
            val month = c.get(Calendar.MONTH)
            val day = c.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(
                context, { view, year, monthOfYear, dayOfMonth ->
                    val dat = (dayOfMonth.toString() + "-" + (monthOfYear + 1) + "-" + year)
                    datePicker.setText(dat)
                    loader.visibility = View.VISIBLE

                    db.collection("operation")
                        .whereEqualTo("id", identifiant)
                        .whereEqualTo("date", datePicker.text.toString())
                        .get()
                        .addOnSuccessListener { documents->
                            loader.visibility = View.GONE

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

                            var sumDep1 = 0
                            var sumRet1 = 0
                            var sumDep2 = 0
                            var sumRet2 = 0
                            var sumDep3 = 0
                            var sumRet3 = 0
                            var sumDep4 = 0
                            var sumRet4 = 0
                            var sumDep5 = 0
                            var sumRet5 = 0

                            if (!documents.isEmpty())
                            {
                                for (donnee in documents)
                                {
                                    val operateur = donnee.data["operateur"].toString()
                                    val typeOperateur = donnee.data["typeoperation"].toString()
                                    val statut = donnee.data["statut"].toString().toBoolean()

                                    // Statut pour vérifier que la transaction n'est pas annulée
                                    if (operateur == "orange" && statut)
                                    {
                                        if (typeOperateur == "Dépôt")
                                        {
                                            val sum = donnee.data["montant"].toString()
                                            val caractere = ','
                                            val newValue = sum.filter { it != caractere }
                                            totalOrange1 += newValue.toInt()
                                            sumDep1 += 1
                                        }else if (typeOperateur == "Retrait") {
                                            val sum = donnee.data["montant"].toString()
                                            val caractere = ','
                                            val newValue = sum.filter { it != caractere }
                                            totalOrange2 += newValue.toInt()
                                            sumRet1 += 1
                                        }
                                    }
                                    if (operateur == "mtn" && statut)
                                    {
                                        if (typeOperateur == "Dépôt")
                                        {
                                            val sum = donnee.data["montant"].toString()
                                            val caractere = ','
                                            val newValue = sum.filter { it != caractere }
                                            totalMtn1 += newValue.toInt()
                                            sumDep2 += 1
                                        }else if (typeOperateur == "Retrait") {
                                            val sum = donnee.data["montant"].toString()
                                            val caractere = ','
                                            val newValue = sum.filter { it != caractere }
                                            totalMtn2 += newValue.toInt()
                                            sumRet2 += 1
                                        }
                                    }
                                    if (operateur == "moov" && statut)
                                    {
                                        if (typeOperateur == "Dépôt")
                                        {
                                            val sum = donnee.data["montant"].toString()
                                            val caractere = ','
                                            val newValue = sum.filter { it != caractere }
                                            totalMoov1 += newValue.toInt()
                                            sumDep3 += 1
                                        }else if (typeOperateur == "Retrait") {
                                            val sum = donnee.data["montant"].toString()
                                            val caractere = ','
                                            val newValue = sum.filter { it != caractere }
                                            totalMoov2 += newValue.toInt()
                                            sumRet3 += 1
                                        }
                                    }
                                    if (operateur == "wave" && statut)
                                    {
                                        if (typeOperateur == "Dépôt")
                                        {
                                            val sum = donnee.data["montant"].toString()
                                            val caractere = ','
                                            val newValue = sum.filter { it != caractere }
                                            totalWave1 += newValue.toInt()
                                            sumDep4 += 1
                                        }else if (typeOperateur == "Retrait") {
                                            val sum = donnee.data["montant"].toString()
                                            val caractere = ','
                                            val newValue = sum.filter { it != caractere }
                                            totalWave2 += newValue.toInt()
                                            sumRet4 += 1
                                        }
                                    }
                                    if (operateur == "tresor money" && statut)
                                    {
                                        if (typeOperateur == "Dépôt")
                                        {
                                            val sum = donnee.data["montant"].toString()
                                            val caractere = ','
                                            val newValue = sum.filter { it != caractere }
                                            totalTresor1 += newValue.toInt()
                                            sumDep5 += 1
                                        }else if (typeOperateur == "Retrait") {
                                            val sum = donnee.data["montant"].toString()
                                            val caractere = ','
                                            val newValue = sum.filter { it != caractere }
                                            totalTresor2 += newValue.toInt()
                                            sumRet5 += 1
                                        }
                                    }
                                }
                            }else{
                                val builder = AlertDialog.Builder(context)
                                builder.setMessage("Aucun résultat disponible à cette date")
                                builder.show()
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

                            sumDepot1.text = "($sumDep1)"
                            sumRetrait1.text = "($sumRet1)"
                            sumDepot2.text = "($sumDep2)"
                            sumRetrait2.text = "($sumRet2)"
                            sumDepot3.text = "($sumDep3)"
                            sumRetrait3.text = "($sumRet3)"
                            sumDepot4.text = "($sumDep4)"
                            sumRetrait4.text = "($sumRet4)"
                            sumDepot5.text = "($sumDep5)"
                            sumRetrait5.text = "($sumRet5)"
                        }
                },
                year,
                month,
                day
            )
            datePickerDialog.show()
        }

        val linkToHomeListPoint = view.findViewById<ImageView>(R.id.backToListHomeSuperviseur1)
        val menuFragment = MenuAssistSuperviseur(context)
        linkToHomeListPoint.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("id", identifiant)
            bundle.putString("nom", nom)
            bundle.putString("module", module)
            bundle.putString("capital", capital)
            menuFragment.arguments = bundle
            context.supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container_superviseur, menuFragment)
                .addToBackStack(null)
                .commit()
        }

        return view
    }

}