package com.money.protect.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Switch
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.money.protect.models.AccountModel
import com.money.protect.repository.AssistantRepository
import com.money.protect.R

class AssistantListAdapter(
    private var assistantArrayList: ArrayList<AccountModel>
) : RecyclerView.Adapter<AssistantListAdapter.AssitantViewHolder>() {
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
            currentAssistant.statut = !currentAssistant.statut
            repo.updateStatut(currentAssistant)
        }
        holder.switch.isChecked = currentAssistant.statut
    }
}