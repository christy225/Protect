package com.money.protect.fragment_superviseur

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.money.protect.R
import com.money.protect.SuperviseurActivity

class MenuAssistSuperviseur(private val context: SuperviseurActivity, ) : Fragment() {
    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_superviseur_menu_assist, container, false)

        val data = arguments
        val id = data?.getString("id")
        val nom = data?.getString("nom")
        val assignation = data?.getString("module")

        val buttonCumul = view.findViewById<Button>(R.id.popupCumul)
        val cumulFragment = CumulTransactionSuperviseur(context)
        buttonCumul.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("id", id)
            bundle.putString("nom", nom)
            bundle.putString("module", assignation)
            cumulFragment.arguments = bundle
            context.supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container_superviseur, cumulFragment)
                .addToBackStack(null)
                .commit()
        }

        val buttonBack = view.findViewById<ImageView>(R.id.backFromMenuSuperviseur)
        val homeFragment = HomeSuperviseurFragment(context)
        buttonBack.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("id", id)
            bundle.putString("nom", nom)
            bundle.putString("module", assignation)
            homeFragment.arguments = bundle
            context.supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container_superviseur, homeFragment)
                .addToBackStack(null)
                .commit()
        }

        val buttonTransaction = view.findViewById<Button>(R.id.popupTransaction)
        val transacFragment = TransactionAssistantFragment(context)
        buttonTransaction.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("id", id)
            bundle.putString("nom", nom)
            bundle.putString("module", assignation)
            transacFragment.arguments = bundle
            context.supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container_superviseur, transacFragment)
                .addToBackStack(null)
                .commit()
        }

        val buttonCapital = view.findViewById<Button>(R.id.popupCapital)
        val capitalFragment = CapitalSuperviseurFragment(context)
        buttonCapital.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("id", id)
            bundle.putString("nom", nom)
            bundle.putString("module", assignation)
            capitalFragment.arguments = bundle
            context.supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container_superviseur, capitalFragment)
                .addToBackStack(null)
                .commit()
        }

        return view
    }

}