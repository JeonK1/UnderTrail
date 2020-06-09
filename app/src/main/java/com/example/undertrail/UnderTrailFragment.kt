package com.example.undertrail

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

/**
 * A simple [Fragment] subclass.
 */
class UnderTrailFragment : Fragment() {

    lateinit var myDBHelper:MyDBHelper

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_under_trail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        testQuery()
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
