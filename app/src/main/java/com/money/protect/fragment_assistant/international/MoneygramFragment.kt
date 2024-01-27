package com.money.protect.fragment_assistant.international

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import androidx.activity.addCallback
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
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID

class MoneygramFragment(private val context: MainActivity) : Fragment() {
    private var db = Firebase.firestore
    lateinit var auth: FirebaseAuth
    private lateinit var textTelephone: EditText
    private lateinit var typeOperation: Spinner
    private lateinit var buttonRegister: AppCompatButton
    private lateinit var buttonUpload: Button
    lateinit var progressBar: ProgressBar
    private lateinit var stateInfo: TextView

    private var storageRef = Firebase.storage
    private var uri: Uri? = null
    private var uploaded: Boolean = false

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_assistant_operation_moneygram, container, false)

        context.blockScreenShot()

        requireActivity().onBackPressedDispatcher.addCallback(context) {

        }

        auth = FirebaseAuth.getInstance()
        storageRef = FirebaseStorage.getInstance()

        textTelephone = view.findViewById(R.id.tel_input_moneygram)
        typeOperation = view.findViewById(R.id.type_op_spinner_moneygram)

        buttonUpload = view.findViewById(R.id.uploadPhotoMoneygram)
        stateInfo = view.findViewById(R.id.stateInfoMoneygram)

        buttonRegister = view.findViewById(R.id.btn_register_input_moneygram)
        progressBar = view.findViewById(R.id.progressBar_input_moneygram)

        val link1 = view.findViewById<ImageView>(R.id.assistant_link1_moneygram)
        val link2 = view.findViewById<ImageView>(R.id.assistant_link2_moneygram)

        link1.setOnClickListener {
            if (textTelephone.text.isNotEmpty())
            {
                Toast.makeText(context, "Vous n'avez pas enregistré la transaction", Toast.LENGTH_SHORT).show()
            }else {
                context.supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.fragment_container, WesternFragment(context))
                    .addToBackStack(null)
                    .commit()
            }
        }
        link2.setOnClickListener {
            if (textTelephone.text.isNotEmpty())
            {
                Toast.makeText(context, "Vous n'avez pas enregistré la transaction", Toast.LENGTH_SHORT).show()
            }else {
                context.supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.fragment_container, RiaFragment(context))
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


        val btnCancel = view.findViewById<TextView>(R.id.btnCancelOperationMoneygram)
        btnCancel.setOnClickListener {
            textTelephone.text.clear()

            // Masquer le block
            uploaded = false
            stateInfo.visibility = View.GONE
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

        buttonRegister.setOnClickListener {
            if (checkForInternet(context)) {
                if(textTelephone.text.isEmpty() || typeOperation.selectedItem.toString() == "Sélectionner")
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
                    progressBar.visibility = View.VISIBLE
                    buttonRegister.isEnabled = false

                    // Générer la date
                    val current = LocalDateTime.now()
                    val formatterDate = DateTimeFormatter.ofPattern("d-M-yyyy")
                    val dateFormatted = current.format(formatterDate)

                    // Générer l'heure
                    val formatterHour = DateTimeFormatter.ofPattern("HH:mm")
                    val hourFormatted = current.format(formatterHour)

                    val telInput = textTelephone.text
                    val typeSpinner = typeOperation.selectedItem.toString()

                    // On upload l'image avant d'enregistrer les données au cas où l'utilisateur a enregistré une image
                    if (checkForInternet(context)) {
                        if (uploaded)
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
                                                "operateur" to "moneygram",
                                                "telephone" to telInput.toString(),
                                                "montant" to "N/A",
                                                "typeoperation" to typeSpinner,
                                                "statut" to true,
                                                "url" to it.toString(),
                                                "idDoc" to uid
                                            )


                                            db.collection("operation")
                                                .document(uid)
                                                .set(operationMap)
                                                .addOnCompleteListener {
                                                    val tel = textTelephone.text

                                                    tel.clear()
                                                    progressBar.visibility = View.INVISIBLE
                                                    stateInfo.visibility = View.INVISIBLE
                                                    buttonRegister.isEnabled = true
                                                    typeOperation.setSelection(0)
                                                    Toast.makeText(context, "Enregistré avec succès", Toast.LENGTH_SHORT).show()

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
                                "operateur" to "moneygram",
                                "telephone" to telInput.toString(),
                                "montant" to "N/A",
                                "typeoperation" to typeSpinner,
                                "statut" to true,
                                "url" to "null",
                                "idDoc" to uid
                            )

                            db.collection("operation")
                                .document(uid)
                                .set(operationMap)
                                .addOnCompleteListener {
                                    val tel = textTelephone.text

                                    tel.clear()
                                    progressBar.visibility = View.INVISIBLE
                                    typeOperation.setSelection(0)
                                    buttonRegister.isEnabled = true
                                    Toast.makeText(context, "Enregistré avec succès", Toast.LENGTH_SHORT).show()

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
            }else{
                Toast.makeText(context, "Aucune connexion internet", Toast.LENGTH_SHORT).show()
            }
        }
        return view
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            //Image Uri will not be null for RESULT_OK
            val it: Uri = data?.data!!
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