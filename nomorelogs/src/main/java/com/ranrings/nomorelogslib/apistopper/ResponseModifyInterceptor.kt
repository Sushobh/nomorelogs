package com.ranrings.nomorelogslib.apistopper

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Response
import okhttp3.ResponseBody
import java.lang.Exception

class ResponseModifyInterceptor : Interceptor {

    var REQUEST_TIME_OUT = 5000L

    override fun intercept(chain: Interceptor.Chain): Response {
        resetState()
        return if(waitForResponseInput) {
            currentUrl = chain.request().url.toString()
            val response  = getResponse(chain)
            GlobalScope.launch {
                resetState()
            }
            response
        }
        else {
            chain.proceed(chain.request())
        }
    }


    fun getResponse(chain: Interceptor.Chain): Response {
       return runBlocking {

            launch(Dispatchers.IO) {
                val response = chain.proceed(chain.request())
                currentReceivedResponse = response
                currentReceivedResponseJson = response.body!!.string()
                currentReceivdCode = response.code

            }

            while(waitForResponseInput && !userHasSubmittedAnyInput()){
                Thread.sleep(200)
            }

            if(userHasSubmittedAnyInput()) {
                submittedInput!!
            }
            else {
                if(currentReceivedResponse != null) {
                  currentReceivedResponse =   currentReceivedResponse!!.newBuilder()
                            .body(createRequestBodyFromJsonString(currentReceivedResponseJson!!))
                            .build()
                }
                else {
                    Thread.sleep(REQUEST_TIME_OUT)
                }
                currentReceivedResponse =   currentReceivedResponse!!.newBuilder()
                        .body(createRequestBodyFromJsonString(currentReceivedResponseJson!!))
                        .build()
                resetState()
                currentReceivedResponse!!
            }

        }
    }






    private fun userHasSubmittedAnyInput() : Boolean{
        return submittedInput != null
    }





    companion object {
          var currentReceivedResponseJson : String? = null
          var currentReceivdCode : Int? = null
          var currentReceivedResponse : Response? = null
          var submittedInput : Response? = null
          var waitForResponseInput : Boolean = false
          var currentUrl = ""

          fun createRequestBodyFromJsonString(jsonString : String)  : ResponseBody {
              return ResponseBody.create(
                      "application/json; charset=utf-8".toMediaTypeOrNull(),
                      jsonString)
          }

          fun resetState(){
              currentReceivedResponseJson = null
              currentReceivdCode = null
              currentReceivedResponse
              submittedInput = null
              currentUrl = ""
          }
    }
}