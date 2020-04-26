package com.ranrings.nomorelogslib

class HttpTransaction() {
    var id : Int = 0
    var methodType : String = ""
    var url : String = ""
    var requestHeaders : HashMap<String,String> = HashMap()
    var requestBody : String = ""
    var responseHeaders : HashMap<String,String>  = HashMap()
    var responseStatus : String = ""
    var responseBody : String = ""
    var timeTakenInMiliSeconds : Int = 0
}

