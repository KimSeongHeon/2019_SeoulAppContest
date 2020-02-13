package com.project.seoul_project

import android.app.AlarmManager
import android.app.AlertDialog
import android.app.PendingIntent
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseSettings
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.inputmethod.InputMethodManager
import android.widget.*
import com.google.gson.JsonParser
import com.project.seoul_project.MainActivity.Companion.Date_str
import com.project.seoul_project.MainActivity.Companion.am
import com.project.seoul_project.MainActivity.Companion.beacon
import com.project.seoul_project.MainActivity.Companion.btManager
import com.project.seoul_project.MainActivity.Companion.sender
import com.project.seoul_project.MainActivity.Companion.user_info
import kotlinx.android.synthetic.main.activity_select_train.search_edittext
import kotlinx.android.synthetic.main.activity_share.*
import kotlinx.android.synthetic.main.activity_share.confirm_button
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


class ShareActivity : AppCompatActivity(),View.OnClickListener {


    //자리 스크롤 뷰 표시
    var seats =
             ("UUU_AAAAAAA_AAAAAAA_AAAAAAA_UUU/"
            + "___R_______R_______R_______R___/"
            + "___R_______R_______R_______R___/"
            + "UUU_AAAAAAA_AAAAAAA_AAAAAAA_UUU/")

    var seatGaping = 3
    var seat_size = 100
    var seatViewList = ArrayList<TextView>()
    var selectedIds = ""
    var turn_flag = 0

    // 지하철 연동
    var line_list = ArrayList<All_Station_Info>()
    var search_list = ArrayList<All_Station_Info>()

    // 도착 시간
    lateinit var dest_info:All_Station_Info
    lateinit var terminal_info:All_Station_Info
    var dest_time = ""
    var terminal_time = ""
    var dest:String = ""
    lateinit var Using_train:Train_Info
    lateinit var share_adapter:Share_recycler_adapter
    var rcyview_flag = false

    //자리 정보
    var seat_info:Seat_info = Seat_info("","","","","")
    //spinner 선택 inner_class
    inner class SpinnerSelectedListner : AdapterView.OnItemSelectedListener{
        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            seat_info.room = parent?.getItemAtPosition(position).toString()
        }

