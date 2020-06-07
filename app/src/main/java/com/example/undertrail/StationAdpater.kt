package com.example.undertrail

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class StationAdpater(val items:ArrayList<Station>): RecyclerView.Adapter<StationAdpater.MyViewHolder>() {
    interface  OnItemClickListener{
        fun OnItemClick(holder: MyViewHolder, view: View, data:Station, position: Int)
    }

    var itemClickListener:OnItemClickListener?=null

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var sName: TextView = itemView.findViewById(R.id.sName)
        var sLineNum: TextView = itemView.findViewById(R.id.sLineNum)
        var sDistance: TextView = itemView.findViewById(R.id.sDistance)
        //var textMeaning: TextView = itemView.findViewById(R.id.textMeaning)
        init {
            itemView.setOnClickListener {
                itemClickListener?.OnItemClick(this, it, items[adapterPosition], adapterPosition)
            }
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StationAdpater.MyViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.raw_station, parent, false)
        return MyViewHolder(v)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: StationAdpater.MyViewHolder, position: Int) {
        holder.sName.text = items[position].sName
        holder.sLineNum.text = items[position].sLine_num
        holder.sDistance.text = items[position].sDistance.toString() + "m"
    }

}