package com.project.seoul_project

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import com.google.firebase.database.*
import com.kakao.auth.AuthType
import com.kakao.auth.Session
import kotlinx.android.synthetic.main.activity_login__acitivity.*

class Login_Acitivity : AppCompatActivity() {

    lateinit var input_password:String
    lateinit var input_id:String
    lateinit var imageURL: String
    lateinit var rdb: DatabaseReference
    lateinit var callback: SessionCallback
    val PERMISSION_ACCEES_FINE_LOCATION = 1000
    val PERMISSION_ACCESS_COARSE_LOCATION = 1001
    val user_info_list = ArrayList<User_info>()
    // 변수 끝
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login__acitivity)
        initFirebase()
        buttonClick()
        initPermission()
        onGPS()
    }
    fun onGPS(){
        if(!((getSystemService(Context.LOCATION_SERVICE) as LocationManager).isProviderEnabled(LocationManager.GPS_PROVIDER))){
            val builder = AlertDialog.Builder(this)
            builder.setMessage("GPS가 켜져있어야 더 나은 서비스를 제공할 수 있습니다.")
                .setTitle("권한 허용")
                .setIcon(R.drawable.abc_ic_star_black_48dp)
            builder.setPositiveButton("확인"){_, _ ->
                val gpsOptionIntent = Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(gpsOptionIntent)
            }
            builder.setNegativeButton("취소"){_, _ ->

            }
            builder.show()
        }
    }

    fun buttonClick(){
        val session = Session.getCurrentSession()
        callback = SessionCallback(this)
        session.addCallback(callback)
        btn_custom_login.setOnClickListener {
            session.open(AuthType.KAKAO_LOGIN_ALL, this@Login_Acitivity)
        }
    }
    fun initPermission() {
        if (!(checkAppPermission(
                arrayOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                )
            ))
        ) {
            val builder = AlertDialog.Builder(this)
            builder.setMessage("반드시 위치와 WIFI에 대한 권한이 허용되어야 합니다.")
                .setTitle("권한 허용")
                .setIcon(R.drawable.abc_ic_star_black_48dp)
            builder.setPositiveButton("OK") { _, _ ->
                askPermission(
                    arrayOf(
                        android.Manifest.permission.ACCESS_FINE_LOCATION,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION
                    ), PERMISSION_ACCEES_FINE_LOCATION, PERMISSION_ACCESS_COARSE_LOCATION
                );
            } // OK! -> askPermission 호출
            val dialog = builder.create()
            dialog.show()
        } else {
            Toast.makeText(
                getApplicationContext(),
                "권한이 승인되었습니다.", Toast.LENGTH_SHORT
            ).show();
        }
    }
    fun checkAppPermission(requestPermission: Array<String>): Boolean {
        val requestResult = BooleanArray(requestPermission.size)
        for (i in requestResult.indices) {
            requestResult[i] = ContextCompat.checkSelfPermission(
                this,
                requestPermission[i]
            ) == PackageManager.PERMISSION_GRANTED
            if (!requestResult[i]) {
                return false
            }
        }
        return true
    } // checkAppPermission

    fun askPermission(requestPermission: Array<String>, REQ_PERMISSION: Int, REQ_PERMISSION2: Int) {
        ActivityCompat.requestPermissions(
            this, requestPermission,
            REQ_PERMISSION
        )
        ActivityCompat.requestPermissions(
            this, requestPermission,
            REQ_PERMISSION2
        )
    } // askPermission

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSION_ACCEES_FINE_LOCATION -> if (checkAppPermission(permissions)) { //퍼미션 동의했을 때 할 일
                Toast.makeText(this, "권한이 승인됨", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "권한이 거절됨", Toast.LENGTH_SHORT).show()
                finish()
            }
            PERMISSION_ACCESS_COARSE_LOCATION -> if (checkAppPermission(permissions)) { //퍼미션 동의했을 때 할 일
                Toast.makeText(this, "권한이 승인됨", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "권한이 거절됨", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    } // onRequestPermissionsResult
    fun checkAccount(id: Long, nickname: String,p_imageURL:String){
        input_id=id.toString()
        imageURL = p_imageURL
        Log.v("checkid", input_id +" "+ nickname)
        input_password = nickname
        if(IDcheckVaild()) { // 이미 생성된 계정
            Log.v("checkidtrue", "true")
            LoadMain(input_id, nickname)
        }
        else {
            val items = User_info(input_id,0,0,10)
            rdb.child(input_id).setValue(items)
            LoadMain(input_id, nickname)
            Log.v("checkidtrue", "false")
        }
    }
    fun initFirebase(){
        rdb = FirebaseDatabase.getInstance().getReference("USERID") //테이블을 만듬
        rdb.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }
            override fun onDataChange(p0: DataSnapshot) {
                user_info_list.clear()
                for (data in p0.children) {
                    val pdata = data.getValue(User_info::class.java)
                    Log.d("pdata",pdata!!.id)
                    if(pdata != null){
                        user_info_list.add(pdata)
                    }
                }
            }
        })
    }
    fun IDcheckVaild():Boolean{
        if(user_info_list.size > 0){
            for(i in 0..user_info_list.size-1){
                if(input_id.equals(user_info_list[i].id))
                {
                    Log.v("checkidcur", input_id)
                    return true
                }
            }
            return false
        }
        else
            return false

    }
    fun LoadMain(id:String, nickname : String){ // 일반 로그인
        val intent = Intent(this,MainActivity::class.java)

            MainActivity.user_info = user_info_list.find {
                it.id == input_id
            }
        if(MainActivity.user_info == null){ // 새로만들어진 경우
            MainActivity.user_info = User_info(input_id,0,0,10)
        }

        Log.d("user_info",MainActivity.user_info!!.id)
        startActivity(intent)
        finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (Session.getCurrentSession().handleActivityResult(requestCode, resultCode, data)) {
            return
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
}
