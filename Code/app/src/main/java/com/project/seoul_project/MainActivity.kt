package com.project.seoul_project

import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.NavigationView
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.Toast
import com.google.firebase.database.*
import com.google.gson.JsonParser
import com.kakao.auth.Session
import com.kakao.usermgmt.UserManagement
import com.kakao.usermgmt.callback.LogoutResponseCallback
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.main_nav_header.*
import org.altbeacon.beacon.Beacon
import org.altbeacon.beacon.BeaconParser
import org.altbeacon.beacon.BeaconTransmitter
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList




class MainActivity : AppCompatActivity(), LocationListener,NavigationView.OnNavigationItemSelectedListener{

    lateinit var lm: LocationManager
    var location: Location? = null
    lateinit var provider: String
    lateinit var longitude: String
    lateinit var latitude: String
    var distance_to_zero: Double = Double.MIN_VALUE
    var station_array = ArrayList<near_station_Info>()
    var exist_flag:Int = 0

    lateinit var backPressCloseHandler:BackPressCloseHandler
    var try_index: Int = 0
    var handler = Handler()
    var handler_r: Runnable = object : Runnable {
        override fun run() {
            Log.v("handler 작동", try_index++.toString())
            if (try_index <= 3) {
                handler.postDelayed(this, 750)
                update_location()
            } else
                handler.removeCallbacks(this)
        }
    }
    var place_thread = Thread(object : Runnable {
        override fun run() {
            try {
                if(location != null){
                    Log.v("쓰레드", "동작 중")
                    place_parsing()
                }
            } catch (e: InterruptedException) {
                e.printStackTrace()
            } finally {
                Log.v("쓰레드", "죽었음")
            }
        }
    })
    var updnLine = 2; //0은 상행 .. 1은 하행.. 2 는 알 수 없음(정차 중)
    // 호선 별 지하철 역 정보
    companion object{
        var Using_train: Train_Info = Train_Info("", "", 2, "", 0,"",-1,"")
        var sharing_terminal_time = ""
        var sharing_dest_time=""

        var Line1 = ArrayList<All_Station_Info>();
        var Line2 = ArrayList<All_Station_Info>();
        var Line3 = ArrayList<All_Station_Info>();
        var Line4 = ArrayList<All_Station_Info>();
        var Line5 = ArrayList<All_Station_Info>();
        var Line6 = ArrayList<All_Station_Info>();
        var Line7 = ArrayList<All_Station_Info>();
        var Line8 = ArrayList<All_Station_Info>();
        var Line9 = ArrayList<All_Station_Info>();
        var Line_kyunyui = ArrayList<All_Station_Info>();
        var Line_Kyungchun = ArrayList<All_Station_Info>();
        var Line_Bundang = ArrayList<All_Station_Info>();
        var Line_Suin = ArrayList<All_Station_Info>();
        var Line_Incheon1 = ArrayList<All_Station_Info>();
        var Line_Incheon2 = ArrayList<All_Station_Info>();
        var Line_AirPort = ArrayList<All_Station_Info>();
        var Line_NewBundang = ArrayList<All_Station_Info>();
        var Line_YongIn = ArrayList<All_Station_Info>();
        var Line_UijeongBu = ArrayList<All_Station_Info>();
        var Line_UI = ArrayList<All_Station_Info>();
        var Line_KyungKang = ArrayList<All_Station_Info>();
        var Line_WestSea = ArrayList<All_Station_Info>();
        //라인
        lateinit var beacon: Beacon
        lateinit var beaconParser:BeaconParser
        lateinit var beaconTransmitter: BeaconTransmitter
        lateinit var btManager:BluetoothManager
        var beaconlist:ArrayList<Beacon_info> = ArrayList<Beacon_info>()
        //비콘
        lateinit var rdb:DatabaseReference //비콘리스트를 담는..
        lateinit var rds:DatabaseReference//seatList를 담는..
        //데이터베이스
        var user_info:User_info? = null
        //sharing_flag
        var sharing_flag:Int = 0 //0 : 공유 x || 1 : 비콘 공유 || 2: 자리 공유
        //현재 시간
        var cal = Calendar.getInstance()
        var year = cal.get(Calendar.YEAR)
        var month = cal.get(Calendar.MONTH)+1
        var date = cal.get(Calendar.DATE)
        var hour = cal.get(Calendar.HOUR_OF_DAY)
        var Date_str = ""
        //알람
        lateinit var am: AlarmManager
        lateinit var sender:PendingIntent

    }

