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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * A simple [Fragment] subclass.
 */
class QuickUnderTrailFragment : Fragment() {

    private var locationManager: LocationManager? = null
    private var myLoc: Location? = null
    lateinit var myDBHelper:MyDBHelper
    lateinit var stationRecyclerView: RecyclerView
    lateinit var stationAdapter: StationAdpater

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
        locationInit()
    }

    private val locationListener: LocationListener = object : LocationListener{
        override fun onLocationChanged(location: Location?) {
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
        stationRecyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        stationAdapter = StationAdpater(resultList)
        stationAdapter.itemClickListener = object:StationAdpater.OnItemClickListener{
            override fun OnItemClick(
                holder: StationAdpater.MyViewHolder,
                view: View,
                data: Station,
                position: Int
            ) {
                //item 클릭했을때의 상황..
            }
        }
        stationRecyclerView.adapter = stationAdapter
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
}
