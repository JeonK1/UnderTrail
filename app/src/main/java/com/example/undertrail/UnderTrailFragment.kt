package com.example.undertrail

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import kotlinx.android.synthetic.main.fragment_detail_under_trail.*
import kotlinx.android.synthetic.main.fragment_under_trail.*

/**
 * A simple [Fragment] subclass.
 */
class UnderTrailFragment : Fragment() {

    lateinit var myDBHelper:MyDBHelper
    lateinit var nameList:ArrayList<String>;
    lateinit var adapter: ArrayAdapter<String>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_under_trail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initAutoComplete()
        testQuery()
    }

    private fun initAutoComplete() {
        myDBHelper = MyDBHelper(context)
        nameList = myDBHelper.getAllStationName2() // 이거 수정하세요
        adapter = ArrayAdapter(
            this!!.context!!,
            android.R.layout.simple_dropdown_item_1line,
            nameList
        )
        startStationEditText.setAdapter(adapter)
        endStationEditText.setAdapter(adapter)
    }

    private fun testQuery() {
        Log.e("queryTest", "start")
        myDBHelper = MyDBHelper(context)
        val result = myDBHelper.findRoute(355, 402)
        for(res in result){
            Log.e("testQueryRoute", res.toString())
        }
    }
}
