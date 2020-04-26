package com.ranrings.nomorelogslib

import android.app.Application
import android.content.Context
import com.ranrings.libs.androidapptorest.AndroidRestServer
import com.ranrings.libs.androidapptorest.Base.GetRequestHandler
import com.ranrings.libs.androidapptorest.Base.PostRequestHandler
import com.ranrings.libs.androidapptorest.HandlerRepo.PublicFileRequestHandler
import com.ranrings.libs.androidapptorest.HandlerRepo.WebAppRequestHandler
import com.ranrings.libs.androidapptorest.WebAppExtractor

import com.ranrings.nomorelogslib.HttpTransactionRepo.Companion.transactionList
import java.io.File

internal class ApiLogServer {




    companion object {
        var ZIPNAME = "WebLogApp.zip"
        var FOLDER_NAME = "fh832r8hf8ewh8r73w"
        val REAL_FOLDER_NAME = "WebLogApp"


        var MAX_API_CALLS_TO_SEND = 15
        var PORT = 3000
        private lateinit var  androidRestServer  : AndroidRestServer


        fun get(application : Application) : AndroidRestServer {
            if(this::androidRestServer.isInitialized) {
                return androidRestServer
            }
            androidRestServer = AndroidRestServer.Builder()
                    .setApplication(application)
                    .setPort(PORT)
                    .startWebApp(false)
                    .addRequestHandler(object : GetRequestHandler<List<HttpTranscationRowItem>>(){
                        override fun getMethodName(): String {
                            return "getapilist"
                        }

                        override fun onGetRequest(uri: String): List<HttpTranscationRowItem> {
                            return HttpTransactionRepo.transactionList.map {
                                HttpTranscationRowItem(it.methodType,it.responseStatus,it.url,it.id)
                            }.subList(
                                    if(transactionList.size - MAX_API_CALLS_TO_SEND >= 0) {
                                        transactionList.size- MAX_API_CALLS_TO_SEND
                                    }else {
                                        0
                                    },
                                   transactionList.size
                            ).sortedByDescending { it.serialNumber }
                        }

                    })
                    .addRequestHandler(object : PostRequestHandler<HttpTranscationRowItem, HttpTransaction>(HttpTranscationRowItem::class) {
                        override fun getMethodName(): String {
                           return "getapidetails"
                        }

                        override fun onRequest(requestBody: HttpTranscationRowItem): HttpTransaction {
                            return HttpTransactionRepo.transactionList.find {
                                it.id == requestBody.serialNumber
                            } ?: HttpTransaction()
                        }

                    })
                    .addRequestHandler(object : WebAppRequestHandler(application) {
                        override fun getIndexHtmlFilePath(): String {
                            return getWebFolderPath(application)+"/index.html"
                        }

                        override fun getMethodName(): String {
                            return "apilogapp"
                        }
                    })
                    .addRequestHandler(object : PublicFileRequestHandler(application) {
                        override fun getFilePath(uri: String): String {
                            return getWebFolderPath(application)+"/${uri.split("/")[2]}"
                        }

                        override fun getMethodName(): String {
                            return "weblogpublic"
                        }
                    })
                    .build()
                    val webfolder = File(getWebFolderPath(application))
                    if(!webfolder.exists()) {
                        WebAppExtractor(application,FOLDER_NAME).extractFromAssets(ZIPNAME)
                    }

            return androidRestServer
        }

        fun getWebFolderPath(context: Context) : String{
            return context.filesDir.path+"/$FOLDER_NAME/$REAL_FOLDER_NAME"
        }

    }



}