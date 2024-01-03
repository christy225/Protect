package com.money.protect.fragment_assistant.national

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.money.protect.MainActivity
import com.money.protect.R

class OrangeRedirectionFragment(private val context: MainActivity) : Fragment() {
    private lateinit var button: Button
    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_assistant_orange_redirection, container, false)
        button = view.findViewById(R.id.buttonLaunchOrange)
        button.setOnClickListener {
            context.supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, OrangeFragment(context))
                .addToBackStack(null)
                .commit()

            val packageName = "com.orange.ci.ompdv"  // Package name de Facebook
            val className = "com.orange.ci.ompdv.MainActivity"

            val intent = Intent()
            intent.component = ComponentName(packageName, className)
            try {
                context.startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                // Gérez le cas où L'Application WAVE Agent n'est pas installé ou ne permet pas d'ouvrir cette activité
                val intent = AlertDialog.Builder(context)
                intent.setTitle("Infos")
                intent.setMessage("Veuillez installer l'application Wave Agent")
                intent.show()
            }
        }
        return view
    }
}