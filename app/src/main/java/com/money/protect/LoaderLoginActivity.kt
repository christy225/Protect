package com.money.protect

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.ActivityInfo
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.money.protect.repository.AssistantRepository

class LoaderLoginActivity : AppCompatActivity() {
    lateinit var auth : FirebaseAuth
    lateinit var loader: ProgressBar
    private var db = Firebase.firestore
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        setContentView(R.layout.activity_loader_login)
        auth = FirebaseAuth.getInstance()
        loader = findViewById(R.id.progressBar_loader_login)

        db = FirebaseFirestore.getInstance()

    }

    override fun onStart() {
        super.onStart()
        loader.visibility = View.VISIBLE
        val query = db.collection("account")

        if (auth.currentUser != null)
        {
            query.whereEqualTo("id", auth.currentUser!!.uid)
                .get().addOnSuccessListener { document->
                    for (data in document)
                    {
                        val role = data.data!!["role"].toString()
                        if (role == "superviseur") {
                            loader.visibility = View.INVISIBLE
                            val intent = Intent(this, SuperviseurActivity::class.java)
                            startActivity(intent)
                        } else if (role == "assistant") {
                            loader.visibility = View.INVISIBLE
                            val intent = Intent(this, MainActivity::class.java)
                            startActivity(intent)
                        }
                    }
                }.addOnFailureListener {
                    val builder = AlertDialog.Builder(this)
                    builder.setTitle("Erreur")
                    builder.setMessage("$it")
                    builder.show()
                }
        }else{
            val intent2 = Intent(this, LoginActivity::class.java)
            startActivity(intent2)
        }
    }
}