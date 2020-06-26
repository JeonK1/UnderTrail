package com.example.undertrail

import android.database.Cursor
import android.database.CursorIndexOutOfBoundsException
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.net.toUri
import kotlinx.android.synthetic.main.fragment_detail_under_trail.*
import kotlinx.android.synthetic.main.fragment_under_trail.*
import java.io.File

/**
 * A simple [Fragment] subclass.
 */
class DetailUnderTrailFragment : Fragment() {
    lateinit var myDBHelper:MyDBHelper
    lateinit var nameList:ArrayList<String>;
    lateinit var adapter: ArrayAdapter<String>
    var firstExecuteFlag=0
    var radioFlag = 1
    var nowDBID = "-1"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_detail_under_trail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        firstExecuteFlag=0
        resultLayout.visibility = View.INVISIBLE
        initAutoComplete()
        initBtn()

    }
    private fun showTableLayout(db_id:String, radioFlag:Int) {
        myDBHelper = MyDBHelper(context)
        //선택한게 평일이면, 공휴일이면
        val cursor = myDBHelper.getTimeTable(db_id, radioFlag)

        cursor.moveToFirst()
        val columnCount = cursor.columnCount
        val rowCount = cursor.count
        val activity = context as MainActivity
        activity.stationTimeTable.removeAllViewsInLayout()        //모두 제거하겠던 소리
        val activity2 = context as MainActivity
        activity2.stationTimeTable2.removeAllViewsInLayout()        //모두 제거하겠던 소리

        //column 타이틀 만들기 ( activity )
        val tablerow = TableRow(activity)
        val rowParam = TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT, columnCount.toFloat())
        tablerow.layoutParams = rowParam
        val viewParams = TableRow.LayoutParams(0, 100, 1f)
        if(cursor.getColumnName(0)=="1") {
            for (i in 1 until columnCount - 1) {
                val textView = TextView(activity)
                textView.layoutParams = viewParams
                textView.text = cursor.getColumnName(i)
                textView.setBackgroundColor(Color.LTGRAY)
                textView.textSize = 15.0f
                textView.gravity = Gravity.CENTER
                textView.gravity = Gravity.CENTER
                tablerow.addView(textView)
            }
            activity.stationTimeTable.addView(tablerow)
        }

        //column 타이틀 만들기 ( activity2 )
        val tablerow2 = TableRow(activity)
        val rowParam2 = TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT, columnCount.toFloat())
        tablerow2.layoutParams = rowParam2
        val viewParams2 = TableRow.LayoutParams(0, 100, 1f)
        if(cursor.getColumnName(0)=="1") {
            for (i in 1 until columnCount - 1) {
                val textView = TextView(activity)
                textView.layoutParams = viewParams2
                textView.text = cursor.getColumnName(i)
                textView.setBackgroundColor(Color.LTGRAY)
                textView.textSize = 15.0f
                textView.gravity = Gravity.CENTER
                tablerow2.addView(textView)
            }
            activity.stationTimeTable2.addView(tablerow2)
        }


        //실제 레코드 읽어오기
        do{
            val row = TableRow(activity)
            row.layoutParams = rowParam
            if(cursor.getString(0)=="1") {
                for (i in 1 until columnCount) {
                    val textView = TextView(activity)
                    textView.layoutParams = viewParams
                    if(i==columnCount-1) {
                        var tmp = cursor.getString(i) + " 행"
                        textView.text = tmp
                    }
                    else{
                        textView.text = cursor.getString(i)
                    }
                    textView.textSize = 13.0f
                    textView.setTag(i) // 현재가 몇번째 값인지 tag 걸어줌
                    textView.gravity = Gravity.CENTER
                    row.addView(textView)
                }
                activity.stationTimeTable.addView(row)
            }
            else if(cursor.getString(0)=="2") {
                for (i in 1 until columnCount) {
                    val textView = TextView(activity)
                    textView.layoutParams = viewParams2
                    textView.text = cursor.getString(i)
                    textView.textSize = 13.0f
                    textView.setTag(i) // 현재가 몇번째 값인지 tag 걸어줌
                    textView.gravity = Gravity.CENTER
                    row.addView(textView)
                }
                activity2.stationTimeTable2.addView(row)
            }
        }while(cursor.moveToNext())

    }

    private fun initBtn() {
        searchBnt.setOnClickListener {
            if(autoCompleteText.text.toString() != ""){
                val tmpText = autoCompleteText.text.toString()
                try{
                    var sName = tmpText.split("(")[0]
                    var sLineNum = tmpText.split("(")[1]
                    sLineNum = sLineNum.substring(0, sLineNum.length-1)

                    myDBHelper = MyDBHelper(context)
                    val station = myDBHelper.getStationDetailInfo(sName, sLineNum)
                    if(!station.sId.equals("")) {
                        firstExecuteFlag=1
                        resultLayout.visibility = View.VISIBLE
                        stationName.setText(station.sName)
                        stationLineNum.setText(station.sLineNum)
                        stationAddress.setText(station.sAddress)
                        stationPhoneNum.setText(station.sPhoneNum)
                        stationMapURL.setText(station.sMapURL)

                        nowDBID = myDBHelper.SIDtoDBID(station.sId.toInt())
                        showTableLayout(nowDBID, radioFlag)
                    }
                }
                catch (e: CursorIndexOutOfBoundsException){
                    Toast.makeText(context, "결과를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
                }
                catch (e: StringIndexOutOfBoundsException){
                    Toast.makeText(context, "결과를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
                }
                catch (e: IndexOutOfBoundsException){
                Toast.makeText(context, "결과를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
                }
            }
        }
        radio_group.setOnCheckedChangeListener { group, checkedId ->
            if(!nowDBID.equals("-1")) {
                if (checkedId == R.id.radioButton1) {
                    //평일
                    radioFlag = 1
                    showTableLayout(nowDBID, radioFlag)
                } else if (checkedId == R.id.radioButton2) {
                    //주말
                    radioFlag = 2
                    showTableLayout(nowDBID, radioFlag)
                }
            }
        }
    }

    private fun initAutoComplete() {
        myDBHelper = MyDBHelper(context)
        nameList = myDBHelper.getAllStationName()
        adapter = ArrayAdapter(
            this!!.context!!,
            android.R.layout.simple_dropdown_item_1line,
            nameList
        )
        autoCompleteText.setAdapter(adapter)
    }

}
