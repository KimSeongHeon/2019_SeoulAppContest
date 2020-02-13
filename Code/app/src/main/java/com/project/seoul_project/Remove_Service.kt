package com.project.seoul_project

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import com.google.firebase.database.FirebaseDatabase
import com.project.seoul_project.MainActivity.Companion.sharing_dest_time

class Remove_Service:Service() {
    override fun onBind(intent: Intent?): IBinder? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onDestroy() {
        super.onDestroy()
        /*Log.d("serviceondestory","on")
        if(MainActivity.sharing_flag == 1){
            Log.d("firebase_info", MainActivity.Date_str + MainActivity.Using_train.subwawyNm+ MainActivity.Using_train.trainNo + "_" + MainActivity.Using_train.statnTnm+ "_" + MainActivity.sharing_terminal_time + MainActivity.beacon.id1.toString())
            var e = FirebaseDatabase.getInstance().getReference("Train").child(MainActivity.Date_str).child(MainActivity.Using_train.subwawyNm)
                .child(MainActivity.Using_train.trainNo + "_" + MainActivity.Using_train.statnTnm+ "_" + MainActivity.sharing_terminal_time)
                .child("BeaconList").child(MainActivity.beacon.id1.toString()).removeValue().exception
            Log.d("firebase_exception",e.toString())

            if(sharing_dest_time != "")  MainActivity.am.cancel(MainActivity.sender)
            MainActivity.beaconTransmitter.stopAdvertising()
        }else if(MainActivity.sharing_flag == 2){
            var e = FirebaseDatabase.getInstance().getReference("Train").child(MainActivity.Date_str).child(MainActivity.Using_train.subwawyNm)
                .child(MainActivity.Using_train.trainNo + "_" + MainActivity.Using_train.statnTnm+ "_" + MainActivity.sharing_terminal_time)
                .child("SeatList").child(MainActivity.user_info!!.id.toString()).removeValue().exception
        }*/
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        Log.d("onTaskRemoved","on")
        if(MainActivity.sharing_flag == 1){
            Log.d("firebase_info", MainActivity.Date_str + MainActivity.Using_train.subwawyNm+ MainActivity.Using_train.trainNo + "_" + MainActivity.Using_train.statnTnm+ "_" + MainActivity.sharing_terminal_time + MainActivity.beacon.id1.toString())
            var e = FirebaseDatabase.getInstance().getReference("Train").child(MainActivity.Date_str).child(MainActivity.Using_train.subwawyNm)
                .child(MainActivity.Using_train.trainNo + "_" + MainActivity.Using_train.statnTnm+ "_" + MainActivity.sharing_terminal_time)
                .child("BeaconList").child(MainActivity.beacon.id1.toString()).removeValue().exception
            Log.d("firebase_exception",e.toString())

            if(sharing_dest_time != "")  MainActivity.am.cancel(MainActivity.sender)
            MainActivity.beaconTransmitter.stopAdvertising()
        }else if(MainActivity.sharing_flag == 2){
            var e = FirebaseDatabase.getInstance().getReference("Train").child(MainActivity.Date_str).child(MainActivity.Using_train.subwawyNm)
                .child(MainActivity.Using_train.trainNo + "_" + MainActivity.Using_train.statnTnm+ "_" + MainActivity.sharing_terminal_time)
                .child("SeatList").child(MainActivity.user_info!!.id.toString()).removeValue().exception
        }
    }
}