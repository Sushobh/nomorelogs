package com.ranrings.nomorelogslib

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.net.wifi.WifiManager
import android.os.Build
import android.text.format.Formatter
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.ranrings.libs.androidapptorest.AndroidRestServer
import com.ranrings.libs.androidapptorest.Base.GetRequestHandler
import com.ranrings.libs.androidapptorest.Base.PostRequestHandler
import com.ranrings.libs.androidapptorest.HandlerRepo.PublicFileRequestHandler
import com.ranrings.libs.androidapptorest.HandlerRepo.WebAppRequestHandler
import com.ranrings.libs.androidapptorest.WebAppExtractor
import com.ranrings.nomorelogslib.HttpTransactionRepo.Companion.transactionList
import com.ranrings.nomorelogslib.apistopper.ApiModifiedResponse
import com.ranrings.nomorelogslib.apistopper.ApiStopEnable
import com.ranrings.nomorelogslib.apistopper.CurrentReceivedApiResponse
import com.ranrings.nomorelogslib.apistopper.ResponseModifyInterceptor
import java.io.File

class ApiLogServer {




    companion object {
        private const val ZIPNAME = "WebLogApp.zip"
        var FOLDER_NAME = "fh832r8hf8ewh8r73w"
        private const val REAL_FOLDER_NAME = "WebLogApp"
        private const val NOTIFICATION_CHANNEL_ID = "213123"


        var MAX_API_CALLS_TO_SEND = 15
        var PORT = 3000
        private lateinit var  androidRestServer  : AndroidRestServer


        fun start(application: Application, portToRunServerOn: Int, maxHistoryOfApiCalls: Int) {
            if(this::androidRestServer.isInitialized) {
                return
            }
            PORT = portToRunServerOn
            MAX_API_CALLS_TO_SEND = maxHistoryOfApiCalls
                initServerObject(application)
                extractWebApp(application)
                createNotificationChannel(application)
                displayNotification(application)
                androidRestServer.start()
        }

        private fun initServerObject(application: Application){
            androidRestServer = AndroidRestServer.Builder()
                .setApplication(application)
                .setPort(PORT)
                .startWebApp(false)
                .addRequestHandler(object : GetRequestHandler<List<HttpTranscationRowItem>>() {
                    override fun getMethodName(): String {
                        return "getapilist"
                    }

                    override fun onGetRequest(uri: String): List<HttpTranscationRowItem> {
                        return HttpTransactionRepo.transactionList.map {
                            HttpTranscationRowItem(
                                it.methodType,
                                it.responseStatus,
                                it.url,
                                it.id
                            )
                        }.subList(
                            if (transactionList.size - MAX_API_CALLS_TO_SEND >= 0) {
                                transactionList.size - MAX_API_CALLS_TO_SEND
                            } else {
                                0
                            },
                            transactionList.size
                        ).sortedByDescending { it.serialNumber }
                    }

                })
                .addRequestHandler(object :
                    PostRequestHandler<HttpTranscationRowItem, HttpTransaction>(
                        HttpTranscationRowItem::class
                    ) {
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
                        return getWebFolderPath(application) + "/index.html"
                    }

                    override fun getMethodName(): String {
                        return "apilogapp"
                    }
                })
                .addRequestHandler(object : WebAppRequestHandler(application) {
                    override fun getIndexHtmlFilePath(): String {
                        return getWebFolderPath(application) + "/index.html"
                    }

                    override fun getMethodName(): String {
                        return "apiPause"
                    }
                })
                .addRequestHandler(object : PublicFileRequestHandler(application) {
                    override fun getFilePath(uri: String): String {
                        return getWebFolderPath(application) + "/${uri.split("/")[2]}"
                    }

                    override fun getMethodName(): String {
                        return "weblogpublic"
                    }
                }).
                addRequestHandler(object : PublicFileRequestHandler(application) {
                    override fun getFilePath(uri: String): String {
                        val assetFilePath = uri.substring(uri.indexOf('/') + 1)
                        val fullPath = getWebFolderPath(application) + "/${assetFilePath}"
                        return fullPath
                    }

                    override fun getMethodName(): String {
                        return "assets"
                    }

                    override fun getMimeType(requestUri: String): String {
                        val mimeType = super.getMimeType(requestUri)
                        if (mimeType.equals("application/css")) {
                            return "text/css"
                        }
                        return mimeType
                    }
                }).
                addRequestHandler(object : PostRequestHandler<ApiStopEnable, String>(
                    ApiStopEnable::class
                ) {
                    override fun getMethodName(): String {
                        return "enableapistop"
                    }

                    override fun onRequest(requestBody: ApiStopEnable): String {
                        ResponseModifyInterceptor.waitForResponseInput =
                            requestBody.enableApiStop
                        return "Done"
                    }

                }).
                addRequestHandler(object : PostRequestHandler<ApiModifiedResponse, String>(
                    ApiModifiedResponse::class
                ) {
                    override fun getMethodName(): String {
                        return "modifyResponse"
                    }

                    override fun onRequest(requestBody: ApiModifiedResponse): String {
                        ResponseModifyInterceptor.submittedInput =
                            ResponseModifyInterceptor.currentReceivedResponse!!.newBuilder()
                                .code(requestBody.responseCode)
                                .body(
                                    ResponseModifyInterceptor.createRequestBodyFromJsonString(
                                        requestBody.responseBody
                                    )
                                ).build()
                        ResponseModifyInterceptor.waitForResponseInput = false
                        return "Done"
                    }

                }).addRequestHandler(object : GetRequestHandler<CurrentReceivedApiResponse>() {
                    override fun getMethodName(): String {
                        return "getCurrentState"
                    }

                    override fun onGetRequest(uri: String): CurrentReceivedApiResponse {
                        ResponseModifyInterceptor.currentReceivedResponse?.let {
                            ResponseModifyInterceptor.currentReceivedResponseJson?.run {
                                return CurrentReceivedApiResponse(
                                    it.code, this, ResponseModifyInterceptor.currentUrl, true,
                                    ResponseModifyInterceptor.waitForResponseInput
                                )
                            }
                        }
                        return CurrentReceivedApiResponse(
                            null, null, "", false,
                            ResponseModifyInterceptor.waitForResponseInput
                        )
                    }

                })
                .build()
        }

