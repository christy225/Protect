package com.money.protect.popup

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import com.money.protect.MainActivity
import com.money.protect.R
import java.text.NumberFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID

class OrangePopup(
    private val context: MainActivity
) : Dialog(context) {
    private val db = Firebase.firestore
    private val auth = FirebaseAuth.getInstance()
    private var radio: String? = null
    private var textWatcher: TextWatcher? = null
    private lateinit var montant: EditText
    private lateinit var numero: EditText
    private lateinit var buttonUpload: Button
    private lateinit var stateInfo: TextView

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.item_orange_form)

        val radioButton = findViewById<RadioGroup>(R.id.radioGroupOrangePopup)
        numero = findViewById(R.id.numeroOrangePopup)
        montant = findViewById(R.id.montantOrangePopup)
        buttonUpload = findViewById(R.id.uploadPhotoOrangeCompte2)
        stateInfo = findViewById(R.id.stateUploadPhotoOrangeCompte)
        val button = findViewById<Button>(R.id.buttonRegisterOrangePopup)

        // BLOQUER LE NOMBRE DE CARACTERES DE SAISIE
        numero.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, start: Int, count: Int, after: Int) {
                // Avant que le texte change
            }

            override fun onTextChanged(charSequence: CharSequence, start: Int, before: Int, count: Int) {
                // Pendant que le texte change
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
        montant.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, start: Int, count: Int, after: Int) {
                // Avant que le texte change
            }

            override fun onTextChanged(charSequence: CharSequence, start: Int, before: Int, count: Int) {
                // Pendant que le texte change
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

        // PERMET DE FORMATTER LA SAISIE DU MONTANT EN MILLIER
        textWatcher = object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                //
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                //
            }

            override fun afterTextChanged(s: Editable?) {
                this@OrangePopup.formatEditext(s)
            }

        }
        montant.addTextChangedListener(textWatcher)

        radioButton.setOnCheckedChangeListener { group, checkedId ->
            val selectedRadioButton = findViewById<RadioButton>(checkedId)
            val selectedValue = selectedRadioButton.text.toString()
            radio = selectedValue
        }

        buttonUpload.setOnClickListener {
            ImagePicker.with(context)
                .crop()
                .cameraOnly()    			//Crop image(Optional), Check Customization for more option
                .compress(100)			//Final image size will be less than 1 MB(Optional)
                .maxResultSize(480, 450)	//Final image resolution will be less than 1080 x 1080(Optional)
                .start()
        }

        button.setOnClickListener {
            if (montant.text.isEmpty() || numero.text.isEmpty() || radio.isNullOrBlank()) {
                val builder = AlertDialog.Builder(context)
                builder.setMessage("Veuillez saisir tous les champs SVP")
                builder.show()
            }else{
                // Générer la date
                val current = LocalDateTime.now()
                val formatterDate = DateTimeFormatter.ofPattern("d-M-yyyy")
                val dateFormatted = current.format(formatterDate)

                // Générer l'heure
                val formatterHour = DateTimeFormatter.ofPattern("HH:mm")
                val hourFormatted = current.format(formatterHour)

                //formater le montant
                val theAmount = montant.text.toString()
                val caractere = ','
                val theNewAmount = theAmount.filter { it != caractere }

                val uid = UUID.randomUUID().toString()

                val operationMap = hashMapOf(
                    "id" to auth.currentUser?.uid,
                    "date" to dateFormatted,
                    "heure" to hourFormatted,
                    "operateur" to "orange",
                    "telephone" to numero.text.toString(),
                    "montant" to theNewAmount,
                    "typeoperation" to radio.toString(),
                    "statut" to true,
                    "url" to "null",
                    "idDoc" to uid
                )
                db.collection("operation")
                    .document(uid)
                    .set(operationMap)
                    .addOnCompleteListener {
                        if (it.isSuccessful){
                            Toast.makeText(context, "Enregistré avec succès", Toast.LENGTH_SHORT).show()
                            dismiss()
                        }
                    }.addOnFailureListener {
                        val builder = AlertDialog.Builder(context)
                        builder.setTitle("Alerte")
                        builder.setMessage(R.string.onFailureText)
                        builder.show()
                    }
            }
        }
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
                montant.removeTextChangedListener(textWatcher)
                montant.setText(formattedText)
                montant.setSelection(formattedText.length)
                montant.addTextChangedListener(textWatcher)
            }
        }
    }

}