package com.ranrings.nomorelogslib.apistopper

data class CurrentReceivedApiResponse(var code  : Int? , var string: String? ,
                                      var currentUrl : String,
                                      var hasBeenReceived : Boolean , var isApiStopped : Boolean) {
}