package com.money.protect

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.ActivityInfo
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth

class UpdatePasswordActivity : AppCompatActivity() {
    lateinit var auth: FirebaseAuth
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        setContentView(R.layout.activity_update_password)

        val buttonUpdate = findViewById<Button>(R.id.buttonUpdated)

        auth = FirebaseAuth.getInstance()
        buttonUpdate.isEnabled = true

        // Button Retour en arrière
        val buttonBack = findViewById<ImageView>(R.id.backButtonFromUpdatePassword)
        buttonBack.setOnClickListener{
            finish()
        }

        // Button de mise à jour
        buttonUpdate.setOnClickListener {
            val password = findViewById<EditText>(R.id.newPasswordUpdated).text
            val user = auth.currentUser

            if (password.length >= 6)
            {
                buttonUpdate.isEnabled = false
                // On met à jour le mot de passe
                user!!.updatePassword(password.toString()).addOnCompleteListener {task->
                    if (task.isSuccessful) {
                        val credential = EmailAuthProvider.getCredential(user.email!!, password.toString())

                        // On récrée l'authentification de l'utilisateur
                        user.reauthenticate(credential)
                            .addOnCompleteListener { task->
                                if (task.isSuccessful)
                                {
                                    buttonUpdate.isEnabled = true
                                    auth.signOut()
                                    password.clear()
                                    val intent = Intent(this, LoginActivity::class.java)
                                    startActivity(intent)
                                }else{
                                    val builder = AlertDialog.Builder(this)
                                    builder.setMessage("Une erreur s'est produite")
                                    builder.show()
                                }
                            }
                    }else{
                        val builder = AlertDialog.Builder(this)
                        builder.setMessage("Le mot de passe n'a pas pu être modifié. Rééssayez plus tard")
                        builder.show()
                    }
                }
            }else{
                val builder = AlertDialog.Builder(this)
                builder.setMessage("Le mot de passe doit contenir au moins 6 caractères")
                builder.show()
            }
        }
    }
}