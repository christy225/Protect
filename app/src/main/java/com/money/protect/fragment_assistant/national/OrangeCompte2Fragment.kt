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
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatButton
import androidx.cardview.widget.CardView
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

class OrangeCompte2Fragment(private val context: MainActivity) : Fragment() {
    private var db = Firebase.firestore
    lateinit var auth: FirebaseAuth
    private lateinit var textTelephone: EditText
    private lateinit var textMontant: EditText
    private lateinit var typeOperation: Spinner
    private lateinit var checkBox: CheckBox
    private lateinit var previewImage: ImageView
    private lateinit var buttonRegister: AppCompatButton
    private lateinit var buttonUpload: Button
    lateinit var progressBar: ProgressBar
    private lateinit var sectionUpload: CardView

    private var textWatcher: TextWatcher? = null

    private var storageRef = Firebase.storage
    lateinit var uri: Uri
    var uploaded: Boolean = false

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_assistant_operation_orange_compte2, container, false)

        auth = FirebaseAuth.getInstance()
        storageRef = FirebaseStorage.getInstance()

        textTelephone = view.findViewById(R.id.tel_input_orange_compte2)
        textMontant = view.findViewById(R.id.montant_input_orange_compte2)
        typeOperation = view.findViewById(R.id.type_op_spinner_orange_compte2)

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
        textMontant.addTextChangedListener(textWatcher)

        val link1 = view.findViewById<ImageView>(R.id.assistant_link_orange_compte2)

        link1.setOnClickListener {
            if (textTelephone.text.isNotEmpty() && textMontant.text.isNotEmpty())
            {
                Toast.makeText(context, "Vous n'avez pas enregistré la transaction", Toast.LENGTH_SHORT).show()
            }else {
                context.supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.fragment_container, TresorCompte2Fragment(context))
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

        progressBar.visibility = View.INVISIBLE

        // ON MASQUE LA SECTION DE L'UPLOAD
        val params = sectionUpload.layoutParams as LinearLayout.LayoutParams
        params.height = 0

        checkBox.setOnClickListener {
            if (checkBox.isChecked)
            {
                val params = sectionUpload.layoutParams as LinearLayout.LayoutParams
                params.height = 500
                sectionUpload.layoutParams = params
            }else{
                previewImage.setImageResource(0)
                val params = sectionUpload.layoutParams as LinearLayout.LayoutParams
                params.height = 0
                sectionUpload.layoutParams = params
            }
        }

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
                    buttonRegister.text = "effectuer la transaction"
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Ne fait rien ici
            }
        }

        val btnCancel = view.findViewById<TextView>(R.id.btnCancelOperationOrange_compte2)
        btnCancel.setOnClickListener {
            textTelephone.text.clear()
            textMontant.text.clear()
            buttonRegister.text = "effectuer la transaction"

            // Masquer le block
            previewImage.setImageResource(0)
            val params = sectionUpload.layoutParams as LinearLayout.LayoutParams
            params.height = 0
            sectionUpload.layoutParams = params

            typeOperation.setSelection(0)

            checkBox.isChecked = false

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
                                        .addOnSuccessListener {
                                            val operationMap = hashMapOf(
                                                "id" to auth.currentUser?.uid,
                                                "date" to dateFormatted,
                                                "heure" to hourFormatted,
                                                "operateur" to "orange",
                                                "telephone" to telInput.toString(),
                                                "montant" to montantInput.toString(),
                                                "typeoperation" to typeSpinner,
                                                "url" to it.toString()
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

                                            db.collection("operation").add(operationMap).addOnCompleteListener {
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
                            val operationMap = hashMapOf(
                                "id" to auth.currentUser?.uid,
                                "date" to dateFormatted,
                                "heure" to hourFormatted,
                                "operateur" to "orange",
                                "telephone" to telInput.toString(),
                                "montant" to montantInput.toString(),
                                "typeoperation" to typeSpinner,
                                "url" to "null"
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

                            db.collection("operation").add(operationMap).addOnCompleteListener {
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