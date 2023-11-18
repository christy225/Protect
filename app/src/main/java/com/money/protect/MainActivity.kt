package com.money.protect

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.ktx.isFlexibleUpdateAllowed
import com.google.android.play.core.ktx.isImmediateUpdateAllowed
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.money.protect.fragment_assistant.HomeFragment
import com.money.protect.fragment_assistant.SettingsFragment
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.time.Duration.Companion.seconds

class MainActivity : AppCompatActivity() {
    companion object {
        const val REQUEST_CALL_PERMISSION = 456
    }

    private lateinit var appUpdateManager: AppUpdateManager
    private val updateType = AppUpdateType.FLEXIBLE

    lateinit var auth: FirebaseAuth
    private var db = Firebase.firestore
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        appUpdateManager = AppUpdateManagerFactory.create(applicationContext)
        if (updateType == AppUpdateType.FLEXIBLE)
        {
            appUpdateManager.registerListener(installStateUpdatedListener)
        }
        checkForAppUpdate()
        setContentView(R.layout.activity_main)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigation)

        // Vérifier si la permission est déjà accordée
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
            // La permission est déjà accordée, vous pouvez effectuer des appels ici
        } else {
            // La permission n'est pas accordée, demandez-la à l'utilisateur
            requestCallPermission()
        }


        // RECHERCHER LE COMPTE ASSISTANT POUR RECUPERER L'ID SUPERVISEUR

        db.collection("account")
            .whereEqualTo("id", auth.currentUser!!.uid)
            .get()
            .addOnSuccessListener { document->
                for (data in document)
                {
                    val superviseurId = data!!.data["superviseur"].toString()

                    // VERIFIER L'ETAT D'ABONNEMENT DE SON SUPERVISEUR

                    db.collection("abonnement")
                        .whereEqualTo("id", superviseurId)
                        .get()
                        .addOnSuccessListener {document->
                            for (data in document)
                            {
                                val dureeAutorisee = data!!.data["duration"].toString()
                                val creation = data!!.data["creation"].toString()

                                // On formate la date de création réçue de la BDD
                                val formatter = DateTimeFormatter.ofPattern("d-M-yyyy")
                                val date0 = LocalDate.parse(creation, formatter)

                                // On récupère la date du jour
                                val current = LocalDateTime.now()
                                val dateFormatted = current.format(formatter)
                                val date1 = LocalDate.parse(dateFormatted, formatter)

                                val jourEcoules = ChronoUnit.DAYS.between(date0, date1)

                                // SI ON A ATTEINT LA DUREE AUTORISEE LE COMPTE EST VEROUILLE

                                if (dureeAutorisee.toLong() >= jourEcoules)
                                {
                                    loadFragment(HomeFragment(this))
                                    bottomNavigationView.setOnItemSelectedListener {
                                        when(it.itemId)
                                        {
                                            R.id.home -> loadFragment(HomeFragment(this))
                                            R.id.settings -> loadFragment(SettingsFragment(this))
                                        }
                                        false
                                    }
                                } else if (dureeAutorisee.toLong() < jourEcoules){

                                    // APPLIQUER EXPIRATION DE L'ABONNEMENT

                                    val abonnementMap = hashMapOf(
                                        "creation" to creation,
                                        "duration" to "30",
                                        "id" to superviseurId,
                                        "statut" to false
                                    )
                                    db.collection("abonnement")
                                        .document(superviseurId!!)
                                        .set(abonnementMap)
                                        .addOnSuccessListener {
                                            auth.signOut()
                                            val intent = Intent(this, com.money.protect.AbonnementActivity::class.java)
                                            startActivity(intent)
                                        }.addOnFailureListener {
                                            val builder = AlertDialog.Builder(this)
                                            builder.setTitle("Erreur")
                                            builder.setMessage("Une erreur s'est produite.")
                                            builder.show()
                                        }

                                    // FIN APPLIQUER EXPIRATION DE L'ABONNEMENT
                                }
                            }
                        }.addOnFailureListener {
                            Toast.makeText(this, R.string.onFailureText, Toast.LENGTH_SHORT).show()
                        }

                    // VERIFIER L'ETAT D'ABONNEMENT DU SUPERVISEUR
                }
            }.addOnFailureListener {
                Toast.makeText(this, R.string.onFailureText, Toast.LENGTH_SHORT).show()
            }

    }

    override fun onStart() {
        super.onStart()
        if (auth.currentUser == null)
        {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }

    private fun requestCallPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.CALL_PHONE), REQUEST_CALL_PERMISSION)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_CALL_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // La permission d'appel a été accordée, vous pouvez maintenant effectuer des appels
                } else {
                    // La permission d'appel a été refusée, informez l'utilisateur ou prenez des mesures alternatives
                }
            }
            // Gérer d'autres demandes de permissions si nécessaire
        }
    }

    private val installStateUpdatedListener = InstallStateUpdatedListener { state->
        if (state.installStatus() == InstallStatus.DOWNLOADED)
        {
            Toast.makeText(applicationContext,
                "Mise à jour réussie. L'application va redemarrer dans 5 secondes",
                Toast.LENGTH_SHORT)
                .show()
            lifecycleScope.launch {
                delay(5.seconds)
                appUpdateManager.completeUpdate()
            }
        }
    }

    private fun checkForAppUpdate(){
        appUpdateManager.appUpdateInfo.addOnSuccessListener { info->
            val isUpdateAvaible = info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
            val isUpdateAllowed = when(updateType){
                AppUpdateType.FLEXIBLE -> info.isFlexibleUpdateAllowed
                AppUpdateType.IMMEDIATE -> info.isImmediateUpdateAllowed
                else -> false
            }
            if (isUpdateAvaible && isUpdateAllowed)
            {
                appUpdateManager.startUpdateFlowForResult(
                    info,
                    updateType,
                    this,
                    123
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (updateType == AppUpdateType.IMMEDIATE)
        {
            appUpdateManager.appUpdateInfo.addOnSuccessListener { info->
                if (info.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS)
                {
                    appUpdateManager.startUpdateFlowForResult(
                        info,
                        updateType,
                        this,
                        123
                    )
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 123)
        {
            if (resultCode != RESULT_OK)
            {
                Toast.makeText(this, "Une erreur s'est produite pendant la mise à jour", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (updateType == AppUpdateType.FLEXIBLE)
        {
            appUpdateManager.unregisterListener(installStateUpdatedListener)
        }
    }

    fun blockScreenShot()
    {
        window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }

    fun bottomNavBlocked(mont: String, num: String) {
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        if (mont.isNotEmpty() || num.isNotEmpty())
        {
            bottomNavigationView.setOnItemSelectedListener {
                Toast.makeText(this, "Vous n'avez pas enregisté la transaction.", Toast.LENGTH_SHORT).show()
                false }
        }
    }

    fun bottomNavUnlock()
    {
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNavigationView.setOnItemSelectedListener {
            when(it.itemId)
            {
                R.id.home -> loadFragment(HomeFragment(this))
                R.id.settings -> loadFragment(SettingsFragment(this))
            }
            false
        }
    }
}