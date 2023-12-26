package com.money.protect.fragment_assistant.national

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Telephony
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.appcompat.widget.AppCompatButton
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.money.protect.MainActivity
import com.money.protect.R
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import com.money.protect.adapter.SmsAdapter
import com.money.protect.popup.OrangePopup
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

class OrangeCompte2Fragment(private val context: MainActivity) : Fragment() {
    private var db = Firebase.firestore
    lateinit var auth: FirebaseAuth
    private lateinit var checkBox: CheckBox
    private lateinit var previewImage: ImageView
    private lateinit var buttonRegister: AppCompatButton
    private lateinit var buttonUpload: Button
    lateinit var progressBar: ProgressBar
    private lateinit var sectionUpload: CardView
    private lateinit var numeroOrange: TextView
    private lateinit var typeOperation: TextView
    private lateinit var montantOrange: TextView
    private lateinit var idTransaction: TextView
    private lateinit var soldeOrange: TextView
    private lateinit var buttonPopupOrange: Button

    private var textWatcher: TextWatcher? = null

    private var storageRef = Firebase.storage
    lateinit var uri: Uri
    var uploaded: Boolean = false

    // Déclarez une liste pour stocker les SMS.
    private lateinit var smsList: ArrayList<String>

    private val SMS_PERMISSION_REQUEST_CODE = 123
    private lateinit var recyclerView: RecyclerView
    private lateinit var smsAdapterOrange: SmsAdapter

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("MissingInflatedId", "SetTextI18n", "NotifyDataSetChanged")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_assistant_operation_orange_compte2, container, false)

        auth = FirebaseAuth.getInstance()
        storageRef = FirebaseStorage.getInstance()

        // Récupérer le dernier message et l'introduire dans le texteView
        val data = arguments

        smsList = arrayListOf()
        recyclerView = view.findViewById(R.id.recyclerViewSmsOrange)
        numeroOrange = view.findViewById(R.id.numeroOrange)
        typeOperation = view.findViewById(R.id.operationOrange)
        montantOrange = view.findViewById(R.id.montantOrange)
        idTransaction = view.findViewById(R.id.idTransactionOrange)
        soldeOrange = view.findViewById(R.id.soldeOrange)
        buttonPopupOrange = view.findViewById(R.id.openOrangePopup)

        smsAdapterOrange = SmsAdapter(context, smsList)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = smsAdapterOrange
        recyclerView.isEnabled = false

        buttonPopupOrange.setOnClickListener {
            OrangePopup(this).show()
        }

        // Utilisation de la locale par défaut pour obtenir le séparateur de milliers correct
        val format = DecimalFormat("#,###", DecimalFormatSymbols.getInstance(Locale.getDefault()))

        val message = data?.getString("message")
        if (message != null) {
            if (message.contains("Retrait"))
            {
                typeOperation.text = "Retrait"
                val tableauDeChaines = message.split(".")
                val chaineDesireeNumero = tableauDeChaines.getOrNull(3)

                val textNumero = chaineDesireeNumero?.split(" ")
                val valNumero = textNumero?.getOrNull(2)

                numeroOrange.text = valNumero

                val chaineDesireeMontant = tableauDeChaines.getOrNull(4)
                val textMontant = chaineDesireeMontant?.split(" ")
                val valMontant = textMontant?.getOrNull(2)

                montantOrange.text = format.format(valMontant?.toInt()).toString()

                val chaineDesireeTransaction = tableauDeChaines.getOrNull(6)
                val textIdTransac = chaineDesireeTransaction?.split(" ")

                val valIdTransac1= textIdTransac?.getOrNull(2)
                val valIdTransac2= tableauDeChaines.getOrNull(7)
                val valIdTransac3= tableauDeChaines.getOrNull(8)

                idTransaction.text = "$valIdTransac1.$valIdTransac2.$valIdTransac3"

                val chaineDesireeSolde = tableauDeChaines.getOrNull(1)
                val textSolde = chaineDesireeSolde?.split(" ")
                val valSolde = textSolde?.getOrNull(3)
                soldeOrange.text = format.format(valSolde?.toInt()).toString()

            }else if (message.contains("Depot")){
                typeOperation.text = "Dépôt"
                val tableauDeChaines2 = message.split(".")
                val chaineDesiree2 = tableauDeChaines2.getOrNull(0)

                val textNumero2 = chaineDesiree2?.split(" ")
                val valNumero2 = textNumero2?.getOrNull(2)

                numeroOrange.text = valNumero2

                val chaineDesiree3 = tableauDeChaines2.getOrNull(1)
                val textMontant3 = chaineDesiree3?.split(" ")
                val valMontant3 = textMontant3?.getOrNull(2)

                montantOrange.text = format.format(valMontant3?.toInt()).toString()

                val chaineDesireeTransactionX = tableauDeChaines2.getOrNull(4)
                val textIdTransac1 = chaineDesireeTransactionX?.split(" ")

                val valIdTransac1= textIdTransac1?.getOrNull(4)
                val valIdTransac2= tableauDeChaines2.getOrNull(5)

                val xx = tableauDeChaines2.getOrNull(6)
                val dd = xx?.split(',')
                val valIdTransac3 = dd?.getOrNull(0)

                idTransaction.text = "$valIdTransac1.$valIdTransac2.$valIdTransac3"

                val chaineDesireeSolde = tableauDeChaines2.getOrNull(6)
                val textSolde = chaineDesireeSolde?.split(" ")
                val valSolde = textSolde?.getOrNull(3)
                soldeOrange.text = format.format(valSolde?.toInt()).toString()
            }
        }

        if (hasReadSmsPermission()) {
            // Vous avez déjà la permission pour lire les SMS.
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_SMS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // Ne rien faire
            }
            readSms()
        } else {
            requestSmsPermission()
        }

        previewImage = view.findViewById(R.id.image_preview_orange_compte2)
        checkBox = view.findViewById(R.id.checkBoxOrange_compte2)
        sectionUpload = view.findViewById(R.id.section_upload_input_orange_compte2)
        buttonUpload = view.findViewById(R.id.uploadPhotoOrange_compte2)

        buttonRegister = view.findViewById(R.id.btn_register_input_orange_compte2)
        progressBar = view.findViewById(R.id.progressBar_input_orange_compte2)

        // PERMET DE FORMATTER LA SAISIE DU MONTANT EN MILLIER
        textWatcher = object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                //
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                //
            }

            override fun afterTextChanged(s: Editable?) {
                this@OrangeCompte2Fragment.formatEditext(s)
            }

        }

        val link1 = view.findViewById<ImageView>(R.id.assistant_link_orange_compte2)

        link1.setOnClickListener {

        }


        progressBar.visibility = View.INVISIBLE

        checkBox.setOnClickListener {
            if (checkBox.isChecked)
            {
                sectionUpload.visibility = View.VISIBLE
            }else{
                sectionUpload.visibility = View.GONE
            }
        }

        // Empêcher le retour en arrière si les champs ne sont pas vide
        context.blockBackNavigation(buttonRegister)

        // Upload Photo
        buttonUpload.setOnClickListener {
            ImagePicker.with(this)
                .crop()
                .cameraOnly()    			//Crop image(Optional), Check Customization for more option
                .compress(100)			//Final image size will be less than 1 MB(Optional)
                .maxResultSize(480, 450)	//Final image resolution will be less than 1080 x 1080(Optional)
                .start()
        }

        buttonRegister.text = "effectuer la transaction"

        val btnCancel = view.findViewById<TextView>(R.id.btnCancelOperationOrange_compte2)
        btnCancel.setOnClickListener {
            buttonRegister.text = "effectuer la transaction"

            // Masquer le block
            previewImage.setImageResource(0)
            val params = sectionUpload.layoutParams as LinearLayout.LayoutParams
            params.height = 0
            sectionUpload.layoutParams = params

            checkBox.isChecked = false

            context.bottomNavUnlock()
        }

        /*buttonRegister.setOnClickListener {
            if (checkForInternet(context)) {
                if(textMontant.text.isEmpty())
                {
                    val builder = AlertDialog.Builder(context)
                    builder.setTitle("Alerte")
                    builder.setMessage("Veuillez saisir tous les champs SVP.")
                    builder.show()

                }else{
                    // OUVRIR APPLICATION TIERS
                    if (buttonRegister.text === "effectuer la transaction")
                    {
                        if (typeOperation.selectedItem.toString() == "Dépôt")
                        {
                            val syntaxe = Uri.encode("#") + "145*1" + Uri.encode("#")
                            val callIntent = Intent(Intent.ACTION_CALL)
                            callIntent.data = Uri.parse("tel:$syntaxe")
                            startActivity(callIntent)
                            buttonRegister.text = "enregistrer opération"

                            // Bloquer le bottomNavigation si l'utilisateur a oublié d'enregistrer la transaction
                            context.bottomNavBlocked(textTelephone.text.toString(), textMontant.text.toString())

                        }else if (typeOperation.selectedItem.toString() == "Retrait"){
                            val syntaxe = Uri.encode("#") + "145*2" + Uri.encode("#")
                            val callIntent = Intent(Intent.ACTION_CALL)
                            callIntent.data = Uri.parse("tel:$syntaxe")
                            startActivity(callIntent)
                            buttonRegister.text = "enregistrer opération"

                            // Bloquer le bottomNavigation si l'utilisateur a oublié d'enregistrer la transaction
                            context.bottomNavBlocked(textTelephone.text.toString(), textMontant.text.toString())
                        }
                    }else if (buttonRegister.text === "enregistrer opération") {
                        progressBar.visibility = View.VISIBLE
                        buttonRegister.text = "effectuer la transaction"
                        context.bottomNavUnlock()

                        // Générer la date
                        val current = LocalDateTime.now()
                        val formatterDate = DateTimeFormatter.ofPattern("d-M-yyyy")
                        val dateFormatted = current.format(formatterDate)

                        // Générer l'heure
                        val formatterHour = DateTimeFormatter.ofPattern("HH:mm")
                        val hourFormatted = current.format(formatterHour)

                        val telInput = textTelephone.text
                        val montantInput = textMontant.text
                        val typeSpinner = typeOperation.selectedItem.toString()

                        // On upload l'image avant d'enregistrer les données au cas où l'utilisateur a enregistré une image
                        if (uploaded == true)
                        {
                            storageRef.getReference("images").child(System.currentTimeMillis().toString())
                                .putFile(uri)
                                .addOnSuccessListener { task->
                                    task.metadata!!.reference!!.downloadUrl
                                        .addOnSuccessListener {it->
                                            //formater le montant
                                            val theAmount = montantInput.toString()
                                            val caractere = ','
                                            val theNewAmount = theAmount.filter { it != caractere }

                                            val uid = UUID.randomUUID().toString()
                                            val operationMap = hashMapOf(
                                                "id" to auth.currentUser?.uid,
                                                "date" to dateFormatted,
                                                "heure" to hourFormatted,
                                                "operateur" to "orange",
                                                "telephone" to telInput.toString(),
                                                "montant" to theNewAmount,
                                                "typeoperation" to typeSpinner,
                                                "statut" to true,
                                                "url" to "null",
                                                "idDoc" to uid
                                            )

                                            val tel = textTelephone.text
                                            val amount = textMontant.text

                                            tel.clear()
                                            amount.clear()
                                            progressBar.visibility = View.INVISIBLE
                                            buttonRegister.text = "effectuer la transaction"
                                            previewImage.setImageResource(0)
                                            typeOperation.setSelection(0)
                                            Toast.makeText(context, "Enregistré avec succès", Toast.LENGTH_SHORT).show()

                                            db.collection("operation")
                                                .document(uid)
                                                .set(operationMap)
                                                .addOnCompleteListener {
                                                // Ne rien faire ici

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
                            //formater le montant
                            val theAmount = montantInput.toString()
                            val caractere = ','
                            val theNewAmount = theAmount.filter { it != caractere }

                            val uid = UUID.randomUUID().toString()
                            val operationMap = hashMapOf(
                                "id" to auth.currentUser?.uid,
                                "date" to dateFormatted,
                                "heure" to hourFormatted,
                                "operateur" to "orange",
                                "telephone" to telInput.toString(),
                                "montant" to theNewAmount,
                                "typeoperation" to typeSpinner,
                                "statut" to true,
                                "url" to "null",
                                "idDoc" to uid
                            )

                            val tel = textTelephone.text
                            val amount = textMontant.text

                            tel.clear()
                            amount.clear()
                            progressBar.visibility = View.INVISIBLE
                            buttonRegister.text = "effectuer la transaction"
                            previewImage.setImageResource(0)
                            typeOperation.setSelection(0)
                            Toast.makeText(context, "Enregistré avec succès", Toast.LENGTH_SHORT).show()

                            db.collection("operation")
                                .document(uid)
                                .set(operationMap)
                                .addOnCompleteListener {
                                // Ne rien faire ici
                            }.addOnFailureListener {
                                val builder = AlertDialog.Builder(context)
                                builder.setTitle("Alerte")
                                builder.setMessage(R.string.onFailureText)
                                builder.show()
                            }
                        }
                    }

                }
            }else{
                Toast.makeText(context, "Aucune connexion internet", Toast.LENGTH_SHORT).show()
            }
        }*/
        return view
    }

    private fun hasReadSmsPermission(): Boolean {
        val permission = Manifest.permission.READ_SMS
        val granted = PackageManager.PERMISSION_GRANTED
        return ContextCompat.checkSelfPermission(context, permission) == granted
    }

    private fun requestSmsPermission() {
        ActivityCompat.requestPermissions(
            context,
            arrayOf(Manifest.permission.READ_SMS),
            SMS_PERMISSION_REQUEST_CODE
        )
    }

    @SuppressLint("SupportAnnotationUsage", "NotifyDataSetChanged")
    @RequiresPermission(android.Manifest.permission.READ_SMS)
    private fun readSms() {
        val contentResolver = context.contentResolver

        val selection = "${Telephony.Sms.ADDRESS} = ?" // Sélectionnez les SMS avec l'adresse spécifique
        val selectionArgs = arrayOf("+2250747033014")

        val uri = Telephony.Sms.Inbox.CONTENT_URI
        val cursor = contentResolver.query(uri, null, selection, selectionArgs, null)

        if (cursor != null) {
            while (cursor.moveToNext()) {
                val bodyIndex = cursor.getColumnIndex(Telephony.Sms.BODY)
                val smsBody = cursor.getString(bodyIndex)
                smsList.add(smsBody)
            }
            cursor.close()
            smsAdapterOrange.notifyDataSetChanged() // Notifiez l'adaptateur que les données ont changé.
        }
    }

    // PERMET DE FORMATTER LA SAISIE DU MONTANT EN MILLIER
    private fun formatEditext(s: Editable?) {
        if (!s.isNullOrBlank())
        {
            val originalText = s.toString().replace(",","")
            val number = originalText.toBigDecimalOrNull()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            //Image Uri will not be null for RESULT_OK
            val it: Uri = data?.data!!
            // Use Uri object instead of File to avoid storage permissions
            previewImage.setImageURI(it)
            uri = it
            uploaded = true
        } else if (resultCode == ImagePicker.RESULT_ERROR) {
            Toast.makeText(context, ImagePicker.getError(data), Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Prise annulée", Toast.LENGTH_SHORT).show()
        }
    }

}