package com.project.seoul_project

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.TabHost
import kotlinx.android.synthetic.main.activity_how_to.*

class HowToActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_how_to)
        init_tab()
        account_button.setOnClickListener {
            finish()
        }
        account_text0.setText("사용자는 모든 기능을 이용하기 위해서 열차선택을 하여야 합니다." +
                "보통 GPS와 네트워크를 통하여 가까운 역을 찾고, 자동으로 그 역에 도착하거나 출발한 열차를 찾는 식으로 이용중인 열차를 찾습니다. " +
                "하지만 이런 방식으로 찾지 못한다면 사용자는 펜과 종이가 있는 버튼을 누르고, 가까이에 있는 역을 입력하면, 그 역에 도착하거나 출발,혹은 진입하고 있는 열차정보가 검색됩니다. 해당하는 열차를 선택해주시면 됩니다.")
        account_text1.setText("공유하기 기능은 두가지로 나뉩니다. 첫째는 블루투스 통신으로 상대방과 통신하여 자리 정보를 공유하는 것이고 다른 하나는 공유자가 직접 입력하여 자리 정보를 공유하는 것입니다. " +
                "첫번째 방법을 이용하면 공유자는 도착지와 좌석 정보만 입력하면 됩니다. 좌석 정보만 입력하면 블루투스 통신을 통하여 상대방이 공유자와 얼마만큼 떨어져있는지 파악하여 위치를 찾을것입니다." +
                "두번쨰 방법을 이용하면 공유자는 도착지와 좌석 정보 뿐만 아니라 객실 정보(몇호실)인지도 입력하여야 합니다. 상대방은 그 정보를 통해 위치를 찾게 됩니다 " +
                "도착지를 입력하게 되면 예상 도착 시간에 진동으로 알람이 울리게 됩니다. 공유자가 혹시 지하철 이용중 자게 된다면 알람으로 사용하여도 좋습니다." +
                "도착지에 도착하게 되면 사용자는 앱을 종료하거나 로그아웃, 또는 돌아가고 있는 이미지를 클릭하여 공유를 중단하는 것이 좋습니다.(배터리 절약 ^^)")
        account_text2.setText("자리 찾기 기능도 두가지로 나뉩니다. 첫쨰는 블루투스 통신으로 공유하고 있는 자리를 찾는 것이고, 다른 하나는 공유자가 직접 입력해준 자리를 찾는것 입니다. " +
                "첫번째 방법을 이용하면 사용자는 80m 범위 이내의 블루투스 신호를 받게 됩니다. 받는 신호의 세기를 토대로 거리가 측정됩니다. 거리와 공유자의 좌석정보를 토대로 공유자가 앉아있는 찾아가서 대기하면 됩니다. " +
                "두번째 방법을 이용하면 사용자는 공유자가 직접 입력한 좌석번호와 객실번호를 보고 공유자의 좌석에 찾아가서 대기하면 됩니다. " +
                "공유자가 어디에 내릴지 공유되기때문에 사용자는 가장 빨리 내리는 공유자의 자리를 찾아가는것이 이득일 것입니다 :) " +
                "공유자가 내려야 할 역인데 혹시라도 자고 있다면 직접 꺠워주세요 ^-^..")
        account_text3.setText("같은 열차를 타고있는 사람들끼리 소통할 수 있는 기능입니다. " +
                "지하철 몇번째칸에서 취객이 난동을 피우니까 오지마세요. 혹은 ~~호실에 자리가 있어요! 빨리 앉으세요 등 같은 열차안의 사람들끼리 대화를 할 수 있습니다.")
        account_text4.setText("해당 열차에 불편한 점이 있다면 문자나 전화로 담당기관에 신고할 수 있습니다." +
                "저기 누가 음료수를 쏟고 도망갔어요.. 혹은 ~~호실에 문제가 생겼어요 등 불편사항을 문자나 전화를 통하여 보내주시면 됩니다. " +
                "번호는 이용중인 열차에 맞게 세팅 되어 있습니다. 내용만 입력하고 보내주시면 됩니다 :)")
    }
    fun init_tab(){
            val tabHost1 = findViewById<View>(R.id.howto_tabHost1) as TabHost
            tabHost1.setup()
            tabHost1.setOnTabChangedListener{

            }
            // 첫 번째 Tab. (탭 표시 텍스트:"TAB 1"), (페이지 뷰:"content1")
            val ts1 = tabHost1.newTabSpec("Tab Spec 1")
            ts1.setContent(R.id.howto_content0)
            ts1.setIndicator("열차 찾기")
            tabHost1.addTab(ts1)


            // 두 번째 Tab. (탭 표시 텍스트:"TAB 2"), (페이지 뷰:"content2")
            val ts2 = tabHost1.newTabSpec("Tab Spec 2")
            ts2.setContent(R.id.howto_content1)
            ts2.setIndicator("자리 공유")
            tabHost1.addTab(ts2)
        val ts3 = tabHost1.newTabSpec("Tab Spec 3")
        ts3.setContent(R.id.howto_content2)
        ts3.setIndicator("자리 찾기")
        tabHost1.addTab(ts3)
        val ts4 = tabHost1.newTabSpec("Tab Spec 4")
        ts4.setContent(R.id.howto_content3)
        ts4.setIndicator("열차 채팅")
        tabHost1.addTab(ts4)
        val ts5 = tabHost1.newTabSpec("Tab Spec 5")
        ts5.setContent(R.id.howto_content4)
        ts5.setIndicator("불편 신고")
        tabHost1.addTab(ts5)
    }

}
