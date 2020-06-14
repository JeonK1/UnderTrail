package com.example.undertrail

import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import kotlinx.android.synthetic.main.fragment_detail_under_trail.*
import kotlinx.android.synthetic.main.fragment_under_trail.*
import kotlinx.android.synthetic.main.raw_station.*

/**
 * A simple [Fragment] subclass.
 */
class UnderTrailFragment : Fragment() {

    private var locationManager: LocationManager? = null
    private var myLoc: Location? = null
    lateinit var myDBHelper:MyDBHelper
    lateinit var nameList:ArrayList<String>
    lateinit var adapter: ArrayAdapter<String>
    var flag=false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        locationInit()
        flag=false
        return inflater.inflate(R.layout.fragment_under_trail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    private fun init() {
        startStationEditText.setText("")
        initAutoComplete()
        initButton()
    }

    private fun locationInit() {
        locationManager = activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        try {
            locationManager?.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                0L,
                0f,
                locationListener
            )
        }
        catch (ex: SecurityException){
            Log.e("QuickUnderTailFragment", "onViewCreate : Security Exception, no locatin available")
        }
    }

    private val locationListener: LocationListener = object : LocationListener {
        override fun onLocationChanged(location: Location?) {
            myLoc = location
            if(!flag) {
                Log.e("test", "hello")
                flag=true
                startStationEditText.setText(myDBHelper.findNearSubwayStation(myLoc)[0].sName)
            }
        }
        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
        }

        override fun onProviderEnabled(provider: String?) {
        }

        override fun onProviderDisabled(provider: String?) {
        }
    }

    private fun initButton() {
        clearBtn.setOnClickListener {
            endStationEditText.setText("")
        }
        findRouteBtn.setOnClickListener {
            //확인버튼
            myDBHelper = MyDBHelper(context)
            val strSid = myDBHelper.SNametoSID(startStationEditText.text.toString())
            val endSid = myDBHelper.SNametoSID(endStationEditText.text.toString())
            val result = myDBHelper.findRoute(strSid, endSid)
            for(res in result){
                Log.e("testQueryRoute", res.toString())
            }
        }
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
}
