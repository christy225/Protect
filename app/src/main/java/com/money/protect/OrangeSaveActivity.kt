package com.money.protect

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.money.protect.fragment_assistant.checkInternet.checkForInternet
import com.money.protect.popup.SmsOrange
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

class OrangeSaveActivity : AppCompatActivity() {
    private var db = Firebase.firestore
    private lateinit var auth: FirebaseAuth
    private lateinit var intents: Intent

    private lateinit var operation: TextView
    private lateinit var numero: TextView
    private lateinit var montant: TextView
    private lateinit var idTransaction: TextView
    private lateinit var newSolde: TextView
    private lateinit var buttonUpload: Button
    private lateinit var stateInfo: TextView
    private lateinit var button: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var buttonCancel: TextView
    private lateinit var origineFond: EditText
    private lateinit var checkOrigine: CheckBox
    var origine: String? = null

    private var storageRef = Firebase.storage
    private var uri: Uri? = null
    private var uploaded: Boolean = false

    private var typeOp: String? = null
    private var montantDB: String? = null
    private var idT: String? = null

    @SuppressLint("CutPasteId")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_orange_save)
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        operation = findViewById(R.id.operationOrange)
        numero = findViewById(R.id.numeroOrange)
        montant = findViewById(R.id.montantOrange)
        idTransaction = findViewById(R.id.idTransactionOrange)
        newSolde = findViewById(R.id.soldeOrange)
        button = findViewById(R.id.btn_register_orange)
        buttonUpload = findViewById(R.id.uploadPhotoOrange)
        stateInfo = findViewById(R.id.stateInfoOrange)
        buttonCancel = findViewById(R.id.btnCancelOperationOrangeMoney)
        progressBar = findViewById(R.id.progressBarOrange)
        origineFond = findViewById(R.id.origine_fond_orange)
        checkOrigine = findViewById(R.id.origine_check_orange)

        checkOrigine.setOnClickListener {
            if (checkOrigine.isChecked) {
                origineFond.visibility = View.VISIBLE
            }else{
                origineFond.visibility = View.GONE
            }
        }

        if (origineFond.text.isEmpty())
        {
            origine = "non défini"
        }else{
            origine = origineFond.text.toString()
        }

        intents = intent
        val sms = intents.getStringExtra("sms")
        if (sms!!.contains("Depot")) {
            depot()
        }else if (sms.contains("Retrait")){
            retrait()
        }
        val linkToPopup = findViewById<Button>(R.id.popupSMSOrange)
        linkToPopup.setOnClickListener {
            SmsOrange(this).show()
        }


        val btnCancel = findViewById<TextView>(R.id.btnCancelOperationOrangeMoney)
        btnCancel.setOnClickListener {
            val intent = Intent(this, OrangeSmsListActivity::class.java)
            startActivity(intent)
        }

        val linkToOrangePopup = findViewById<Button>(R.id.openOrangePopup)
        linkToOrangePopup.setOnClickListener {
            val intent = Intent(this, OrangeMissingSmsConfirmActivity::class.java)
            startActivity(intent)
        }

        buttonUpload.setOnClickListener {
            ImagePicker.with(this)
                .crop()
                .cameraOnly()    			//Crop image(Optional), Check Customization for more option
                .compress(1024)			//Final image size will be less than 1 MB(Optional)
                .maxResultSize(530, 500)	//Final image resolution will be less than 1080 x 1080(Optional)
                .start()
        }

        button.setOnClickListener {
            if (operation.text.isEmpty() ||
                numero.text.isEmpty() ||
                montant.text.isEmpty() ||
                idTransaction.text.toString() == "null" ||
                montant.text.toString() == "null" ||
                numero.text.toString() == "null" ||
                montantDB.toString() == "null" ||
                idTransaction.text.toString() == "sms en attente..." ||
                numero.text.toString() == "sms en attente..." ||
                montant.text.toString() == "sms en attente..." ||
                idTransaction.text.toString() == "sms en attente...") {
                Toast.makeText(this, "Aucune donnée à enregistrer", Toast.LENGTH_SHORT).show()
            }else if (button.text == "enregistrer opération"){

                // Enregistrement de la transaction

                val builder = AlertDialog.Builder(this)
                builder.setMessage("Enregistrer la transaction ?")

                builder.setPositiveButton(android.R.string.yes) { dialog, which ->
                    button.isEnabled = false
                    button.visibility = View.GONE
                    progressBar.visibility = View.VISIBLE
                    // Générer la date
                    val current = LocalDateTime.now()
                    val formatterDate = DateTimeFormatter.ofPattern("d-M-yyyy")
                    val dateFormatted = current.format(formatterDate)

                    // Générer l'heure
                    val formatterHour = DateTimeFormatter.ofPattern("HH:mm")
                    val hourFormatted = current.format(formatterHour)

                    // On upload l'image avant d'enregistrer les données au cas où l'utilisateur a enregistré une image
                    if (checkForInternet(this)) {
                        if (uploaded)
                        {
                            storageRef.getReference("earth").child(System.currentTimeMillis().toString())
                                .putFile(uri!!)
                                .addOnSuccessListener { task->
                                    task.metadata!!.reference!!.downloadUrl
                                        .addOnSuccessListener {

                                            val sms = intents.getStringExtra("sms")
                                            if (sms!!.contains("Depot"))
                                            {
                                                typeOp = "Dépôt"
                                            }else if (sms.contains("Retrait")){
                                                typeOp = "Retrait"
                                            }

                                            val uid = UUID.randomUUID().toString()
                                            val caractereASupprimer = ','
                                            val operationMap = hashMapOf(
                                                "id" to auth.currentUser?.uid,
                                                "date" to dateFormatted,
                                                "heure" to hourFormatted,
                                                "operateur" to "orange",
                                                "telephone" to numero.text.toString(),
                                                "montant" to montantDB.toString(),
                                                "typeoperation" to typeOp,
                                                "statut" to true,
                                                "url" to it.toString(),
                                                "idDoc" to uid,
                                                "idTransac" to idT.toString().replace(caractereASupprimer.toString(),""),
                                                "origine" to origine
                                            )

                                            db.collection("operation")
                                                .document(uid)
                                                .set(operationMap)
                                                .addOnCompleteListener {
                                                    if (it.isSuccessful)
                                                    {
                                                        progressBar.visibility = View.GONE
                                                        stateInfo.visibility = View.GONE
                                                        button.visibility = View.VISIBLE
                                                        button.isEnabled = true

                                                        Toast.makeText(this, "Enregistré avec succès", Toast.LENGTH_SHORT).show()

                                                        val intent = Intent(this, OrangeRedirectionActivity::class.java)
                                                        startActivity(intent)
                                                    }
                                                }.addOnFailureListener {
                                                    val builder = AlertDialog.Builder(this)
                                                    builder.setTitle("Alerte")
                                                    builder.setMessage(R.string.onFailureText)
                                                    builder.show()
                                                }
                                        }.addOnFailureListener {
                                            val builer = AlertDialog.Builder(this)
                                            builer.setMessage(R.string.onFailureText)
                                            builer.show()
                                        }
                                }.addOnFailureListener{
                                    val builer = AlertDialog.Builder(this)
                                    builer.setMessage("Erreur pendant le téléchargement de l'image.")
                                    builer.show()
                                }
                        }else{
                            val sms1 = intents.getStringExtra("sms")
                            if (sms1!!.contains("Depot"))
                            {
                                typeOp = "Dépôt"
                            }else if (sms1.contains("Retrait")){
                                typeOp = "Retrait"
                            }
                            val uid = UUID.randomUUID().toString()
                            val caractereASupprimer = ','
                            val operationMap = hashMapOf(
                                "id" to auth.currentUser?.uid,
                                "date" to dateFormatted,
                                "heure" to hourFormatted,
                                "operateur" to "orange",
                                "telephone" to numero.text.toString(),
                                "montant" to montantDB.toString(),
                                "typeoperation" to typeOp,
                                "statut" to true,
                                "url" to "null",
                                "idDoc" to uid,
                                "idTransac" to idT.toString().replace(caractereASupprimer.toString(),""),
                                "origine" to origine
                            )
                            db.collection("operation")
                                .document(uid)
                                .set(operationMap)
                                .addOnCompleteListener {
                                    if (it.isSuccessful)
                                    {
                                        progressBar.visibility = View.GONE
                                        stateInfo.visibility = View.GONE
                                        button.visibility = View.VISIBLE
                                        button.isEnabled = true

                                        Toast.makeText(this, "Enregistré avec succès", Toast.LENGTH_SHORT).show()

                                        val intent = Intent(this, OrangeRedirectionActivity::class.java)
                                        startActivity(intent)
                                    }
                                }.addOnFailureListener {
                                    val builder = AlertDialog.Builder(this)
                                    builder.setTitle("Alerte")
                                    builder.setMessage(R.string.onFailureText)
                                    builder.show()
                                }
                        }
                    }else{
                        Toast.makeText(this, "Aucune connexion internet", Toast.LENGTH_SHORT).show()
                    }
                }

                builder.setNegativeButton(android.R.string.no) { dialog, which ->
                    // Ne rien faire
                }

                builder.show()

            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun depot() {
        CoroutineScope(Dispatchers.IO).launch {
            val sms = intents.getStringExtra("sms")

            val chainesDeCaracteres = listOf(sms)

            val positionDuMotNumero = 2
            val positionDuMotMontant = 5
            val positionDuMotId = 15
            val positionDuMotSolde = 18

            for (chaine in chainesDeCaracteres) {
                val motNumero = obtenirMotALaPosition(chaine!!, positionDuMotNumero)
                val motMontant = obtenirMotALaPosition(chaine, positionDuMotMontant)
                val motId = obtenirMotALaPosition(chaine, positionDuMotId)
                val motSolde = obtenirMotALaPosition(chaine, positionDuMotSolde)

                val caractereASupprimer = ','
                idT = motId.toString().replace(caractereASupprimer.toString(),"")

                withContext(Dispatchers.Main) {
                    db.collection("operation")
                        .whereEqualTo("idTransac", idT)
                        .get()
                        .addOnSuccessListener { documents->
                            if (!documents.isEmpty)
                            {
                                val builder = AlertDialog.Builder(this@OrangeSaveActivity)
                                builder.setMessage("Cette transaction a déjà été enregistrée !")
                                builder.show()
                                operation.text = "sms en attente..."
                                numero.text = "sms en attente..."
                                montant.text = "sms en attente..."
                                idTransaction.text = "sms en attente..."
                                newSolde.text = "sms en attente..."
                                button.visibility = View.GONE
                            }else{
                                if (numero.text.toString() == "null" || montant.text.toString() == "null" || idTransaction.text.toString() == "null" || newSolde.text.toString() == "null")
                                {
                                    operation.text = "sms en attente..."
                                    numero.text = "sms en attente..."
                                    montant.text = "sms en attente..."
                                    idTransaction.text = "sms en attente..."
                                    newSolde.text = "sms en attente..."
                                    button.visibility = View.GONE
                                }else{
                                    val dep = "Dépôt"
                                    val num: String = motNumero.toString()
                                    val mont: String = motMontant.toString()
                                    val mId: String = motId.toString()
                                    val sold: String = motSolde.toString()

                                    operation.text = dep
                                    numero.text = num
                                    montant.text =  mont
                                    montantDB = supprimerZerosApresPoint(mont)
                                    idTransaction.text = mId.replace(caractereASupprimer.toString(),"")
                                    newSolde.text = sold
                                    button.visibility = View.VISIBLE
                                    if (numero.text.toString() == "sms en attente..." || montantDB.toString() == "null") {
                                        numero.text = num
                                        montantDB = supprimerZerosApresPoint(mont)
                                    }
                                }
                            }
                        }.addOnFailureListener {
                            val builder = AlertDialog.Builder(this@OrangeSaveActivity)
                            builder.setMessage(R.string.onFailureText)
                            builder.show()
                        }
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun retrait() {
        CoroutineScope(Dispatchers.IO).launch {
            val sms = intents.getStringExtra("sms")

            val chainesDeCaracteres = listOf(sms)

            val positionDuMotNumero = 16
            val positionDuMotMontant = 19
            val positionDuMotId = 22
            val positionDuMotSolde = 6

            for (chaine in chainesDeCaracteres) {
                val motNumero = obtenirMotALaPosition(chaine!!, positionDuMotNumero)
                val motMontant = obtenirMotALaPosition(chaine, positionDuMotMontant)
                val motId = obtenirMotALaPosition(chaine, positionDuMotId)
                val motSolde = obtenirMotALaPosition(chaine, positionDuMotSolde)

                val caractereASupprimer = ','
                idT = motId.toString().replace(caractereASupprimer.toString(),"")

                withContext(Dispatchers.Main){
                    db.collection("operation")
                        .whereEqualTo("idTransac", idT)
                        .get()
                        .addOnSuccessListener { documents->
                            if (!documents.isEmpty)
                            {
                                val builder = AlertDialog.Builder(this@OrangeSaveActivity)
                                builder.setMessage("Cette transaction a déjà été enregistrée !")
                                builder.show()
                                operation.text = "sms en attente..."
                                numero.text = "sms en attente..."
                                montant.text = "sms en attente..."
                                idTransaction.text = "sms en attente..."
                                newSolde.text = "sms en attente..."
                                button.visibility = View.GONE
                            }else{
                                if (numero.text.toString() == "null" || montant.text.toString() == "null" || idTransaction.text.toString() == "null" || newSolde.text.toString() == "null")
                                {
                                    operation.text = "sms en attente..."
                                    numero.text = "sms en attente..."
                                    montant.text = "sms en attente..."
                                    idTransaction.text = "sms en attente..."
                                    newSolde.text = "sms en attente..."
                                    button.visibility = View.GONE
                                }else{
                                    val ret = "Retrait"
                                    val num: String = motNumero.toString()
                                    val mont: String = motMontant.toString()
                                    val mId: String = motId.toString()
                                    val sold: String = motSolde.toString()

                                    operation.text = ret
                                    numero.text = num
                                    montant.text =  mont
                                    montantDB = supprimerZerosApresPoint(mont)
                                    idTransaction.text = mId.replace(caractereASupprimer.toString(),"")
                                    newSolde.text = sold
                                    button.visibility = View.VISIBLE
                                    if (numero.text.toString() == "sms en attente..." || montantDB.toString() == "null") {
                                        numero.text = num
                                        montantDB = supprimerZerosApresPoint(mont)
                                    }
                                }
                            }
                        }.addOnFailureListener {
                            val builder = AlertDialog.Builder(this@OrangeSaveActivity)
                            builder.setMessage(R.string.onFailureText)
                            builder.show()
                        }
                }
            }
        }
    }

    private fun supprimerZerosApresPoint(chaine: String): String {
        val partieEntiere = chaine.substringBefore('.')
        val partieDecimale = chaine.substringAfter('.', "")

        // Supprimer les zéros en trop
        val nouvellePartieDecimale = partieDecimale.trimEnd('0')

        // Reconstruire la chaîne avec ou sans partie décimale
        return if (nouvellePartieDecimale.isEmpty()) {
            partieEntiere
        } else {
            "$partieEntiere.$nouvellePartieDecimale"
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (numero.text.toString() != "sms en attente..." || montant.text.toString() != "sms en attente...") {
            //
        }else{
            super.onBackPressed()
        }
    }

    private fun obtenirMotALaPosition(chaine: String, position: Int): String? {
        val mots = chaine.split(" ")
        return if (position in 0 until mots.size) {
            mots[position]
        } else {
            null
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            //Image Uri will not be null for RESULT_OK
            var it: Uri = data?.data!!
            // Use Uri object instead of File to avoid storage permissions
            stateInfo.visibility = View.VISIBLE
            uri = it
            uploaded = true
        } else if (resultCode == ImagePicker.RESULT_ERROR) {
            Toast.makeText(this, ImagePicker.getError(data), Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Prise annulée", Toast.LENGTH_SHORT).show()
        }
    }
}