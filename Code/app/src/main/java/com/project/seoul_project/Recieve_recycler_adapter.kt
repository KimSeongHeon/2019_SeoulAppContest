package com.project.seoul_project

import android.content.Context
import android.graphics.Color
import android.support.v7.widget.RecyclerView
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView

class Recieve_recycler_adapter (var bdata: ArrayList<Beacon_info_distance>)
    : RecyclerView.Adapter<Recieve_recycler_adapter.ViewHolder>() { // will be error if no inner class(ViewHolder) defined

    lateinit var context: Context
    var seats =
        ("UUU_AAAAAAA_AAAAAAA_AAAAAAA_UUU/"
                + "___R_______R_______R_______R___/"
                + "___R_______R_______R_______R___/"
                + "UUU_AAAAAAA_AAAAAAA_AAAAAAA_UUU/")


    var seatGaping = 3
    var seat_size = 100
    var seatViewList = ArrayList<TextView>()
    var selectedIds = ""
    var seat_flag = 0
    var detail_flag = 0

    interface OnItemClickListner {
        fun OnItemClick(holder: ViewHolder, view: View, data: Beacon_info_distance, position: Int)
    }

    var ItemClickListener: OnItemClickListner? = null

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder { // inflate layout and return
        // TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        val v = LayoutInflater.from(p0.context).inflate(R.layout.beacon_list_card, p0, false)
        context = p0.context
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        //  TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        return bdata.size
    }

    override fun onBindViewHolder(p0: ViewHolder, p1: Int) {
        p0.dest.text = bdata.get(p1).dest
        if(bdata.get(p1).distance == Float.MAX_VALUE) p0.distance.text = "범위 안에 없음"
        else  p0.distance.text = bdata.get(p1).distance.toString()+ " m"
        p0.seat.text = bdata.get(p1).seat
       // p0.reserve.setOnClickListener {}
        p0.detail.setOnClickListener {
                if(detail_flag == 0){
                    p0.detail.text = "접기"
                    detail_flag = 1
                    p0.init_seat(seat_flag,p1)
                    p0.frame.visibility = View.VISIBLE
                    p0.turn.visibility = View.VISIBLE
                }
                else{
                    p0.detail.text = "자세히 보기"
                    detail_flag = 0
                    p0.frame.visibility = View.GONE
                    p0.turn.visibility = View.INVISIBLE
                }

        }
        p0.turn.setOnClickListener {
            if(seat_flag == 0){
                seat_flag = 1
                p0.init_seat(1,p1)
            }
            else if(seat_flag == 1){
                seat_flag = 0
                p0.init_seat(0,p1)
            }
        }

    }


    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        //var reserve:Button
        var detail:Button
        var distance:TextView
        var dest:TextView
        var seat:TextView
        var layout:ViewGroup
        var turn:ImageButton
        var frame:LinearLayout

        init {
            //reserve = itemView.findViewById(R.id.recieve_reserve_button)
            detail = itemView.findViewById(R.id.recieve_detail_button)
            distance = itemView.findViewById(R.id.receive_distance_textview)
            dest = itemView.findViewById(R.id.receive_dest_textview)
            seat = itemView.findViewById(R.id.receive_seat_textview)
            layout = itemView.findViewById(R.id.card_layoutSeat)
            turn = itemView.findViewById(R.id.card_turn_imagebutton)
            frame = itemView.findViewById(R.id.card_frame)
            itemView.setOnClickListener {
                val position = adapterPosition
                ItemClickListener?.OnItemClick(this, it, bdata[position], position)
            }
        }
        fun init_seat(flag:Int,position:Int){
            seatViewList.clear()
            selectedIds = ""
            seats = "/" + seats;
            layout = itemView.findViewById(R.id.card_layoutSeat)
            layout.removeAllViewsInLayout()
            var layoutSeat =  LinearLayout(context);
            var params =  LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            layoutSeat.setOrientation(LinearLayout.VERTICAL);
            layoutSeat.setLayoutParams(params);
            layoutSeat.setPadding(1 * seatGaping, 1 * seatGaping, 1 * seatGaping, 1 * seatGaping);
            layout.addView(layoutSeat);
            //var layout:LinearLayout? = null;

            if(flag == 0){
                var count = 0;
                for (i in 0.. seats.length-1) {
                    if (seats.get(i)== '/') {
                        layout = LinearLayout(context);
                        (layout as LinearLayout).setOrientation(LinearLayout.HORIZONTAL);
                        layoutSeat.addView(layout);
                    } else if (seats.get(i) == 'U') {
                        var view =  TextView(context);
                        var layoutParams =  LinearLayout.LayoutParams(seat_size, seat_size);
                        layoutParams.setMargins(seatGaping, seatGaping, seatGaping, seatGaping);
                        view.setLayoutParams(layoutParams);
                        view.setPadding(0, 0, 0, 2 * seatGaping);
                        view.setGravity(Gravity.CENTER);
                        view.setBackgroundResource(R.drawable.fillsquare);
                        view.setTextColor(Color.WHITE);
                        view.setTextSize(20.toFloat())
                        view.setTag(1);
                        view.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 9.toFloat());
                        layout.addView(view);
                        seatViewList.add(view);
                    } else if (seats.get(i) ==  'A') {
                        count++;
                        var view =  TextView(context);
                        var layoutParams =  LinearLayout.LayoutParams(seat_size, seat_size);
                        layoutParams.setMargins(seatGaping, seatGaping, seatGaping, seatGaping);
                        view.setLayoutParams(layoutParams);
                        view.setPadding(0, 0, 0, 2 * seatGaping);
                        view.setId(count);
                        view.setGravity(Gravity.CENTER);
                        view.setBackgroundResource(R.drawable.square);
                        view.setText(count.toString() + "");
                        view.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 9.toFloat());
                        view.setTextColor(Color.BLACK);
                        view.setTag(2);
                        if(bdata.get(position).seat == count.toString()){
                            view.setBackgroundResource(R.drawable.textview_corner4)
                        }
                        layout.addView(view);
                        seatViewList.add(view);
                    } else if (seats.get(i) == 'R') {
                        var view =  TextView(context);
                        var layoutParams =  LinearLayout.LayoutParams(seat_size, seat_size);
                        layoutParams.setMargins(seatGaping, seatGaping, seatGaping, seatGaping);
                        view.setLayoutParams(layoutParams);
                        view.setPadding(0, 0, 0, 2 * seatGaping);
                        view.setGravity(Gravity.CENTER);
                        if(flag == 0)
                            view.setBackgroundResource(R.drawable.rightarrows1)
                        else if(flag == 1)
                            view.setBackgroundResource(R.drawable.leftarrows)
                        view.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 9.toFloat());
                        view.setTextColor(Color.WHITE);
                        view.setTag(3);
                        layout.addView(view);
                        seatViewList.add(view);
                    } else if (seats.get(i) == '_') {
                        var view = TextView(context);
                        var layoutParams = LinearLayout.LayoutParams(seat_size, seat_size);
                        layoutParams.setMargins(seatGaping, seatGaping, seatGaping, seatGaping);
                        view.setLayoutParams(layoutParams);
                        view.setBackgroundColor(Color.TRANSPARENT);
                        view.setText("");
                        layout.addView(view);
                    }
                }
            }
            else if(flag == 1){
                var count = 43
                for(i in seats.length-1 downTo 0){
                    if (seats.get(i)== '/') {
                        layout = LinearLayout(context);
                        (layout as LinearLayout).setOrientation(LinearLayout.HORIZONTAL);
                        layoutSeat.addView(layout);
                    } else if (seats.get(i) == 'U') {
                        var view =  TextView(context);
                        var layoutParams =  LinearLayout.LayoutParams(seat_size, seat_size);
                        layoutParams.setMargins(seatGaping, seatGaping, seatGaping, seatGaping);
                        view.setLayoutParams(layoutParams);
                        view.setPadding(0, 0, 0, 2 * seatGaping);
                        view.setGravity(Gravity.CENTER);
                        view.setBackgroundResource(R.drawable.fillsquare);
                        view.setTextColor(Color.WHITE);
                        view.setTextSize(20.toFloat())
                        view.setTag(1);
                        view.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 9.toFloat());
                        layout.addView(view);
                        seatViewList.add(view);
                        //view.setOnClickListener(this);
                    } else if (seats.get(i) ==  'A') {
                        count--;
                        var view =  TextView(context);
                        var layoutParams =  LinearLayout.LayoutParams(seat_size, seat_size);
                        layoutParams.setMargins(seatGaping, seatGaping, seatGaping, seatGaping);
                        view.setLayoutParams(layoutParams);
                        view.setPadding(0, 0, 0, 2 * seatGaping);
                        view.setId(count);
                        view.setGravity(Gravity.CENTER);
                        view.setBackgroundResource(R.drawable.square);
                        view.setText(count.toString() + "");
                        view.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 9.toFloat());
                        view.setTextColor(Color.BLACK);
                        view.setTag(2);
                        if(bdata.get(position).seat == count.toString()){
                            view.setBackgroundResource(R.drawable.textview_corner4)
                        }
                        layout.addView(view);
                        seatViewList.add(view);
                    } else if (seats.get(i) == 'R') {
                        var view =  TextView(context);
                        var layoutParams =  LinearLayout.LayoutParams(seat_size, seat_size);
                        layoutParams.setMargins(seatGaping, seatGaping, seatGaping, seatGaping);
                        view.setLayoutParams(layoutParams);
                        view.setPadding(0, 0, 0, 2 * seatGaping);
                        view.setGravity(Gravity.CENTER);
                        if(flag == 0)
                            view.setBackgroundResource(R.drawable.rightarrows1)
                        else if(flag == 1)
                            view.setBackgroundResource(R.drawable.leftarrows)
                        view.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 9.toFloat());
                        view.setTextColor(Color.WHITE);
                        view.setTag(3);
                        layout.addView(view);
                        seatViewList.add(view);
                        //view.setOnClickListener(this);
                    } else if (seats.get(i) == '_') {
                        var view = TextView(context);
                        var layoutParams = LinearLayout.LayoutParams(seat_size, seat_size);
                        layoutParams.setMargins(seatGaping, seatGaping, seatGaping, seatGaping);
                        view.setLayoutParams(layoutParams);
                        view.setBackgroundColor(Color.TRANSPARENT);
                        view.setText("");
                        layout.addView(view);
                    }
                }
            }

        }
    }




}

