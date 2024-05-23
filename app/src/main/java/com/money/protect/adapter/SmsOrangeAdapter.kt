package com.money.protect.adapter

import android.app.AlertDialog
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.money.protect.OrangeRedirectionActivity
import com.money.protect.OrangeSaveActivity
import com.money.protect.OrangeSmsListActivity
import com.money.protect.R

class SmsOrangeAdapter(
    private val context: OrangeSmsListActivity,
    private var smsList: ArrayList<String>,
) : RecyclerView.Adapter<SmsOrangeAdapter.SmsViewHolder>() {
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

        holder.itemView.setOnClickListener{
            val builder = AlertDialog.Builder(context)
            builder.setMessage("Ce SMS correspond-il vraiment à votre dernière transaction ?")
            builder.setPositiveButton(android.R.string.yes) { dialog, which ->
                val intent = Intent(context, OrangeSaveActivity::class.java)
                intent.putExtra("sms", sms)
                it.context.startActivity(intent)
            }
            builder.setNegativeButton(android.R.string.no){ dialog, which->
                // Ne rien faire
            }
            builder.show()
        }
    }

    override fun getItemCount(): Int {
        if (smsList.size <= 0)
        {
            return 0
        }
        return 1
    }

}


