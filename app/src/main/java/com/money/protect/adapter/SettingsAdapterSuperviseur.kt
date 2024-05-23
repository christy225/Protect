package com.money.protect.adapter

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.money.protect.models.AccountModel
import com.money.protect.repository.AssistantRepository
import com.money.protect.R
import com.money.protect.SuperviseurActivity

class SettingsAdapterSuperviseur(
    private val context: SuperviseurActivity,
    private var assistantArrayList: ArrayList<AccountModel>
) : RecyclerView.Adapter<SettingsAdapterSuperviseur.AssitantViewHolder>() {
    class AssitantViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nomcomplet = itemView.findViewById<TextView>(R.id.nomcompletAssistanItemVw)
        val tel = itemView.findViewById<TextView>(R.id.telAssistantItemVw)
        @SuppressLint("UseSwitchCompatOrMaterialCode")
        val switch = itemView.findViewById<Switch>(R.id.switchAssistantItemVw)
    }

    val repo = AssistantRepository()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AssitantViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_list_assistant, parent, false)
        return AssitantViewHolder(view)
    }

    override fun getItemCount(): Int {
        return assistantArrayList.size
    }

    override fun onBindViewHolder(holder: AssitantViewHolder, position: Int) {
        val currentAssistant = assistantArrayList[position]
        holder.nomcomplet.text = currentAssistant.nomcomplet
        holder.tel.text = currentAssistant.telephone
        holder.switch.setOnClickListener {
            if (holder.switch.isChecked)
            {
                val builder = AlertDialog.Builder(context)
                builder.setTitle("Activation")
                    .setMessage("Voulez-vous activer le compte de cet Assistant ?")
                    .setPositiveButton("Oui"){ dialog, id->
                        currentAssistant.statut = !currentAssistant.statut
                        repo.updateStatut(currentAssistant)
                        Toast.makeText(context, "Le compte est maintenant réactivé", Toast.LENGTH_SHORT).show()
                    }.setNegativeButton("Non"){ dialod, id->
                        // Ne rien faire
                        holder.switch.isChecked = currentAssistant.statut
                    }
                builder.create().show()
            }else{
                val builder = AlertDialog.Builder(context)
                builder.setTitle("Désactivation")
                    .setMessage("Vous êtes sur le point de désactiver le compte de votre gérant.\nVoulez-vous continuer ?")
                    .setPositiveButton("Oui"){ dialog, id->
                        currentAssistant.statut = !currentAssistant.statut
                        repo.updateStatut(currentAssistant)
                        Toast.makeText(context, "Le compte est maintenant désactivé", Toast.LENGTH_SHORT).show()
                    }.setNegativeButton("Non"){ dialod, id->
                        // Ne rien faire
                        holder.switch.isChecked = currentAssistant.statut
                    }
                builder.create().show()
            }
        }
        holder.switch.isChecked = currentAssistant.statut
    }
}