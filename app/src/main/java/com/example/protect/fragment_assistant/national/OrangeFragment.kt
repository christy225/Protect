package com.example.protect.fragment_assistant.national

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
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatButton
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import com.example.protect.MainActivity
import com.example.protect.R
import com.example.protect.fragment_assistant.checkInternet.checkForInternet
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class OrangeFragment(private val context: MainActivity) : Fragment() {
    private var db = Firebase.firestore
    lateinit var auth: FirebaseAuth
    lateinit var textTelephone: EditText
    lateinit var textMontant: EditText
    lateinit var typeOperation: Spinner
    lateinit var checkBox: CheckBox
    lateinit var previewImage: ImageView
    lateinit var buttonRegister: AppCompatButton
    lateinit var buttonUpload: AppCompatButton
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
        val view = inflater.inflate(R.layout.fragment_assistant_operation_orange, container, false)
        auth = FirebaseAuth.getInstance()
        storageRef = FirebaseStorage.getInstance()

        textTelephone = view.findViewById(R.id.tel_input_orange)
        textMontant = view.findViewById(R.id.montant_input_orange)
        typeOperation = view.findViewById(R.id.type_op_spinner_orange)

        previewImage = view.findViewById(R.id.image_preview_orange)
        checkBox = view.findViewById(R.id.checkBox)
        sectionUpload = view.findViewById(R.id.section_upload_input_orange)
        buttonUpload = view.findViewById(R.id.button_upload_image_orange_transaction)

        buttonRegister = view.findViewById(R.id.btn_register_input_orange)
        progressBar = view.findViewById(R.id.progressBar_input_orange)

        val link1 = view.findViewById<ImageView>(R.id.assistant_link1_orange)
        val link2 = view.findViewById<ImageView>(R.id.assistant_link2_orange)
        val link3 = view.findViewById<ImageView>(R.id.assistant_link3_orange)
        val link4 = view.findViewById<ImageView>(R.id.assistant_link4_orange)

        link1.setOnClickListener {
            context.supportFragmentManager
                .beginTransaction()
                .replace(R.id.fragment_container, MtnFragment(context))
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
        link3.setOnClickListener {
            context.supportFragmentManager
                .beginTransaction()
                .replace(R.id.fragment_container, WaveFragment(context))
                .addToBackStack(null)
                .commit()
        }
        link4.setOnClickListener {
            context.supportFragmentManager
                .beginTransaction()
                .replace(R.id.fragment_container, TresorFragment(context))
                .addToBackStack(null)
                .commit()
        }

        sectionUpload.visibility = View.INVISIBLE
        progressBar.visibility = View.INVISIBLE
        checkBox.setOnClickListener {
            if (checkBox.isChecked)
            {
                sectionUpload.visibility = View.VISIBLE
                var params: ConstraintLayout.LayoutParams = buttonRegister.layoutParams as ConstraintLayout.LayoutParams
                params.setMargins(30, 300, 30, 0)
                buttonRegister.layoutParams = params
            }else{
                sectionUpload.visibility = View.INVISIBLE
                var params: ConstraintLayout.LayoutParams = buttonRegister.layoutParams as ConstraintLayout.LayoutParams
                params.setMargins(30, 0, 30, 0)
                buttonRegister.layoutParams = params
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

        buttonRegister.setOnClickListener {
            if (checkForInternet(context)) {
                if(textTelephone.text.isEmpty() || textMontant.text.isEmpty())
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
                                            "telephone" to telInput.toString().trim(),
                                            "montant" to montantInput.toString().trim(),
                                            "typeoperation" to typeSpinner,
                                            "url" to it.toString()
                                        )

                                        db.collection("operation").add(operationMap).addOnCompleteListener {
                                            telInput.clear()
                                            montantInput.clear()
                                            progressBar.visibility = View.INVISIBLE
                                            buttonRegister.setText(R.string.register_operation)
                                            buttonRegister.isEnabled = true
                                            Toast.makeText(context, "Enregistré avec succès.", Toast.LENGTH_SHORT).show()
                                        }.addOnFailureListener {
                                            val builder = AlertDialog.Builder(context)
                                            builder.setTitle("Alerte")
                                            builder.setMessage(R.string.onFailureText)
                                            builder.show()
                                        }
                                    }.addOnFailureListener {
                                        Toast.makeText(context, R.string.onFailureText, Toast.LENGTH_SHORT).show()
                                    }
                            }.addOnFailureListener{
                                val builer = AlertDialog.Builder(context)
                                builer.setMessage("Erreur pendant le téléchargement de l'image. Veuillez réessayer SVP.")
                                builer.show()
                            }
                    }else{

                        // Dans le cas où l'utilisateur n'a pas enregistré d'image on met la valeur à NULL
                        val operationMap = hashMapOf(
                            "id" to auth.currentUser?.uid,
                            "date" to dateFormatted,
                            "heure" to hourFormatted,
                            "operateur" to "orange",
                            "telephone" to telInput.toString().trim(),
                            "montant" to montantInput.toString().trim(),
                            "typeoperation" to typeSpinner,
                            "url" to "null"
                        )

                        db.collection("operation").add(operationMap).addOnCompleteListener {
                            telInput.clear()
                            montantInput.clear()
                            progressBar.visibility = View.INVISIBLE
                            buttonRegister.setText(R.string.register_operation)
                            buttonRegister.isEnabled = true
                            Toast.makeText(context, "Enregistré avec succès.", Toast.LENGTH_SHORT).show()
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