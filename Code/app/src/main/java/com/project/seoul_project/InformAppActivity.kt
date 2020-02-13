package com.project.seoul_project

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_inform_app.*

class InformAppActivity : AppCompatActivity() {

    val phone = "010-7472-3267"
    val email = "rlat302@gmail.com"
    val developer = "김성헌"
    val image = "flaticon"
    val app_version = "1.0"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_inform_app)
        inform_app_button.setOnClickListener {
            finish()
        }
        inform_text1.setText(app_version)
        inform_text2.setText(image)
        inform_text3.setText(developer)
        inform_text4.setText(email)
        inform_text5.setText(phone)
        qnxkr_text.setText("안녕하세요. 개발자 입니다. \n 혹시 사용하시다가 불편하신점이 있으시면 꼭!! 개인 이메일이나 핸드폰으로 알려주세요.\n" +
                "빠르게 수정하여 새로운 버전으로 업데이트 하겠습니다!! 감사합니다!!")

    }
}
