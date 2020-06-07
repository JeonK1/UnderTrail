package com.example.undertrail

import android.location.Location

data class Station(val sId:Int, val sName:String, val sLoc:Location?, val sLine_num:String, val sDistance:Int) {
}