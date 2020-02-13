package com.project.seoul_project

import android.os.Bundle
import android.os.RemoteException
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TabHost
import com.google.firebase.database.*
import com.google.gson.JsonParser
import kotlinx.android.synthetic.main.activity_receive_.*
import org.altbeacon.beacon.*
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.net.URL
import java.util.*
import kotlin.collections.ArrayList

class Receive_Activity : AppCompatActivity(),BeaconConsumer {
    protected val TAG = "RangingActivity"
    var line_list = ArrayList<All_Station_Info>()
    lateinit var rdb: DatabaseReference
    lateinit var rds :DatabaseReference
    lateinit var Using_train:Train_Info
    lateinit var beaconManager: BeaconManager
    var search_list = ArrayList<Beacon_info_distance>()
    var seat_list = ArrayList<Seat_info>()
    lateinit var seat_adapter:Seat_recycler_adapter
    lateinit var recieve_adapter:Recieve_recycler_adapter
    var beacon_list = ArrayList<Beacon_info>()
    var terminal_time = ""
    lateinit var terminal_info:All_Station_Info
    val MAX = 15
    var update_try = 0

    inner class descending:Comparator<Beacon_info_distance>{
        override fun compare(o1: Beacon_info_distance?, o2: Beacon_info_distance?): Int {
            //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            return o1!!.distance.compareTo(o2!!.distance)
        }

    }
    override fun onBeaconServiceConnect() { // 비콘 관련 작업
        beaconManager.removeAllRangeNotifiers()
        beaconManager.addRangeNotifier(object : RangeNotifier {
            override fun didRangeBeaconsInRegion(p0: MutableCollection<Beacon>?, p1: Region?) {
                Log.d("update_try",update_try.toString())
                if(update_try >= MAX){
                    remove_no_compliance()
                    update_try = 0
                    recieve_adapter.notifyDataSetChanged()

                }
                if (p0!!.size > 0) {
                    update_try++
                    receive_noinfo_textview.visibility = View.INVISIBLE
                    recieve_search_recyview.visibility = View.VISIBLE
                    Log.i(TAG, "The first beacon I see is about "+p0!!.iterator().next().getDistance()+" meters away.");
                    Log.i(TAG, "The first beacon UUID "+p0!!.iterator().next().id1+" ");
                    var temp_array = ArrayList<Beacon>()
                    temp_array.addAll(p0!!)
                    arrange_beaconlist(temp_array)
                    var des = descending()
                    Collections.sort(search_list,descending())
                    recieve_adapter.notifyDataSetChanged()
                }
                else{
                    update_try++
                    if(search_list.size == 0){
                        receive_noinfo_textview.visibility = View.VISIBLE
                        recieve_search_recyview.visibility = View.INVISIBLE
                    }
                }
            }

        })
        try {
            beaconManager.startRangingBeaconsInRegion(Region("myRangingUniqueId", null, null, null))
        }
        catch (e: RemoteException) {

        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_receive_)
        init_tab()
        init()
        load_database()
        init_listner()
        initadapter()
        initView()
        set_text()
    }
    fun remove_no_compliance(){
        for(i in 0..search_list.size-1){
            if(search_list.get(i).isUpdate){
                search_list.get(i).isUpdate = false
            }
            else{
                search_list.get(i).distance = Float.MAX_VALUE
            }
        }
    }
    fun arrange_beaconlist(p0: ArrayList<Beacon>)
    {
        //search_list.clear()
        for(i in 0..p0.size-1){
            var uuid = p0[i].id1.toString()
            Log.d("uuid",uuid)
            var temp = beacon_list.find { it.UUID == uuid }
            if(temp != null){
                var temp_list = search_list.find {
                    it.UUID == uuid
                }
                temp_list!!.distance = p0[i].distance.toFloat()
                temp_list!!.isUpdate = true
            }
        }
    }
    fun load_database(){
        if( FirebaseDatabase.getInstance().getReference("Train").child(MainActivity.Date_str).child(Using_train.subwawyNm)
                .child(Using_train.trainNo + "_" + Using_train.statnTnm+ "_" + terminal_time).child("BeaconList") == null){
            Log.d("Firebase is NULL","NULL")
        }
        else{
            rdb = FirebaseDatabase.getInstance().getReference("Train").child(MainActivity.Date_str).child(Using_train.subwawyNm)
                .child(Using_train.trainNo + "_" + Using_train.statnTnm+ "_" + terminal_time).child("BeaconList")
            rdb.addValueEventListener(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {

                }
                override fun onDataChange(p0: DataSnapshot) {
                    beacon_list.clear()
                    search_list.clear()
                    for (data in p0.children) {
                        val pdata = data.getValue(Beacon_info::class.java)
                        if(pdata != null && pdata.reserve == ""){
                            beacon_list.add(pdata)
                            search_list.add(Beacon_info_distance(pdata.UUID,pdata.User_ID,pdata.dest,pdata.trainno,pdata.seat,
                                Float.MAX_VALUE,false))
                        }
                    }
                    recieve_user_textview.setText("현재 블루투스를 이용하여 자리 공유중인 좌석 수는 "+beacon_list.size + " 개 입니다.")
                    Log.d("beacon",beacon_list.size.toString())
                }
            })
        }
        if( FirebaseDatabase.getInstance().getReference("Train").child(MainActivity.Date_str).child(Using_train.subwawyNm)
                .child(Using_train.trainNo + "_" + Using_train.statnTnm+ "_" + terminal_time).child("SeatList") == null){
            Log.d("Firebase is NULL","NULL")
        }
        else{
            rds = FirebaseDatabase.getInstance().getReference("Train").child(MainActivity.Date_str).child(Using_train.subwawyNm)
                .child(Using_train.trainNo + "_" + Using_train.statnTnm+ "_" + terminal_time).child("SeatList")
            rds.addValueEventListener(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {

                }
                override fun onDataChange(p0: DataSnapshot) {
                    seat_list.clear()
                    for (data in p0.children) {
                        val pdata = data.getValue(Seat_info::class.java)
                        if(pdata != null && pdata.reserve == ""){
                            seat_list.add(pdata)
                        }
                    }
                    seat_adapter.notifyDataSetChanged()
                    seat_textview.setText("현재 직접 입력하여 자리 공유중인 좌석 수는 "+ seat_list.size + " 개 입니다.")
                }
            })
        }
    }
    fun initadapter(){
        val receive_manager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL,false)
        recieve_adapter = Recieve_recycler_adapter(search_list)
        recieve_search_recyview.layoutManager = receive_manager
        recieve_search_recyview.adapter = recieve_adapter
        val seat_manager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL,false)
        seat_adapter = Seat_recycler_adapter(seat_list)
        recieve_seat_search_recyview.layoutManager = seat_manager
        recieve_seat_search_recyview.adapter = seat_adapter
    }
    fun init(){
        val intent = getIntent()
        Using_train = intent.getSerializableExtra("Using_train") as Train_Info
        Log.d("Using_train2",Using_train.trainNo)
        line_list = match_str_Array(Using_train.subwawyNm)
        if(line_list.find { it.station_num ==  Using_train.statnTnm} != null){
            Log.d("not null","not")
            terminal_info = line_list.find { it.station_num ==  Using_train.statnTnm}!!
        }
        else{
            Log.d(" null","null")
            terminal_info = All_Station_Info(Using_train.subwawyNm,Using_train.statnTnm,"1","0")
        }
        val p_thread = Thread(object : Runnable {
            override fun run() {
                try {
                    Log.v("쓰레드", "동작 중")
                    exception_process()
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

        beaconManager = BeaconManager.getInstanceForApplication(this);
        beaconManager.beaconParsers.add(BeaconParser().setBeaconLayout("m:2-3=beac,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25"))
        beaconManager.bind(this);
    }
    fun init_listner(){
        recieve_confirm_button.setOnClickListener {
            beaconManager.removeAllRangeNotifiers()
            finish()
        }
    }
    fun parsing_for_find(str: String, flag: Int) {
        var holiday_int: Int = 0
        if (!isHoliday.isHoliday(MainActivity.Date_str)) {
            holiday_int = 1
        } else {
            if (isHoliday.getDayOfWeek(MainActivity.Date_str) == 7) {
                holiday_int = 2
            } else {
                holiday_int = 3
            }
        }
        var updnLine_int = exception_process()
        var url: URL
        var key: String = "6b48764572726c6132326147475243"
        try {
            url = URL(
                "http://openAPI.seoul.go.kr:8088/" + key + "/json/SearchSTNTimeTableByIDService/0/500/" + str + "/" + holiday_int.toString() + "/" + updnLine_int.toString()
            )
            Log.d("URL", url.toString())
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
                if (Using_train.subwawyNm != "2호선") {
                    if (jarr[i].asJsonObject.get("TRAIN_NO").asString.replace(
                            "[^0-9]".toRegex(),
                            ""
                        ).toInt() != Using_train.trainNo.replace("[^0-9]".toRegex(), "").toInt()
                    ) {
                        continue
                    } else {
                        if (flag == 0) {

                        } else if (flag == 1) {
                            terminal_time = jarr[i].asJsonObject.get("ARRIVETIME").asString
                            Log.d("terminal_time", terminal_time.toString())
                        }
                    }
                } else {
                    if (jarr[i].asJsonObject.get("ARRIVETIME").asString != "00:00:00") {
                        if (jarr[i].asJsonObject.get("TRAIN_NO").asString.replace(
                                "[^0-9]".toRegex(),
                                ""
                            ).toInt() % 1000 != Using_train.trainNo.replace("[^0-9]".toRegex(), "").toInt() % 1000
                        ) {
                            continue
                        } else {
                            if (flag == 0) {
                            } else if (flag == 1) {
                                terminal_time = jarr[i].asJsonObject.get("ARRIVETIME").asString
                                Log.d("terminal_time", terminal_time.toString())
                            }
                        }
                    }


                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
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
    fun set_text(){
        var status_str = ""
        var updnLine_str = ""
        var directat_str = ""
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
        when(Using_train.directAt){
            1->directat_str = "급행"
            0->directat_str = "일반"
        }
        when(Using_train.subwawyNm){
            "1호선"->recive_subwaynm_imageview.setImageResource(R.drawable.line1)
            "2호선"->recive_subwaynm_imageview.setImageResource(R.drawable.line2)
            "3호선"->recive_subwaynm_imageview.setImageResource(R.drawable.line3)
            "4호선"->recive_subwaynm_imageview.setImageResource(R.drawable.line4)
            "5호선"->recive_subwaynm_imageview.setImageResource(R.drawable.line5)
            "6호선"->recive_subwaynm_imageview.setImageResource(R.drawable.line6)
            "7호선"->recive_subwaynm_imageview.setImageResource(R.drawable.line7)
            "8호선"->recive_subwaynm_imageview.setImageResource(R.drawable.line8)
            "9호선"->recive_subwaynm_imageview.setImageResource(R.drawable.line9)
            "경춘선"->recive_subwaynm_imageview.setImageResource(R.drawable.kyungchun)
            "분당선"->recive_subwaynm_imageview.setImageResource(R.drawable.bundang)
            "신분당선"->recive_subwaynm_imageview.setImageResource(R.drawable.newbundang)
            "경의중앙선"->recive_subwaynm_imageview.setImageResource(R.drawable.kyungyui)
            "인천선"->recive_subwaynm_imageview.setImageResource(R.drawable.incheon1)
            "인천2호선"->recive_subwaynm_imageview.setImageResource(R.drawable.incheon2)
            "공항철도"->recive_subwaynm_imageview.setImageResource(R.drawable.airport)
            "용인경전철"->recive_subwaynm_imageview.setImageResource(R.drawable.yongin)
            "우의신설경전철"->recive_subwaynm_imageview.setImageResource(R.drawable.ui)
            "의정부경전철"->recive_subwaynm_imageview.setImageResource(R.drawable.uijeongbu)
            "경강선"->recive_subwaynm_imageview.setImageResource(R.drawable.kyungkang)
            "서해선"->recive_subwaynm_imageview.setImageResource(R.drawable.west)
            "수인선"->recive_subwaynm_imageview.setImageResource(R.drawable.suin)
        }
        recieve_train_direction_text.setText(updnLine_str)
        recieve_train_dest_text.setText(Using_train.statnTnm)
        recieve_trainno_text.setText(Using_train.trainNo)
        recieve_directat_textview.setText(directat_str)
    }
    fun initView(){
        var imgAndroid = findViewById<ImageView>(R.id.radio_imagebutton)
        imgAndroid.visibility = View.VISIBLE
        var anim = AnimationUtils.loadAnimation(this,R.anim.sharing)
        imgAndroid.animation = anim
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
    fun init_tab(){
        val tabHost1 = findViewById<View>(R.id.Receive_tabHost1) as TabHost
        tabHost1.setup()
        tabHost1.setOnTabChangedListener{

        }
        // 첫 번째 Tab. (탭 표시 텍스트:"TAB 1"), (페이지 뷰:"content1")
        val ts1 = tabHost1.newTabSpec("Tab Spec 1")
        ts1.setContent(R.id.receive_content1)
        ts1.setIndicator("거리로 찾기")
        tabHost1.addTab(ts1)


        // 두 번째 Tab. (탭 표시 텍스트:"TAB 2"), (페이지 뷰:"content2")
        val ts2 = tabHost1.newTabSpec("Tab Spec 2")
        ts2.setContent(R.id.receive_content2)
        ts2.setIndicator("객실 번호로 찾기")
        tabHost1.addTab(ts2)
    }
}
