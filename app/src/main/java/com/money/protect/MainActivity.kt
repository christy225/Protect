package com.money.protect

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import android.widget.Button
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
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
import com.money.protect.fragment_assistant.HomeFragment
import com.money.protect.fragment_assistant.SettingsFragment
import com.money.protect.fragment_assistant.national.MoovFragment
import com.money.protect.fragment_assistant.national.TresorFragment
import com.money.protect.popup.MenuPopupCompte1Assistant
import com.money.protect.popup.MenuPopupCompte2Assistant
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

class MainActivity : AppCompatActivity() {

    private lateinit var appUpdateManager: AppUpdateManager
    private val updateType = AppUpdateType.FLEXIBLE

    private lateinit var auth: FirebaseAuth
    private var compte: String? = null
    private var nomcommercial: String? = null
    private var creation: String? = null
    private var module: String? = null
    private var duration: String? = null
    private var email: String? = null
    private var nomcomplet: String? = null
    private var quartier: String? = null
    private var superviseur: String? = null
    private var telephone: String? = null
    private var ville: String? = null
    private var abonnement: String? = null
    private var capital: String? = null

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED) {

        } else {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.READ_SMS),
                DoubleAccountActivity.READ_SMS_PERMISSION_REQUEST
            )
        }

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {

        } else {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.CALL_PHONE),
                DoubleAccountActivity.REQUEST_CALL_PERMISSION
            )
        }

        // Récupère la dernière page visitée
        val sharedPreferences = getSharedPreferences("my_app_preferences", Context.MODE_PRIVATE)
        val lastPage = sharedPreferences.getString("last_page", null)

        // Ouvre la dernière page visitée
        if (lastPage != null) {
            val intent = Intent(this, Class.forName(lastPage))
            startActivity(intent)
        }

        blockScreenShot()
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        appUpdateManager = AppUpdateManagerFactory.create(applicationContext)
        if (updateType == AppUpdateType.FLEXIBLE)
        {
            appUpdateManager.registerListener(installStateUpdatedListener)
        }
        checkForAppUpdate()
        setContentView(R.layout.activity_main)

        auth = FirebaseAuth.getInstance()

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigation)

        val intent: Intent = intent
        compte = intent.getStringExtra("compte")
        nomcommercial = intent.getStringExtra("nomcommercial")
        creation = intent.getStringExtra("creation")
        module = intent.getStringExtra("module")
        duration = intent.getStringExtra("duration")

        email = intent.getStringExtra("email")
        nomcomplet = intent.getStringExtra("nomcomplet")
        quartier = intent.getStringExtra("quartier")
        superviseur = intent.getStringExtra("superviseur")
        telephone = intent.getStringExtra("telephone")
        ville = intent.getStringExtra("ville")
        abonnement = intent.getStringExtra("abonnement")
        capital = intent.getStringExtra("capital")



        // Dans ActivityB.onCreate
        val fragmentId = intent.getStringExtra("fragment_to_show")

        if (fragmentId == "tresor") {
            loadFragment(TresorFragment(this))
        } else if (fragmentId == "moov") {
            loadFragment(MoovFragment(this))
        }else{
            loadFragment(HomeFragment(this))
        }
        bottomNavigationView.setOnItemSelectedListener {
            when(it.itemId)
            {
                R.id.home -> loadFragment(HomeFragment(this))
                R.id.settings -> loadFragment(SettingsFragment(this))
            }
            false
        }

    }

    fun Nomcom(): String? {
        return nomcommercial
    }
    fun Creation(): String? {
        return creation
    }
    fun Module(): String? {
        return module
    }
    fun Duration(): String? {
        return duration
    }


    fun blockBackNavigation(query: Button, fragment: Fragment){
        val onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (query.text === "enregistrer opération") {
                    val builder = AlertDialog.Builder(this@MainActivity)
                    builder.setTitle("Avertissement")
                        .setMessage("Vous n'avez pas enregistré l'opération.\nCliquer sur Annuler si vous souhaitez interrompre la transaction")
                        .show()
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .addToBackStack(null)
                        .commit()
                }else{
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, HomeFragment(this@MainActivity))
                        .addToBackStack(null)
                        .commit()
                }
            }
        }
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }

    private val installStateUpdatedListener = InstallStateUpdatedListener { state->
        if (state.installStatus() == InstallStatus.DOWNLOADED)
        {
            val builder = AlertDialog.Builder(this)
            builder.setMessage("Mise à jour réussie. L'application va rédemarrer dans 5 secondes")
            builder.show()
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
                val builder = AlertDialog.Builder(this)
                builder.setMessage("Une erreur s'est produite pendant la mise à jour")
                builder.show()
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

    // Affichage du menu popup en fonction du type de compte
    fun Compte(){
        if (compte == "compte1")
        {
            MenuPopupCompte1Assistant(this).show()
        }else if (compte == "compte2"){
            MenuPopupCompte2Assistant(this).show()
        }
    }

    // Pour Afficher/Masquer le réseau en fonction du compte 1 ou 2
    fun comptePourFacture() : String{
        return compte!!
    }


    // POUR LES RESEAUX MOOV, MTN, WAVE, TRESOR
    fun bottomNavBlocked(mont: String, num: String) {
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        if (mont.isNotEmpty() || num.isNotEmpty())
        {
            bottomNavigationView.setOnItemSelectedListener {
                Toast.makeText(this, "Vous n'avez pas enregisté la transaction.", Toast.LENGTH_SHORT).show()
                false }
        }
    }

    // POUR LES RESEAUX MOOV, MTN, WAVE, TRESOR
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