    override fun onBackPressed() {
        //super.onBackPressed()
        backPressCloseHandler.onBackPressed()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        startService(Intent(this,Remove_Service::class.java))
        Log.d("date_str", Date_str)
        Log.d("Hour",hour.toString() + month.toString() + date.toString())
        backPressCloseHandler = BackPressCloseHandler(this)
        calculate_date()
        init_database()
        load_station()
        initlistener()
        if( (getSystemService(Context.LOCATION_SERVICE) as LocationManager).isProviderEnabled(LocationManager.GPS_PROVIDER )){
            init_location()
            place_thread.start()
            place_thread.join()
            Log.v("thread exit","thread exit")
            handler.post(handler_r)
        }
        else{
            info_linear.visibility = View.INVISIBLE
            no_info_text.visibility = View.VISIBLE
            loading_text.visibility = View.INVISIBLE
        }

    }
    fun calculate_date(){
        if(hour <= 3){
            var month_str = ""; var date_str = ""
            var calendar = GregorianCalendar()
            calendar.add(Calendar.DATE,-1)
            var sdf = SimpleDateFormat("yyyyMMdd")
            var str = sdf.format(calendar.time)
            year = Integer.parseInt(str.substring(0, 4))
            month  = Integer.parseInt(str.substring(4, 6))
            date = Integer.parseInt(str.substring(6))
            if(month < 10){
                month_str = "0"+month.toString()
            }else{
                month_str = month.toString()
            }
            if(date < 10){
                date_str = "0"+date.toString()
            }
            else{
                date_str = date.toString()
            }
            Date_str = year.toString()+month_str+date_str
            Log.d("date_str", Date_str)
        }
        else{
            var month_str = ""; var date_str = ""
            if(month < 10){
                month_str = "0"+month.toString()
            }else{
                month_str = month.toString()
            }
            if(date < 10){
                date_str = "0"+date.toString()
            }
            else{
                date_str = date.toString()
            }
            Date_str = year.toString()+month_str+date_str
            Log.d("date_str", Date_str)
        }
    }
    fun init_database(){
        rdb = FirebaseDatabase.getInstance().getReference("Train")
        rds = FirebaseDatabase.getInstance().getReference("Train")
        MainActivity.rdb.addValueEventListener(object: ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onDataChange(p0: DataSnapshot) {
                //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                beaconlist.clear()
                for(data in p0.children){
                    val pdata = data.getValue(Beacon_info::class.java)
                    if (pdata != null)
                        beaconlist.add(pdata)
                }
            }

        })
    }
    fun match_str_Array(str: String): ArrayList<All_Station_Info> {
        when (str) {
            "1호선" -> return Line1
            "2호선" -> return Line2
            "3호선" -> return Line3
            "4호선" -> return Line4
            "5호선" -> return Line5
            "6호선" -> return Line6
            "7호선" -> return Line7
            "8호선" -> return Line8
            "9호선" -> return Line9
            "경의중앙선" -> return Line_kyunyui
            "분당선" -> return Line_Bundang
            "경춘선" -> return Line_Kyungchun
            //"경강선" -> return Line_KyungKang
            "수인선" -> return Line_Suin
            //"인천선" -> return Line_Incheon1
            //"인천2호선" -> return Line_Incheon2
            "공항철도" -> return Line_AirPort
            //"신분당선" -> return Line_NewBundang
            //"용인경전철" -> return Line_YongIn
            //"의정부경전철" -> return Line_UijeongBu
            //"우이신설경전철" -> return Line_UI
            //"서해선" -> return Line_WestSea
        }
        return ArrayList<All_Station_Info>()
    }

    fun load_station() {//모든 역 로딩
        Line1.clear();Line2.clear();Line3.clear();Line4.clear();Line5.clear();Line6.clear();Line7.clear();Line8.clear();Line9.clear();Line_YongIn.clear();
        Line_UijeongBu.clear();Line_UI.clear();Line_WestSea.clear();Line_Suin.clear();Line_NewBundang.clear();Line_Bundang.clear();Line_Kyungchun.clear();Line_KyungKang.clear();Line_kyunyui.clear();
        Line_AirPort.clear();Line_Incheon1.clear();Line_Incheon2.clear();
        Log.d("load_station","실행")
        try {
            val scan = Scanner(resources.openRawResource(R.raw.seoulmetro))
            var page = ""
            while (scan.hasNextLine()) {
                val line = scan.nextLine()
                page += line
            }
            Log.v("page", page)
            val parser = JsonParser()
            val json = parser.parse(page).asJsonObject
            val jarr = json.getAsJsonArray("DATA").asJsonArray
            var line_num = "";
            var station_nm = "";
            var fr_code = ""
            var station_cd = ""
            for (i in 0 until jarr.size()) {
                line_num = jarr[i].asJsonObject.get("line_num").asString
                station_nm = jarr[i].asJsonObject.get("station_nm").asString
                fr_code = jarr[i].asJsonObject.get("fr_code").asString
                station_cd = jarr[i].asJsonObject.get("station_cd").asString
                if(match_str_Array(line_num)!=null)
                    match_str_Array(line_num).add(All_Station_Info(line_num, station_nm, fr_code,station_cd))
            }
            Log.v("역 로딩 완료", "완료")
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    fun train_parsing() {//현재 이용중인 지하철 번호 찾기
        exist_flag = 0
        Using_train = Train_Info("", "", 2, "", 0,"",-1,"")
        var url: URL
        var key: String = "7266504a6f726c613130384f72494f50"
        var str = station_array[0].subwayNm
        try {
            url = URL(
                "http://swopenAPI.seoul.go.kr/api/subway/" + key + "/json/realtimePosition/0/99/" + str
            )
            var Is: InputStream = url.openStream()
            var rd: BufferedReader = BufferedReader(InputStreamReader(Is, "UTF-8"))
            var line: String? = null
            var page = ""
            line = rd.readLine()
            Log.v("line", line)
            while (line != null) {
                page += line
                Log.v("train_line", line)
                line = rd.readLine()
            }
            Log.v("train_page", page)
            val parser = JsonParser()
            val json = parser.parse(page).asJsonObject
            val jarr = json.getAsJsonArray("realtimePositionList").asJsonArray
            var subwawyNm: String = "";
            var trainNo: String = "";
            var updn = 3;
            var statnTnm: String = "";
            var directAt: Int = 0;
            var statnNm = "";var trainSttus = -1; var recptime:String=""
            for (i in 0 until jarr.size()) {
                subwawyNm = jarr[i].asJsonObject.get("subwayNm").asString
                trainNo = jarr[i].asJsonObject.get("trainNo").asString
                updn = jarr[i].asJsonObject.get("updnLine").asInt
                statnTnm = jarr[i].asJsonObject.get("statnTnm").asString
                directAt = jarr[i].asJsonObject.get("directAt").asInt
                statnNm = jarr[i].asJsonObject.get("statnNm").asString
                trainSttus = jarr[i].asJsonObject.get("trainSttus").asInt
                recptime = jarr[i].asJsonObject.get("recptnDt").asString
                if(statnTnm == "938") statnTnm = "중앙보훈병원"
                Log.v("statnNm", statnNm)
                if(station_array.size >= 2){
                    if (station_array[0].name == statnNm) {
                        if (updnLine == updn || updnLine == 2) {
                            exist_flag = 1
                            Using_train = Train_Info(subwawyNm, trainNo, updn, statnTnm, directAt,statnNm,trainSttus,recptime)
                            Log.v("extra_flag=1",subwawyNm+statnTnm+statnNm)
                            break;
                        }
                    } else if (station_array[1].name == statnNm) {
                        if (updnLine == updn || updnLine == 2) {
                            exist_flag = 2
                            Using_train = Train_Info(subwawyNm, trainNo, updn, statnTnm, directAt,statnNm,trainSttus,recptime)
                            Log.v("extra_flag=2",subwawyNm+statnTnm+statnNm)
                            break;
                        }
                    }
                    else{
                        //exist_flag = 0
                        Log.v("extra_flag=0",subwawyNm+statnTnm+statnNm)
                    }
                }
                else if(station_array.size == 1){
                    if(station_array[0].name == statnNm){
                        exist_flag = 1
                        Using_train = Train_Info(subwawyNm, trainNo, updn, statnTnm, directAt,statnNm,trainSttus,recptime)
                        Log.v("array_size 1 : extra_flag=1",subwawyNm+statnTnm+statnNm)
                        break;
                    }
                }
                else if(station_array.size == 0){
                    exist_flag = 0
                }

            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun place_parsing() { // 근처 지하철 역 탐색{
        station_array.clear()
            var url: URL
            var key: String = "716e576d47726c613833677a57674f"
            var transform = GeoTrans.convert(GeoTrans.GEO, GeoTrans.GRS80, GeoPoint(longitude.toDouble(), latitude.toDouble()))
            var convert_longitude = transform.getX().toString()
            var convert_latitude = transform.getY().toString()
            try {
                url = URL(
                    "http://swopenAPI.seoul.go.kr/api/subway/" + key + "/json/nearBy/0/5/" + convert_longitude + "/" + convert_latitude
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
                    Log.v("line", line)
                    line = rd.readLine()
                }
                Log.v("page", page)
                val parser = JsonParser()
                val json = parser.parse(page).asJsonObject
                val jarr = json.getAsJsonArray("stationList").asJsonArray
                var name: String = ""
                var convert_x = 0.toDouble();
                var convert_y = 0.toDouble()
                var subwayNm: String = ""
                name = jarr[0].asJsonObject.get("statnNm").asString
                subwayNm = jarr[0].asJsonObject.get("subwayNm").asString
                convert_x = jarr[0].asJsonObject.get("subwayXcnts").asDouble
                convert_y = jarr[0].asJsonObject.get("subwayYcnts").asDouble
                station_array.add(near_station_Info(name, subwayNm, GeoPoint(convert_x, convert_y)))
                for (i in 0 until jarr.size()) {
                    Log.d("지하철 이름",subwayNm)
                    subwayNm = jarr[i].asJsonObject.get("subwayNm").asString
                    convert_x = jarr[i].asJsonObject.get("subwayXcnts").asDouble
                    convert_y = jarr[i].asJsonObject.get("subwayYcnts").asDouble
                    if (subwayNm == station_array[0].subwayNm) {
                        name = jarr[i].asJsonObject.get("statnNm").asString
                        station_array.add(near_station_Info(name, subwayNm, GeoPoint(convert_x, convert_y)))
                    }
                }
                distance_to_zero = GeoTrans.getDistancebyGrs80(station_array[0].geoPoint, transform).toDouble()
                //Log.v("station_array",station_array[0].name + "/ "+station_array[1].name+"/"+station_array[3].name+"/"+station_array[4].name+"/")
            } catch (e: Exception) {
                e.printStackTrace()
            }


    }

    fun init_location() {
        Log.v("init_location", "init_location");
        lm = getSystemService(Context.LOCATION_SERVICE) as LocationManager
            try {
                if (lm.getLastKnownLocation(LocationManager.GPS_PROVIDER) == null) {
                    if (lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER) == null) {
                        Toast.makeText(this,"GPS를 사용할 수 없습니다. GPS를 확인해주세요",Toast.LENGTH_SHORT).show()
                        return
                    } else {
                        location = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                        lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 15000, Float.MAX_VALUE, this);
                    }
                } else {
                    location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                    lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 15000, Float.MAX_VALUE, this);
                }
                provider = location!!.provider
                longitude = location!!.longitude.toString()
                latitude = location!!.latitude.toString()
                Log.v("변환전", longitude + "," + latitude)
                Log.v(
                    "변환후",
                    GeoTrans.convert(
                        GeoTrans.GEO,
                        GeoTrans.GRS80,
                        GeoPoint(longitude.toDouble(), latitude.toDouble())
                    ).getX().toString() + "  / " + GeoTrans.convert(
                        GeoTrans.GEO,
                        GeoTrans.GRS80,
                        GeoPoint(longitude.toDouble(), latitude.toDouble())
                    ).getY().toString()
                )

            } catch (e: SecurityException) {
                Log.v("오류","오류")
                e.printStackTrace();
            }


    }

    fun initlistener() {
        navigationView.setNavigationItemSelectedListener(this)
        renew_button.setOnClickListener{
            if(!((getSystemService(Context.LOCATION_SERVICE) as LocationManager).isProviderEnabled(LocationManager.GPS_PROVIDER))) {
                val gpsOptionIntent = Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(gpsOptionIntent)
            }
            else{
                if(sharing_flag == 0){
                    init_location()
                    val p_thread = Thread(object : Runnable {
                        override fun run() {
                            try {
                                if(location != null){
                                    Log.v("쓰레드", "동작 중")
                                    place_parsing()
                                }
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
                    try_index = 0
                    handler.post(handler_r)
                }
                else{
                    val builder = AlertDialog.Builder(this)
                    builder.setMessage("현재 공유 중인 자리가 있어 할 수 없습니다. 열차를 잘못선택하셨으면 공유를 취소한 후 다시 이용해주세요.")
                        .setTitle("공유 중인 자리 있음")
                        .setIcon(R.drawable.abc_ic_star_black_48dp)
                    builder.setPositiveButton("확인"){_, _ ->
                    }
                    builder.show()
                }
            }
        }
        write_button.setOnClickListener{
            if(sharing_flag == 0){
                val intent = Intent(this,SelectTrain::class.java)
                startActivityForResult(intent,1)
            }
            else{
                val builder = AlertDialog.Builder(this)
                builder.setMessage("현재 공유 중인 자리가 있어 할 수 없습니다. 열차를 잘못선택하셨으면 공유를 취소한 후 다시 이용해주세요.")
                    .setTitle("공유 중인 자리 있음")
                    .setIcon(R.drawable.abc_ic_star_black_48dp)
                builder.setPositiveButton("확인"){_, _ ->
                }
                builder.show()
            }
        }
        share_imagebutton.setOnClickListener {
            if(sharing_flag != 0){
                val builder = AlertDialog.Builder(this)
                builder.setMessage("현재 공유 중인 자리가 있어 다시 공유 할 수 없습니다. 공유를 취소한 후 다시 이용해주세요.")
                    .setTitle("공유 할 수 없음")
                    .setIcon(R.drawable.abc_ic_star_black_48dp)
                builder.setPositiveButton("확인"){_, _ ->
                }
                builder.show()
            }
            else{
                if(Using_train.trainNo!=""){
                        val intent = Intent(this,ShareActivity::class.java)
                        intent.putExtra("Using_train",Using_train)
                        startActivityForResult(intent,2)
                }
                else{
                    val builder = AlertDialog.Builder(this)
                    builder.setMessage("현재 이용중인 열차를 입력해야만 이용가능합니다.")
                        .setTitle("열차 선택 필수")
                        .setIcon(R.drawable.abc_ic_star_black_48dp)
                    builder.setPositiveButton("확인"){_, _ ->
                    }
                    builder.show()
                }
            }

        }
        animation_imagebutton.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setMessage("자리 공유를 중단하시겠습니까?")
                .setTitle("공유 중지")
                .setIcon(R.drawable.abc_btn_check_to_on_mtrl_015)
            builder.setPositiveButton("확인"){_, _ ->
                var imgAndroid = findViewById<ImageView>(R.id.animation_imagebutton)
                it.clearAnimation()
                it.visibility = View.INVISIBLE
                if(sharing_flag == 1){
                    rdb.child(Date_str).child(Using_train.subwawyNm).child(Using_train.trainNo + "_" + Using_train.statnTnm+ "_" + sharing_terminal_time).child("BeaconList").child(beacon.id1.toString()).removeValue()
                    if(sharing_dest_time != "")  {
                        Log.d("sharing_Dest_time","cancel")
                        am.cancel(sender)
                    }
                    //비콘 종료
                    beaconTransmitter.stopAdvertising()
                }
                else if(sharing_flag == 2){
                    rds.child(Date_str).child(Using_train.subwawyNm).child(Using_train.trainNo + "_" + Using_train.statnTnm+ "_" + sharing_terminal_time).child("SeatList").child(
                        user_info!!.id).removeValue()
                }
                sharing_flag = 0

            }
            builder.setNegativeButton("취소"){_,_->

            }
            builder.show()
        }
        receive_imagebutton.setOnClickListener {//자리 찾기 버튼
            if(sharing_flag != 0){ //공유 중일땐 제한
                val builder = AlertDialog.Builder(this)
                builder.setMessage("공유 중인 상태로는 이용할 수 없습니다.")
                    .setTitle("이용 제한")
                    .setIcon(R.drawable.abc_ic_star_black_48dp)
                builder.setPositiveButton("확인"){_, _ ->
                }
                builder.show()
            }
            else{
                if(Using_train.trainNo!=""){
                    Log.d("Using_train",Using_train.statnTnm)
                    val intent = Intent(this,Receive_Activity::class.java)
                    intent.putExtra("Using_train",Using_train)
                    startActivityForResult(intent,3)
                }
                else{
                    val builder = AlertDialog.Builder(this)
                    builder.setMessage("현재 이용중인 열차를 입력해야만 이용가능합니다.")
                        .setTitle("열차 선택 필수")
                        .setIcon(R.drawable.abc_ic_star_black_48dp)
                    builder.setPositiveButton("확인"){_, _ ->
                    }
                    builder.show()
                }
            }
        }
        chat_imagebutton.setOnClickListener {
            if(Using_train.trainNo!=""){
                Log.d("Using_train",Using_train.statnTnm)
                val intent = Intent(this,ChatActivity::class.java)
                startActivityForResult(intent,4)
            }
            else{
                val builder = AlertDialog.Builder(this)
                builder.setMessage("현재 이용중인 열차를 입력해야만 이용가능합니다.")
                    .setTitle("열차 신고시 열차 선택 필수")
                    .setIcon(R.drawable.abc_ic_star_black_48dp)
                builder.setPositiveButton("확인"){_, _ ->
                }
                builder.show()
            }

        }
        myinfo_imagebutton.setOnClickListener {
            if(Using_train.trainNo!=""){
                Log.d("Using_train",Using_train.statnTnm)
                val intent = Intent(this,ReportActivity::class.java)
                intent.putExtra("Using_train",Using_train)
                startActivityForResult(intent,5)
            }
            else{
                val builder = AlertDialog.Builder(this)
                builder.setMessage("현재 이용중인 열차를 입력해야만 이용가능합니다.")
                    .setTitle("열차 신고시 열차 선택 필수")
                    .setIcon(R.drawable.abc_ic_star_black_48dp)
                builder.setPositiveButton("확인"){_, _ ->
                }
                builder.show()
            }
        }
        slide_imagebutton.setOnClickListener {
            var drawer = findViewById<View>(R.id.navigationView)
            drwaler_layout.openDrawer(drawer)
            user_profile_image.setImageResource(R.mipmap.ic_launcher)
            user_name_textview.setText(user_info!!.id.toString() + "(익명)")
        }
    }
    fun initView(){
        var imgAndroid = findViewById<ImageView>(R.id.animation_imagebutton)
        imgAndroid.visibility = View.VISIBLE
        var anim = AnimationUtils.loadAnimation(this,R.anim.sharing)
        imgAndroid.animation = anim
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == 1){ //write
            if(resultCode == Activity.RESULT_OK){
                Using_train = data!!.getSerializableExtra("Using_train_info") as Train_Info
                no_info_text.visibility = View.INVISIBLE
                loading_text.visibility = View.INVISIBLE
                info_linear.visibility = View.VISIBLE
                set_text("직접 완성")
                //Using_train = Train_Info("", "", 2, "", false,"",-1,"")
            }
        }
        if(requestCode == 2){ //share
            if(resultCode == Activity.RESULT_OK){
                sharing_dest_time = data!!.getStringExtra("dest_time")
                sharing_terminal_time = data!!.getStringExtra("terminal_time")
                if(sharing_dest_time == ""){
                    val builder = android.app.AlertDialog.Builder(this)
                    builder.setMessage("해당 열차의 목적지 정차 정보를 확인 할 수 없습니다. 목적지에 도착하게 되면 수동으로 자리공유를 중단해주시기 바랍니다.")
                        .setTitle("도착 시 공유 중단 요청")
                        .setIcon(R.drawable.abc_ic_star_black_48dp)
                    builder.setPositiveButton("확인"){_, _ ->
                    }
                    builder.show()
                }
                sharing_flag = data!!.getIntExtra("sharing_flag",0)
                initView()
            }
        }
    }


    override fun onProviderEnabled(provider: String?) {
        Log.v("onProvideEnabled", "true");
        try {
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3000, 10.toFloat(), this)
            lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 3000, 10.toFloat(), this)
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    override fun onRestart() {
        super.onRestart()
        Log.d("onRestart","on")
    }

    override fun onResume() {
        super.onResume()
        Log.d("onResume","on")

    }

    override fun onPause() {
        super.onPause()
        Log.d("onPause","on")

    }

    override fun onProviderDisabled(provider: String?) {
        //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
        //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onLocationChanged(location: Location?) {

    }

    override fun onNavigationItemSelected(p0: MenuItem): Boolean {
        when(p0!!.itemId){ // 네비게이션 메뉴가 클릭되면 스낵바가 나타난다.
            R.id.app->
            {
                var intent = Intent(this,InformAppActivity::class.java)
                startActivity(intent)
            }
            R.id.log_out->
            {
                if(sharing_flag != 0){
                    val builder = AlertDialog.Builder(this)
                    builder.setMessage("지금 자리공유 중 입니다. 로그아웃하게 되면 자동적으로 자리 공유가 취소됩니다. 로그아웃하시겠습니까?")
                        .setTitle("자리 공유 중 로그아웃")
                        .setIcon(R.drawable.abc_btn_check_to_on_mtrl_015)
                    builder.setPositiveButton("확인"){_, _ ->
                        if(sharing_flag == 1){
                            rdb.child(Date_str).child(Using_train.subwawyNm).child(Using_train.trainNo + "_" + Using_train.statnTnm+ "_" + sharing_terminal_time).child("BeaconList").child(beacon.id1.toString()).removeValue()
                            if(sharing_dest_time != "")  am.cancel(sender)
                            //비콘 종료
                            beaconTransmitter.stopAdvertising()
                        }
                        else if(sharing_flag == 2){
                            rds.child(Date_str).child(Using_train.subwawyNm).child(Using_train.trainNo + "_" + Using_train.statnTnm+ "_" + sharing_terminal_time).child("SeatList").child(
                                user_info!!.id).removeValue()
                        }
                        sharing_flag = 0

                        //로그아웃
                        UserManagement.getInstance().requestLogout(object : LogoutResponseCallback() {
                            override fun onCompleteLogout() {
                                val intent = Intent(this@MainActivity, Login_Acitivity::class.java)
                                intent.putExtra("logout", "logout")
                                startActivity(intent)
                                Session.getCurrentSession().close()
                                Session.getCurrentSession().clearCallbacks()
                                Log.v("opensession", "logout")
                                finish()
                            }
                        })
                    }
                    builder.setNegativeButton("취소"){_,_->

                    }
                    builder.show()
                }
                else{
                    UserManagement.getInstance().requestLogout(object : LogoutResponseCallback() {
                        override fun onCompleteLogout() {
                            val intent = Intent(this@MainActivity, Login_Acitivity::class.java)
                            intent.putExtra("logout", "logout")
                            startActivity(intent)
                            Session.getCurrentSession().close()
                            Session.getCurrentSession().clearCallbacks()
                            Log.v("opensession", "logout")
                            finish()
                        }
                    })
                }


            }
        }
        drwaler_layout.closeDrawers() // 기능을 수행하고 네비게이션을 닫아준다.
        return false
    }

    fun update_location() {
        Log.v("update_location", "sss")
        Log.v("Using_train_info", Using_train.statnTnm)
        init_location()
        if(location != null){
            if (station_array.size > 1 ) {
                var transform = GeoTrans.convert(GeoTrans.GEO, GeoTrans.GRS80, GeoPoint(location!!.longitude.toDouble(), location!!.latitude.toDouble()))
                var flag: Int = 0
                if (distance_to_zero > GeoTrans.getDistancebyGrs80(station_array[0].geoPoint, transform)) { //점점 가까워져 간다. 즉 station_array[0] 쪽으로 가는 경우
                    flag = 1;

                } else { //1쪽으로
                    flag = 2;
                }
                if (match_str_Array(station_array[0].subwayNm).find { it.station_num == station_array[0].name }!!.fr_code >
                    match_str_Array(station_array[1].subwayNm).find { it.station_num == station_array[1].name }!!.fr_code
                ) {
                    if (flag == 1) updnLine = 1 //하행선
                    else if (flag == 2) updnLine = 0 // 상행선
                } else {
                    if (flag == 1) updnLine = 0
                    else if (flag == 2) updnLine = 1
                }
                Log.v("try_index",try_index.toString())
                if(try_index ==3){
                    var train_thread = Thread(object : Runnable {
                        override fun run() {
                            try {
                                Log.v("update 쓰레드", "동작 중")
                                train_parsing()
                                runOnUiThread(object: Runnable{
                                    override fun run() {
                                    }
                                })
                            } catch (e: InterruptedException) {
                                e.printStackTrace()
                            }
                        }
                    })
                    train_thread.start()
                    train_thread.join()
                    if (Using_train.trainNo == "") {
                        info_linear.visibility = View.INVISIBLE
                        loading_text.visibility = View.INVISIBLE
                        no_info_text.visibility = View.VISIBLE
                    }
                    else{
                        no_info_text.visibility = View.INVISIBLE
                        loading_text.visibility = View.INVISIBLE
                        info_linear.visibility = View.VISIBLE
                        set_text("자동 완성")
                    }
                }
                else{
                    loading_text.visibility = View.VISIBLE
                    no_info_text.visibility = View.INVISIBLE
                    info_linear.visibility = View.INVISIBLE
                }
            }
            else if(station_array.size == 1){
                if(try_index == 3){
                    if (Using_train.trainNo == "") {
                        info_linear.visibility = View.INVISIBLE
                        loading_text.visibility = View.INVISIBLE
                        no_info_text.visibility = View.VISIBLE
                    }
                    else{
                        no_info_text.visibility = View.INVISIBLE
                        loading_text.visibility = View.INVISIBLE
                        info_linear.visibility = View.VISIBLE
                        set_text("자동 완성")
                    }
                }
                else{
                    no_info_text.visibility = View.INVISIBLE
                    info_linear.visibility = View.INVISIBLE
                    loading_text.visibility = View.VISIBLE
                }

            }
            else {
                info_linear.visibility = View.INVISIBLE
                no_info_text.visibility = View.VISIBLE
            }
        }


    }


    override fun onDestroy() {
        if(sharing_flag == 1){
           var e = FirebaseDatabase.getInstance().getReference("Train").child(Date_str).child(Using_train.subwawyNm)
                   .child(Using_train.trainNo + "_" + Using_train.statnTnm+ "_" + sharing_terminal_time)
                   .child("BeaconList").child(beacon.id1.toString()).removeValue().exception
            Log.d("firebase_exception",e.toString())

            if(sharing_dest_time != "")  am.cancel(sender)
            beaconTransmitter.stopAdvertising()
        }
        else if(sharing_flag == 2){
            var e = FirebaseDatabase.getInstance().getReference("Train").child(Date_str).child(Using_train.subwawyNm)
                .child(Using_train.trainNo + "_" + Using_train.statnTnm+ "_" + sharing_terminal_time)
                .child("SeatList").child(user_info!!.id.toString()).removeValue().exception
        }
        super.onDestroy()
        Log.d("Ondestroy","ondestroy")

        finishAffinity()
        System.runFinalization()
        System.exit(0)

    }
    fun set_text(str: String){
        var status_str = ""
        var updnLine_str = ""
        var directat_str = ""
        when(Using_train.status){
            0-> status_str = "진입"
            1->status_str = "도착"
            else->status_str =  "출발"
        }
        when(Using_train.directAt){
            1->directat_str = "급행"
            0->directat_str = "일반"
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
            "1호선"->listicon.setImageResource(R.drawable.line1)
            "2호선"->listicon.setImageResource(R.drawable.line2)
            "3호선"->listicon.setImageResource(R.drawable.line3)
            "4호선"->listicon.setImageResource(R.drawable.line4)
            "5호선"->listicon.setImageResource(R.drawable.line5)
            "6호선"->listicon.setImageResource(R.drawable.line6)
            "7호선"->listicon.setImageResource(R.drawable.line7)
            "8호선"->listicon.setImageResource(R.drawable.line8)
            "9호선"->listicon.setImageResource(R.drawable.line9)
            "경춘선"->listicon.setImageResource(R.drawable.kyungchun)
            "분당선"->listicon.setImageResource(R.drawable.bundang)
            "신분당선"->listicon.setImageResource(R.drawable.newbundang)
            "경의중앙선"->listicon.setImageResource(R.drawable.kyungyui)
            "인천선"->listicon.setImageResource(R.drawable.incheon1)
            "인천2호선"->listicon.setImageResource(R.drawable.incheon2)
            "공항철도"->listicon.setImageResource(R.drawable.airport)
            "용인경전철"->listicon.setImageResource(R.drawable.yongin)
            "우의신설경전철"->listicon.setImageResource(R.drawable.ui)
            "의정부경전철"->listicon.setImageResource(R.drawable.uijeongbu)
            "경강선"->listicon.setImageResource(R.drawable.kyungkang)
            "서해선"->listicon.setImageResource(R.drawable.west)
            "수인선"->listicon.setImageResource(R.drawable.suin)
        }
        train_location_text.setText(Using_train.current_station + " "+status_str)
        directat_textview.setText(directat_str)
        trainno_text.setText(Using_train.trainNo)
        train_direction_text.setText(updnLine_str)
        train_dest_text.setText(Using_train.statnTnm)
        train_time_text.setText(Using_train.recptime)
        if(str == "자동 완성"){
            auto_or_write_text.setText(str + " : " + provider )
        }
        else{
            auto_or_write_text.setText(str)
        }
    }
}




