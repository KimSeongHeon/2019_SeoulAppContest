package com.project.seoul_project
import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView

class chat_recycler_adapter (var CData: ArrayList<ChatData>,var user_info: User_info)
    : RecyclerView.Adapter<chat_recycler_adapter.ViewHolder>() { // will be error if no inner class(ViewHolder) defined

    lateinit var context:Context
    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder { // inflate layout and return
        // TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        val v = LayoutInflater.from(p0.context).inflate(R.layout.message_card, p0, false)
        context = p0.context
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        //  TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        return CData.size
    }

    override fun onBindViewHolder(p0: ViewHolder, p1: Int) {

       // p0.id.text = CData.get(p1).USERID + "/" +CData.get(p1).time

        if(CData.get(p1).USERID != user_info.id){
            p0.date.text = CData.get(p1).USERID + " : " + CData.get(p1).time
            p0.content.text = CData.get(p1).content
            p0.innerLayout.setBackgroundResource(R.drawable.textview_corner3)
            p0.rightview.visibility = View.GONE
            p0.leftview.visibility = View.INVISIBLE
        }
        else{
            p0.date.text = " 나 "+ " : " + CData.get(p1).time
            p0.content.text = CData.get(p1).content
            p0.innerLayout.setBackgroundResource(R.drawable.textview_corner4)
            p0.rightview.visibility = View.INVISIBLE
            p0.leftview.visibility = View.GONE


            //p0.userid.text = " 나 "+ "    " + CData.get(p1).time
        }
    }


    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var content:TextView
        var layout:LinearLayout
        var innerLayout:LinearLayout
       var date:TextView
        var rightview:View
        var leftview:View
        //var profile:TextView
        init {
            content = itemView.findViewById(R.id.text)
            layout = itemView.findViewById(R.id.layout)
           date = itemView.findViewById(R.id.date_text)
            innerLayout = itemView.findViewById(R.id.inner_layout)
            rightview = itemView.findViewById(R.id.imageViewright)
            leftview = itemView.findViewById(R.id.imageViewleft)
           // profile = itemView.findViewById(R.id.chat_profile_textview)


        }
    }
}


