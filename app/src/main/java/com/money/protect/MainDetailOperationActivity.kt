package com.money.protect

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.money.protect.popup.AfficheIdentite1
import com.money.protect.popup.AfficheIdentite2
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale


class MainDetailOperationActivity : AppCompatActivity() {
    lateinit var auth: FirebaseAuth
    private val db = Firebase.firestore
    private var database = Firebase.firestore
    private lateinit var intents: Intent
    private lateinit var origineX: TextView
    private lateinit var buttonAffiche1: Button
    private lateinit var buttonAffiche2: Button
    private lateinit var buttonCancel: Button
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        setContentView(R.layout.activity_main_detail_operation)
        buttonAffiche1 = findViewById(R.id.afficher_image_details1)
        buttonAffiche2 = findViewById(R.id.afficher_image_details2)
        auth = FirebaseAuth.getInstance()
        database = FirebaseFirestore.getInstance()

        window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)

        val buttonBack = findViewById<ImageView>(R.id.backButtonDetailtransactionVw)

        buttonBack.setOnClickListener {
            finish()
        }

        intents = intent
        val operateurX = findViewById<TextView>(R.id.operateurDetailTransactionVw)
        val telX = findViewById<TextView>(R.id.phoneDetailTransactionVw)
        val typeX = findViewById<TextView>(R.id.typeOpDetailTransactionVw)
        val montantX = findViewById<TextView>(R.id.montantDetailTransactionVw)
        val dateX = findViewById<TextView>(R.id.dateDetailTransactionVw)
        val heureX = findViewById<TextView>(R.id.heureDetailTransactionVw)
        val info = findViewById<TextView>(R.id.text_info_DetailTransactionVw)
        origineX = findViewById(R.id.originFund)

        val infoCancel = findViewById<TextView>(R.id.ta)

        buttonCancel = findViewById(R.id.AnnulerDetailTransaction)

        info.visibility = View.INVISIBLE

        val identifiant = intents.getStringExtra("id")
        val idDoc = intents.getStringExtra("idDoc")
        val operateur = intents.getStringExtra("operateur")
        val tel = intents.getStringExtra("telephone")
        val type = intents.getStringExtra("typeoperation")
        val montant = intents.getStringExtra("montant")
        val date = intents.getStringExtra("date")
        val heure = intents.getStringExtra("heure")
        val url1 = intents.getStringExtra("url1")
        val url2 = intents.getStringExtra("url2")
        val statut = intents.getStringExtra("statut")
        val origine = intents.getStringExtra("origine")

        // Utilisation de la locale par défaut pour obtenir le séparateur de milliers correct
        val format = DecimalFormat("#,###", DecimalFormatSymbols.getInstance(Locale.getDefault()))

        operateurX.text = operateur
        telX.text = tel
        typeX.text = type
        montantX.text = format.format(montant?.toInt()).toString()
        dateX.text = date
        heureX.text = heure
        origineX.text = origine

        // Empêcher le superviseur d'annuler une transaction
        database.collection("account")
            .whereEqualTo("id", auth.currentUser?.uid)
            .get()
            .addOnSuccessListener { documents->
                for (user in documents){
                    val role = user.data["role"].toString()
                    if (role == "superviseur")
                    {
                        buttonCancel.visibility = View.GONE
                    }else{
                        if (!statut.toBoolean())
                        {
                            buttonCancel.visibility = View.GONE
                            infoCancel.visibility = View.VISIBLE
                        }
                    }
                }
            }

        if (url1 == "null")
        {
            info.visibility = View.VISIBLE
        }else{
            buttonAffiche1.visibility = View.VISIBLE
            buttonAffiche1.setOnClickListener {
                AfficheIdentite1(this).show()
            }
            buttonAffiche2.visibility = View.VISIBLE
            buttonAffiche2.setOnClickListener {
                AfficheIdentite2(this).show()
            }
        }

        buttonCancel.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Annuler transaction")
                .setMessage("Annuler cette opération car la transaction n'a pas abouti. Cette action est irréversible")
                .setPositiveButton("Oui"){ dialog, id->
                    val operationMap = hashMapOf(
                        "id" to identifiant,
                        "date" to date,
                        "heure" to heure,
                        "operateur" to operateur,
                        "telephone" to tel,
                        "montant" to montant,
                        "typeoperation" to type,
                        "statut" to false,
                        "url1" to url1,
                        "url2" to url2,
                        "idDoc" to idDoc
                    )
                    db.collection("operation")
                        .document(idDoc!!)
                        .set(operationMap)
                        .addOnCompleteListener { it->
                            // Ne rien faire ici
                            if (it.isSuccessful)
                            {
                                Toast.makeText(this, "Transaction annulée", Toast.LENGTH_SHORT).show()
                                finish()
                            }
                        }.addOnFailureListener {
                            Toast.makeText(this, R.string.onFailureText, Toast.LENGTH_SHORT).show()
                        }
                }
                .setNegativeButton("Non"){ dialod, id->
                    // Ne rien faire
                }

            builder.create().show()
        }

    }

    fun lienUrl1(): String? {
        return intent!!.getStringExtra("url1")
    }
    fun lienUrl2(): String? {
        return intent!!.getStringExtra("url2")
    }

    override fun onStart() {
        super.onStart()
        if (auth.currentUser == null)
        {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }
}