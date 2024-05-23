package com.money.protect.fragment_assistant.national

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.ComponentName
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
import java.text.NumberFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID

class WaveFragment(private val context: MainActivity) : Fragment() {
    private var db = Firebase.firestore
    lateinit var auth: FirebaseAuth
    private lateinit var textTelephone: EditText
    private lateinit var textMontant: EditText
    private lateinit var typeOperation: Spinner
    private lateinit var buttonRegister: AppCompatButton
    private lateinit var buttonUpload: Button
    private lateinit var progressBar: ProgressBar
    private var textWatcher: TextWatcher? = null

    private lateinit var imagePreview1: ImageView
    private lateinit var imagePreview2: ImageView
    private lateinit var constraintImagePreview: ConstraintLayout

    private var storageRef = Firebase.storage
    private var uri1: Uri? = null
    private var uri2: Uri? = null
    private var uploaded1: Boolean = false
    private var uploaded2: Boolean = false

    private lateinit var stateInfo: TextView
    private var phoneVal: String ? = null

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_assistant_operation_wave, container, false)

        context.blockScreenShot()

        auth = FirebaseAuth.getInstance()
        storageRef = FirebaseStorage.getInstance()
        imagePreview1 = view.findViewById(R.id.imagePreviewWave1)
        imagePreview2 = view.findViewById(R.id.imagePreviewWave2)
        constraintImagePreview = view.findViewById(R.id.constraintImagePreviewWave)

        textTelephone = view.findViewById(R.id.tel_input_wave)
        textMontant = view.findViewById(R.id.montant_input_wave)
        typeOperation = view.findViewById(R.id.type_op_spinner_wave)
        stateInfo = view.findViewById(R.id.stateInfoWave)
        buttonUpload = view.findViewById(R.id.uploadPhotoWave)

        buttonRegister = view.findViewById(R.id.btn_register_input_wave)
        progressBar = view.findViewById(R.id.progressBar_input_wave)

        // PERMET DE FORMATTER LA SAISIE DU MONTANT EN MILLIER
        textWatcher = object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                //
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                //
            }

            override fun afterTextChanged(s: Editable?) {
                this@WaveFragment.formatEditext(s)
            }

        }
        textMontant.addTextChangedListener(textWatcher)

        val link1 = view.findViewById<ImageView>(R.id.assistant_link1_wave)

        link1.setOnClickListener {
            if (typeOperation.selectedItem.toString() !== "Sélectionner" && textMontant.text.isNotEmpty())
            {
                Toast.makeText(context, "Vous n'avez pas enregistré la transaction", Toast.LENGTH_SHORT).show()
            }else{
                context.supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.fragment_container, MtnFragment(context))
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
                buttonRegister.text = "effectuer la transaction"
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
                buttonRegister.text = "effectuer la transaction"
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

        // Empêcher le retour en arrière si les champs ne sont pas vide
        context.blockBackNavigation(buttonRegister, this)

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

        // EVENEMENT SUR LE SPINNER

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
                    buttonRegister.text = "effectuer la transaction"
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Ne fait rien ici
            }
        }

        val checkbox = view.findViewById<CheckBox>(R.id.checkboxAfficheNumeroEdt)
        val txt = view.findViewById<TextView>(R.id.txt_tel)
        val edt = view.findViewById<ConstraintLayout>(R.id.edt_tel)
        checkbox.setOnClickListener {
            if (checkbox.isChecked) {
                txt.visibility = View.VISIBLE
                edt.visibility = View.VISIBLE
            }else{
                txt.visibility = View.GONE
                edt.visibility = View.GONE
            }
        }


        val btnCancel = view.findViewById<TextView>(R.id.btnCancelOperationWave)
        btnCancel.setOnClickListener {
            textTelephone.text.clear()
            textMontant.text.clear()
            buttonRegister.text = "effectuer la transaction"
            uploaded1 = false
            uploaded2 = false
            imagePreview1.setImageURI(null)
            imagePreview2.setImageURI(null)
            constraintImagePreview.visibility = View.GONE
            stateInfo.visibility = View.GONE
            context.bottomNavUnlock()
            buttonRegister.visibility = View.VISIBLE
        }

        buttonRegister.setOnClickListener {
            if (checkForInternet(context)) {
                if(textMontant.text.isEmpty() || typeOperation.selectedItem.toString() == "Sélectionner")
                {
                    val builder = AlertDialog.Builder(context)
                    builder.setTitle("Alerte")
                    builder.setMessage("Veuillez saisir tous les champs SVP.")
                    builder.show()

                }else{
                    // OUVRIR APPLICATION TIERS
                    if (buttonRegister.text === "effectuer la transaction")
                    {
                        val packageName = "com.sendwave.agent"  // Package name de Facebook
                        val className = "com.sendwave.agent.activities.RootActivity"  // Nom de la classe de l'activité que vous voulez démarrer

                        val intent = Intent()
                        intent.component = ComponentName(packageName, className)
                        try {
                            startActivity(intent)
                            Handler(Looper.getMainLooper()).postDelayed({
                                buttonRegister.text = "enregistrer opération" // Remplacez "Nouveau Texte" par le texte que vous souhaitez afficher
                            }, 3000)

                            // Bloquer le bottomNavigation si l'utilisateur a oublié d'enregistrer la transaction
                            context.bottomNavBlocked(textTelephone.text.toString(), textMontant.text.toString())

                        } catch (e: ActivityNotFoundException) {
                            // Gérez le cas où L'Application WAVE Agent n'est pas installé ou ne permet pas d'ouvrir cette activité
                            val intent = AlertDialog.Builder(context)
                            intent.setTitle("Infos")
                            intent.setMessage("Veuillez installer l'application Wave Agent")
                            intent.show()
                        }
                    }else if (buttonRegister.text === "enregistrer opération") {

                        val builder = AlertDialog.Builder(context)
                        builder.setMessage("Enregistrer la transaction ?")
                        builder.setPositiveButton(android.R.string.yes) { dialog, which->
                            progressBar.visibility = View.VISIBLE
                            buttonRegister.text = "effectuer la transaction"
                            context.bottomNavUnlock()

                            buttonRegister.isEnabled = false

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
                            if (checkForInternet(context)) {
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
                                                                    //formater le montant
                                                                    val theAmount = montantInput.toString()
                                                                    val caractere = ','
                                                                    val theNewAmount = theAmount.filter { it != caractere }

                                                                    val uid = UUID.randomUUID().toString()


                                                                    if (checkbox.isChecked) {
                                                                        phoneVal = telInput.toString()
                                                                    }else{
                                                                        phoneVal = "Non défini"
                                                                    }

                                                                    val operationMap = hashMapOf(
                                                                        "id" to auth.currentUser?.uid,
                                                                        "date" to dateFormatted,
                                                                        "heure" to hourFormatted,
                                                                        "operateur" to "wave",
                                                                        "telephone" to phoneVal.toString(),
                                                                        "montant" to theNewAmount,
                                                                        "typeoperation" to typeSpinner,
                                                                        "statut" to true,
                                                                        "url1" to it1.toString(),
                                                                        "url2" to it2.toString(),
                                                                        "idDoc" to uid
                                                                    )

                                                                    val tel = textTelephone.text
                                                                    val amount = textMontant.text

                                                                    db.collection("operation")
                                                                        .document(uid)
                                                                        .set(operationMap)
                                                                        .addOnCompleteListener {
                                                                            if (it.isSuccessful) {
                                                                                tel.clear()
                                                                                amount.clear()
                                                                                imagePreview1.setImageURI(null)
                                                                                imagePreview2.setImageURI(null)
                                                                                constraintImagePreview.visibility = View.GONE
                                                                                buttonRegister.isEnabled = true
                                                                                stateInfo.visibility = View.GONE
                                                                                progressBar.visibility = View.INVISIBLE
                                                                                buttonRegister.text = "effectuer la transaction"
                                                                                typeOperation.setSelection(0)
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
                                                        }.addOnFailureListener{
                                                            val builer = AlertDialog.Builder(context)
                                                            builer.setMessage(R.string.onFailureText)
                                                            builer.show()
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

                                    if (checkbox.isChecked) {
                                        phoneVal = telInput.toString()
                                    }else{
                                        phoneVal = "Non défini"
                                    }

                                    val operationMap = hashMapOf(
                                        "id" to auth.currentUser?.uid,
                                        "date" to dateFormatted,
                                        "heure" to hourFormatted,
                                        "operateur" to "wave",
                                        "telephone" to phoneVal.toString(),
                                        "montant" to theNewAmount,
                                        "typeoperation" to typeSpinner,
                                        "statut" to true,
                                        "url1" to "null",
                                        "url2" to "null",
                                        "idDoc" to uid
                                    )

                                    val tel = textTelephone.text
                                    val amount = textMontant.text


                                    db.collection("operation")
                                        .document(uid)
                                        .set(operationMap)
                                        .addOnCompleteListener {
                                            if (it.isSuccessful) {
                                                tel.clear()
                                                amount.clear()
                                                buttonRegister.isEnabled = true
                                                stateInfo.visibility = View.GONE
                                                progressBar.visibility = View.INVISIBLE
                                                buttonRegister.text = "effectuer la transaction"
                                                typeOperation.setSelection(0)
                                                Toast.makeText(context, "Enregistré avec succès", Toast.LENGTH_SHORT).show()
                                            }
                                        }.addOnFailureListener {
                                            val builder = AlertDialog.Builder(context)
                                            builder.setTitle("Alerte")
                                            builder.setMessage(R.string.onFailureText)
                                            builder.show()
                                        }
                                }
                            }else{
                                Toast.makeText(context, "Aucune connexion internet", Toast.LENGTH_SHORT).show()
                            }
                        }
                        builder.setNegativeButton(android.R.string.no) { dialog, which->
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