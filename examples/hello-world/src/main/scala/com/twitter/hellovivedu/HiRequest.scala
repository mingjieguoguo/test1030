package com.twitter.hello

case class HiRequest(id: Long, name: String)
case class Uesr(userId:String,userName:String,userPwd:String)

//curl -XGET -d "http://localhost:8888/hi?name=zhngsan"
//
//curl -XPUT -d "http://localhost:8888/hi?name=zhngsan"
//
//swagger-ui.html