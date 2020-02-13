package com.project.seoul_project



import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView


class Share_recycler_adapter(var station_info: ArrayList<All_Station_Info>)
    : RecyclerView.Adapter<Share_recycler_adapter.ViewHolder>(){ // will be error if no inner class(ViewHolder) defined

    interface OnItemClickListner {
        fun OnItemClick(holder: Share_recycler_adapter.ViewHolder, view: View, data: All_Station_Info, position: Int)
    }

    var ItemClickListener: OnItemClickListner? = null

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder { // inflate layout and return
        val v = LayoutInflater.from(p0.context).inflate(R.layout.share_recycler_card,p0,false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return station_info.size
    }
    override fun onBindViewHolder(p0: ViewHolder, p1: Int) { // define relations with recipelist(cardViewLayout) and elements of rData
        p0.stationName.text = station_info.get(p1).station_num
        p0.linenum_text.text = station_info.get(p1).line_num
        when(station_info.get(p1).line_num){
            "1호선"->p0.linenum.setImageResource(com.project.seoul_project.R.drawable.line1)
            "2호선"->p0.linenum.setImageResource(com.project.seoul_project.R.drawable.line2)
            "3호선"->p0.linenum.setImageResource(com.project.seoul_project.R.drawable.line3)
            "4호선"->p0.linenum.setImageResource(com.project.seoul_project.R.drawable.line4)
            "5호선"->p0.linenum.setImageResource(com.project.seoul_project.R.drawable.line5)
            "6호선"->p0.linenum.setImageResource(com.project.seoul_project.R.drawable.line6)
            "7호선"->p0.linenum.setImageResource(com.project.seoul_project.R.drawable.line7)
            "8호선"->p0.linenum.setImageResource(com.project.seoul_project.R.drawable.line8)
            "9호선"->p0.linenum.setImageResource(com.project.seoul_project.R.drawable.line9)
            "경춘선"->p0.linenum.setImageResource(com.project.seoul_project.R.drawable.kyungchun)
            "분당선"->p0.linenum.setImageResource(com.project.seoul_project.R.drawable.bundang)
            "신분당선"->p0.linenum.setImageResource(com.project.seoul_project.R.drawable.newbundang)
            "경의중앙선"->p0.linenum.setImageResource(com.project.seoul_project.R.drawable.kyungyui)
            "인천선"->p0.linenum.setImageResource(com.project.seoul_project.R.drawable.incheon1)
            "인천2호선"->p0.linenum.setImageResource(com.project.seoul_project.R.drawable.incheon2)
            "공항철도"->p0.linenum.setImageResource(com.project.seoul_project.R.drawable.airport)
            "용인경전철"->p0.linenum.setImageResource(com.project.seoul_project.R.drawable.yongin)
            "우이신설경전철"->p0.linenum.setImageResource(com.project.seoul_project.R.drawable.ui)
            "의정부경전철"->p0.linenum.setImageResource(com.project.seoul_project.R.drawable.uijeongbu)
            "경강선"->p0.linenum.setImageResource(com.project.seoul_project.R.drawable.kyungkang)
            "서해선"->p0.linenum.setImageResource(com.project.seoul_project.R.drawable.west)
            "수인선"->p0.linenum.setImageResource(com.project.seoul_project.R.drawable.suin)
        }
    }
    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){

        var stationName: TextView
        var linenum: ImageView
        var linenum_text:TextView


        init{
            stationName = itemView.findViewById(R.id.station_textview)
            linenum_text= itemView.findViewById(R.id.linenum_text)
            linenum=itemView.findViewById<ImageView>(R.id.line_imageview)
            itemView.setOnClickListener {
                val position = adapterPosition
                ItemClickListener?.OnItemClick(this, it, station_info[position], position)
            }
        }


    }
}