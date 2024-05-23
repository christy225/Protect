package com.money.protect

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.money.protect.fragment_superviseur.ListAssistantHome
import com.money.protect.fragment_superviseur.SettingsSuperviseur

class SuperviseurActivity : AppCompatActivity() {
    lateinit var auth: FirebaseAuth
    private lateinit var bottomNavigationView: BottomNavigationView
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        setContentView(R.layout.activity_superviseur)
        auth = FirebaseAuth.getInstance()
        bottomNavigationView = findViewById(R.id.superviseur_bottomNavigationView)

        loadFragment(ListAssistantHome(this))
        bottomNavigationView.setOnItemSelectedListener {
            when(it.itemId)
            {
                R.id.homeMenuSuperviseur -> loadFragment(ListAssistantHome(this))
                R.id.listAssistantMenuSuperviseur -> loadFragment(
                    SettingsSuperviseur(this)
                )
            }
            false
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

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container_superviseur, fragment)
            .addToBackStack(null)
            .commit()
    }
}