package com.sethchhim.kuboo_client.ui.preview

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.sethchhim.kuboo_client.Extensions.fadeVisible
import com.sethchhim.kuboo_client.Extensions.showDelayed
import com.sethchhim.kuboo_client.Extensions.toGlideUrl
import com.sethchhim.kuboo_client.Settings
import com.sethchhim.kuboo_client.ui.preview.custom.CropTransformation
import com.sethchhim.kuboo_remote.model.Login
import timber.log.Timber

@SuppressLint("Registered")
open class PreviewActivityImpl1_Content : PreviewActivityImpl0_View() {

    private fun getLowResRequest(login: Login, requestOptions: RequestOptions): RequestBuilder<Drawable> {
        val stringUrlLow = currentBook.getPreviewUrl()
        val glideUrlLow = stringUrlLow.toGlideUrl(login)
        return Glide.with(this)
                .load(glideUrlLow)
                .apply(requestOptions)
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                        Timber.e("Failed to load low res image: $stringUrlLow")
                        onLoadLowResFinished()
                        return false
                    }

                    override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                        Timber.i("Low res image found: $stringUrlLow")
                        onLoadLowResFinished()
                        return false
                    }
                })
    }

    private fun onLoadLowResFinished() {
        supportStartPostponedEnterTransition()
        fab.showDelayed()
        textView.fadeVisible()
    }

    protected fun preloadCurrentPage() {
        if (currentBook.isComic()) {
            val stringUrl = viewModel.getActiveServer() + currentBook.getPse(Settings.MAX_PAGE_WIDTH, currentBook.currentPage)
            Glide.with(this)
                    .load(stringUrl)
                    .apply(RequestOptions()
                            .priority(Priority.LOW)
                            .disallowHardwareConfig()
                            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC))
                    .listener(object : RequestListener<Drawable> {
                        override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                            Timber.e("Preload failed: $stringUrl")
                            return false
                        }

                        override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                            Timber.i("Preload success: $stringUrl")
                            return false
                        }
                    })
                    .preload()
        }
    }

    protected fun ImageView.loadImage() {
        val loginItem = viewModel.getActiveLogin()
        val width = systemUtil.getSystemWidth()
        val height = (systemUtil.getSystemHeight() * 0.8).toInt()

        val requestOptions = RequestOptions()
                .priority(Priority.IMMEDIATE)
                .disallowHardwareConfig()
                .transform(CropTransformation(context, width, height, CropTransformation.CropType.TOP))
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .format(DecodeFormat.PREFER_RGB_565)

        val requestLowRes = getLowResRequest(loginItem, requestOptions)
        val stringUrlHigh = when (currentBook.isComic()) {
            true -> currentBook.getPreviewUrl(loginItem, Settings.THUMBNAIL_SIZE_RECENT)
            false -> currentBook.getPreviewUrl()
        }
        val glideUrlHigh = stringUrlHigh.toGlideUrl(loginItem)
        Glide.with(this)
                .load(glideUrlHigh)
                .apply(requestOptions)
                .thumbnail(requestLowRes)
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                        Timber.i("Failed to load high res image: $stringUrlHigh")
                        return false
                    }

                    override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                        Timber.i("High res image found, initiating swap $stringUrlHigh")
                        return false
                    }
                })
                .into(this)
    }

}