        override fun onNothingSelected(parent: AdapterView<*>?) {
        }
    }
    //블루투스 정보 변화 감지
    val BluetoothReceiver = object:BroadcastReceiver(){
        override fun onReceive(context: Context?, intent: Intent?) {
            var action = intent!!.action
            if(action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)){
                var state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,BluetoothAdapter.ERROR)
                when(state){
                    BluetoothAdapter.STATE_OFF->bluetooth_set_text()
                    BluetoothAdapter.STATE_TURNING_OFF->bluetooth_set_text()
                    BluetoothAdapter.STATE_ON->bluetooth_set_text()
                }
            }
        }
    }
    //모드 선택
    var select_mode = 1

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(BluetoothReceiver)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.activity_share)
        var filter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        registerReceiver(BluetoothReceiver,filter) //블루투스 상태변화 감지 리시버 등록
        share_liner_layout1.visibility = View.INVISIBLE
        share_liner_layout2.visibility = View.INVISIBLE
        share_radio_group.visibility = View.INVISIBLE
        share_scroll_framview.visibility = View.INVISIBLE
        init_seat(turn_flag)
        init()
        initlistener()
    }
    override fun onClick(v: View?) { //seat 선택했을떄 변환.
        //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        if(v!!.getTag()== 2){
           if(selectedIds != ""){
               if (selectedIds.contains(v.getId().toString())) { //본인거 누름..취소
                   selectedIds = selectedIds.replace(v.getId().toString() , "");
                   v.setBackgroundResource(R.drawable.square)
               } else {//다른거 누름.. 취소후 다른거 선택
                   seatViewList.find {
                       it.id.toString() == selectedIds
                   }!!.setBackgroundResource(R.drawable.square)
                   selectedIds = v.getId().toString();
                   v.setBackgroundResource(R.drawable.textview_corner4);
               }
           }
            else{ //처음 누름..선택
               selectedIds = v.getId().toString();
               v.setBackgroundResource(R.drawable.textview_corner4);
           }
        }
    }
    fun hidekeyboard(){ // 입력시 키보드 강제로 밑으로 내림
        var imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(search_edittext.windowToken,0)
    }
    fun init_seat(flag:Int){ //스크롤뷰에 seat 뷰 생성함
        seatViewList.clear()
        selectedIds = ""
        var layout = findViewById<ViewGroup>(R.id.layoutSeat);

        seats = "/" + seats;

        layout.removeAllViewsInLayout()
        var layoutSeat =  LinearLayout(this);
        var params =  LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutSeat.setOrientation(LinearLayout.VERTICAL);
        layoutSeat.setLayoutParams(params);
        layoutSeat.setPadding(1 * seatGaping, 1 * seatGaping, 1 * seatGaping, 1 * seatGaping);
        layout.addView(layoutSeat);
        //var layout:LinearLayout? = null;

        if(flag == 0){
            var count = 0;
            for (i in 0.. seats.length-1) {
                if (seats.get(i)== '/') {
                    layout = LinearLayout(this);
                    layout.setOrientation(LinearLayout.HORIZONTAL);
                    layoutSeat.addView(layout);
                } else if (seats.get(i) == 'U') {
                    var view =  TextView(this);
                    var layoutParams =  LinearLayout.LayoutParams(seat_size, seat_size);
                    layoutParams.setMargins(seatGaping, seatGaping, seatGaping, seatGaping);
                    view.setLayoutParams(layoutParams);
                    view.setPadding(0, 0, 0, 2 * seatGaping);
                    view.setGravity(Gravity.CENTER);
                    view.setBackgroundResource(R.drawable.fillsquare);
                    view.setTextColor(Color.WHITE);
                    view.setTextSize(20.toFloat())
                    view.setTag(1);
                    view.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 9.toFloat());
                    layout.addView(view);
                    seatViewList.add(view);
                    //view.setOnClickListener(this);
                } else if (seats.get(i) ==  'A') {
                    count++;
                    var view =  TextView(this);
                    var layoutParams =  LinearLayout.LayoutParams(seat_size, seat_size);
                    layoutParams.setMargins(seatGaping, seatGaping, seatGaping, seatGaping);
                    view.setLayoutParams(layoutParams);
                    view.setPadding(0, 0, 0, 2 * seatGaping);
                    view.setId(count);
                    view.setGravity(Gravity.CENTER);
                    view.setBackgroundResource(R.drawable.square);
                    view.setText(count.toString() + "");
                    view.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 9.toFloat());
                    view.setTextColor(Color.BLACK);
                    view.setTag(2);
                    layout.addView(view);
                    seatViewList.add(view);
                    view.setOnClickListener(this);
                } else if (seats.get(i) == 'R') {
                    var view =  TextView(this);
                    var layoutParams =  LinearLayout.LayoutParams(seat_size, seat_size);
                    layoutParams.setMargins(seatGaping, seatGaping, seatGaping, seatGaping);
                    view.setLayoutParams(layoutParams);
                    view.setPadding(0, 0, 0, 2 * seatGaping);
                    view.setGravity(Gravity.CENTER);
                    if(flag == 0)
                        view.setBackgroundResource(R.drawable.rightarrows1)
                    else if(flag == 1)
                        view.setBackgroundResource(R.drawable.leftarrows)
                    view.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 9.toFloat());
                    view.setTextColor(Color.WHITE);
                    view.setTag(3);
                    layout.addView(view);
                    seatViewList.add(view);
                    //view.setOnClickListener(this);
                } else if (seats.get(i) == '_') {
                    var view = TextView(this);
                    var layoutParams = LinearLayout.LayoutParams(seat_size, seat_size);
                    layoutParams.setMargins(seatGaping, seatGaping, seatGaping, seatGaping);
                    view.setLayoutParams(layoutParams);
                    view.setBackgroundColor(Color.TRANSPARENT);
                    view.setText("");
                    layout.addView(view);
                }
            }
        }
        else if(flag == 1){
            var count = 43
            for(i in seats.length-1 downTo 0){
                if (seats.get(i)== '/') {
                    layout = LinearLayout(this);
                    layout.setOrientation(LinearLayout.HORIZONTAL);
                    layoutSeat.addView(layout);
                } else if (seats.get(i) == 'U') {
                    var view =  TextView(this);
                    var layoutParams =  LinearLayout.LayoutParams(seat_size, seat_size);
                    layoutParams.setMargins(seatGaping, seatGaping, seatGaping, seatGaping);
                    view.setLayoutParams(layoutParams);
                    view.setPadding(0, 0, 0, 2 * seatGaping);
                    view.setGravity(Gravity.CENTER);
                    view.setBackgroundResource(R.drawable.fillsquare);
                    view.setTextColor(Color.WHITE);
                    view.setTextSize(20.toFloat())
                    view.setTag(1);
                    view.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 9.toFloat());
                    layout.addView(view);
                    seatViewList.add(view);
                    //view.setOnClickListener(this);
                } else if (seats.get(i) ==  'A') {
                    count--;
                    var view =  TextView(this);
                    var layoutParams =  LinearLayout.LayoutParams(seat_size, seat_size);
                    layoutParams.setMargins(seatGaping, seatGaping, seatGaping, seatGaping);
                    view.setLayoutParams(layoutParams);
                    view.setPadding(0, 0, 0, 2 * seatGaping);
                    view.setId(count);
                    view.setGravity(Gravity.CENTER);
                    view.setBackgroundResource(R.drawable.square);
                    view.setText(count.toString() + "");
                    view.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 9.toFloat());
                    view.setTextColor(Color.BLACK);
                    view.setTag(2);
                    layout.addView(view);
                    seatViewList.add(view);
                    view.setOnClickListener(this);
                } else if (seats.get(i) == 'R') {
                    var view =  TextView(this);
                    var layoutParams =  LinearLayout.LayoutParams(seat_size, seat_size);
                    layoutParams.setMargins(seatGaping, seatGaping, seatGaping, seatGaping);
                    view.setLayoutParams(layoutParams);
                    view.setPadding(0, 0, 0, 2 * seatGaping);
                    view.setGravity(Gravity.CENTER);
                    if(flag == 0)
                        view.setBackgroundResource(R.drawable.rightarrows1)
                    else if(flag == 1)
                        view.setBackgroundResource(R.drawable.leftarrows)
                    view.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 9.toFloat());
                    view.setTextColor(Color.WHITE);
                    view.setTag(3);
                    layout.addView(view);
                    seatViewList.add(view);
                    //view.setOnClickListener(this);
                } else if (seats.get(i) == '_') {
                    var view = TextView(this);
                    var layoutParams = LinearLayout.LayoutParams(seat_size, seat_size);
                    layoutParams.setMargins(seatGaping, seatGaping, seatGaping, seatGaping);
                    view.setLayoutParams(layoutParams);
                    view.setBackgroundColor(Color.TRANSPARENT);
                    view.setText("");
                    layout.addView(view);
                }
            }
        }

    }
    fun init(){
        val intent = getIntent()
        Using_train = intent.getSerializableExtra("Using_train") as Train_Info
        Log.d("Using_Tran",Using_train.subwawyNm + "/" + Using_train.current_station)
        line_list = match_str_Array(Using_train.subwawyNm)
        if(line_list.find { it.station_num ==  Using_train.statnTnm} != null){
            Log.d("not null","not")
            terminal_info = line_list.find { it.station_num ==  Using_train.statnTnm}!!
        }
        else{
            Log.d(" null","null")
            terminal_info = All_Station_Info(Using_train.subwawyNm,Using_train.statnTnm,"1","0")
        }
        set_text()
        bluetooth_set_text()
    }
    fun newBeacon(flag:Int){ //도착시간 확인 후 DB에 저장
        val p_thread = Thread(object : Runnable {
            override fun run() {
                try {
                    Log.v("쓰레드", "동작 중")
                    exception_process()
                    parsing_for_find(dest_info.station_cd,0)
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
        if(flag == 0){
            val items = Beacon_info(beacon.id1.toString(),MainActivity.user_info!!.id,dest,Using_train.trainNo,selectedIds,"")
            MainActivity.rdb.child(Date_str).child(Using_train.subwawyNm).child(Using_train.trainNo + "_" + Using_train.statnTnm + "_" + terminal_time).child("BeaconList").child(beacon.id1.toString()).setValue(items)
        }
        else if(flag == 1){
            val items = Seat_info(seat_info.room, user_info!!.id,dest,Using_train.trainNo,selectedIds,"")
            MainActivity.rdb.child(Date_str).child(Using_train.subwawyNm).child(Using_train.trainNo + "_" + Using_train.statnTnm + "_" + terminal_time).child("SeatList").child(
                user_info!!.id).setValue(items)
        }
    }
    fun get_time(){
        MainActivity.cal = Calendar.getInstance()
        MainActivity.year = MainActivity.cal.get(Calendar.YEAR)
        MainActivity.month = MainActivity.cal.get(Calendar.MONTH)+1
        MainActivity.date = MainActivity.cal.get(Calendar.DATE)
        MainActivity.hour = MainActivity.cal.get(Calendar.HOUR_OF_DAY)
        MainActivity.Date_str = ""
        if(MainActivity.hour <= 3){
            var month_str = ""; var date_str = ""
            var calendar = GregorianCalendar()
            calendar.add(Calendar.DATE,-1)
            var sdf = SimpleDateFormat("yyyyMMdd")
            var str = sdf.format(calendar.time)
            MainActivity.year = Integer.parseInt(str.substring(0, 4))
            MainActivity.month = Integer.parseInt(str.substring(4, 6))
            MainActivity.date = Integer.parseInt(str.substring(6))
            if(MainActivity.month < 10){
                month_str = "0"+ MainActivity.month.toString()
            }else{
                month_str = MainActivity.month.toString()
            }
            if(MainActivity.date < 10){
                date_str = "0"+ MainActivity.date.toString()
            }
            else{
                date_str = MainActivity.date.toString()
            }
            Date_str = MainActivity.year.toString()+month_str+date_str
            Log.d("date_str", Date_str)
        }
        else{
            var month_str = ""; var date_str = ""
            if(MainActivity.month < 10){
                month_str = "0"+ MainActivity.month.toString()
            }else{
                month_str = MainActivity.month.toString()
            }
            if(MainActivity.date < 10){
                date_str = "0"+ MainActivity.date.toString()
            }
            else{
                date_str = MainActivity.date.toString()
            }
            Date_str = MainActivity.year.toString()+month_str+date_str
            Log.d("date_str", Date_str)
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

    fun parsing_for_find(str:String,flag:Int){
        var holiday_int:Int = 0
        if(!isHoliday.isHoliday(Date_str)){
            holiday_int=1
        }
        else{
            if(isHoliday.getDayOfWeek(Date_str) == 7){
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
                        if(flag == 0){
                            dest_time = jarr[i].asJsonObject.get("ARRIVETIME").asString
                            Log.d("dest_time",dest_time.toString())
                        }
                        else if(flag == 1){
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
                            if(flag == 0){
                                dest_time = jarr[i].asJsonObject.get("ARRIVETIME").asString
                                Log.d("dest_time",dest_time.toString())
                            }
                            else if(flag == 1){
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
    fun initlistener(){

        share_change_imagebutton.setOnClickListener {
            if(turn_flag == 0){
                turn_flag = 1
                init_seat(1)
            }
            else if(turn_flag == 1){
                turn_flag = 0
                init_seat(0)
            }
        }
        rewrite_button.setOnClickListener {
            search_edittext.visibility = View.VISIBLE
            inner_linear.visibility = View.INVISIBLE
            share_liner_layout1.visibility = View.INVISIBLE
            share_liner_layout2.visibility = View.INVISIBLE
            share_scroll_framview.visibility = View.INVISIBLE
            share_radio_group.visibility = View.INVISIBLE
            dest_info = All_Station_Info("","","","")
            dest = ""
            dest_textview.setText("")
        }
        search_edittext.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if(s!!.length == 0){
                    search_list.clear()
                    init_adapter()
                }
                else{
                    if(rcyview_flag == false){//리사이클러뷰 꺼져있으면
                        val new = dynamic.layoutParams
                        new.height = ViewGroup.LayoutParams.WRAP_CONTENT
                        dynamic.layoutParams = new
                        rcyview.setLayoutParams(LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))
                        rcyview.setBackgroundResource(R.drawable.textview_corner2)
                        init_adapter()
                        rcyview_flag = true
                    }
                    search_list.clear()
                    Log.v("afterTextChanged","play")
                    val str = s.toString()
                    search_list.addAll(line_list.filter {
                        SoundSearcher.matchString(it.station_num,str)
                    })
                    Log.d("station_info",search_list.size.toString())
                    init_adapter()
                }

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }
        })
        confirm_button.setOnClickListener {
            get_time()
            if(share_radio_group.checkedRadioButtonId == R.id.share_radiobutton1){
                MainActivity.btManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
                var btadapter = btManager.adapter
                val intent = Intent(this,MainActivity::class.java)
                if(btadapter.isEnabled){
                    if(btadapter.isMultipleAdvertisementSupported){
                        if(selectedIds != ""){
                            //비콘 생성
                            beacon = Beacon.Builder()
                                .setId1(UUID.randomUUID().toString())  // uuid for beacon
                                .setId2("42")  // major
                                .setId3("43")  // minor
                                .setManufacturer(0x0118)  // Radius Networks. 0x0118 : Change this for other beacon layouts // 0x004C : for iPhone
                                .setTxPower(-59)  // Power in dB
                                .setDataFields(Arrays.asList(0L))  // Remove this for beacon layouts without d: fields
                                .build()
                            MainActivity.beaconParser = BeaconParser()
                                .setBeaconLayout("m:2-3=beac,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25")
                            MainActivity.beaconTransmitter = BeaconTransmitter(applicationContext,MainActivity.beaconParser)
                            //데이터베이스에 해당 내용 저장
                            newBeacon(0)
                            MainActivity.beaconTransmitter.startAdvertising(beacon, object : AdvertiseCallback() {
                                override fun onStartSuccess(settingsInEffect: AdvertiseSettings) {
                                    super.onStartSuccess(settingsInEffect)
                                    Toast.makeText(applicationContext,"위치가 성공적으로 공유 되었습니다",Toast.LENGTH_SHORT).show()
                                }

                                override fun onStartFailure(errorCode: Int) {
                                    super.onStartFailure(errorCode)
                                }
                            })
                            intent.putExtra("terminal_time",terminal_time)
                            intent.putExtra("dest_time",dest_time)
                            setResult(RESULT_OK,intent)
                            intent.putExtra("sharing_flag",1)
                            set_alarm()
                            finish()
                        }
                        else{
                            val builder = AlertDialog.Builder(this)
                            builder.setMessage("자리 정보를 입력해야 합니다.")
                                .setTitle("자리 정보 입력 요망")
                                .setIcon(R.drawable.ic_mtrl_chip_checked_circle)
                            builder.setPositiveButton("확인"){_, _ ->
                            }
                            builder.show()
                        }
                    }
                    else{ //블루투스 꺼져있는 경우
                        val builder = AlertDialog.Builder(this)
                        builder.setMessage("블루투스가 활성화 되어있어야 합니다.")
                            .setTitle("블루투스 권한 요청")
                            .setIcon(R.drawable.abc_ic_star_black_48dp)
                        builder.setPositiveButton("확인"){_, _ ->
                            val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                            startActivityForResult(intent, 1)
                        }
                        builder.show()
                    }
                }
            }
            else if(share_radio_group.checkedRadioButtonId == R.id.share_radiobutton2){ // 직접입력
                if(selectedIds != "" && seat_info.room != ""){
                    newBeacon(1)
                    intent.putExtra("terminal_time",terminal_time)
                    intent.putExtra("dest_time",dest_time)
                    intent.putExtra("sharing_flag",2)
                    setResult(RESULT_OK,intent)
                    set_alarm()
                    finish()
                }
                else{
                    val builder = AlertDialog.Builder(this)
                    builder.setMessage("자리 정보와 객실 정보 모두 입력해야 합니다.")
                        .setTitle("자리 정보 입력 요망")
                        .setIcon(R.drawable.ic_mtrl_chip_checked_circle)
                    builder.setPositiveButton("확인"){_, _ ->
                    }
                    builder.show()
                }

            }

        }
        cancel_button.setOnClickListener {
            finish()
        }
        share_spinner.onItemSelectedListener = SpinnerSelectedListner()
        // RadioButton 이벤트
        share_radio_group.setOnCheckedChangeListener { group, checkedId ->
            when(checkedId){
                R.id.share_radiobutton1->{
                    share_spinner.visibility = View.INVISIBLE
                    select_mode = 1
                }
                R.id.share_radiobutton2->{
                    share_spinner.visibility = View.VISIBLE
                    select_mode = 2
                }
            }
        }
    }
    fun init_adapter(){
        val search_rlayoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL,false)
        share_adapter = Share_recycler_adapter(search_list)
        rcyview.layoutManager = search_rlayoutManager
        rcyview.adapter = share_adapter
        share_adapter.ItemClickListener = object:Share_recycler_adapter.OnItemClickListner{
            override fun OnItemClick(holder: Share_recycler_adapter.ViewHolder, view: View, data: All_Station_Info, position: Int) {
                Log.v("선택","onItemClick")
                dest_info = data
                search_edittext.visibility = View.INVISIBLE
                inner_linear.visibility = View.VISIBLE
                dest_textview.setText(data.station_num)
                dest = data.station_num
                search_list.clear()
                init_adapter()
                hidekeyboard()
                share_liner_layout1.visibility = View.VISIBLE
                share_liner_layout2.visibility = View.VISIBLE
                share_scroll_framview.visibility = View.VISIBLE
                share_radio_group.visibility = View.VISIBLE
            }
        }
        var spinner_adapter = ArrayAdapter.createFromResource(this,R.array.room,android.R.layout.simple_spinner_item)
        spinner_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        share_spinner.adapter = spinner_adapter

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        //super.onActivityResult(requestCode, resultCode, data)
        when(requestCode){
            1->{
                bluetooth_set_text()
            }
        }
    }
    fun bluetooth_set_text(){
        MainActivity.btManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        var btadapter = btManager.adapter
        if(!btadapter.isEnabled){
            share_info_textview.setText("블루투스 기능이 꺼져있습니다.. \n"+ "블루투스 공유 기능을 사용할 수 없습니다.")
            share_radio_group.check(R.id.share_radiobutton2)
            share_radiobutton1.isEnabled = false
            share_spinner.visibility = View.VISIBLE
        }
        else{
            if(btadapter.isMultipleAdvertisementSupported){
                share_info_textview.setText("해당 기기는 블루투스 공유 기능이 가능한 기기입니다.")
                share_radiobutton1.isEnabled = true
                if(share_radio_group.checkedRadioButtonId == R.id.share_radiobutton1){
                    share_spinner.visibility = View.INVISIBLE

                }
                else{
                    share_spinner.visibility = View.VISIBLE

                }
            }
            else{
                share_info_textview.setText("해당 기기는 블루투스 공유 기능이 불가능한 기기입니다.")
                share_radio_group.check(R.id.share_radiobutton2)
                share_radiobutton1.isEnabled = false
                share_spinner.visibility = View.VISIBLE
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
        train_direction_text.setText(updnLine_str)
        train_dest_text.setText(Using_train.statnTnm)
        trainno_text.setText(Using_train.trainNo)
        directat_textview.setText(directat_str)
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
    fun set_alarm(){
        if(dest_time != ""){
                var calendar:Calendar = Calendar.getInstance()
                am = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            var intent = Intent(this, BroadcastD::class.java)
            intent.putExtra("dest",dest)
                sender = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT)
                var dest_hour = dest_time.substring(0,2)
                var dest_minute  = dest_time.substring(3,5)
                var dest_second = dest_time.substring(6,7)
            if(dest_hour == "24") dest_hour = "00"
            else if(dest_hour == "25") dest_hour ="01"
                calendar.set(calendar.get(Calendar.YEAR),calendar.get(Calendar.MONTH),calendar.get(Calendar.DATE),dest_hour.toInt(),dest_minute.toInt(),dest_second.toInt())
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    am.set(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, sender)
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    am.set(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, sender)
                } else {
                    am.set(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, sender)
                }
            }
            else{
            Log.d("dest == null",dest)

            }
        }
}
