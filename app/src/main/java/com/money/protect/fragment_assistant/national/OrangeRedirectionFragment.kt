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
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.addCallback
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatButton
import androidx.cardview.widget.CardView
import androidx.core.view.isEmpty
import androidx.fragment.app.Fragment
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.money.protect.MainActivity
import com.money.protect.OrangeSmsListActivity
import com.money.protect.R
import java.text.NumberFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.UUID

class OrangeRedirectionFragment(private val context: MainActivity) : Fragment() {
    private lateinit var button: Button
    private lateinit var auth: FirebaseAuth
    private var db = Firebase.firestore
    private var radio: String? = null
    private lateinit var checkBox: CheckBox
    private lateinit var card: LinearLayout
    private lateinit var buttonRegister: AppCompatButton
    private lateinit var telephone: EditText
    private lateinit var montant: EditText
    private var textWatcher: TextWatcher? = null
    private lateinit var buttonUpload: Button
    private lateinit var progressBar: ProgressBar

    private lateinit var stateInfo: TextView
    private var storageRef = Firebase.storage
    private var uri: Uri? = null
    private var uploaded: Boolean = false
    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_assistant_orange_redirection, container, false)
        auth = FirebaseAuth.getInstance()
        button = view.findViewById(R.id.buttonLaunchOrange)
        telephone = view.findViewById(R.id.tel_input_orangeRedirection)
        montant = view.findViewById(R.id.montant_input_orangeRedirection)
        buttonRegister = view.findViewById(R.id.btn_register_input_orangeRedirection)
        card = view.findViewById(R.id.cardTransactionOrange)
        checkBox = view.findViewById(R.id.checkBoxOrange_syntaxe)
        stateInfo = view.findViewById(R.id.stateInfo_orangeRedirection)
        buttonUpload = view.findViewById(R.id.uploadPhoto_OrangeRedirection)
        progressBar = view.findViewById(R.id.progressBar_orangeRedirection)


        val link1 = view.findViewById<ImageView>(R.id.assistant_link_tresor_redirection)
        link1.setOnClickListener {
            context.supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, TresorFragment(context))
                .addToBackStack(null)
                .commit()
        }
        val link2 = view.findViewById<ImageView>(R.id.assistant_link_moov_redirection)
        link2.setOnClickListener {
            context.supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, MoovFragment(context))
                .addToBackStack(null)
                .commit()
        }

        button.setOnClickListener {
            val intent1 = Intent(context, OrangeSmsListActivity::class.java)
            startActivity(intent1)

            val packageName = "com.orange.ci.ompdv"  // Package name de Facebook
            val className = "com.orange.ci.ompdv.MainActivity"

            val intent = Intent()
            intent.component = ComponentName(packageName, className)
            try {
                context.startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                // Gérez le cas où L'Application WAVE Agent n'est pas installé ou ne permet pas d'ouvrir cette activité
                val intent = AlertDialog.Builder(context)
                intent.setTitle("Infos")
                intent.setMessage("Veuillez installer l'application Wave Agent")
                intent.show()
            }
        }

        buttonUpload.setOnClickListener {
            ImagePicker.with(this)
                .crop()
                .cameraOnly()    			//Crop image(Optional), Check Customization for more option
                .compress(100)			//Final image size will be less than 1 MB(Optional)
                .maxResultSize(480, 450)	//Final image resolution will be less than 1080 x 1080(Optional)
                .start()
        }

        val cardLaunchApps = view.findViewById<CardView>(R.id.cardOrangeRedirection)

        checkBox.setOnClickListener {
            if (checkBox.isChecked) {
                card.visibility = View.VISIBLE
                cardLaunchApps.visibility = View.GONE
            }else{
                card.visibility = View.GONE
                cardLaunchApps.visibility = View.VISIBLE
            }
        }

        // BLOQUER LE NOMBRE DE CARACTERES DE SAISIE
        telephone.addTextChangedListener(object : TextWatcher {
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
                this@OrangeRedirectionFragment.formatEditext(s)
            }

        }
        montant.addTextChangedListener(textWatcher)

        val radioButton = view.findViewById<RadioGroup>(R.id.radioGroup_orangeRedirection)
        radioButton.setOnCheckedChangeListener { group, checkedId ->
            val selectedRadioButton = view.findViewById<RadioButton>(checkedId)
            val selectedValue = selectedRadioButton.text.toString()
            radio = selectedValue
        }

        buttonRegister.text = "effectuer la transaction"

        buttonRegister.setOnClickListener {
            if (telephone.text.isEmpty() || montant.text.isEmpty() || radio.isNullOrBlank())
            {
                Toast.makeText(context, "Veuillez saisir tous les champs SVP", Toast.LENGTH_SHORT).show()
            }else{
                if (buttonRegister.text == "effectuer la transaction") {
                    if (radio == "Dépôt") {
                        val syntaxe = Uri.encode("#") + "145*1" + Uri.encode("#")
                        val callIntent = Intent(Intent.ACTION_CALL)
                        callIntent.data = Uri.parse("tel:$syntaxe")
                        startActivity(callIntent)
                        Handler(Looper.getMainLooper()).postDelayed({
                            buttonRegister.text = "enregistrer opération" // Remplacez "Nouveau Texte" par le texte que vous souhaitez afficher
                        }, 5000)
                    }else if (radio == "Retrait"){
                        val syntaxe = Uri.encode("#") + "145*2" + Uri.encode("#")
                        val callIntent = Intent(Intent.ACTION_CALL)
                        callIntent.data = Uri.parse("tel:$syntaxe")
                        startActivity(callIntent)
                        Handler(Looper.getMainLooper()).postDelayed({
                            buttonRegister.text = "enregistrer opération" // Remplacez "Nouveau Texte" par le texte que vous souhaitez afficher
                        }, 5000)
                    }
                }else if(buttonRegister.text == "enregistrer opération") {
                    // Générer la date
                    val current = LocalDateTime.now()
                    val formatterDate = DateTimeFormatter.ofPattern("d-M-yyyy")
                    val dateFormatted = current.format(formatterDate)

                    // Générer l'heure
                    val formatterHour = DateTimeFormatter.ofPattern("HH:mm")
                    val hourFormatted = current.format(formatterHour)

                    progressBar.visibility = View.VISIBLE

                    // On upload l'image avant d'enregistrer les données au cas où l'utilisateur a enregistré une image
                    if (uploaded)
                    {
                        storageRef.getReference("images").child(System.currentTimeMillis().toString())
                            .putFile(uri!!)
                            .addOnSuccessListener { task->
                                //formater le montant
                                val theAmount = montant.text.toString()
                                val caractere = ','
                                val theNewAmount = theAmount.filter { it != caractere }

                                task.metadata!!.reference!!.downloadUrl
                                    .addOnSuccessListener {
                                        val uid = UUID.randomUUID().toString()
                                        val operationMap = hashMapOf(
                                            "id" to auth.currentUser?.uid,
                                            "date" to dateFormatted,
                                            "heure" to hourFormatted,
                                            "operateur" to "orange",
                                            "telephone" to telephone.text.toString(),
                                            "montant" to theNewAmount,
                                            "typeoperation" to radio.toString(),
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
                                                    montant.text.clear()
                                                    telephone.text.clear()
                                                    progressBar.visibility = View.GONE
                                                    stateInfo.visibility = View.GONE
                                                    button.isEnabled = true
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
                                builer.setMessage("Erreur pendant le téléchargement de l'image.")
                                builer.show()
                            }
                    }else{

                        // Dans le cas où l'utilisateur n'a pas enregistré d'image on met la valeur à NULL
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
                            "telephone" to telephone.text.toString(),
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
                                if (it.isSuccessful)
                                {
                                    montant.text.clear()
                                    telephone.text.clear()
                                    progressBar.visibility = View.GONE
                                    stateInfo.visibility = View.GONE
                                    button.isEnabled = true
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
                montant.removeTextChangedListener(textWatcher)
                montant.setText(formattedText)
                montant.setSelection(formattedText.length)
                montant.addTextChangedListener(textWatcher)
            }
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
            Toast.makeText(context, ImagePicker.getError(data), Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Prise annulée", Toast.LENGTH_SHORT).show()
        }
    }
}