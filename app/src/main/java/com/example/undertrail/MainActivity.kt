package com.example.undertrail

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    val quickUnderTrailFragment = QuickUnderTrailFragment()
    val underTrailFragment = UnderTrailFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        fragmentInit()
        navigatorInit()
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
}
