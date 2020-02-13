package com.project.seoul_project

import android.app.Activity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Window
import kotlinx.android.synthetic.main.activity_select_train.*

class SelectTrain : Activity() {

    var train_list = ArrayList<Train_Info>()
    var search_list = ArrayList<All_Station_Info>()
    lateinit var search_adapter:search_recycler_adapter
    lateinit var inner_adapter:inner_recycler_adapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.activity_select_train)
        initlistner()
    }

    fun initlistner() {
        search_edittext.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                search_list.clear()
                Log.v("afterTextChanged","play")
                val str = s.toString()
                add_list(str)
                search_init_adapter()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }
        })
        confirm_button.setOnClickListener{
            finish()
        }

    }
    fun search_init_adapter(){
        val search_rlayoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL,false)
        search_adapter = search_recycler_adapter(search_list)
        search_recyview.layoutManager = search_rlayoutManager
        search_recyview.adapter = search_adapter

    }

    // 불필요
    fun add_list(str:String){
        search_list.addAll(MainActivity.Line1.filter {
            SoundSearcher.matchString(it.station_num,str)
        })
        search_list.addAll(MainActivity.Line2.filter {
            SoundSearcher.matchString(it.station_num,str)
        })
        search_list.addAll(MainActivity.Line3.filter {
            SoundSearcher.matchString(it.station_num,str)
        })
        search_list.addAll(MainActivity.Line4.filter {
            SoundSearcher.matchString(it.station_num,str)
        })
        search_list.addAll(MainActivity.Line5.filter {
            SoundSearcher.matchString(it.station_num,str)
        })
        search_list.addAll(MainActivity.Line6.filter {
            SoundSearcher.matchString(it.station_num,str)
        })
        search_list.addAll(MainActivity.Line7.filter {
            SoundSearcher.matchString(it.station_num,str)
        })
        search_list.addAll(MainActivity.Line8.filter {
            SoundSearcher.matchString(it.station_num,str)
        })
        search_list.addAll(MainActivity.Line9.filter {
            SoundSearcher.matchString(it.station_num,str)
        })
        search_list.addAll(MainActivity.Line_AirPort.filter {
            SoundSearcher.matchString(it.station_num,str)
        })
        search_list.addAll(MainActivity.Line_Bundang.filter {
            SoundSearcher.matchString(it.station_num,str)
        })
        search_list.addAll(MainActivity.Line_Kyungchun.filter {
            SoundSearcher.matchString(it.station_num,str)
        })
        search_list.addAll(MainActivity.Line_kyunyui.filter {
            SoundSearcher.matchString(it.station_num,str)
        })
        search_list.addAll(MainActivity.Line_NewBundang.filter {
            SoundSearcher.matchString(it.station_num,str)
        })
        search_list.addAll(MainActivity.Line_Suin.filter {
            SoundSearcher.matchString(it.station_num,str)
        })





    }
}
