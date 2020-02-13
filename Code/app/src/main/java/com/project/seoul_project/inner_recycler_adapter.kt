package com.project.seoul_project
import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView

class inner_recycler_adapter (var pData: ArrayList<Train_Info>)
    : RecyclerView.Adapter<inner_recycler_adapter.ViewHolder>() { // will be error if no inner class(ViewHolder) defined

    lateinit var context:Context
    var activity = this
    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder { // inflate layout and return
        // TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        val v = LayoutInflater.from(p0.context).inflate(R.layout.searcglist_card, p0, false)
        context = p0.context
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        //  TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        return pData.size
    }

    override fun onBindViewHolder(p0: ViewHolder, p1: Int) {
        var status_str = ""
        when(pData.get(p1).status){
            0-> status_str = "진입"
            1->status_str = "도착"
            else->status_str =  "출발"
        }
        p0.location.text = pData.get(p1).current_station + " " + status_str
        if(pData.get(p1).subwawyNm == "2호선"){
            when(pData.get(p1).updnLine){
                0 -> p0.direction.text = "외선";
                1 ->p0.direction.text = "내선";
            }
        }
        else{
            when(pData.get(p1).updnLine){
                0->p0.direction.text = "상행선";
                1->p0.direction.text = "하행선";
            }
        }
        when(pData.get(p1).directAt){
            1->p0.directat.text = "급행"
            0->p0.directat.text="일반"
        }
        p0.trainno.text = pData.get(p1).trainNo
        p0.dest.text = pData.get(p1).statnTnm
        p0.time.text = pData.get(p1).recptime
        p0.select.setOnClickListener {
            val intent = Intent(context,MainActivity::class.java)
            intent.putExtra("Using_train_info",Train_Info(pData.get(p1).subwawyNm,pData.get(p1).trainNo,pData.get(p1).updnLine,pData.get(p1).statnTnm,pData.get(p1).directAt,
                pData.get(p1).current_station,pData.get(p1).status,pData.get(p1).recptime))
            (context as Activity).setResult(RESULT_OK,intent)
            (context as Activity).finish()

        }

    }


    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var location: TextView
        var direction: TextView
        var dest: TextView
        var time: TextView
        var select: ImageButton
        var directat:TextView
        var trainno:TextView

        init {
            trainno = itemView.findViewById(R.id.inner_trainno_text)
            directat = itemView.findViewById(R.id.inner_directat_textview)
            location = itemView.findViewById(R.id.inner_train_location_text)
            direction = itemView.findViewById(R.id.inner_train_direction_text)
            dest = itemView.findViewById(R.id.inner_train_dest_text)
            time = itemView.findViewById(R.id.inner_train_time_text)
            select = itemView.findViewById<ImageButton>(R.id.select_button)

        }
    }
}

