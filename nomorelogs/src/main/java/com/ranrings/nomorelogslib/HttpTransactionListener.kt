package com.ranrings.nomorelogslib

interface HttpTransactionListener {
    fun getNewHttpTransaction() : HttpTransaction
    fun onTransactionStarted(httpTransaction: HttpTransaction)
    fun onTransactionComplete(httpTransaction: HttpTransaction)
    fun onTransactionFailed(message : String,  httpTransaction: HttpTransaction)
}