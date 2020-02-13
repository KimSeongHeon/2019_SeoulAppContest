package com.project.seoul_project
import android.Manifest
import android.Manifest.permission.CALL_PHONE
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.support.v4.app.ActivityCompat.requestPermissions
import android.support.v4.content.PermissionChecker.checkSelfPermission
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView

class Call_recycler_adapter (var pData: ArrayList<Pair<String,String>>)
    : RecyclerView.Adapter<Call_recycler_adapter.ViewHolder>() { // will be error if no inner class(ViewHolder) defined

    lateinit var context:Context
    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder { // inflate layout and return
        // TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        val v = LayoutInflater.from(p0.context).inflate(R.layout.report_call_caard, p0, false)
        context = p0.context
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        //  TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        return pData.size
    }

    override fun onBindViewHolder(p0: ViewHolder, p1: Int) {
        p0.call.setOnClickListener {

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {

                if (checkSelfPermission(context, CALL_PHONE) == PackageManager.PERMISSION_DENIED) {

                    Log.d("permission", "permission denied to SEND_SMS - requesting it")
                    val permissions = arrayOf<String>(Manifest.permission.CALL_PHONE)

                    requestPermissions(context as Activity,permissions, 2)

                }
            }
            if(checkSelfPermission(context, CALL_PHONE) == PackageManager.PERMISSION_GRANTED){
                val intent = Intent(Intent.ACTION_CALL,Uri.parse("tel:"+ pData.get(p1).second))
                (context as Activity).startActivity(intent)
            }


        }
        p0.phone.text = pData.get(p1).second
        p0.name.text = pData.get(p1).first

    }


    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var phone: TextView
        var name: TextView
        var call: ImageButton

        init {
            phone = itemView.findViewById(R.id.call_phone_textview)
            name = itemView.findViewById(R.id.call_name_textview)
            call = itemView.findViewById(R.id.call_imagebutton)

        }
    }
}

