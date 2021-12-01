package com.sethchhim.kuboo_client.ui.reader.base

import android.annotation.SuppressLint
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.sethchhim.kuboo_client.Extensions.removeAllObservers
import com.sethchhim.kuboo_client.Temporary
import com.sethchhim.kuboo_client.data.enum.Source
import com.sethchhim.kuboo_client.data.model.PageUrl
import com.sethchhim.kuboo_client.data.model.Progress
import com.sethchhim.kuboo_remote.model.Book

@Suppress("UNCHECKED_CAST")
@SuppressLint("Registered")
open class ReaderBaseActivityImpl4_Content : ReaderBaseActivityImpl3_Overlay() {

    private val dualPaneLiveData = MutableLiveData<Boolean>()

    protected fun populateDualPaneList(): MutableLiveData<Boolean> {
        dualPaneLiveData.removeAllObservers(this)
        when (isLocal) {
            true -> viewModel.getSinglePaneList().toLocalDualPaneList()
            false -> viewModel.getSinglePaneList().toRemoteDualPaneList()
        }
        return dualPaneLiveData
    }

    protected fun populateSinglePaneList() {
        viewModel.setSinglePaneList(when (isLocal) {
            true -> viewModel.createLocalSinglePaneList(currentBook)
            false -> viewModel.createRemoteSinglePaneReaderList(currentBook)
        })
    }

    protected fun isDownload() = source == Source.DOWNLOAD

    private fun List<PageUrl>.toLocalDualPaneList() = viewModel.singleToDualLocal(this).apply {
        (this[0] as MutableLiveData<Progress>).observe(this@ReaderBaseActivityImpl4_Content, Observer { result -> result?.let { handleDualPaneLoadingProgress(it) } })
        (this[1] as MutableLiveData<List<PageUrl>>).observe(this@ReaderBaseActivityImpl4_Content, Observer { result -> result?.let { handleDualPaneResult(it) } })
    }

    private fun List<PageUrl>.toRemoteDualPaneList() = viewModel.singleToDualRemote(this@ReaderBaseActivityImpl4_Content, this).apply {
        (this[0] as MutableLiveData<Progress>).observe(this@ReaderBaseActivityImpl4_Content, Observer { result -> result?.let { handleDualPaneLoadingProgress(it) } })
        (this[1] as MutableLiveData<List<PageUrl>>).observe(this@ReaderBaseActivityImpl4_Content, Observer { result -> result?.let { handleDualPaneResult(it) } })
    }

    private fun handleDualPaneResult(list: List<PageUrl>) {
        viewModel.setDualPaneList(list)
        dualPaneLiveData.value = true
    }

    internal open fun goToFirstPage() {
        //override in children
    }

    internal open fun goToLastPage() {
        //override in children
    }

    internal open fun goToPreviousPage() {
        //override in children
    }

    internal open fun goToNextPage() {
        //override in children
    }

    protected open fun startNextBook() {
        currentBook.isFinished = true
        Temporary.USER_API_UPDATE_LIST.add(currentBook)
//        viewModel.addFinish(currentBook)
        addFinishedDownload(currentBook)
        deleteFinishedFetchDownload(currentBook)
        //override in children
    }

    protected open fun finishBook() {
        currentBook.isFinished = true
        Temporary.USER_API_UPDATE_LIST.add(currentBook)
//        viewModel.addFinish(currentBook)
        addFinishedDownload(currentBook)
        //override in children
    }

    protected fun startDownloadTracking(book: Book) {
        if (isDownload() && book.isFavorite) {
            trackingService.startTrackingByBook(viewModel.getActiveLogin(), book)
        }
    }

    private fun addFinishedDownload(book: Book){
        if (isDownload()) {
            viewModel.addDownload(book)
        }
    }

    protected fun deleteFinishedFetchDownload(book: Book) {
        if (isDownload() && book.isFavorite) {
            viewModel.deleteFetchDownload(book)
        }
    }

}
