package com.example.undertrail

import android.Manifest
import android.content.pm.PackageManager
import android.database.sqlite.SQLiteOpenHelper
import android.location.Location
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import kotlinx.android.synthetic.main.activity_main.*
import java.io.FileOutputStream

class MainActivity : AppCompatActivity() {

    val quickUnderTrailFragment = QuickUnderTrailFragment()
    val underTrailFragment = UnderTrailFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestPermission()
        setContentView(R.layout.activity_main)
        fragmentInit()
        navigatorInit()
        initDB()

    }

    private fun requestPermission() {
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)== PackageManager.PERMISSION_GRANTED){
        }
        else{
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), 100)
        }
    }

    private fun initDB() {
        val dbfile = this.getDatabasePath("SmarterSubway.db")
        if(!dbfile.parentFile.exists())
            dbfile.parentFile.mkdir() // 없으면 만들기
        if(!dbfile.exists()) {
            val file = resources.openRawResource(R.raw.smartersubway)
            val fileSize = file.available() // 파일크기반환
            val buffer = ByteArray(fileSize)
            file.read(buffer)
            file.close()
            dbfile.createNewFile() // mydb.db 라는 파일 만들어짐
            val output = FileOutputStream(dbfile)
            output.write(buffer)
            output.close()
        }
    }

    private fun fragmentInit() {
        val fragment = supportFragmentManager.beginTransaction()
        fragment.addToBackStack(null)
        fragment.replace(R.id.myFragment, quickUnderTrailFragment)
        fragment.commit()
    }

    private fun navigatorInit() {
        bottomNavigationView.setOnNavigationItemSelectedListener{
            when(it.itemId){
                R.id.navigation_first -> {
                    //fragment select
                    val fragment = supportFragmentManager.beginTransaction()
                    fragment.addToBackStack(null)
                    fragment.replace(R.id.myFragment, quickUnderTrailFragment)
                    fragment.commit()
                    true
                }
                R.id.navigation_second -> {
                    //fragment select
                    val fragment = supportFragmentManager.beginTransaction()
                    fragment.addToBackStack(null)
                    fragment.replace(R.id.myFragment, underTrailFragment)
                    fragment.commit()
                    true
                }
                else -> { false }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == 100){
            if(grantResults[0]==PackageManager.PERMISSION_GRANTED &&
                grantResults[1]==PackageManager.PERMISSION_GRANTED){
            }
            else{
                Toast.makeText(this, "위치정보 제공을 하셔야 합니다.", Toast.LENGTH_SHORT)
            }
        }
    }
}
