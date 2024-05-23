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
import androidx.activity.OnBackPressedCallback
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatButton
import com.money.protect.fragment_assistant.checkInternet.checkForInternet
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class LoginActivity : AppCompatActivity() {
    lateinit var auth: FirebaseAuth
    private var db = Firebase.firestore
    private lateinit var email: EditText
    private lateinit var password: EditText
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

        email = findViewById(R.id.emailCnx)
        password = findViewById(R.id.passwordCnx)
        button = findViewById<AppCompatButton>(R.id.button_login_user)
        progressBar = findViewById(R.id.progressBarLogin)

        button.setOnClickListener {
            val mail = email.text.toString()
            val pass = password.text.toString()
            if (checkForInternet(this)) {
                if (mail.isNotEmpty() && pass.isNotEmpty()){
                    button.setText(R.string.button_loading)
                    button.isEnabled = false
                    // CONVERSION DU N° TELEPHONE EN MAIL
                    val emailTel = "ci-" + mail + "@mail.com"

                    auth.signInWithEmailAndPassword(emailTel, pass).addOnSuccessListener {
                        query.whereEqualTo("id", auth.currentUser!!.uid)
                            .get().addOnSuccessListener { document->
                                for (data in document)
                                {
                                    val role = data.data["role"].toString()
                                    val nomcommercial = data.data["nomcommercial"].toString()
                                    val nomcomplet = data.data["nomcomplet"].toString()
                                    val quartier = data.data["quartier"].toString()
                                    val creation = data.data["creation"].toString()
                                    val module = data.data["module"].toString()
                                    val superviseur = data.data["superviseur"].toString()
                                    val phone = data.data["telephone"].toString()
                                    val ville = data.data["ville"].toString()
                                    val abonnement = data.data["abonnement"].toString()
                                    val duration = data.data["duration"].toString()
                                    val capital = data.data["capital"].toString()

                                    // On formate la date de création réçue de la BDD
                                    val formatter = DateTimeFormatter.ofPattern("d-M-yyyy")
                                    val date0 = LocalDate.parse(creation, formatter)

                                    // On récupère la date du jour
                                    val current = LocalDateTime.now()
                                    val dateFormatted = current.format(formatter)
                                    val date1 = LocalDate.parse(dateFormatted, formatter)

                                    // On calcule la différence entre les 2 dates
                                    val jourEcoules = ChronoUnit.DAYS.between(date0, date1)
                                    val duree = duration.toLong() - jourEcoules

                                    if (duree >= 0)
                                    {
                                        if (role == "superviseur") {
                                            val intent = Intent(this, SuperviseurActivity::class.java)
                                            startActivity(intent)
                                            button.isEnabled = true
                                            button.setText(R.string.login_button_default_text)
                                        } else if (role == "assistant") {
                                            val intent = Intent(this, DoubleAccountActivity::class.java)
                                            intent.putExtra("module", module)
                                            intent.putExtra("nomcommercial", nomcommercial)
                                            intent.putExtra("creation", creation)
                                            intent.putExtra("duration", duration)

                                            intent.putExtra("email", emailTel)
                                            intent.putExtra("nomcomplet", nomcomplet)
                                            intent.putExtra("quartier", quartier)
                                            intent.putExtra("superviseur", superviseur)
                                            intent.putExtra("telephone", phone)
                                            intent.putExtra("ville", ville)
                                            intent.putExtra("abonnement", abonnement)
                                            intent.putExtra("capital", capital)
                                            startActivity(intent)
                                            button.isEnabled = true
                                            button.setText(R.string.login_button_default_text)
                                        }
                                    }else{
                                        val accountMap = hashMapOf(
                                            "creation" to creation,
                                            "email" to emailTel,
                                            "id" to auth.currentUser!!.uid,
                                            "nomcommercial" to nomcommercial,
                                            "nomcomplet" to nomcomplet,
                                            "quartier" to quartier,
                                            "role" to role,
                                            "statut" to false,
                                            "module" to module,
                                            "superviseur" to superviseur,
                                            "telephone" to phone,
                                            "duration" to "60",
                                            "ville" to ville,
                                            "abonnement" to abonnement,
                                            "capital" to capital
                                        )
                                        db.collection("account")
                                            .document(auth.currentUser!!.uid)
                                            .set(accountMap)
                                            .addOnSuccessListener {
                                                auth.signOut()
                                                val intent = Intent(this, AbonnementActivity::class.java)
                                                startActivity(intent)
                                            }.addOnFailureListener{
                                                val builder = AlertDialog.Builder(this)
                                                builder.setMessage(R.string.onFailureText)
                                                builder.show()
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
                val builder = AlertDialog.Builder(this)
                builder.setMessage("Aucune connexion internet")
                builder.show()
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

        progressBar.visibility = View.VISIBLE

        if (!checkForInternet(this@LoginActivity)) {
            val builder = AlertDialog.Builder(this@LoginActivity)
            builder.setMessage("Aucune connexion internet")
            builder.show()
            progressBar.visibility = View.INVISIBLE
            button.isEnabled = true
            button.setText(R.string.login_button_default_text)
        }else{
            if (auth.currentUser != null)
            {
                db.collection("account")
                    .whereEqualTo("id", auth.currentUser!!.uid)
                    .get()
                    .addOnSuccessListener { document->
                        for (data in document)
                        {
                            val role = data.data["role"].toString()
                            val nomcommercial = data.data["nomcommercial"].toString()
                            val nomcomplet = data.data["nomcomplet"].toString()
                            val quartier = data.data["quartier"].toString()
                            val creation = data.data["creation"].toString()
                            val module = data.data["module"].toString()
                            val superviseur = data.data["superviseur"].toString()
                            val phone = data.data["telephone"].toString()
                            val ville = data.data["ville"].toString()
                            val abonnement = data.data["abonnement"].toString()
                            val duration = data.data["duration"].toString()
                            val capital = data.data["capital"].toString()

                            // On formate la date de création réçue de la BDD
                            val formatter = DateTimeFormatter.ofPattern("d-M-yyyy")
                            val date0 = LocalDate.parse(creation, formatter)

                            // On récupère la date du jour
                            val current = LocalDateTime.now()
                            val dateFormatted = current.format(formatter)
                            val date1 = LocalDate.parse(dateFormatted, formatter)

                            // On calcule la différence entre les 2 dates
                            val jourEcoules = ChronoUnit.DAYS.between(date0, date1)
                            val duree = duration.toLong() - jourEcoules

                            if (duree >= 0)
                            {
                                if (role == "superviseur") {
                                    val intent = Intent(this, SuperviseurActivity::class.java)
                                    startActivity(intent)
                                    button.isEnabled = true
                                    button.setText(R.string.login_button_default_text)
                                } else if (role == "assistant") {
                                    val intent = Intent(this, DoubleAccountActivity::class.java)
                                    intent.putExtra("module", module)
                                    intent.putExtra("nomcommercial", nomcommercial)
                                    intent.putExtra("creation", creation)
                                    intent.putExtra("duration", duration)
                                    startActivity(intent)
                                    button.isEnabled = true
                                    button.setText(R.string.login_button_default_text)
                                }
                            }else{
                                val accountMap = hashMapOf(
                                    "creation" to creation,
                                    "email" to email.text.toString(),
                                    "id" to auth.currentUser!!.uid,
                                    "nomcommercial" to nomcommercial,
                                    "nomcomplet" to nomcomplet,
                                    "quartier" to quartier,
                                    "role" to role,
                                    "statut" to false,
                                    "module" to module,
                                    "superviseur" to superviseur,
                                    "telephone" to phone,
                                    "duration" to "60",
                                    "ville" to ville,
                                    "abonnement" to abonnement,
                                    "capital" to capital
                                )
                                db.collection("account")
                                    .document(auth.currentUser!!.uid)
                                    .set(accountMap)
                                    .addOnSuccessListener {
                                        auth.signOut()
                                        val intent = Intent(this, AbonnementActivity::class.java)
                                        startActivity(intent)
                                    }.addOnFailureListener{
                                        val builder = AlertDialog.Builder(this)
                                        builder.setMessage(R.string.onFailureText)
                                        builder.show()
                                    }
                            }
                        }
                    }
                    .addOnFailureListener {
                        val builder = AlertDialog.Builder(this@LoginActivity)
                        builder.setTitle("Erreur")
                        builder.setMessage(R.string.onFailureText)
                        builder.show()
                    }
            }else{
                progressBar.visibility = View.INVISIBLE
            }
        }

    }
}