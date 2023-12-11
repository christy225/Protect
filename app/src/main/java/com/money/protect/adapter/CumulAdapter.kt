package com.money.protect.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.money.protect.R
import com.money.protect.models.TransactionModel

class CumulAdapter(
    private var cumulArrayList: ArrayList<TransactionModel>
) : RecyclerView.Adapter<CumulAdapter.CumulViewHolder>() {
    class CumulViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val operateur: TextView = itemView.findViewById(R.id.operateurCumul)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CumulViewHolder{
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_cumul, parent, false)
        return CumulViewHolder(view)
    }

    override fun getItemCount(): Int {
        return cumulArrayList.size
    }

    override fun onBindViewHolder(holder: CumulViewHolder, position: Int) {
        val currentCumul = cumulArrayList[position]
        holder.operateur.text = currentCumul.operateur
    }
}