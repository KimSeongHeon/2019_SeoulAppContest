package com.project.seoul_project

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.gson.JsonParser
import com.project.seoul_project.MainActivity.Companion.Using_train
import com.project.seoul_project.MainActivity.Companion.user_info
import kotlinx.android.synthetic.main.activity_chat.*
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.net.URL
import java.text.SimpleDateFormat


class ChatActivity : AppCompatActivity() {

    var terminal_time = ""
    lateinit var ref:DatabaseReference
    var chat_list = ArrayList<ChatData>()
    var line_list = ArrayList<All_Station_Info>()
    lateinit var terminal_info:All_Station_Info
    lateinit var adapter:chat_recycler_adapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        init()
        val p_thread = Thread(object : Runnable {
            override fun run() {
                try {
                    Log.v("쓰레드", "동작 중")
                    parsing_for_find(terminal_info.station_cd,1)
                }
                catch (e: InterruptedException) {
                    e.printStackTrace()
                } finally {
                    Log.v("쓰레드", "죽었음")
                }
            }
        })
        p_thread.start()
        p_thread.join()
        init_database()
        set_text()
        init_adapter()
        init_listener()
    }
    fun init(){
        line_list = match_str_Array(Using_train.subwawyNm)
        if(line_list.find { it.station_num ==  Using_train.statnTnm} != null){
            terminal_info = line_list.find { it.station_num ==  Using_train.statnTnm}!!
        }
        else{
            terminal_info = All_Station_Info(Using_train.subwawyNm,Using_train.statnTnm,"1","0")
        }
    }
    fun init_adapter(){
        val chat_rlayoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL,false)
        adapter = chat_recycler_adapter(chat_list, user_info!!)
        chat_view.layoutManager = chat_rlayoutManager
        chat_view.adapter = adapter
    }
    fun init_database(){
        ref = MainActivity.rdb.child(MainActivity.Date_str)
            .child(MainActivity.Using_train.subwawyNm)
            .child(MainActivity.Using_train.trainNo + "_" + MainActivity.Using_train.statnTnm+ "_" + terminal_time)
            .child("Chat")
        var query = ref.orderByKey()
        query.addValueEventListener(object:ValueEventListener{
            override fun onDataChange(p0: DataSnapshot) {
                chat_list.clear()
                for(data in p0.children){
                    val cdata = data.getValue(ChatData::class.java)
                    if (cdata != null)
                        chat_list.add(cdata)
                }
                chat_view.scrollToPosition(chat_list.size - 1)
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }
    fun init_listener() {
        chat_sent.setOnClickListener {

            if (chat_edit.text.toString() != "") {
                var time = System.currentTimeMillis()
                var day_time = SimpleDateFormat("hh시 mm분 ss초")
                var str_time = day_time.format(time)
                val chat = ChatData(chat_edit.text.toString(), user_info!!.id,str_time) //ChatDTO를 이용하여 데이터를 묶는다.
                ref.child(str_time).setValue(chat) // 데이터 푸쉬
                chat_edit.setText("") //입력창 초기화
                chat_view.scrollToPosition(chat_list.size - 1)
            }


        }
    }
    fun set_text(){
        var status_str = ""
        var updnLine_str = ""
        var directat_str = ""
        when(Using_train.directAt){
            1->directat_str = "급행"
            0->directat_str = "일반"
        }
        when(Using_train.status){
            0-> status_str = "진입"
            1->status_str = "도착"
            else->status_str =  "출발"
        }
        if(Using_train.subwawyNm == "2호선"){
            when(Using_train.updnLine){
                0->updnLine_str = "외선";
                1->updnLine_str = "내선";
            }
        }
        else{
            when(Using_train.updnLine){
                0->updnLine_str = "상행선";
                1->updnLine_str = "하행선";
            }
        }
        when(Using_train.subwawyNm){
            "1호선"->chat_subwaynm_imageview.setImageResource(R.drawable.line1)
            "2호선"->chat_subwaynm_imageview.setImageResource(R.drawable.line2)
            "3호선"->chat_subwaynm_imageview.setImageResource(R.drawable.line3)
            "4호선"->chat_subwaynm_imageview.setImageResource(R.drawable.line4)
            "5호선"->chat_subwaynm_imageview.setImageResource(R.drawable.line5)
            "6호선"->chat_subwaynm_imageview.setImageResource(R.drawable.line6)
            "7호선"->chat_subwaynm_imageview.setImageResource(R.drawable.line7)
            "8호선"->chat_subwaynm_imageview.setImageResource(R.drawable.line8)
            "9호선"->chat_subwaynm_imageview.setImageResource(R.drawable.line9)
            "경춘선"->chat_subwaynm_imageview.setImageResource(R.drawable.kyungchun)
            "분당선"->chat_subwaynm_imageview.setImageResource(R.drawable.bundang)
            "신분당선"->chat_subwaynm_imageview.setImageResource(R.drawable.newbundang)
            "경의중앙선"->chat_subwaynm_imageview.setImageResource(R.drawable.kyungyui)
            "인천선"->chat_subwaynm_imageview.setImageResource(R.drawable.incheon1)
            "인천2호선"->chat_subwaynm_imageview.setImageResource(R.drawable.incheon2)
            "공항철도"->chat_subwaynm_imageview.setImageResource(R.drawable.airport)
            "용인경전철"->chat_subwaynm_imageview.setImageResource(R.drawable.yongin)
            "우의신설경전철"->chat_subwaynm_imageview.setImageResource(R.drawable.ui)
            "의정부경전철"->chat_subwaynm_imageview.setImageResource(R.drawable.uijeongbu)
            "경강선"->chat_subwaynm_imageview.setImageResource(R.drawable.kyungkang)
            "서해선"->chat_subwaynm_imageview.setImageResource(R.drawable.west)
            "수인선"->chat_subwaynm_imageview.setImageResource(R.drawable.suin)
        }
        chat_train_direction_text.setText(updnLine_str)
        chat_train_dest_text.setText(Using_train.statnTnm)
        chat_trainno_text.setText(Using_train.trainNo)
        chat_directat_textview.setText(directat_str)
    }
    fun exception_process():Int{
        var updnLine_int = 0
        var str:String = ""
        //9호선. 공항철도 상 하행 반대로.. 나중에 데이터 수정되면 고칠것
        if(Using_train.subwawyNm == "9호선" || Using_train.subwawyNm == "공항철도"){
            when(Using_train.updnLine){
                0->updnLine_int = 2
                1->updnLine_int = 1
            }
        }
        else{
            when(Using_train.updnLine){
                0->updnLine_int = 1
                1->updnLine_int = 2
            }
        }
        if(Using_train.subwawyNm=="2호선"){
            if(Using_train.statnTnm.contains("성수")){
                terminal_info.station_cd = "0211"
            }
        }
        if(Using_train.subwawyNm=="6호선"){
            if(Using_train.statnTnm.contains("응암")){
                terminal_info.station_cd = "2611"
            }

        }
        if(Using_train.subwawyNm == "7호선"){
            if(Using_train.statnTnm == "장암"){
                terminal_info.station_cd = "2712"
            }
        }
        return updnLine_int
    }
    fun parsing_for_find(str:String,flag:Int){
        var holiday_int:Int = 0
        if(!isHoliday.isHoliday(MainActivity.Date_str)){
            holiday_int=1
        }
        else{
            if(isHoliday.getDayOfWeek(MainActivity.Date_str) == 7){
                holiday_int = 2
            }
            else{
                holiday_int = 3
            }
        }
        var updnLine_int = exception_process()
        var url: URL
        var key: String = "6b48764572726c6132326147475243"
        try {
            url = URL(
                "http://openAPI.seoul.go.kr:8088/" + key + "/json/SearchSTNTimeTableByIDService/0/500/" + str +"/"+ holiday_int.toString() +"/"+updnLine_int.toString()
            )
            Log.d("URL",url.toString())
            var Is: InputStream = url.openStream()
            var rd: BufferedReader = BufferedReader(InputStreamReader(Is, "UTF-8"))
            var line: String? = null
            var page = ""
            line = rd.readLine()
            Log.v("line", line)
            while (line != null) {
                page += line
                line = rd.readLine()
            }
            Log.v("parsing_for_find_page", page)
            val parser = JsonParser()
            val json = parser.parse(page).asJsonObject
            val json_part = json.getAsJsonObject("SearchSTNTimeTableByIDService")
            val jarr = json_part.getAsJsonArray("row").asJsonArray
            for (i in 0 until jarr.size()) {
                if(Using_train.subwawyNm != "2호선"){
                    if(jarr[i].asJsonObject.get("TRAIN_NO").asString.replace("[^0-9]".toRegex(), "").toInt() != Using_train.trainNo.replace("[^0-9]".toRegex(), "").toInt()){
                        continue
                    }
                    else{
                      if(flag == 1){
                            terminal_time = jarr[i].asJsonObject.get("ARRIVETIME").asString
                            Log.d("terminal_time",terminal_time.toString())
                        }
                    }
                }
                else{
                    if(jarr[i].asJsonObject.get("ARRIVETIME").asString!="00:00:00") {
                        if(jarr[i].asJsonObject.get("TRAIN_NO").asString.replace("[^0-9]".toRegex(), "").toInt() % 1000 != Using_train.trainNo.replace("[^0-9]".toRegex(), "").toInt() % 1000){
                            continue
                        }
                        else{
                           if(flag == 1){
                                terminal_time = jarr[i].asJsonObject.get("ARRIVETIME").asString
                                Log.d("terminal_time",terminal_time.toString())
                            }
                        }
                    }


                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    fun match_str_Array(str: String): ArrayList<All_Station_Info> {
        when (str) {
            "1호선" -> return MainActivity.Line1
            "2호선" -> return MainActivity.Line2
            "3호선" -> return MainActivity.Line3
            "4호선" -> return MainActivity.Line4
            "5호선" -> return MainActivity.Line5
            "6호선" -> return MainActivity.Line6
            "7호선" -> return MainActivity.Line7
            "8호선" -> return MainActivity.Line8
            "9호선" -> return MainActivity.Line9
            "경의중앙선" -> return MainActivity.Line_kyunyui
            "분당선" -> return MainActivity.Line_Bundang
            "경춘선" -> return MainActivity.Line_Kyungchun
            "경강선" -> return MainActivity.Line_KyungKang
            "수인선" -> return MainActivity.Line_Suin
            "인천선" -> return MainActivity.Line_Incheon1
            "인천2호선" -> return MainActivity.Line_Incheon2
            "공항철도" -> return MainActivity.Line_AirPort
            "신분당선" -> return MainActivity.Line_NewBundang
            "용인경전철" -> return MainActivity.Line_YongIn
            "의정부경전철" -> return MainActivity.Line_UijeongBu
            "우이신설경전철" -> return MainActivity.Line_UI
            "서해선" -> return MainActivity.Line_WestSea
        }
        return ArrayList<All_Station_Info>()
    }
}
