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

    private var requestQueue: RequestQueue? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        var root = inflater.inflate(R.layout.fragment_quick_under_trail, container, false)
        stationRecyclerView = root.findViewById(R.id.stationRecyclerView)
        locationInit()
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        jsonTest()
    }

    private fun jsonTest() {
        //val url = "https://api.myjson.com/bins/xbspb"
        val url = "http://swopenapi.seoul.go.kr/api/subway/704b5a54586b616e35365955696849/json/realtimePosition/1/10/8%ED%98%B8%EC%84%A0/"
        Log.e("test", "Setting..")
        val request = JsonObjectRequest(Request.Method.GET, url, null, Response.Listener {
                response ->try {
            val jsonObject = response.getString("message")
            Log.e("test", jsonObject.toString())
            /*
            val jsonArray = response.getJSONArray("employees")
            for (i in 0 until jsonArray.length()) {
                val employee = jsonArray.getJSONObject(i)
                val firstName = employee.getString("firstname")
                val age = employee.getInt("age")
                val mail = employee.getString("mail")
                val str = "$firstName, $age, $mail\n\n"
                Log.e("test", str)
            }
             */
        } catch (e: JSONException) {
            Log.e("test", "failed ㅜㅜ")
            e.printStackTrace()
        }
        }, Response.ErrorListener { error -> error.printStackTrace() })
        requestQueue?.add(request)

        // Access the RequestQueue through your singleton class.
        //MySingleton.getInstance(this).addToRequestQueue(jsonObjectRequest)
        /*
        val url = "http://jsonplaceholder.typicode.com/posts/1"
        //val url = "http://swopenapi.seoul.go.kr/api/subway/704b5a54586b616e35365955696849/json/realtimePosition/1/10/8호선/"
        val client = OkHttpClient()
        val request = Request.Builder()
            .url(url)
            .build()

        println(Thread.currentThread())
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call?, e: IOException?) {
                Log.e("testFail", e?.printStackTrace().toString())
            }

            override fun onResponse(call: Call?, response: Response) {
                if (!response.isSuccessful) {
                    System.err.println("Response not successful")
                    return
                }
                val json = response.body()!!.string()
                val myData = Klaxon().parse<MyData>(json)
                Log.e("test", "Data = "+myData!!.userId.toString())
                Log.e("test", "Data = "+myData!!.title.toString())
                Log.e("test", Thread.currentThread().toString())
            }

        })
        // Shutdown the executor as soon as the request is handled
        client.dispatcher().executorService().shutdown()
         */
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
}
