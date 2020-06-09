package com.example.undertrail

import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.net.toUri
import kotlinx.android.synthetic.main.fragment_detail_under_trail.*
import java.io.File

/**
 * A simple [Fragment] subclass.
 */
class DetailUnderTrailFragment : Fragment() {
    lateinit var nameList:ArrayList<String>;
    lateinit var myDBHelper:MyDBHelper
    lateinit var adapter: ArrayAdapter<String>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_detail_under_trail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initAutoComplete()
        initBtn()
    }

    private fun initBtn() {
        searchBnt.setOnClickListener {
            if(autoCompleteText.text.toString() != ""){
                val tmpText = autoCompleteText.text.toString()
                var sName = tmpText.split("(")[0]
                var sLineNum = tmpText.split("(")[1]
                sLineNum = sLineNum.substring(0, sLineNum.length-1)

                myDBHelper = MyDBHelper(context)
                val station = myDBHelper.getStationDetailInfo(sName, sLineNum)
                stationName.setText(station.sName)
                stationLineNum.setText(station.sLineNum)
                stationAddress.setText(station.sAddress)
                stationPhoneNum.setText(station.sPhoneNum)
                stationMapURL.setText(station.sMapURL)
                
                //table ㄱㄱ
                //않이 근데 시간표 대체 어디있는걸로 가져오는거지;;; 이상허네..
            }
        }
    }

    private fun initAutoComplete() {
        myDBHelper = MyDBHelper(context)
        //val result = myDBHelper.findProduct("1916")
        nameList = myDBHelper.getAllStationName()
        adapter = ArrayAdapter(
            this!!.context!!,
            android.R.layout.simple_dropdown_item_1line,
            nameList
        )
        autoCompleteText.setAdapter(adapter)
    }

}
