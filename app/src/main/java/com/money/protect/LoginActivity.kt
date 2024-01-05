package com.money.protect

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatButton
import com.money.protect.fragment_assistant.checkInternet.checkForInternet
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class LoginActivity : AppCompatActivity() {
    lateinit var auth: FirebaseAuth
    private var db = Firebase.firestore
    private var database = Firebase.firestore
    private lateinit var progressBar: ProgressBar
    private lateinit var button: Button
    @RequiresApi(Build.VERSION_CODES.M)
    @SuppressLint("SuspiciousIndentation", "MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        setContentView(R.layout.activity_login)


        // Récupère la dernière page visitée
        val sharedPreferences = getSharedPreferences("my_app_preferences", Context.MODE_PRIVATE)
        val lastPage = sharedPreferences.getString("last_page", null)

        // Ouvre la dernière page visitée
        if (lastPage != null) {
            val intent = Intent(this, Class.forName(lastPage))
            startActivity(intent)
        }

        auth = FirebaseAuth.getInstance()

        db = FirebaseFirestore.getInstance()

        val query = db.collection("account")
        button = findViewById<AppCompatButton>(R.id.button_login_user)

        button.setOnClickListener {
            if (checkForInternet(this)) {
                button.isEnabled = false
                val email = findViewById<EditText>(R.id.emailCnx).text.toString()
                val password = findViewById<EditText>(R.id.passwordCnx).text.toString()

                if (email.isNotEmpty() && password.isNotEmpty()){
                    button.setText(R.string.button_loading)
                    // CONVERSION DU N° TELEPHONE EN MAIL
                    val emailTel = "ci-" + email + "@mail.com"
                    if (!checkForInternet(this)) {
                        Toast.makeText(this, "Aucune connexion internet", Toast.LENGTH_SHORT).show()
                    }
                    auth.signInWithEmailAndPassword(emailTel, password).addOnSuccessListener {
                        query.whereEqualTo("id", auth.currentUser!!.uid)
                            .get().addOnSuccessListener { document->
                                for (data in document)
                                {
                                    val role = data.data["role"].toString()
                                    val statut = data.data["statut"].toString().toBoolean()

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
                                            // Historique de connexion

                                            // Générer la date
                                            val current = LocalDateTime.now()
                                            val formatterDate = DateTimeFormatter.ofPattern("d-M-yyyy")
                                            val dateFormatted = current.format(formatterDate)

                                            // Générer l'heure
                                            val formatterHour = DateTimeFormatter.ofPattern("HH:mm")
                                            val hourFormatted = current.format(formatterHour)

                                            val connexionMap = hashMapOf(
                                                "id" to auth.currentUser!!.uid,
                                                "statut" to "connexion",
                                                "date" to dateFormatted,
                                                "heure" to hourFormatted
                                            )

                                            database.collection("connexion")
                                                .add(connexionMap)
                                                .addOnCompleteListener {
                                                    // Ne rien faire
                                                }

                                            if (!checkForInternet(this)) {
                                                Toast.makeText(this, "Aucune connexion internet", Toast.LENGTH_SHORT).show()
                                            }
                                            
                                            val intent = Intent(this, DoubleAccountActivity::class.java)
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

        val onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                auth.signOut()
                // Redirection vers une autre Activity
                val intent = Intent(this@LoginActivity, LoginActivity::class.java)
                startActivity(intent)
                finish() // Facultatif : fermez l'activité actuelle si vous ne voulez pas y retourner
            }
        }

        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onStart() {
        super.onStart()
        progressBar = findViewById(R.id.progressBarLogin)

        progressBar.visibility = View.VISIBLE
        if (auth.currentUser != null)
        {
            if (!checkForInternet(this)) {
                Toast.makeText(this, "Aucune connexion internet", Toast.LENGTH_SHORT).show()
            }
            db.collection("account").whereEqualTo("id", auth.currentUser!!.uid)
                .get()
                .addOnSuccessListener { document->
                    for (donnee in document)
                    {
                        val role = donnee.data["role"].toString()
                        if (role == "assistant")
                        {
                            if (!checkForInternet(this)) {
                                Toast.makeText(this, "Aucune connexion internet", Toast.LENGTH_SHORT).show()
                            }
                            db.collection("annexe")
                                .get()
                                .addOnSuccessListener { documents->
                                    if (!documents.isEmpty) {
                                        for (donne in documents)
                                        {
                                            progressBar.visibility = View.INVISIBLE
                                            val mainprofil = donne.data["mainprofil"].toString().toBoolean()

                                            if (mainprofil)
                                            {
                                                // Vérifier si le double-compte est activé et rediriger
                                                val intent = Intent(this, DoubleAccountActivity::class.java)
                                                startActivity(intent)
                                                button.isEnabled = true
                                                button.setText(R.string.login_button_default_text)
                                            }else{
                                                // Vérifier si le double-compte est activé et rediriger
                                                val intent = Intent(this, MainActivity::class.java)
                                                startActivity(intent)
                                                button.isEnabled = true
                                                button.setText(R.string.login_button_default_text)
                                            }
                                        }
                                    }else{
                                        // Vérifier si le double-compte est activé et rediriger
                                        val intent = Intent(this, MainActivity::class.java)
                                        startActivity(intent)
                                        button.isEnabled = true
                                        button.setText(R.string.login_button_default_text)
                                    }
                                }.addOnFailureListener {
                                    Toast.makeText(applicationContext, R.string.onFailureText, Toast.LENGTH_SHORT).show()
                                }
                        }else if (role == "superviseur"){
                            val intent2 = Intent(this, SuperviseurActivity::class.java)
                            startActivity(intent2)
                        }
                    }
                }
        }else{
            progressBar.visibility = View.INVISIBLE
        }
    }
}