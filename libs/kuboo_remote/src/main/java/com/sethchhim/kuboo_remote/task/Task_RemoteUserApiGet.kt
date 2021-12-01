package com.sethchhim.kuboo_remote.task

import androidx.lifecycle.MutableLiveData
import com.sethchhim.kuboo_remote.KubooRemote
import com.sethchhim.kuboo_remote.model.Book
import com.sethchhim.kuboo_remote.model.Login
import okhttp3.Call
import okhttp3.ResponseBody
import org.json.JSONObject
import timber.log.Timber

class Task_RemoteUserApiGet(kubooRemote: KubooRemote, login: Login, book: Book) : Task_RemoteUserApiBase(kubooRemote, login, book) {

    internal val liveData = MutableLiveData<Book>()
    private val startTime = System.currentTimeMillis()

    init {
        kubooRemote.networkIO.execute {
            kubooRemote.mainThread.execute { liveData.value = null }
        }
    }

    /**
     * The following line is an example of the remote bookmark api for formatting purposes:
     * "mark" : "15#0.0"
     *
     * Note1: First value before the # symbol is page number, Second value after the # symbol is scroll position.
     * Note2: If page position is not saved, the # symbol will be missing.
     */
    private fun JSONObject.getMark(): List<String> {
        val bookmarkPage: String
        val bookmarkScrollPosition: String

        val markResult = getString("mark")
        when (markResult.contains("#")) {
            true -> {
                val resultPoundPosition = markResult.indexOf("#")
                val resultPageNumber = markResult.substring(0, resultPoundPosition)
                val resultPagePosition = markResult.substring(resultPoundPosition + 1, markResult.lastIndex)

                bookmarkPage = resultPageNumber
                bookmarkScrollPosition = resultPagePosition
            }
            false -> {
                when (markResult == "null") {
                    true -> {
                        bookmarkPage = "0"
                        Timber.e("UserApi bookmark page return literal string null! There was probably an error while saving.")
                    }
                    false -> bookmarkPage = markResult
                }
                bookmarkScrollPosition = "0.0"
            }
        }
        return listOf(bookmarkPage, bookmarkScrollPosition)
    }

    /**
     * The following line is an example of the remote isFinish api for formatting purposes:
     *  "isFinished" : false
     */
    private fun JSONObject.getIsFinished() = getBoolean("isFinished")

}
