package com.example.undertrail

import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.location.Location
import android.location.LocationManager
import android.util.Log
import androidx.core.app.ActivityCompat
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList

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
    fun DBIDtoSName(db_id: String):String{
        val db = this.readableDatabase
        val strsql = "SELECT stat_name FROM SEOUL_LIST WHERE db_id = "+db_id
        val cursor = db.rawQuery(strsql, null)
        cursor.moveToFirst()
        return cursor.getString(0);
    }
    fun SIDtoSName(sid: Int):String{
        val db = this.readableDatabase
        val strsql = "SELECT stat_name FROM SEOUL_LIST WHERE _id = "+sid
        val cursor = db.rawQuery(strsql, null)
        cursor.moveToFirst()
        return cursor.getString(0);
    }
    fun SNametoSID(sname: String):Int{
        val db = this.readableDatabase
        val strsql = "SELECT _id FROM SEOUL_LIST WHERE stat_name = \""+sname+"\""
        val cursor = db.rawQuery(strsql, null)
        cursor.moveToFirst()
        return cursor.getString(0).toInt()
    }
    fun SIDtoDBID(sid: Int):String{
        val db = this.readableDatabase
        val strsql = "SELECT db_id FROM SEOUL_LIST WHERE _id = "+sid
        val cursor = db.rawQuery(strsql, null)
        cursor.moveToFirst()
        return cursor.getString(0)
    }

    fun getTimeTable(db_id:String, radioFlag:Int): Cursor {
        var whereContext = ""
        if(radioFlag==1)
            whereContext = "(a.date=7 or a.date=4)"
        else
            whereContext = "(a.date=7 or a.date=3)"

        val tableName = "SEOUL_"+db_id+" a, SEOUL_LIST b"
        val strsql = "SELECT a.dire_, a.time, b.stat_name FROM "+tableName+" WHERE "+whereContext +" and (b.db_id=a.dest_)"
        //SELECT a.dire_, a.time, b.stat_name FROM SEOUL_2727 a, SEOUL_LIST b WHERE (a.date=7 or a.date=4) and (b.db_id=a.dest_)
        val db = this.readableDatabase
        val cursor = db.rawQuery(strsql, null)
        return cursor
    }
    fun findRoute(strSId:Int, endSId:Int): Pair<ArrayList<Int>, ArrayList<Int>> {
        data class MyConnect(val dstSId:Int, val weight:Int){
        }
        data class MyStation(val mySId:Int, val connectList: ArrayList<MyConnect>, var backSId:Int, var backWeight:Int, var totalWeight:Int){
        }
        class MyPair(val value:Int, val key:Int) : Comparable<MyPair>{
            override fun compareTo(other: MyPair): Int {
                return value.compareTo(other.value)
            }
        }
        val db = this.readableDatabase
        var strsql=""
        //get subway count
        strsql = "SELECT count(*) " +
                " FROM SEOUL_LIST"
        var cursor = db.rawQuery(strsql, null)
        cursor.moveToFirst()
        val stationCnt = cursor.getString(0).toInt()
        var stationCheck = ArrayList<Boolean>()
        var stations = ArrayList<MyStation>()
        for (i in 1..stationCnt){
            var tmpConnect = ArrayList<MyConnect>()
            stations.add(MyStation(i, tmpConnect, -1, -1, 0))
            stationCheck.add(false)
        }
        //get subway connect
        strsql = "SELECT strStId, arrStId, weight FROM SEOUL_WEIGHT ORDER BY strStId"
        cursor = db.rawQuery(strsql, null)
        cursor.moveToFirst()
        do{
            val strStId = cursor.getString(0).toInt()
            val arrStId = cursor.getString(1).toInt()
            val weight = cursor.getString(2).toInt()
            stations[strStId-1].connectList.add(MyConnect(arrStId, weight))
            cursor.moveToNext()
        }while (!cursor.isLast)
        //start algorithm
        var totalWeight:Int;
        var route = ArrayList<Int>()
        var routeWeight = ArrayList<Int>()
        var q = PriorityQueue<MyPair>() // minHeap
        q.add(MyPair(0, strSId)) // first Value
        while(true){
            val out = q.poll() // poll() = pop()
            if(stationCheck[out.key-1]){
                continue
            }
            if(out.key == endSId){
                totalWeight = out.value
                var tmpSId = endSId
                var tmpSWeight = stations[endSId-1].backWeight
                //Log.e("goBack", "now : "+tmpSId)
                route.add(tmpSId)
                routeWeight.add(tmpSWeight)
                while(tmpSId!=strSId){
                    //Log.e("goBack", "now : "+tmpSId)
                    tmpSId = stations[tmpSId-1].backSId
                    tmpSWeight = stations[tmpSId-1].backWeight
                    route.add(tmpSId)
                    routeWeight.add(tmpSWeight)
                }
                break
            }
            stationCheck[out.key-1] = true
            for (i in 0 until stations[out.key-1].connectList.size){
                val tmpConnect = stations[out.key-1].connectList[i]
                if(stationCheck[tmpConnect.dstSId-1]){
                    continue
                }
                q.add(MyPair(out.value+tmpConnect.weight, tmpConnect.dstSId))
                stations[tmpConnect.dstSId-1].backSId = out.key
                stations[tmpConnect.dstSId-1].backWeight = tmpConnect.weight
            }
        }
        //처음 역 환승으로 인한 중복일시 중복 제거
        var tmpSize = route.size
        if(SIDtoSName(route[tmpSize-1]).equals(SIDtoSName(route[tmpSize-2]))){
            route.removeAt(tmpSize-1)
            routeWeight.removeAt(routeWeight.size-1)
        }
        tmpSize = route.size
        for(w in routeWeight){
            Log.e("routeWeight : ", w.toString())
        }
        //마지막 역 환승으로 인한 중복일시 중복 제거
        if(SIDtoSName(route[0]).equals(SIDtoSName(route[1]))){
            route.removeAt(0)
            routeWeight.removeAt(0)
        }
        return Pair(route, routeWeight)
        // 출발지나 도착지가 환승역인 경우.. 예를들어... "군자 군자 아차산 .... 모란 모란" 이런식으로 두번 불려서 걸리는시간이 원래보다 더 추가될 수 있음. 그니까 맨 앞이랑 맨 뒤가 중복되는게 있으면 알아서 짤라주셔야합니다.
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
//                    Log.e("stationGPS", stationLoc?.latitude.toString()+ " " +stationLoc?.longitude.toString())
//                    Log.e("myGPS", loc.latitude.toString()+ " " +loc.longitude.toString())
//                    Log.e("distance", sDistance.toString())
                    stationList.add(Station(stationID.toInt(), stationName, stationLoc, stationLineNum, sDistance.toInt()))
                    cursor.moveToNext()
                }
            }
            cursor.close()
            db.close()
        }
        return stationList
    }

    fun getAllStationName():ArrayList<String>{
        // ~~역 (~호선)
        val nameList = ArrayList<String>()
        val strsql = "SELECT stat_name, line_num" +
                " FROM SEOUL_LIST"
        val db = this.readableDatabase
        val cursor = db.rawQuery(strsql, null)
        if(cursor.count != 0) {
            cursor.moveToFirst()
            for(i in 0 until cursor.count) {
                val tmpStr = cursor.getString(0) + "(" + cursor.getString(1) +")"
                nameList.add(tmpStr)
                cursor.moveToNext()
            }
        }
        cursor.close()
        db.close()
        return nameList
    }
    fun getAllStationName2():ArrayList<String>{
        // ~~역
        val nameList = ArrayList<String>()
        val strsql = "SELECT stat_name" +
                " FROM SEOUL_LIST" +
                " GROUP BY stat_name"
        val db = this.readableDatabase
        val cursor = db.rawQuery(strsql, null)
        if(cursor.count != 0) {
            cursor.moveToFirst()
            for(i in 0 until cursor.count) {
                val tmpStr = cursor.getString(0)
                nameList.add(tmpStr)
                cursor.moveToNext()
            }
        }
        cursor.close()
        db.close()
        return nameList
    }

    fun getStationDetailInfo(sName:String, sLineNume:String):StationDetail{
        lateinit var stationData:StationDetail
        val strsql = "SELECT *" +
                " FROM SEOUL_LIST"+
                " WHERE stat_name = \"" + sName + "\" and line_num = \"" + sLineNume +"\""
        val db = this.readableDatabase
        val cursor = db.rawQuery(strsql, null)
        if(cursor.count != 0) {
            cursor.moveToFirst()
            for(i in 0 until cursor.count) {
                val  sId = cursor.getString(1)
                val  sName = cursor.getString(3)
                val  sLineNum = cursor.getString(8)
                val  sAddress = cursor.getString(45)
                val  sPhoneNum = cursor.getString(43)
                var  sMapURL = cursor.getString(46)
                if(sMapURL == "")
                    sMapURL = "제공되지않음"
                stationData = StationDetail(sId, sName, sLineNum, sAddress, sPhoneNum, sMapURL)
                cursor.moveToNext()
            }
        }
        else{
            stationData = StationDetail("", sName, "", "", "", "")
        }
        cursor.close()
        db.close()
        return stationData
    }
    fun getNearTrainInfo1(sId:Int):NearTrainInfo{
        val timestamp = LocalDateTime.now()
        val dateTime = timestamp.format(DateTimeFormatter.ofPattern("HH:mm"))
        val tableName = "SEOUL_"+SIDtoDBID(sId)
        var whereContext = ""
        val currentDayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
        if((currentDayOfWeek>1) && (currentDayOfWeek<7))
            whereContext+="(date=7 or date=4)"
        else
            whereContext+="(date=7 or date=3)"

        val strsql = "SELECT time, dest_ FROM "+tableName+" WHERE "+whereContext+" and dire_=1 and (time >= \""+dateTime+"\")"
        val db = this.readableDatabase
        val cursor = db.rawQuery(strsql, null)

        var returnTrainInfo = NearTrainInfo("", "")

        if(cursor.count>0) {
            cursor.moveToFirst()
            val leftTime = cursor.getString(0)
            Log.e("leftTime", leftTime)
            val nowTime = dateTime.split(":")[0].toInt()*60 + dateTime.split(":")[1].toInt()
            val nextTime = leftTime.split(":")[0].toInt()*60 + leftTime.split(":")[1].toInt()
            val leftMinute = (nextTime-nowTime).toString()
            cursor.moveToFirst()
            val lastStationName = DBIDtoSName(cursor.getString(1))
            returnTrainInfo = NearTrainInfo(lastStationName, leftMinute)
        }
        return returnTrainInfo
    }
    fun getNearTrainInfo2(sId:Int):NearTrainInfo{
        val timestamp = LocalDateTime.now()
        val dateTime = timestamp.format(DateTimeFormatter.ofPattern("HH:mm"))
        val tableName = "SEOUL_"+SIDtoDBID(sId)
        var whereContext = ""
        val currentDayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
        if((currentDayOfWeek>1) && (currentDayOfWeek<7))
            whereContext+="(date=7 or date=4)"
        else
            whereContext+="(date=7 or date=3)"

        val strsql = "SELECT time, dest_ FROM "+tableName+" WHERE "+whereContext+" and dire_=2 and (time >= \""+dateTime+"\")"
        val db = this.readableDatabase
        val cursor = db.rawQuery(strsql, null)

        var returnTrainInfo = NearTrainInfo("", "")

        if(cursor.count>0) {
            cursor.moveToFirst()
            val leftTime = cursor.getString(0)
            Log.e("leftTime", leftTime)
            val nowTime = dateTime.split(":")[0].toInt()*60 + dateTime.split(":")[1].toInt()
            val nextTime = leftTime.split(":")[0].toInt()*60 + leftTime.split(":")[1].toInt()
            val leftMinute = (nextTime-nowTime).toString()
            cursor.moveToFirst()
            val lastStationName = DBIDtoSName(cursor.getString(1))
            returnTrainInfo = NearTrainInfo(lastStationName, leftMinute)
        }
        return returnTrainInfo
    }
}

