package com.money.protect.fragment_assistant.national

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatButton
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import com.money.protect.MainActivity
import com.money.protect.R
import com.money.protect.fragment_assistant.checkInternet.checkForInternet
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import com.money.protect.OrangeRedirectionActivity
import com.money.protect.popup.SmsMoov
import java.text.NumberFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID

class MoovFragment(private val context: MainActivity) : Fragment() {
    private var db = Firebase.firestore
    lateinit var auth: FirebaseAuth
    private lateinit var textTelephone: EditText
    private lateinit var textMontant: EditText
    private lateinit var typeOperation: Spinner
    private lateinit var buttonRegister: AppCompatButton
    private lateinit var buttonUpload: Button
    private lateinit var progressBar: ProgressBar

    private var textWatcher: TextWatcher? = null

    private var storageRef = Firebase.storage
    private var uri1: Uri? = null
    private var uri2: Uri? = null
    private var uploaded1: Boolean = false
    private var uploaded2: Boolean = false
    private lateinit var imagePreview1: ImageView
    private lateinit var imagePreview2: ImageView
    private lateinit var constraintImagePreview: ConstraintLayout

    private lateinit var stateInfo: TextView

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_assistant_operation_moov, container, false)

        auth = FirebaseAuth.getInstance()
        storageRef = FirebaseStorage.getInstance()
        imagePreview1 = view.findViewById(R.id.imagePreviewMoov1)
        imagePreview2 = view.findViewById(R.id.imagePreviewMoov2)
        constraintImagePreview = view.findViewById(R.id.constraintImagePreviewMoov)

        textTelephone = view.findViewById(R.id.tel_input_moov)
        textMontant = view.findViewById(R.id.montant_input_moov)
        typeOperation = view.findViewById(R.id.type_op_spinner_moov)
        stateInfo = view.findViewById(R.id.stateInfoMoov)

        buttonUpload = view.findViewById(R.id.uploadPhotoMoov)

        buttonRegister = view.findViewById(R.id.btn_register_input_moov)
        progressBar = view.findViewById(R.id.progressBar_input_moov)

        // PERMET DE FORMATTER LA SAISIE DU MONTANT EN MILLIER
        textWatcher = object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                //
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                //
            }

            override fun afterTextChanged(s: Editable?) {
                this@MoovFragment.formatEditext(s)
            }

        }
        textMontant.addTextChangedListener(textWatcher)

        val link1 = view.findViewById<ImageView>(R.id.assistant_link1_moov)
        val link2 = view.findViewById<ImageView>(R.id.assistant_link2_moov)

        // Empêcher l'utilisateur de quitter la fenêtre s'il a oublié d'enregistrer une transaction
        link1.setOnClickListener {
            if (textTelephone.text.isNotEmpty() && textMontant.text.isNotEmpty())
            {
                Toast.makeText(context, "Vous n'avez pas enregistré la transaction", Toast.LENGTH_SHORT).show()
            }else {
                val intent = Intent(context, OrangeRedirectionActivity::class.java)
                intent.putExtra("nomcommercial", context.Nomcom())
                intent.putExtra("creation", context.Creation())
                intent.putExtra("module", context.Module())
                intent.putExtra("duration", context.Duration())
                context.startActivity(intent)
            }
        }
        link2.setOnClickListener {
            if (textTelephone.text.isNotEmpty() && textMontant.text.isNotEmpty())
            {
                Toast.makeText(context, "Vous n'avez pas enregistré la transaction", Toast.LENGTH_SHORT).show()
            }else {
                context.supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.fragment_container, TresorFragment(context))
                    .addToBackStack(null)
                    .commit()
            }
        }

        // BLOQUER LE NOMBRE DE CARACTERES DE SAISIE
        textTelephone.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, start: Int, count: Int, after: Int) {
                // Avant que le texte change
            }

            override fun onTextChanged(charSequence: CharSequence, start: Int, before: Int, count: Int) {
                // Pendant que le texte change
                if (typeOperation.selectedItem.toString() == "Dépôt")
                {
                    buttonRegister.text = "effectuer la transaction"
                }
            }

            override fun afterTextChanged(editable: Editable) {
                // Après que le texte a changé

                // Vérifier si la longueur du texte est supérieure à la limite
                val maxLength = 10
                if (editable.length > maxLength) {
                    // Supprimer les caractères excédentaires
                    editable.delete(maxLength, editable.length)
                }
            }
        })
        textMontant.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, start: Int, count: Int, after: Int) {
                // Avant que le texte change
            }

            override fun onTextChanged(charSequence: CharSequence, start: Int, before: Int, count: Int) {
                // Pendant que le texte change
                if (typeOperation.selectedItem.toString() == "Dépôt")
                {
                    buttonRegister.text = "effectuer la transaction"
                }
            }

            override fun afterTextChanged(editable: Editable) {
                // Après que le texte a changé

                // Vérifier si la longueur du texte est supérieure à la limite
                val maxLength = 10
                if (editable.length > maxLength) {
                    // Supprimer les caractères excédentaires
                    editable.delete(maxLength, editable.length)
                }
            }
        })

        requireActivity().onBackPressedDispatcher.addCallback(context) {

        }

        // Empêcher le retour en arrière si les champs ne sont pas vide
        context.blockBackNavigation(buttonRegister, this)

        // L'historique des transferts
        val btnHistory = view.findViewById<Button>(R.id.historiqueMoov)
        btnHistory.setOnClickListener {
            val syntaxe = "*156*6" + Uri.encode("#")
            val callIntent = Intent(Intent.ACTION_CALL)
            callIntent.data = Uri.parse("tel:$syntaxe")
            startActivity(callIntent)
        }

        val readSMS = view.findViewById<Button>(R.id.openSMSMoov)
        readSMS.setOnClickListener {
            SmsMoov(context, this).show()
        }

        progressBar.visibility = View.INVISIBLE

        // Upload Photo
        buttonUpload.setOnClickListener {
            ImagePicker.with(this)
                .crop()
                .cameraOnly()    			//Crop image(Optional), Check Customization for more option
                .compress(1024)			//Final image size will be less than 1 MB(Optional)
                .maxResultSize(540, 540)	//Final image resolution will be less than 1080 x 1080(Optional)
                .start()
        }

        buttonRegister.text = "effectuer la transaction"

        // EVENEMENT SUR LE SPINNER (Avec les retrait, il n'y a pas de syntaxe à faire)

        val items = arrayOf("Sélectionner", "Dépôt", "Retrait")

        val adapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, items)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        typeOperation.adapter = adapter

        typeOperation.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                val selectedItem = parent.getItemAtPosition(position).toString()
                if (selectedItem == "Dépôt")
                {
                    buttonRegister.text = "effectuer la transaction"
                }else if(selectedItem == "Retrait"){
                    buttonRegister.text = "enregistrer opération"
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Ne fait rien ici
            }
        }

        // Effacer tout le contenu
        val btnCancel = view.findViewById<TextView>(R.id.btnCancelOperationMoov)
        btnCancel.setOnClickListener {
            textTelephone.text.clear()
            textMontant.text.clear()
            buttonRegister.text = "effectuer la transaction"
            stateInfo.visibility = View.GONE
            uploaded1 = false
            uploaded2 = false
            imagePreview1.setImageURI(null)
            imagePreview2.setImageURI(null)
            constraintImagePreview.visibility = View.GONE
            buttonRegister.visibility = View.VISIBLE
            context.bottomNavUnlock()
        }

        buttonRegister.setOnClickListener {
            if (checkForInternet(context)) {
                if(textTelephone.text.isEmpty() || textMontant.text.isEmpty() || typeOperation.selectedItem.toString() == "Sélectionner")
                {
                    val builder = AlertDialog.Builder(context)
                    builder.setTitle("Alerte")
                    builder.setMessage("Veuillez saisir tous les champs SVP.")
                    builder.show()

                }else if(textTelephone.text.length < 10){
                    val builder = AlertDialog.Builder(context)
                    builder.setMessage("Ce numéro ne comporte pas les 10 chiffres requis")
                    builder.show()

                }else{
                    if (buttonRegister.text == "effectuer la transaction" && typeOperation.selectedItem.toString() == "Dépôt")
                    {
                        val tel = textTelephone.text.toString()
                        val amount = textMontant.text.toString()
                        val caractere = ','
                        val newValue = amount.filter { it != caractere }
                        val syntaxe = "*156*1*4*" + tel + "*" + newValue + Uri.encode("#")
                        val callIntent = Intent(Intent.ACTION_CALL)
                        callIntent.data = Uri.parse("tel:$syntaxe")
                        startActivity(callIntent)

                        Handler(Looper.getMainLooper()).postDelayed({
                            buttonRegister.text = "enregistrer opération" // Remplacez "Nouveau Texte" par le texte que vous souhaitez afficher
                        }, 3000)

                        // Bloquer le bottomNavigation si l'utilisateur a oublié d'enregistrer la transaction
                        context.bottomNavBlocked(textTelephone.text.toString(), textMontant.text.toString())

                    }else if(buttonRegister.text == "enregistrer opération" && typeOperation.selectedItem.toString() == "Dépôt"){
                        val builder = AlertDialog.Builder(context)
                        builder.setMessage("Enregistrer la transaction ?")
                        builder.setPositiveButton(android.R.string.yes) { dialog, which ->
                            if (checkForInternet(context)) {
                                buttonRegister.isEnabled = false
                                envoi()
                                stateInfo.visibility = View.GONE
                                typeOperation.setSelection(0)
                                buttonRegister.text = "effectuer la transaction"
                                context.bottomNavUnlock()
                            }else{
                                Toast.makeText(context, "Aucune connexion internet", Toast.LENGTH_SHORT).show()
                            }
                        }
                        builder.setNegativeButton(android.R.string.no){ dialog, which->
                            // Ne rien faire
                        }
                        builder.show()
                    }else if(buttonRegister.text == "enregistrer opération" && typeOperation.selectedItem.toString() == "Retrait"){
                        val builder = AlertDialog.Builder(context)
                        builder.setMessage("Enregistrer la transaction ?")
                        builder.setPositiveButton(android.R.string.yes) { dialog, which ->
                            if (checkForInternet(context)) {
                                buttonRegister.isEnabled = false
                                envoi()
                                stateInfo.visibility = View.GONE
                                typeOperation.setSelection(0)
                                buttonRegister.text = "effectuer la transaction"
                                context.bottomNavUnlock()
                            }else{
                                Toast.makeText(context, "Aucune connexion internet", Toast.LENGTH_SHORT).show()
                            }
                        }
                        builder.setNegativeButton(android.R.string.no){ dialog, which->
                            // Ne rien faire
                        }
                        builder.show()
                    }
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
                textMontant.removeTextChangedListener(textWatcher)
                textMontant.setText(formattedText)
                textMontant.setSelection(formattedText.length)
                textMontant.addTextChangedListener(textWatcher)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun envoi() {
        progressBar.visibility = View.VISIBLE
        buttonRegister.text = "effectuer la transaction"

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
        if (uploaded1 && uploaded2)
        {
            storageRef.getReference("images").child(System.currentTimeMillis().toString())
                .putFile(uri1!!)
                .addOnSuccessListener { task->
                    task.metadata!!.reference!!.downloadUrl
                        .addOnSuccessListener { it1->
                            storageRef.getReference("images").child(System.currentTimeMillis().toString())
                                .putFile(uri2!!)
                                .addOnSuccessListener {task1->
                                    task1.metadata!!.reference!!.downloadUrl
                                        .addOnSuccessListener { it2->
//formater le montant
                                            val theAmount = montantInput.toString()
                                            val caractere = ','
                                            val theNewAmount = theAmount.filter { it != caractere }

                                            val uid = UUID.randomUUID().toString()
                                            val operationMap = hashMapOf(
                                                "id" to auth.currentUser?.uid,
                                                "date" to dateFormatted,
                                                "heure" to hourFormatted,
                                                "operateur" to "moov",
                                                "telephone" to telInput.toString().trim(),
                                                "montant" to theNewAmount,
                                                "typeoperation" to typeSpinner,
                                                "statut" to true,
                                                "url1" to it1.toString(),
                                                "url2" to it2.toString(),
                                                "idDoc" to uid
                                            )

                                            db.collection("operation")
                                                .document(uid)
                                                .set(operationMap)
                                                .addOnCompleteListener {
                                                    if (it.isSuccessful) {
                                                        telInput.clear()
                                                        montantInput.clear()
                                                        imagePreview1.setImageURI(null)
                                                        imagePreview2.setImageURI(null)
                                                        constraintImagePreview.visibility = View.GONE
                                                        buttonRegister.isEnabled = true
                                                        progressBar.visibility = View.INVISIBLE
                                                        buttonRegister.text = "effectuer la transaction"
                                                        constraintImagePreview.visibility = View.GONE
                                                        Toast.makeText(context, "Enregistré avec succès", Toast.LENGTH_SHORT).show()
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
                "operateur" to "moov",
                "telephone" to telInput.toString(),
                "montant" to theNewAmount,
                "typeoperation" to typeSpinner,
                "statut" to true,
                "url1" to "null",
                "url2" to "null",
                "idDoc" to uid
            )

            db.collection("operation")
                .document(uid)
                .set(operationMap)
                .addOnCompleteListener {
                if (it.isSuccessful){
                    buttonRegister.isEnabled = true
                    telInput.clear()
                    montantInput.clear()
                    progressBar.visibility = View.INVISIBLE
                    buttonRegister.text = "effectuer la transaction"
                    Toast.makeText(context, "Enregistré avec succès", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener {
                val builder = AlertDialog.Builder(context)
                builder.setTitle("Alerte")
                builder.setMessage(R.string.onFailureText)
                builder.show()
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            //Image Uri will not be null for RESULT_OK
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
                    buttonRegister.visibility = View.GONE
                }
            }else{
                stateInfo.visibility = View.GONE
                uri2 = it2
                uploaded2 = true
                it2?.let {uri->
                    imagePreview2.setImageURI(uri2)
                    buttonRegister.visibility = View.VISIBLE
                }
            }
        } else if (resultCode == ImagePicker.RESULT_ERROR) {
            Toast.makeText(context, ImagePicker.getError(data), Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Prise annulée", Toast.LENGTH_SHORT).show()
        }
    }

}