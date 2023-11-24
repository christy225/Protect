package com.money.protect.adapter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.money.protect.R
import com.money.protect.SuperviseurActivity
import com.money.protect.fragment_superviseur.HomeSuperviseurFragment
import com.money.protect.models.AccountModel

class AssistantListAdapterHome(
    private val context: SuperviseurActivity,
    private var assistantArrayList: ArrayList<AccountModel>
) : RecyclerView.Adapter<AssistantListAdapterHome.AssistantVwHolder>() {
    class AssistantVwHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nom : TextView = itemView.findViewById(R.id.nomCompletAssistHome)
        val phone: TextView = itemView.findViewById(R.id.phoneAssistHome)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AssistantVwHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_list_assistant_home, parent, false)
        return AssistantVwHolder(view)
    }

    override fun getItemCount(): Int {
        return assistantArrayList.size
    }

    override fun onBindViewHolder(holder: AssistantVwHolder, position: Int) {
        val currentAssistant = assistantArrayList[position]
        holder.nom.text = currentAssistant.nomcomplet
        holder.phone.text = currentAssistant.telephone
        val secondFragment = HomeSuperviseurFragment(context)
        holder.itemView.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("id", currentAssistant.id)
            bundle.putString("nom", currentAssistant.nomcomplet)
            bundle.putString("module", currentAssistant.module)
            secondFragment.arguments = bundle
            context.supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container_superviseur, secondFragment)
                .addToBackStack(null)
                .commit()
        }
    }
}