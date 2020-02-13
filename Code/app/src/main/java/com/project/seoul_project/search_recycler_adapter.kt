package com.project.seoul_project

import android.animation.ValueAnimator
import android.content.Context
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.util.SparseBooleanArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.google.gson.JsonParser
import com.project.seoul_project.R.id.*
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.net.URL


class search_recycler_adapter(var Station_Data: ArrayList<All_Station_Info>)
    : RecyclerView.Adapter<search_recycler_adapter.ViewHolder>(){ // will be error if no inner class(ViewHolder) defined

    val selectedItems = SparseBooleanArray()
    var prePosition = -1
    var Using_train_list = ArrayList<Train_Info>()
    lateinit var context:Context
    lateinit var inner_adapter:inner_recycler_adapter

    interface OnItemClickListner {
        fun OnItemClick(holder: search_recycler_adapter.ViewHolder, view: View, data: All_Station_Info, position: Int)
    }

    var ItemClickListener: OnItemClickListner? = null

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder { // inflate layout and return
        val v = LayoutInflater.from(p0.context).inflate(com.project.seoul_project.R.layout.search_card,p0,false)
        context = p0.context
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return Station_Data.size
    }
    override fun onBindViewHolder(p0: ViewHolder, p1: Int) { // define relations with recipelist(cardViewLayout) and elements of rData
        p0.onBind()
        p0.changeVisibility(selectedItems.get(p1));
        p0.stationName.text = Station_Data.get(p1).station_num
        p0.linenum_text.text = Station_Data.get(p1).line_num
        when(Station_Data.get(p1).line_num){
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
    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView),View.OnClickListener {
        var card:LinearLayout
        var stationName: TextView
        var linenum: ImageView
        var inner_rcyview: RecyclerView
        var frame:FrameLayout
        var linenum_text:TextView
        var no_train_text:TextView

        init {
            stationName = itemView.findViewById<TextView>(station_textview)
            linenum = itemView.findViewById<ImageView>(line_imageview)
            inner_rcyview = itemView.findViewById<RecyclerView>(R.id.inner_rcyview)
            card = itemView.findViewById<LinearLayout>(R.id.cardview)
            linenum_text=itemView.findViewById<TextView>(R.id.linenum_text)
            no_train_text = itemView.findViewById(R.id.no_train_text)
            frame = itemView.findViewById(R.id.frame)
            card.setOnClickListener{
                val position = adapterPosition
                ItemClickListener?.OnItemClick(this, it, Station_Data[position], position)
            }
        }

        override fun onClick(v: View?) {
            //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            when (v!!.id) {
                cardview -> {
                    if (selectedItems.get(adapterPosition)) {
                        Log.d("카드뷰","닫음")
                        Using_train_list.clear()
                        selectedItems.delete(adapterPosition)
                    } else {
                        selectedItems.delete(prePosition)
                        selectedItems.put(adapterPosition, true)
                       var train_thread = Thread(object : Runnable {
                            override fun run() {
                                try {
                                    Log.v("update 쓰레드", "동작 중")
                                    train_parsing(Station_Data.get(adapterPosition))
                                } catch (e: InterruptedException) {
                                    e.printStackTrace()
                                }
                            }
                        })
                        train_thread.start()
                        train_thread.join()
                        Log.d("카드뷰","염")

                    }
                    if (prePosition != -1) notifyItemChanged(prePosition);
                    notifyItemChanged(adapterPosition)
                    prePosition = adapterPosition
                    //쓰레드 동작.


                }
                R.id.select_button->{

                }
            }
        }
        fun onBind()
        {
            card.setOnClickListener(this)
            inner_adapter = inner_recycler_adapter(Using_train_list)
            inner_rcyview.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL,false)
            inner_rcyview.adapter = inner_adapter
            if(Using_train_list.size == 0){
                no_train_text.visibility = View.VISIBLE
                inner_rcyview.visibility = View.INVISIBLE
            }
            else{
                no_train_text.visibility = View.INVISIBLE
                inner_rcyview.visibility = View.VISIBLE
            }
        }

        fun changeVisibility(isExpanded: Boolean) {
            // height 값을 dp로 지정해서 넣고싶으면 아래 소스를 이용
            val dpValue = ViewGroup.LayoutParams.WRAP_CONTENT
            val d = context.resources.displayMetrics.density
            val height = (dpValue * d).toInt()

            // ValueAnimator.ofInt(int... values)는 View가 변할 값을 지정, 인자는 int 배열
            val va = if (isExpanded) ValueAnimator.ofInt(0, height) else ValueAnimator.ofInt(height, 0)
            // Animation이 실행되는 시간, n/1000초
            va.duration = 600
            va.addUpdateListener { animation ->
                // value는 height 값
                val value = animation.animatedValue as Int
                // imageView의 높이 변경
                frame.getLayoutParams().height = value
                frame.requestLayout()
                // imageView가 실제로 사라지게하는 부분
                frame.setVisibility(if (isExpanded) View.VISIBLE else View.GONE)
            }
            // Animation start
            va.start()
        }
        fun train_parsing(station_Info: All_Station_Info) {//현재 이용중인 지하철 번호 찾기
            Using_train_list.clear()
            var url: URL
            var key: String = "7266504a6f726c613130384f72494f50"
            var str = station_Info.line_num
            try {
                url = URL(
                    "http://swopenAPI.seoul.go.kr/api/subway/" + key + "/json/realtimePosition/0/99/" + str
                )
                var Is: InputStream = url.openStream()
                var rd: BufferedReader = BufferedReader(InputStreamReader(Is, "UTF-8"))
                var line: String? = null
                var page = ""
                line = rd.readLine()
                Log.v("line", line)
                while (line != null) {
                    page += line
                    Log.v("train_line", line)
                    line = rd.readLine()
                }
                Log.v("train_page", page)
                val parser = JsonParser()
                val json = parser.parse(page).asJsonObject
                val jarr = json.getAsJsonArray("realtimePositionList").asJsonArray
                var subwawyNm: String = "";
                var trainNo: String = "";
                var updn = 3;
                var statnTnm: String = "";
                var directAt: Int = 0;
                var statnNm = "";var trainSttus = -1; var recptime:String=""
                for (i in 0 until jarr.size()) {
                    statnNm = jarr[i].asJsonObject.get("statnNm").asString
                    Log.v("statnNm", statnNm)
                    if (statnNm == station_Info.station_num) {
                        subwawyNm = jarr[i].asJsonObject.get("subwayNm").asString
                        trainNo = jarr[i].asJsonObject.get("trainNo").asString
                        updn = jarr[i].asJsonObject.get("updnLine").asInt
                        statnTnm = jarr[i].asJsonObject.get("statnTnm").asString
                        directAt = jarr[i].asJsonObject.get("directAt").asInt
                        trainSttus = jarr[i].asJsonObject.get("trainSttus").asInt
                        recptime = jarr[i].asJsonObject.get("recptnDt").asString
                        if(statnTnm == "938") statnTnm = "중앙보훈병원"

                        Using_train_list.add(Train_Info(subwawyNm,trainNo,updn,statnTnm,directAt,statnNm,trainSttus,recptime))
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}