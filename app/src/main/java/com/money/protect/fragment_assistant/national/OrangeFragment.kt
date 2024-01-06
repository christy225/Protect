package com.money.protect.fragment_assistant.national

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.ContentResolver
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Telephony
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.money.protect.MainActivity
import com.money.protect.R
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import android.content.ContentValues
import android.os.Handler
import android.os.Looper
import android.widget.CheckBox
import android.widget.ProgressBar
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.activity.addCallback
import androidx.constraintlayout.widget.ConstraintLayout
import com.money.protect.OrangeMissingSmsConfirmActivity
import com.money.protect.popup.SmsOrange
import java.util.Locale
import java.util.UUID

class OrangeFragment(private val context: MainActivity) : Fragment() {
    private var db = Firebase.firestore
    private lateinit var auth: FirebaseAuth
    private lateinit var textSMS: TextView

    private lateinit var operation: TextView
    private lateinit var numero: TextView
    private lateinit var montant: TextView
    private lateinit var idTransaction: TextView
    private lateinit var newSolde: TextView
    private lateinit var checkBox: CheckBox
    private var radio: String? = null
    private lateinit var buttonUpload: Button
    private lateinit var stateInfo: TextView
    private lateinit var button: Button
    private lateinit var progressBar: ProgressBar

    private var storageRef = Firebase.storage
    private var uri: Uri? = null
    private var uploaded: Boolean = false

