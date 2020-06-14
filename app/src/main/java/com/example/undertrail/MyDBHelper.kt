package com.example.undertrail

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.location.Location
import android.location.LocationManager
import android.util.Log
import androidx.core.app.ActivityCompat
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

    fun getTimeTable(db_id:Int, radioFlag:Int): Cursor {
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
    fun findRoute(strSId:Int, endSId:Int):ArrayList<Int>{
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
                    routeWeight.add(tmpSId)
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
            routeWeight.removeAt(tmpSize-1)
        }
        tmpSize = route.size
        //마지막 역 환승으로 인한 중복일시 중복 제거
        if(SIDtoSName(route[0]).equals(SIDtoSName(route[1]))){
            route.removeAt(0)
            routeWeight.removeAt(0)
        }
        Log.e("findRoute", "weight : "+route.size.toString())
        return route
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
                    Log.e("stationGPS", stationLoc?.latitude.toString()+ " " +stationLoc?.longitude.toString())
                    Log.e("myGPS", loc.latitude.toString()+ " " +loc.longitude.toString())
                    Log.e("distance", sDistance.toString())
                    stationList.add(Station(stationID.toInt(), stationName, stationLoc, stationLineNum, sDistance.toInt()))
                    //http://swopenAPI.seoul.go.kr/api/subway/(인증키)/json/realtimeStationArrival/0/5/서울
                    /*
                    KEY	String(필수)	인증키	OpenAPI 에서 발급된 인증키
                    TYPE	String(필수)	요청파일타입	xml : xml, xml파일 : xmlf, 엑셀파일 : xls, json파일 : json
                    SERVICE	String(필수)	서비스명	realtimeStationArrival
                    START_INDEX	INTEGER(필수)	요청시작위치	정수 입력 (페이징 시작번호 입니다 : 데이터 행 시작번호)
                    END_INDEX	INTEGER(필수)	요청종료위치	정수 입력 (페이징 끝번호 입니다 : 데이터 행 끝번호)
                    statnNm	STRING(필수)	지하철역명	지하철역명
                     */
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

