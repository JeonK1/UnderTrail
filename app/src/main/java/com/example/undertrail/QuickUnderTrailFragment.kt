package com.example.undertrail

import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_quick_under_trail.*
import org.json.JSONException
import org.json.JSONObject


/**
 * A simple [Fragment] subclass.
 */
class QuickUnderTrailFragment : Fragment() {

    private var locationManager: LocationManager? = null
    private var myLoc: Location? = null
    lateinit var myDBHelper:MyDBHelper
    lateinit var stationRecyclerView: RecyclerView
    lateinit var stationAdapter: StationAdpater
    var imageFlag = false

    private var requestQueue: RequestQueue? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        var root = inflater.inflate(R.layout.fragment_quick_under_trail, container, false)
        stationRecyclerView = root.findViewById(R.id.stationRecyclerView)
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        imageFlag=false
        quickPageImage.visibility = View.VISIBLE
        locationInit()
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

    private val locationListener: LocationListener = object : LocationListener{
        override fun onLocationChanged(location: Location?) {
            imageFlag=true
            if(quickPageImage!=null)
                quickPageImage.visibility = View.INVISIBLE
            myLoc = location
            Log.e("QuickUnderTailFragment", "onLocationChanged : myloc is (" + location?.longitude + "," + location?.latitude + ")")
            getNearStation()
        }

        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
        }

        override fun onProviderEnabled(provider: String?) {
        }

        override fun onProviderDisabled(provider: String?) {
        }
    }

    private fun getNearStation() {
        myDBHelper = MyDBHelper(context)
        val resultList = myDBHelper.findNearSubwayStation(myLoc)
        val resultList2 = ArrayList<QuickStationInfo>()
        for(res in resultList){
            val nearTrainInfo1 = myDBHelper.getNearTrainInfo1(res.sId)
            val nearTrainInfo2 = myDBHelper.getNearTrainInfo2(res.sId)
            resultList2.add(QuickStationInfo(res, nearTrainInfo1, nearTrainInfo2))
        }
        stationRecyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        stationAdapter = StationAdpater(resultList2)
        stationAdapter.itemClickListener = object:StationAdpater.OnItemClickListener{
            override fun OnItemClick(
                holder: StationAdpater.MyViewHolder,
                view: View,
                data: QuickStationInfo,
                position: Int
            ) {
                //item 클릭했을때의 상황..
            }
        }
        stationRecyclerView.adapter = stationAdapter
    }
}
