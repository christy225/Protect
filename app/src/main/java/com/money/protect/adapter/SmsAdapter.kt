package com.money.protect.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.money.protect.MainActivity
import com.money.protect.R

class SmsAdapter(
    private val context: MainActivity,
    private val smsList: ArrayList<String>,
) : RecyclerView.Adapter<SmsAdapter.SmsViewHolder>() {
    class SmsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val smsTextView: TextView = itemView.findViewById(R.id.smsTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SmsViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_sms, parent, false)
        return SmsViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: SmsViewHolder, position: Int) {

        val sms = smsList[position]
        holder.smsTextView.text = sms

    }

    override fun getItemCount(): Int {
        if (smsList.size <= 0) {
            return 0
        }
        return smsList.size
    }

}


