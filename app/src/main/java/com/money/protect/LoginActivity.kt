package com.money.protect

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatButton
import com.money.protect.fragment_assistant.checkInternet.checkForInternet
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class LoginActivity : AppCompatActivity() {
    lateinit var auth: FirebaseAuth
    private var db = Firebase.firestore
    @RequiresApi(Build.VERSION_CODES.M)
    @SuppressLint("SuspiciousIndentation")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val query = db.collection("account")
        val button = findViewById<AppCompatButton>(R.id.button_login_user)

        button.setOnClickListener {
            if (checkForInternet(this)) {
                button.isEnabled = false
                val email = findViewById<EditText>(R.id.emailCnx).text.toString()
                val password = findViewById<EditText>(R.id.passwordCnx).text.toString()

                if (email.isNotEmpty() && password.isNotEmpty()){
                    button.setText(R.string.button_loading)
                    // CONVERSION DU N° TELEPHONE EN MAIL
                    val emailTel = "ci-" + email + "@mail.com"
                    auth.signInWithEmailAndPassword(emailTel, password).addOnSuccessListener {
                        query.whereEqualTo("id", auth.currentUser!!.uid)
                            .get().addOnSuccessListener { document->
                                for (data in document)
                                {
                                    val role = data.data!!["role"].toString()
                                    val statut = data.data!!["statut"].toString().toBoolean()
                                    if (role == "superviseur") {
                                        if (statut)
                                        {
                                            val intent = Intent(this, SuperviseurActivity::class.java)
                                            startActivity(intent)
                                            button.isEnabled = true
                                            button.setText(R.string.login_button_default_text)
                                        }else{
                                            val builder = AlertDialog.Builder(this)
                                            builder.setMessage("Votre abonnement a expiré.")
                                            builder.show()
                                            button.isEnabled = true
                                            button.setText(R.string.login_button_default_text)
                                        }
                                    } else if (role == "assistant") {
                                        if (statut)
                                        {
                                            val intent = Intent(this, MainActivity::class.java)
                                            startActivity(intent)
                                            button.isEnabled = true
                                            button.setText(R.string.login_button_default_text)
                                        }else{
                                            val builder = AlertDialog.Builder(this)
                                            builder.setMessage("Votre compte a été désactivé par votre superviseur.")
                                            builder.show()
                                            button.isEnabled = true
                                            button.setText(R.string.login_button_default_text)
                                        }
                                    }
                                }
                            }.addOnFailureListener {
                                val builder = AlertDialog.Builder(this)
                                builder.setMessage("Une erreur s'est produite")
                                builder.show()
                                button.isEnabled = true
                                button.setText(R.string.login_button_default_text)
                            }
                    }.addOnFailureListener {
                        val builder = AlertDialog.Builder(this)
                        builder.setMessage("Identifiants incorrects")
                        builder.show()
                        button.isEnabled = true
                        button.setText(R.string.login_button_default_text)
                    }
                }else{
                    val builder = AlertDialog.Builder(this)
                    builder.setMessage("Veuillez saisir tous les champs SVP")
                    builder.show()
                    button.isEnabled = true
                }
            }else{
                Toast.makeText(this, "Aucune connexion internet", Toast.LENGTH_SHORT).show()
            }
        }

    }

}