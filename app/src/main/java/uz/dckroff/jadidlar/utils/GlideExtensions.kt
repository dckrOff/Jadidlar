package uz.dckroff.jadidlar.utils

import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestListener
import uz.dckroff.jadidlar.R
import uz.dckroff.jadidlar.data.firebase.FirebaseManager

/**
 * Безопасная загрузка изображения с обработкой ошибок
 */
fun ImageView.loadImageSafe(
    url: String?,
    placeholder: Int = R.drawable.img_placeholder,
    error: Int = R.drawable.img_placeholder
) {
    try {
        if (url.isNullOrBlank()) {
            setImageResource(error)
            return
        }

        Glide.with(this.context)
            .load(url)
            .placeholder(placeholder)
            .error(error)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(this)
    } catch (e: Exception) {
        // Логируем ошибку
        FirebaseManager.crashlytics.recordException(e)
        // Показываем placeholder
        setImageResource(error)
    }
}

/**
 * Загрузка изображения с callback при ошибке
 */
fun ImageView.loadImageWithCallback(
    url: String?,
    placeholder: Int = R.drawable.img_placeholder,
    error: Int = R.drawable.img_placeholder,
    onError: ((Exception?) -> Unit)? = null
) {
    try {
        if (url.isNullOrBlank()) {
            setImageResource(error)
            onError?.invoke(null)
            return
        }

        Glide.with(this.context)
            .load(url)
            .placeholder(placeholder)
            .error(error)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .addListener(object : RequestListener<android.graphics.drawable.Drawable> {
                override fun onLoadFailed(
                    e: com.bumptech.glide.load.engine.GlideException?,
                    model: Any?,
                    target: com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable>,
                    isFirstResource: Boolean
                ): Boolean {
                    onError?.invoke(e)
                    return false
                }

                override fun onResourceReady(
                    resource: android.graphics.drawable.Drawable,
                    model: Any,
                    target: com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable>?,
                    dataSource: com.bumptech.glide.load.DataSource,
                    isFirstResource: Boolean
                ): Boolean {
                    return false
                }
            })
            .into(this)
    } catch (e: Exception) {
        FirebaseManager.crashlytics.recordException(e)
        setImageResource(error)
        onError?.invoke(e)
    }
}