    private var typeOp: String? = null
    private var montantDB: String? = null
    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_assistant_operation_orange, container, false)
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        textSMS = view.findViewById(R.id.orangeTextSMS)

        operation = view.findViewById(R.id.operationOrange)
        numero = view.findViewById(R.id.numeroOrange)
        montant = view.findViewById(R.id.montantOrange)
        idTransaction = view.findViewById(R.id.idTransactionOrange)
        newSolde = view.findViewById(R.id.soldeOrange)
        button = view.findViewById(R.id.btn_register_orange)
        buttonUpload = view.findViewById(R.id.uploadPhotoOrange)
        stateInfo = view.findViewById(R.id.stateInfoOrange)
        progressBar = view.findViewById(R.id.progressBarOrange)

        requireActivity().onBackPressedDispatcher.addCallback(context) {

        }

        if (ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.READ_SMS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            readLastUnreadSMS()
        } else {
            ActivityCompat.requestPermissions(
                context,
                arrayOf(android.Manifest.permission.READ_SMS), READ_SMS_PERMISSION_REQUEST)
        }

        val linkToPopup = view.findViewById<Button>(R.id.popupSMSOrange)
        linkToPopup.setOnClickListener {
            SmsOrange(context, this@OrangeFragment).show()
        }

        val link1 = view.findViewById<ImageView>(R.id.assistant_link_tresor_compte2)
        val link2 = view.findViewById<ImageView>(R.id.assistant_link_moov_compte2)

        // Empêcher l'utilisateur de quitter la fenêtre s'il a oublié d'enregistrer une transaction
        link1.setOnClickListener {
            context.supportFragmentManager
                .beginTransaction()
                .replace(R.id.fragment_container, TresorFragment(context))
                .addToBackStack(null)
                .commit()
        }
        link2.setOnClickListener {
            context.supportFragmentManager
                .beginTransaction()
                .replace(R.id.fragment_container, MoovFragment(context))
                .addToBackStack(null)
                .commit()
        }

        val linkToOrangePopup = view.findViewById<Button>(R.id.openOrangePopup)
        linkToOrangePopup.setOnClickListener {
            val intent = Intent(context, OrangeMissingSmsConfirmActivity::class.java)
            startActivity(intent)
        }

        buttonUpload.setOnClickListener {
            ImagePicker.with(this)
                .crop()
                .cameraOnly()    			//Crop image(Optional), Check Customization for more option
                .compress(100)			//Final image size will be less than 1 MB(Optional)
                .maxResultSize(480, 450)	//Final image resolution will be less than 1080 x 1080(Optional)
                .start()
        }

        button.setOnClickListener {
            if (operation.text.isEmpty() && numero.text.isEmpty() && montant.text.isEmpty()) {
                Toast.makeText(context, "Aucune donnée à enregistrer", Toast.LENGTH_SHORT).show()
            }else{
                if (button.text == "effectuer la transaction"){
                    if (radio.isNullOrBlank()) {
                        val builder = AlertDialog.Builder(context)
                        builder.setMessage("Veuillez sélectionner le type d'opération (Dépôt/Retrait)")
                        builder.show()

                    }else{
                        if (radio.toString() == "Dépôt") {
                            val syntaxe = Uri.encode("#") + "145*1" +  Uri.encode("#")
                            val callIntent = Intent(Intent.ACTION_CALL)
                            typeOp = "Dépôt"
                            callIntent.data = Uri.parse("tel:$syntaxe")
                            startActivity(callIntent)
                            Handler(Looper.getMainLooper()).postDelayed({
                                button.text = "enregistrer opération" // Remplacez "Nouveau Texte" par le texte que vous souhaitez afficher
                            }, 5000)
                        }else if (radio.toString() == "Retrait"){
                            val syntaxe = Uri.encode("#") + "145*2" +  Uri.encode("#")
                            val callIntent = Intent(Intent.ACTION_CALL)
                            typeOp = "Retrait"
                            callIntent.data = Uri.parse("tel:$syntaxe")
                            startActivity(callIntent)
                            Handler(Looper.getMainLooper()).postDelayed({
                                button.text = "enregistrer opération" // Remplacez "Nouveau Texte" par le texte que vous souhaitez afficher
                            }, 5000)
                        }
                    }
                }else if (button.text == "enregistrer opération"){
                    if (!checkBox.isChecked && radio.isNullOrBlank()) {
                        if (textSMS.text.contains("depot"))
                        {
                            typeOp = "Dépôt"
                        }else if (textSMS.text.contains("Retrait")){
                            typeOp = "Retrait"
                        }
                    }
                    button.isEnabled = false
                    progressBar.visibility = View.VISIBLE
                    register()
                    context.supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, OrangeRedirectionFragment(context))
                        .addToBackStack(null)
                        .commit()
                }
            }
        }

        val btnCancel = view.findViewById<TextView>(R.id.btnCancelOperationOrange)
        btnCancel.setOnClickListener {
            uploaded = false
            stateInfo.visibility = View.GONE
        }

        return view
    }

    @SuppressLint("Range", "SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.O)
    private fun readLastUnreadSMS() {
        val resolver: ContentResolver = context.contentResolver
        val uri: Uri = Telephony.Sms.Inbox.CONTENT_URI
        val selection = "${Telephony.Sms.Inbox.READ} = 0 AND ${Telephony.Sms.Inbox.ADDRESS} = '+454'"
        val cursor = resolver.query(
            uri,
            null,
            selection,
            null,
            "${Telephony.Sms.Inbox.DATE} DESC"
        )

        cursor?.use {
            if (it.moveToFirst()) {
                val id = it.getLong(it.getColumnIndex(Telephony.Sms.Inbox.ADDRESS))
                val sender = it.getString(it.getColumnIndex(Telephony.Sms.Inbox.ADDRESS))
                val message = it.getString(it.getColumnIndex(Telephony.Sms.Inbox.BODY))

                textSMS.text = "From: $sender\nMessage: $message"

                markSmsAsRead(id)

                if (textSMS.text.contains("depot"))
                {
                    depot()
                }else if (textSMS.text.contains("Retrait")){
                    retrait()
                }
            } else {
                textSMS.text = "No unread SMS"
            }
        }
    }

    companion object {
        private const val READ_SMS_PERMISSION_REQUEST = 1
    }

    fun depot() {
        val sms = textSMS.text.toString()
        val chainesDeCaracteres = listOf(sms)

        val positionDuMotNumero = 4
        val positionDuMotMontant = 7
        val positionDuMotId = 17
        val positionDuMotSolde = 20

        val format = DecimalFormat("#,###", DecimalFormatSymbols.getInstance(Locale.getDefault()))

        for (chaine in chainesDeCaracteres) {
            val motNumero = obtenirMotALaPosition(chaine, positionDuMotNumero)
            val motMontant = obtenirMotALaPosition(chaine, positionDuMotMontant)
            val motId = obtenirMotALaPosition(chaine, positionDuMotId)
            val motSolde = obtenirMotALaPosition(chaine, positionDuMotSolde)

            operation.text = "Dépôt"
            numero.text = motNumero.toString()
            montant.text = format.format(motMontant!!.toBigDecimal()).toString()
            montantDB = supprimerZerosApresPoint(motMontant.toBigDecimal().toString())
            val caractereASupprimer = ','
            idTransaction.text = motId.toString().replace(caractereASupprimer.toString(),"")
            newSolde.text = format.format(motSolde!!.toBigDecimal()).toString()
        }
    }

    fun retrait() {
        val sms = textSMS.text.toString()
        val chainesDeCaracteres = listOf(sms)

        val positionDuMotNumero = 18
        val positionDuMotMontant = 21
        val positionDuMotId = 24
        val positionDuMotSolde = 8

        val format = DecimalFormat("#,###", DecimalFormatSymbols.getInstance(Locale.getDefault()))

        for (chaine in chainesDeCaracteres) {
            val motNumero = obtenirMotALaPosition(chaine, positionDuMotNumero)
            val motMontant = obtenirMotALaPosition(chaine, positionDuMotMontant)
            val motId = obtenirMotALaPosition(chaine, positionDuMotId)
            val motSolde = obtenirMotALaPosition(chaine, positionDuMotSolde)

            operation.text = "Retrait"
            numero.text = motNumero.toString()
            montant.text = format.format(motMontant!!.toBigDecimal()).toString()
            montantDB = supprimerZerosApresPoint(motMontant.toBigDecimal().toString())
            val caractereASupprimer = '.'
            idTransaction.text = motId.toString().replace(caractereASupprimer.toString(),"")
            newSolde.text = format.format(motSolde!!.toBigDecimal()).toString()
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

    @RequiresApi(Build.VERSION_CODES.O)
    private fun register()
    {
        // Générer la date
        val current = LocalDateTime.now()
        val formatterDate = DateTimeFormatter.ofPattern("d-M-yyyy")
        val dateFormatted = current.format(formatterDate)

        // Générer l'heure
        val formatterHour = DateTimeFormatter.ofPattern("HH:mm")
        val hourFormatted = current.format(formatterHour)

        // On upload l'image avant d'enregistrer les données au cas où l'utilisateur a enregistré une image
        if (uploaded == true)
        {
            storageRef.getReference("images").child(System.currentTimeMillis().toString())
                .putFile(uri!!)
                .addOnSuccessListener { task->
                    task.metadata!!.reference!!.downloadUrl
                        .addOnSuccessListener {

                            val uid = UUID.randomUUID().toString()
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
                                "idDoc" to uid
                            )

                            db.collection("operation")
                                .document(uid)
                                .set(operationMap)
                                .addOnCompleteListener {
                                    if (it.isSuccessful)
                                    {
                                        progressBar.visibility = View.GONE
                                        stateInfo.visibility = View.GONE
                                        button.isEnabled = true
                                        Toast.makeText(context, "Enregistré avec succès", Toast.LENGTH_SHORT).show()
                                        if (!checkBox.isChecked){
                                            Handler(Looper.getMainLooper()).postDelayed({
                                                context.supportFragmentManager.beginTransaction()
                                                    .replace(R.id.fragment_container, OrangeRedirectionFragment(context))
                                                    .addToBackStack(null)
                                                    .commit()
                                            }, 4000)
                                        }
                                    }

                                }.addOnFailureListener {
                                    val builder = AlertDialog.Builder(context)
                                    builder.setTitle("Alerte")
                                    builder.setMessage(R.string.onFailureText)
                                    builder.show()
                                }
                        }.addOnFailureListener {
                            val builer = AlertDialog.Builder(context)
                            builer.setMessage(R.string.onFailureText)
                            builer.show()
                        }
                }.addOnFailureListener{
                    val builer = AlertDialog.Builder(context)
                    builer.setMessage("Erreur pendant le téléchargement de l'image.")
                    builer.show()
                }
        }else{

            // Dans le cas où l'utilisateur n'a pas enregistré d'image on met la valeur à NULL

            val uid = UUID.randomUUID().toString()
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
                "idDoc" to uid
            )

            db.collection("operation")
                .document(uid)
                .set(operationMap)
                .addOnCompleteListener {
                    if (it.isSuccessful)
                    {
                        progressBar.visibility = View.GONE
                        stateInfo.visibility = View.GONE
                        button.isEnabled = true
                        Toast.makeText(context, "Enregistré avec succès", Toast.LENGTH_SHORT).show()
                        Handler(Looper.getMainLooper()).postDelayed({
                            context.supportFragmentManager.beginTransaction()
                                .replace(R.id.fragment_container, OrangeRedirectionFragment(context))
                                .addToBackStack(null)
                                .commit()
                        }, 4000)
                    }

                }.addOnFailureListener {
                    val builder = AlertDialog.Builder(context)
                    builder.setTitle("Alerte")
                    builder.setMessage(R.string.onFailureText)
                    builder.show()
                }
        }
    }

    fun supprimerZerosApresPoint(chaine: String): String {
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

    fun markSmsAsRead(smsId: Long) {
        val contentValues = ContentValues()
        contentValues.put("read", true) // Marque le SMS comme "vu"

        val uri = Uri.parse("content://sms")
        val selection = "_id=?"
        val selectionArgs = arrayOf(smsId.toString())

        context.contentResolver.update(uri, contentValues, selection, selectionArgs)
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
            Toast.makeText(context, ImagePicker.getError(data), Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Prise annulée", Toast.LENGTH_SHORT).show()
        }
    }
}