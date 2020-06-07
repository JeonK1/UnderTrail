package com.example.undertrail

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.location.Location
import android.location.LocationManager
import android.util.Log
import androidx.core.app.ActivityCompat

class MyDBHelper(val context: Context?) : SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {
    companion object{
        val DB_VERSION = 1
        val DB_NAME = "SmarterSubway.db"
        val TABLE_NAME = "SEOUL_LIST"
        val PNAME = "db_id"
    }

    override fun onCreate(db: SQLiteDatabase?) {

    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {

    }

    fun findNearSubwayStation(loc: Location?):ArrayList<Station>{
        //val strsql = "select * from SEOUL_LIST where (x_gps/1000000 - myX)^2 + (y_gps/1000000 - myY)^2
        //x_gps(12), y_gps(13)
        val stationList = ArrayList<Station>()
        if (loc != null) {
            val myX_gps = loc.latitude * 1000000
            val myY_gps = loc.longitude * 1000000
            val strsql = "SELECT *, " +
                    " (("+myX_gps+"-x_gps)*("+myX_gps+"-x_gps)+" +
                    " ("+myY_gps+"-y_gps)*("+myY_gps+"-y_gps))" +
                    " AS distance" +
                    " FROM SEOUL_LIST" +
                    " WHERE distance < 30000000" +
                    " ORDER BY distance" +
                    " LIMIT 0,10"
            val db = this.readableDatabase
            val cursor = db.rawQuery(strsql, null)
            Log.e("findProduct, count", cursor.count.toString())
            if(cursor.count != 0) {
                cursor.moveToFirst()
                for(i in 0 until cursor.count) {
                    var stationLoc = Location(loc)
                    stationLoc.latitude = cursor.getString(12).toDouble()/1000000
                    stationLoc.longitude = cursor.getString(13).toDouble()/1000000
                    val stationID = cursor.getString(1)
                    val stationName = cursor.getString(3)
                    val stationLineNum = cursor.getString(8)
                    val sDistance = stationLoc?.distanceTo(loc)?.toDouble()
                    Log.e("stationGPS", stationLoc?.latitude.toString()+ " " +stationLoc?.longitude.toString())
                    Log.e("myGPS", loc.latitude.toString()+ " " +loc.longitude.toString())
                    Log.e("distance", sDistance.toString())
                    stationList.add(Station(stationID.toInt(), stationName, stationLoc, stationLineNum, sDistance.toInt()))
                    cursor.moveToNext()
                }
                cursor.close()
                db.close()
            }
            cursor.close()
            db.close()
        }
        return stationList
    }

    fun findProduct(pname: String):Boolean{
        //val strsql = "select * from SEOUL_LIST where db_id=1916"
        val strsql = "select * from "+TABLE_NAME + " where "+ PNAME + " = " + pname + ""
        val db = this.readableDatabase
        val cursor = db.rawQuery(strsql, null)
        Log.e("findProduct, count", cursor.count.toString())
        if(cursor.count != 0) {
            cursor.moveToFirst()
            Log.e("findProduct", cursor.getColumnName(3))
            Log.e("findProduct", cursor.getString(3))
            cursor.close()
            db.close()
            return true
        }
        cursor.close()
        db.close()
        return false
    }

}

