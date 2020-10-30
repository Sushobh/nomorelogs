package com.ranrings.nomorelogslib

internal class HttpTransactionRepo : HttpTransactionListener {
    companion object {
        val transactionList = arrayListOf<HttpTransaction>()
    }

    var currentId = 0

    override fun getNewHttpTransaction(): HttpTransaction {
        val transac = HttpTransaction()
        transac.id = currentId
        currentId = currentId + 1
        transactionList.add(transac)
        return transac
    }

    override fun onTransactionStarted(httpTransaction: HttpTransaction) {

    }

    override fun onTransactionComplete(httpTransaction: HttpTransaction) {

    }

    override fun onTransactionFailed(message: String, httpTransaction: HttpTransaction) {

    }

}