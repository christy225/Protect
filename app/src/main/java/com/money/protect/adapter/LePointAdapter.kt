package com.money.protect.adapter

import android.annotation.SuppressLint
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.money.protect.models.PointModel
import com.money.protect.R
import com.money.protect.SuperviseurActivity
import com.money.protect.SuperviseurDetailPointActivity

class LePointAdapter(
    private val context: SuperviseurActivity,
    private var caisseArrayList: ArrayList<PointModel>
    ) : RecyclerView.Adapter<LePointAdapter.CaisseviewHolder>() {
    class CaisseviewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val daterecherche = itemView.findViewById<TextView>(R.id.listPointDateSuperviseurVw)
        val total = itemView.findViewById<TextView>(R.id.listPointTotalSuperviseurVw)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setFilteredList(caisseArrayList: ArrayList<PointModel>){
        this.caisseArrayList = caisseArrayList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CaisseviewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_superviseur_point, parent, false)
        return CaisseviewHolder(view)
    }

    override fun getItemCount(): Int {
        return caisseArrayList.size
    }

    override fun onBindViewHolder(holder: CaisseviewHolder, position: Int) {
        val currentPoint = caisseArrayList[position]
        holder.daterecherche.text = currentPoint.date
        holder.total.text = currentPoint.total

        holder.itemView.setOnClickListener {
            val intent = Intent(context, SuperviseurDetailPointActivity::class.java)
            intent.putExtra("id", currentPoint.id)
            intent.putExtra("orange", currentPoint.orange)
            intent.putExtra("mtn", currentPoint.mtn)
            intent.putExtra("moov", currentPoint.moov)
            intent.putExtra("wave", currentPoint.wave)
            intent.putExtra("tresor", currentPoint.tresor)
            intent.putExtra("especes", currentPoint.especes)
            intent.putExtra("divers", currentPoint.divers)
            intent.putExtra("date", currentPoint.date)
            it.context.startActivity(intent)
        }
    }
}