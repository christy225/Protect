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
import com.money.protect.SuperviseurDetailPointInterActivity
import com.money.protect.SuperviseurDetailPointNational
import java.text.DecimalFormat

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
        holder.total.text = DecimalFormat("#,###").format(currentPoint.total.toInt())

        holder.itemView.setOnClickListener {
            val intent1 = Intent(context, SuperviseurDetailPointActivity::class.java)
            val intent2 = Intent(context, SuperviseurDetailPointNational::class.java)
            val intent3 = Intent(context, SuperviseurDetailPointInterActivity::class.java)

            if (currentPoint.module == "National")
            {
                intent2.putExtra("id", currentPoint.id)
                intent2.putExtra("orange", currentPoint.orange)
                intent2.putExtra("mtn", currentPoint.mtn)
                intent2.putExtra("moov", currentPoint.moov)
                intent2.putExtra("wave", currentPoint.wave)
                intent2.putExtra("tresor", currentPoint.tresor)
                intent2.putExtra("especes", currentPoint.especes)
                intent2.putExtra("divers", currentPoint.divers)
                intent2.putExtra("date", currentPoint.date)
                it.context.startActivity(intent2)
            }else if (currentPoint.module == "International"){
                intent3.putExtra("id", currentPoint.id)
                intent3.putExtra("retrait", currentPoint.retrait)
                intent3.putExtra("envoi", currentPoint.envoi)
                intent3.putExtra("especes", currentPoint.especes)
                intent3.putExtra("divers", currentPoint.divers)
                intent3.putExtra("date", currentPoint.date)
                it.context.startActivity(intent3)
            }else if (currentPoint.module == "National-International")
            {
                intent1.putExtra("id", currentPoint.id)
                intent1.putExtra("orange", currentPoint.orange)
                intent1.putExtra("mtn", currentPoint.mtn)
                intent1.putExtra("moov", currentPoint.moov)
                intent1.putExtra("wave", currentPoint.wave)
                intent1.putExtra("tresor", currentPoint.tresor)
                intent1.putExtra("retrait", currentPoint.retrait)
                intent1.putExtra("envoi", currentPoint.envoi)
                intent1.putExtra("especes", currentPoint.especes)
                intent1.putExtra("divers", currentPoint.divers)
                intent1.putExtra("date", currentPoint.date)
                it.context.startActivity(intent1)
            }
        }
    }
}