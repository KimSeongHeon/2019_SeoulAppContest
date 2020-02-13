package com.project.seoul_project

import android.app.Activity
import android.support.v7.app.AlertDialog
import android.widget.Toast

class BackPressCloseHandler(context:Activity) {
    var backKeyPressedTime:Long = 0
    lateinit var toast:Toast
    var activity = context
    fun onBackPressed()
    {
        if (System.currentTimeMillis() > backKeyPressedTime + 2000) {
            backKeyPressedTime = System.currentTimeMillis()
            showGuide()
            return;
        }
        if (System.currentTimeMillis() <= backKeyPressedTime + 2000) {
            if(MainActivity.sharing_flag == 0){
                activity.finish()
                toast.cancel()
            }
            else{
                val builder = AlertDialog.Builder(activity)
                builder.setMessage("현재 자리가 공유중입니다. 종료하시면 자리 공유도 취소됩니다. 종료하시겠습니까?")
                    .setTitle("자리 공유 중임")
                    .setIcon(R.drawable.ic_mtrl_chip_checked_circle)
                builder.setPositiveButton("확인"){ _, _ ->
                    if(MainActivity.sharing_flag == 1){
                        MainActivity.beaconTransmitter.stopAdvertising()
                    }
                    activity.finish()
                    toast.cancel()
                }
                builder.setNegativeButton("취소"){_, _ ->

                }
                builder.show()
            }

        }
    }
    fun showGuide()
    {
        toast = Toast.makeText(activity, "\'뒤로\'버튼을 한번 더 누르시면 종료됩니다.", Toast.LENGTH_SHORT);
        toast.show();
    }

}