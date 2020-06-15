package com.example.undertrail

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class StationAdpater(val items:ArrayList<QuickStationInfo>): RecyclerView.Adapter<StationAdpater.MyViewHolder>() {
    interface  OnItemClickListener{
        fun OnItemClick(holder: MyViewHolder, view: View, data:QuickStationInfo, position: Int)
    }

    var itemClickListener:OnItemClickListener?=null

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var sName: TextView = itemView.findViewById(R.id.sName)
        var sLineNum: TextView = itemView.findViewById(R.id.sLineNum)
        var sDistance: TextView = itemView.findViewById(R.id.sDistance)
        var lastStationTrain1: TextView = itemView.findViewById(R.id.lastStationTrain1)
        var lastStationTrain2: TextView = itemView.findViewById(R.id.lastStationTrain2)
        var leftMinTrail1: TextView = itemView.findViewById(R.id.leftMinTrail1)
        var leftMinTrail2: TextView = itemView.findViewById(R.id.leftMinTrail2)

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
        holder.sName.text = items[position].station.sName+"역"
        holder.sLineNum.text = items[position].station.sLine_num
        holder.sDistance.text = items[position].station.sDistance.toString() + "m"
        holder.leftMinTrail1.text = items[position].nearTrainInfo1.leftMinute + "분 뒤에 도착합니다."
        holder.leftMinTrail2.text = items[position].nearTrainInfo2.leftMinute + "분 뒤에 도착합니다."
        holder.lastStationTrain1.text = items[position].nearTrainInfo1.lastStationName + "행"
        holder.lastStationTrain2.text = items[position].nearTrainInfo2.lastStationName + "행"
    }

}