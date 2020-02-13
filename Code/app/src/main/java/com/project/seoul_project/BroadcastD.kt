package com.project.seoul_project

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.support.v4.app.NotificationCompat
import android.util.Log


class BroadcastD:BroadcastReceiver()
{

    override fun onReceive(context: Context?, intent: Intent?) {
        //TODO("not implemented") //To change body of created functions use File | Settings | File Temlates.
        Log.d("Onrecieve","Start")
        val CHANNELID = "Notification1"
        val notificationChannel = NotificationChannel(
            CHANNELID,
            "Time Check Notification",
            NotificationManager.IMPORTANCE_HIGH
        )

        notificationChannel.enableVibration(true)
        notificationChannel.vibrationPattern = longArrayOf(1000,3000,1000,3000,1000,3000,1000,3000,1000,3000,1000,3000,1000,3000)

        notificationChannel.enableLights(true)
        notificationChannel.lightColor = Color.BLUE

        val soundUri = RingtoneManager.getActualDefaultRingtoneUri(context, RingtoneManager.TYPE_NOTIFICATION)
        val audioBuilder = AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
            .build()
       // notificationChannel.setSound(soundUri,audioBuilder)

        notificationChannel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE

        val builder =  NotificationCompat.Builder(context!!, CHANNELID)
        builder.setSmallIcon(com.project.seoul_project.R.mipmap.ic_launcher)
        builder.setStyle(NotificationCompat.DecoratedCustomViewStyle())
        builder.setContentTitle("목적지"+ "(" + intent!!.getStringExtra("dest")+")"+"도착 예정시간입니다.")
        builder.setContentText("도착하셨으면 공유버튼을 눌러서 자리 공유를 중단해주십시오.")
        builder.setAutoCancel(true)
        val manager = context!!.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(notificationChannel)
        val notification = builder.build()
        manager.notify(2, notification)


    }

}
