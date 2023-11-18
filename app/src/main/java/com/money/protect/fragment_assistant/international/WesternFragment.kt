package com.money.protect.fragment_assistant.international

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class WesternFragment(private val context: MainActivity) : Fragment() {
    private var db = Firebase.firestore
    lateinit var auth: FirebaseAuth
    lateinit var textTelephone: EditText
    lateinit var typeOperation: Spinner
    lateinit var checkBox: CheckBox
    lateinit var previewImage: ImageView
    lateinit var buttonRegister: AppCompatButton
    lateinit var buttonUpload: Button
    lateinit var progressBar: ProgressBar
    lateinit var sectionUpload: CardView

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
        val view = inflater.inflate(R.layout.fragment_assistant_operation_western, container, false)

        context.blockScreenShot()

        auth = FirebaseAuth.getInstance()
        storageRef = FirebaseStorage.getInstance()

        textTelephone = view.findViewById(R.id.tel_input_western)
        typeOperation = view.findViewById(R.id.type_op_spinner_western)

        previewImage = view.findViewById(R.id.image_preview_western)
        checkBox = view.findViewById(R.id.checkBoxWestern)
        sectionUpload = view.findViewById(R.id.section_upload_input_western)
        buttonUpload = view.findViewById(R.id.uploadPhotoWestern)

        buttonRegister = view.findViewById(R.id.btn_register_input_western)
        progressBar = view.findViewById(R.id.progressBar_input_western)

        val link1 = view.findViewById<ImageView>(R.id.assistant_link1_western)
        val link2 = view.findViewById<ImageView>(R.id.assistant_link2_western)

        link1.setOnClickListener {
            if (textTelephone.text.isNotEmpty())
            {
                Toast.makeText(context, "Vous n'avez pas enregistré la transaction", Toast.LENGTH_SHORT).show()
            }else {
                context.supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.fragment_container, MoneygramFragment(context))
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

        val btnCancel = view.findViewById<TextView>(R.id.btnCancelOperationWestern)
        btnCancel.setOnClickListener {
            textTelephone.text.clear()

            // Masquer le block
            previewImage.setImageResource(0)
            val params = sectionUpload.layoutParams as LinearLayout.LayoutParams
            params.height = 0
            sectionUpload.layoutParams = params
            checkBox.isChecked = false
        }

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
                .cameraOnly()    			//Crop image(Optional), Check Customization for more option
                .compress(100)			//Final image size will be less than 1 MB(Optional)
                .maxResultSize(480, 450)	//Final image resolution will be less than 1080 x 1080(Optional)
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
                }else{
                    progressBar.visibility = View.VISIBLE
                    buttonRegister.isEnabled = false
                    buttonRegister.setText(R.string.button_loading)

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
                                            "operateur" to "western union",
                                            "telephone" to telInput.toString().trim(),
                                            "montant" to "N/A",
                                            "typeoperation" to typeSpinner,
                                            "url" to it.toString()
                                        )

                                        val tel = textTelephone.text

                                        tel.clear()
                                        progressBar.visibility = View.INVISIBLE
                                        buttonRegister.text = "effectuer la transaction"
                                        previewImage.setImageResource(0)
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
                            "operateur" to "western union",
                            "telephone" to telInput.toString().trim(),
                            "montant" to "N/A",
                            "typeoperation" to typeSpinner,
                            "url" to "null"
                        )

                        val tel = textTelephone.text

                        tel.clear()
                        progressBar.visibility = View.INVISIBLE
                        buttonRegister.text = "effectuer la transaction"
                        previewImage.setImageResource(0)
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
            }else{
                Toast.makeText(context, "Impossible de se connecter à internet", Toast.LENGTH_SHORT).show()
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