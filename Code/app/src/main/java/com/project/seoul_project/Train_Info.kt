package com.project.seoul_project

import java.io.Serializable

data class Train_Info(var subwawyNm:String,var trainNo:String,var updnLine:Int,var statnTnm:String,var directAt:Int,var current_station:String,var status:Int,var recptime:String):Serializable {//호선,열차번호,상행선 하행선, 종착 지하철역 명,급행 여부,현재 역위치,상태
}