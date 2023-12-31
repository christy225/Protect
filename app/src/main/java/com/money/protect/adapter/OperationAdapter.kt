package com.money.protect.adapter

import android.annotation.SuppressLint
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.money.protect.MainDetailOperationActivity
import com.money.protect.MainActivity
import com.money.protect.R
import com.money.protect.models.TransactionModel
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

class OperationAdapter(
    val context: MainActivity,
    private var transactionArrayList: ArrayList<TransactionModel>
    ) : RecyclerView.Adapter<OperationAdapter.TransactionViewHolder>() {
    class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image = itemView.findViewById<ImageView>(R.id.imageView)
        val date = itemView.findViewById<TextView>(R.id.dayVw)
        val heure = itemView.findViewById<TextView>(R.id.hourVw)
        val tel = itemView.findViewById<TextView>(R.id.telVw)
        val type = itemView.findViewById<TextView>(R.id.typeVw)
        var montant = itemView.findViewById<TextView>(R.id.montant)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setFilteredList(transactionArrayList: ArrayList<TransactionModel>){
        this.transactionArrayList = transactionArrayList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_assitant_operation, parent, false)
        return TransactionViewHolder(view)
    }

    override fun getItemCount(): Int {
        return transactionArrayList.size
    }

    @SuppressLint("ResourceAsColor")
    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val currentTransaction = transactionArrayList[position]

        // Affiche l'image du réseau en fonction de l'item
        if (currentTransaction.operateur == "orange" )
        {
            holder.image.setImageResource(R.drawable.orange)
        }else if (currentTransaction.operateur == "mtn"){
            holder.image.setImageResource(R.drawable.mtn)
        }else if (currentTransaction.operateur == "moov"){
            holder.image.setImageResource(R.drawable.moov)
        }else if (currentTransaction.operateur == "wave"){
            holder.image.setImageResource(R.drawable.wave)
        }else if (currentTransaction.operateur == "tresor money"){
            holder.image.setImageResource(R.drawable.tresor)
        }else if (currentTransaction.operateur == "western union") {
            holder.image.setImageResource(R.drawable.western)
        }else if (currentTransaction.operateur == "moneygram") {
            holder.image.setImageResource(R.drawable.moneygram)
        }else if (currentTransaction.operateur == "ria") {
            holder.image.setImageResource(R.drawable.ria)
        }
        holder.date.text = currentTransaction.date
        holder.heure.text = currentTransaction.heure
        holder.tel.text = currentTransaction.telephone

        // changer la couleur du background du type : Depot/Retrait
        if (currentTransaction.typeoperation == "Dépôt" || currentTransaction.typeoperation == "Envoi")
        {
            holder.type.setBackgroundResource(R.color.back_type_transfer_depot)
        }else if (currentTransaction.typeoperation == "Retrait"){
            holder.type.setBackgroundResource(R.color.back_type_transfer_retrait)
        }
        holder.type.text = currentTransaction.typeoperation

        // Utilisation de la locale par défaut pour obtenir le séparateur de milliers correct
        val format = DecimalFormat("#,###", DecimalFormatSymbols.getInstance(Locale.getDefault()))
        if (currentTransaction.statut) {
            holder.montant.text = format.format(currentTransaction.montant.toInt()).toString()
        }else {
            holder.montant.text = "Annulé"
        }


        holder.itemView.setOnClickListener {
            val statut = currentTransaction.statut
            val intent = Intent(context, MainDetailOperationActivity::class.java)
            intent.putExtra("id", currentTransaction.id)
            intent.putExtra("idDoc", currentTransaction.idDoc)
            intent.putExtra("operateur", currentTransaction.operateur)
            intent.putExtra("telephone", currentTransaction.telephone)
            intent.putExtra("montant", currentTransaction.montant)
            intent.putExtra("typeoperation", currentTransaction.typeoperation)
            intent.putExtra("date", currentTransaction.date)
            intent.putExtra("heure", currentTransaction.heure)
            intent.putExtra("url", currentTransaction.url)
            intent.putExtra("statut", statut.toString())
            it.context.startActivity(intent)
        }
    }
}