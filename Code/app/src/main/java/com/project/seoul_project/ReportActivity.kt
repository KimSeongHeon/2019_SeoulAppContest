package com.project.seoul_project

import android.Manifest.permission.SEND_SMS
import android.content.pm.PackageManager
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.telephony.SmsManager
import android.util.Log
import android.view.View
import android.widget.TabHost
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_report.*


class ReportActivity : AppCompatActivity() {

    private val PERMISSION_REQUEST_CODE = 1
    private val PERMISSION_REQUEST_CODE2 = 2
    lateinit var Using_train:Train_Info
    var phone_num = ""
    val line1_8 = "1577-1234"
    val line9 = "1544-4009"
    val lineNewbundang = "031-8018-7777"
    val lineair = "1544-7769"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report)
        init_tab()
        init()
        init_adapter()
        initlistner()
    }
    fun init(){
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {

            if (checkSelfPermission(SEND_SMS) == PackageManager.PERMISSION_DENIED) {

                Log.d("permission", "permission denied to SEND_SMS - requesting it")
                val permissions = arrayOf<String>(SEND_SMS)

                requestPermissions(permissions, PERMISSION_REQUEST_CODE)

            }
        }
        val intent = getIntent()
        Using_train = intent.getSerializableExtra("Using_train") as Train_Info
        Log.d("Using_Tran",Using_train.subwawyNm + "/" + Using_train.current_station)
        report_train_textview.setText(Using_train.subwawyNm + " / "  + Using_train.statnTnm +"  행/  "+ Using_train.trainNo +" 번 열차")
        when(Using_train.subwawyNm){
            "1호선"->{
                phone_num = line1_8
                report_phone_textview.setText(phone_num + " " + "(서울메트로)")
            };
            "2호선"-> {
                phone_num = line1_8
                report_phone_textview.setText(phone_num + " " + "(서울메트로)")
            };
            "3호선"->{
                phone_num = line1_8
                report_phone_textview.setText(phone_num + " " + "(서울메트로)")
            };
            "4호선"->{
                phone_num = line1_8
                report_phone_textview.setText(phone_num + " " + "(서울메트로)")
            };
            "5호선"->{
                phone_num = line1_8
                report_phone_textview.setText(phone_num + " " + "(서울메트로)")
            };
            "6호선"->{
                phone_num = line1_8
                report_phone_textview.setText(phone_num + " " + "(서울메트로)")
            };
            "7호선"->{
                phone_num = line1_8
                report_phone_textview.setText(phone_num + " " + "(서울메트로)")
            };
            "8호선"->{
                phone_num = line1_8
                report_phone_textview.setText(phone_num + " " + "(서울메트로)")
            };
            "9호선"->{
                phone_num = line9
                report_phone_textview.setText(phone_num + " " + "(서울메트로)")
            };
            "분당선"->{
            phone_num = lineair
            report_phone_textview.setText(phone_num + " " + "(코레일)")
            };
            "신분당선"->{
                phone_num = lineNewbundang
                report_phone_textview.setText(phone_num + " " + "((주)네오트랜스)")
            };
            "경의선"->{
                phone_num = lineair
                report_phone_textview.setText(phone_num + " " + "(코레일)")
            };
            "공항철도"->{
                phone_num = lineair
                report_phone_textview.setText(phone_num + " " + "(코레일)")
            };
        }
    }
    fun initlistner(){
        report_send_button.setOnClickListener {
            try {
                //전송
                val smsManager = SmsManager.getDefault()
                smsManager.sendTextMessage(phone_num, null,report_train_textview.text.toString() + "\n "+report_edittext.text.toString(), null, null)
                Toast.makeText(applicationContext, "전송 완료!", Toast.LENGTH_LONG).show()
                finish()
            } catch (e: Exception) {
                Toast.makeText(applicationContext, "SMS faild, please try again later!", Toast.LENGTH_LONG).show()
                e.printStackTrace()
            }

        }
        report_cacnel_button.setOnClickListener {
            finish()
        }
    }
    fun init_tab(){
        val tabHost1 = findViewById<View>(R.id.tabHost1) as TabHost
        tabHost1.setup()
        tabHost1.setOnTabChangedListener{

        }
        // 첫 번째 Tab. (탭 표시 텍스트:"TAB 1"), (페이지 뷰:"content1")
        val ts1 = tabHost1.newTabSpec("Tab Spec 1")
        ts1.setContent(R.id.content1)
        ts1.setIndicator("문자 신고")
        tabHost1.addTab(ts1)


        // 두 번째 Tab. (탭 표시 텍스트:"TAB 2"), (페이지 뷰:"content2")
        val ts2 = tabHost1.newTabSpec("Tab Spec 2")
        ts2.setContent(R.id.content2)
        ts2.setIndicator("전화 신고")
        tabHost1.addTab(ts2)

        /*tabHost1.tabWidget.setCurrentTab(0)
        var tv = tabHost1.tabWidget.getChildAt(tabHost1.currentTab).findViewById(android.R.id.title) as TextView
        tv.setTextColor(Color.parseColor("#FFFFFFFF"))*/
    }
    fun init_adapter(){
        val list = ArrayList<Pair<String,String>>()
        list.add(Pair("1호선, 2호선, 3호선, 4호선 (서울 구간)","1577-1234"))
        list.add(Pair("1호선, ,2호선, 4호선 (서울 외 구간),\n" +
            "분당선, 경춘선, 수인선, 경의중앙선, 경강선","1577-7788"))
        list.add(Pair("5호선, 6호선, 7호선, 8호선 (전 구간)","1577-5678"))
        list.add(Pair("인천지하철 1호선, 2호선","032-451-2114"))
        list.add(Pair("공항철도","1599-7788"))
        list.add(Pair("신분당선","031-8018-7777"))
        list.add(Pair("의정부경전철","031-820-1000"))
        list.add(Pair("에버라인(용인경전철)","031-329-3500"))

        val call_rlayoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL,false)
        var call_adapter = Call_recycler_adapter(list)
        report_call_listview.layoutManager = call_rlayoutManager
        report_call_listview.adapter = call_adapter
    }
}