        private fun extractWebApp(application: Application){
            val webfolder = File(getWebFolderPath(application))
            if(!webfolder.exists()) {
                WebAppExtractor(application, FOLDER_NAME).extractFromAssets(ZIPNAME)
            }
        }

        private fun getWebFolderPath(context: Context) : String{
            return context.filesDir.path+"/$FOLDER_NAME/$REAL_FOLDER_NAME"
        }

        private fun createNotificationChannel(application: Application) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val name: CharSequence = application.getString(R.string.channel_name)
                val description: String = application.getString(R.string.channel_description)
                val importance = NotificationManager.IMPORTANCE_DEFAULT
                val channel =
                    NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance)
                channel.description = description
                // Register the channel with the system; you can't change the importance
                // or other notification behaviors after this
                val notificationManager: NotificationManager = application.getSystemService(
                    NotificationManager::class.java
                )
                notificationManager.createNotificationChannel(channel)
            }
        }

        private fun displayNotification(application: Application) {
            val builder = NotificationCompat.Builder(
                application,
                NOTIFICATION_CHANNEL_ID
            )
                .setSmallIcon(R.drawable.ic_wood)
                .setContentTitle("View Logs here")
                .setOngoing(true)
                .setContentText(getIpAddress(application) + ":" + PORT + "/apilogapp")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            val notificationManager = NotificationManagerCompat.from(application)
            notificationManager.notify(100, builder.build())
        }

        private fun getIpAddress(application: Application): String {
            val wifiMgr = application.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val wifiInfo = wifiMgr.connectionInfo
            val ip = wifiInfo.ipAddress
            return Formatter.formatIpAddress(ip)
        }


    }



}