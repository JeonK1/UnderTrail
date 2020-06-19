package com.example.undertrail

import android.content.Context
import android.graphics.Color
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.TableRow
import android.widget.TextView
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
            if(startStationEditText!=null) {
                if (!flag) {
                    flag = true
                    startStationEditText.setText(myDBHelper.findNearSubwayStation(myLoc)[0].sName)
                }
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

            val activity = context as MainActivity
            activity.routeTable.removeAllViewsInLayout()        //모두 제거하겠던 소리
            //column 타이틀 만들기 ( activity )
            val tablerow = TableRow(activity)
            val rowParam = TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT, 1.0f)
            tablerow.layoutParams = rowParam
            val viewParams = TableRow.LayoutParams(0, 100, 1f)

            for (i in 0 until 1) {
                //column은 1개입니다.
                val textView = TextView(activity)
                textView.layoutParams = viewParams
                textView.gravity = Gravity.CENTER
                tablerow.addView(textView)
            }
            activity.routeTable.addView(tablerow)
            //실제 레코드 읽어오기
            var tmp = 0
            while(tmp < result.size){
                val row = TableRow(activity)
                row.layoutParams = rowParam
                val textView = TextView(activity)
                textView.layoutParams = viewParams
                var textStr = myDBHelper.SIDtoSName(result[result.size-1-tmp])
                var boldFlag = false
                if(tmp<result.size-1) {
                    if(myDBHelper.SIDtoSName(result[result.size-1-tmp]).equals(myDBHelper.SIDtoSName(result[result.size-1-(tmp+1)]))) {
                        textStr += " (환승)"
                        boldFlag = true
                        tmp++
                    }
                }
                textView.text = textStr
                textView.textSize = 13.0f
                if(boldFlag)
                    textView.textSize = 15.0f
                textView.gravity = Gravity.CENTER
                row.addView(textView)
                activity.routeTable.addView(row)
                tmp+=1
            }
        }
    }

    private fun initAutoComplete() {
        myDBHelper = MyDBHelper(context)
        nameList = myDBHelper.getAllStationName2()
        adapter = ArrayAdapter(
            this!!.context!!,
            android.R.layout.simple_dropdown_item_1line,
            nameList
        )
        startStationEditText.setAdapter(adapter)
        endStationEditText.setAdapter(adapter)
    }
}
