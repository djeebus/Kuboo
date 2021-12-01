package com.sethchhim.kuboo_client.ui.reader.comic

import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.github.ybq.android.spinkit.SpinKitView
import com.sethchhim.kuboo_client.Constants.ARG_BOOK
import com.sethchhim.kuboo_client.Constants.ARG_LOCAL
import com.sethchhim.kuboo_client.Constants.ARG_POSITION
import com.sethchhim.kuboo_client.Extensions.visible
import com.sethchhim.kuboo_client.data.ViewModel
import com.sethchhim.kuboo_client.util.SystemUtil
import com.sethchhim.kuboo_remote.model.Book
import dagger.android.support.DaggerFragment
import javax.inject.Inject

open class ReaderComicFragment : DaggerFragment() {

    @Inject lateinit var systemUtil: SystemUtil
    @Inject lateinit var viewModel: ViewModel

    protected var position = 0
    protected var isLocal = false

    lateinit var book: Book
    lateinit var readerComicActivity: ReaderComicActivity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        readerComicActivity = (activity as ReaderComicActivity)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.apply {
            isLocal = getBoolean(ARG_LOCAL)
            position = getInt(ARG_POSITION)
            book = getParcelable(ARG_BOOK)!!
        }
    }

    protected fun getPage1() = viewModel.getReaderItemAt(position)?.page0 ?: ""

    protected fun getPage2() = viewModel.getReaderItemAt(position)?.page1 ?: ""

    protected fun getPage1ToInt() = try {
        getPage1().toInt()
    } catch (e: NumberFormatException) {
        0
    }

    protected fun getPage2ToInt() = try {
        getPage2().toInt()
    } catch (e: NumberFormatException) {
        0
    }

    protected fun ImageView.loadImage(source: Any, requestListener: RequestListener<Bitmap>) {
        val requestOptions = RequestOptions()
                .format(DecodeFormat.PREFER_RGB_565)
                .skipMemoryCache(true)
                .override(Target.SIZE_ORIGINAL)
        Glide.with(this@ReaderComicFragment)
                .asBitmap()
                .load(source)
                .apply(requestOptions)
                .listener(requestListener)
                .into(this)
    }

    protected fun ImageView.setScaleToPip(singlePane: Boolean) {
        adjustViewBounds = readerComicActivity.isInPipMode && singlePane
    }

    protected fun SpinKitView.setVisibilityToPip() {
        if (!readerComicActivity.isInPipMode) {
            visible()
        }
    }

}
