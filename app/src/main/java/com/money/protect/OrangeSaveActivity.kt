package com.money.protect

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
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

    private lateinit var imagePreview1: ImageView
    private lateinit var imagePreview2: ImageView
    private lateinit var constraintImagePreview: ConstraintLayout

    private var storageRef = Firebase.storage
    private var uri1: Uri? = null
    private var uri2: Uri? = null
    private var uploaded1: Boolean = false
    private var uploaded2: Boolean = false

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
        imagePreview1 = findViewById(R.id.imagePreviewOrange1)
        imagePreview2 = findViewById(R.id.imagePreviewOrange2)
        constraintImagePreview = findViewById(R.id.constraintImagePreviewOrange)

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

        intents = intent
        val sms = intents.getStringExtra("sms")

        if (sms!!.contains("Depot"))
        {
            depot()
            typeOp = "Dépôt"
        }else if (sms.contains("Retrait")){
            retrait()
            typeOp = "Retrait"
        }

        val linkToPopup = findViewById<Button>(R.id.popupSMSOrange)
        linkToPopup.setOnClickListener {
            SmsOrange(this).show()
        }

        val btnCancel = findViewById<TextView>(R.id.btnCancelOperationOrangeMoney)
        btnCancel.setOnClickListener {
            button.visibility = View.VISIBLE
            constraintImagePreview.visibility = View.GONE
            val intent = Intent(this, OrangeSmsListActivity::class.java)
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
                idTransaction.text.toString() == "sms en attente..." ||
                numero.text.toString() == "sms en attente..." ||
                montant.text.toString() == "sms en attente..." ||
                idTransaction.text.toString() == "sms en attente...") {
                val builder = AlertDialog.Builder(this)
                builder.setMessage("Aucune donnée à enregistrer")
                builder.show()
            }else{
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
                        if (uploaded1 && uploaded2)
                        {
                            storageRef.getReference("images").child(System.currentTimeMillis().toString())
                                .putFile(uri1!!)
                                .addOnSuccessListener { task->
                                    task.metadata!!.reference!!.downloadUrl
                                        .addOnSuccessListener {it1->
                                            storageRef.getReference("images").child(System.currentTimeMillis().toString())
                                                .putFile(uri2!!)
                                                .addOnSuccessListener {task1->
                                                    task1.metadata!!.reference!!.downloadUrl
                                                        .addOnSuccessListener { it2->

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
                                                                "url1" to it1.toString(),
                                                                "url2" to it2.toString(),
                                                                "idDoc" to uid,
                                                                "idTransac" to idT.toString().replace(caractereASupprimer.toString(),"")
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
                                                                        constraintImagePreview.visibility = View.GONE

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
                                                    builer.setMessage(R.string.onFailureText)
                                                    builer.show()
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
                        }else if (uploaded2 == null){
                            builder.setMessage("Veuillez enregistrer le recto de la pièce d'identité")
                            builder.show()
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
                                "url1" to "null",
                                "url2" to "null",
                                "idDoc" to uid,
                                "idTransac" to idT.toString().replace(caractereASupprimer.toString(),"")
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
                                    val build = AlertDialog.Builder(this)
                                    build.setTitle("Alerte")
                                    build.setMessage(R.string.onFailureText)
                                    build.show()
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
                    numero.text = num
                    montantDB = supprimerZerosApresPoint(mont)
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
                    numero.text = num
                    montantDB = supprimerZerosApresPoint(mont)
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
            var it1: Uri = data?.data!!
            var it2: Uri = data?.data!!
            constraintImagePreview.visibility = View.VISIBLE
            // Use Uri object instead of File to avoid storage permissions
            if (imagePreview1.drawable == null){
                stateInfo.visibility = View.VISIBLE
                uploaded1 = true
                stateInfo.setText(R.string.warning_second_image)
                uri1 = it1
                it1?.let {uri->
                    imagePreview1.setImageURI(uri)
                    button.visibility = View.GONE
                }
            }else{
                stateInfo.visibility = View.GONE
                uri2 = it2
                uploaded2 = true
                it2?.let {uri->
                    imagePreview2.setImageURI(uri2)
                    button.visibility = View.VISIBLE
                }
            }
        } else if (resultCode == ImagePicker.RESULT_ERROR) {
            Toast.makeText(this, ImagePicker.getError(data), Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Prise annulée", Toast.LENGTH_SHORT).show()
        }
    }
}