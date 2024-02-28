package oupson.apng.decoder

import android.content.Context
import android.graphics.drawable.AnimatedImageDrawable
import android.graphics.drawable.AnimationDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.widget.ImageView
import androidx.annotation.RawRes
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import oupson.apng.drawable.ApngDrawable
import oupson.apng.utils.Utils.mapResult
import java.io.File
import java.io.FileInputStream
import java.net.URL

class ApngLoader(parent: Job? = null) {
    interface Callback {
        /**
         * Function called when the file was successfully decoded.
         * @param drawable Can be an [ApngDrawable] if successful and an [AnimatedImageDrawable] if the image decoded is not an APNG but a gif. If it is not an animated image, it is a [Drawable].
         */
        fun onSuccess(drawable: Drawable)

        /**
         * Function called when something gone wrong.
         * @param error The problem.
         */
        fun onError(error: Throwable)
    }

    private val job = SupervisorJob(parent)
    private val coroutineScope: CoroutineScope = CoroutineScope(job)

    fun cancelAll() {
        coroutineScope.cancel(CancellationException("Loading was canceled"))
    }


    /**
     * Load Apng into an imageView.
     * @param context Context needed for animation drawable.
     * @param file File to decode.
     * @param imageView Image View.
     * @param config Decoder configuration
     */
    suspend fun decodeApngInto(
        context: Context,
        file: File,
        imageView: ImageView,
        config: ApngDecoder.Config = ApngDecoder.Config()
    ): Result<Drawable> =
        kotlin.runCatching {
            withContext(Dispatchers.IO) {
                FileInputStream(file)
            }
        }.mapResult { input ->
            ApngDecoder(
                input,
                config
            ).getDecoded(context)
        }.onSuccess { drawable ->
            withContext(Dispatchers.Main) {
                imageView.setImageDrawable(drawable)
                (drawable as? AnimationDrawable)?.start()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    (drawable as? AnimatedImageDrawable)?.start()
                }
            }
        }

    /**
     * Load Apng into an imageView.
     * @param context Context needed for animation drawable and content resolver.
     * @param uri Uri to load.
     * @param imageView Image View.
     * @param config Decoder configuration
     */
    suspend fun decodeApngInto(
        context: Context,
        uri: Uri,
        imageView: ImageView,
        config: ApngDecoder.Config = ApngDecoder.Config()
    ): Result<Drawable> =
        kotlin.runCatching {
            withContext(Dispatchers.IO) { context.contentResolver.openInputStream(uri) }!!
        }.mapResult { inputStream ->
            ApngDecoder(
                inputStream,
                config
            ).getDecoded(context)
        }.onSuccess { drawable ->
            withContext(Dispatchers.Main) {
                imageView.setImageDrawable(drawable)
                (drawable as? AnimationDrawable)?.start()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    (drawable as? AnimatedImageDrawable)?.start()
                }
            }
        }


    /**
     * Load Apng into an imageView.
     * @param context Context needed to decode the resource and for the animation drawable.
     * @param res Raw resource to load.
     * @param imageView Image View.
     * @param config Decoder configuration
     */
    suspend fun decodeApngInto(
        context: Context, @RawRes res: Int,
        imageView: ImageView,
        config: ApngDecoder.Config = ApngDecoder.Config()
    ): Result<Drawable> =
        ApngDecoder(
            withContext(Dispatchers.IO) {
                context.resources.openRawResource(res)
            },
            config
        ).getDecoded(context)
            .onSuccess { drawable ->
                withContext(Dispatchers.Main) {
                    imageView.setImageDrawable(drawable)
                    (drawable as? AnimationDrawable)?.start()
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        (drawable as? AnimatedImageDrawable)?.start()
                    }
                }
            }


    /**
     * Load Apng into an imageView, asynchronously.
     * @param context Context needed for the animation drawable.
     * @param url URL to load.
     * @param imageView Image View.
     * @param config Decoder configuration
     */
    suspend fun decodeApngInto(
        context: Context,
        url: URL,
        imageView: ImageView,
        config: ApngDecoder.Config = ApngDecoder.Config()
    ): Result<Drawable> =
        ApngDecoder.constructFromUrl(url, config).getOrElse { return Result.failure(it) }
            .getDecoded(context)
            .onSuccess { drawable ->
                withContext(Dispatchers.Main) {
                    imageView.setImageDrawable(drawable)
                    (drawable as? AnimationDrawable)?.start()
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        (drawable as? AnimatedImageDrawable)?.start()
                    }
                }
            }

    /**
     * Load Apng into an imageView, asynchronously.
     * @param context Context needed for decoding the image and creating the animation drawable.
     * @param string URL to load
     * @param imageView Image View.
     * @param config Decoder configuration
     */
    @Suppress("unused")
    suspend fun decodeApngInto(
        context: Context,
        string: String,
        imageView: ImageView,
        config: ApngDecoder.Config = ApngDecoder.Config()
    ): Result<Drawable> {
        return if (string.startsWith("http://") || string.startsWith("https://")) {
            kotlin.runCatching { URL(string) }
                .mapResult { url ->
                    decodeApngInto(
                        context,
                        url,
                        imageView,
                        config
                    )
                }

        } else if (File(string).exists()) {
            var pathToLoad =
                if (string.startsWith("content://")) string else "file://$string"
            pathToLoad = pathToLoad.replace("%", "%25").replace("#", "%23")
            decodeApngInto(
                context,
                Uri.parse(pathToLoad),
                imageView,
                config
            )
        } else if (string.startsWith("file:///android_asset/")) {
            kotlin.runCatching {
                withContext(Dispatchers.IO) {
                    context.assets.open(string.replace("file:///android_asset/", ""))
                }
            }
                .mapResult { inputStream -> ApngDecoder(inputStream, config).getDecoded(context) }
                .onSuccess { drawable ->
                    withContext(Dispatchers.Main) {
                        imageView.setImageDrawable(drawable)
                        (drawable as? AnimationDrawable)?.start()
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                            (drawable as? AnimatedImageDrawable)?.start()
                        }
                    }
                }
        } else {
            throw Exception("Cannot open string")
        }
    }


// region with callback
    /**
     * Load Apng into an imageView, asynchronously.
     * @param context Context needed for animation drawable.
     * @param file File to decode.
     * @param imageView Image View.
     * @param callback [ApngLoader.Callback] to handle success and error.
     * @param config Decoder configuration
     */
    @Suppress("unused")
    @JvmOverloads
    fun decodeApngAsyncInto(
        context: Context,
        file: File,
        imageView: ImageView,
        callback: Callback? = null,
        config: ApngDecoder.Config = ApngDecoder.Config()
    ) =
        coroutineScope.launch(Dispatchers.Default) {
            val drawable = decodeApngInto(context, file, imageView, config)
            withContext(Dispatchers.Main) {
                drawable
                    .onSuccess { callback?.onSuccess(it) }
                    .onFailure { callback?.onError(it) }
            }
        }


    /**
     * Load Apng into an imageView, asynchronously.
     * @param context Context needed for animation drawable and content resolver.
     * @param uri Uri to load.
     * @param imageView Image View.
     * @param callback [ApngLoader.Callback] to handle success and error.
     * @param config Decoder configuration
     */
    @Suppress("unused")
    @JvmOverloads
    fun decodeApngAsyncInto(
        context: Context,
        uri: Uri,
        imageView: ImageView,
        callback: Callback? = null,
        config: ApngDecoder.Config = ApngDecoder.Config()
    ) = coroutineScope.launch(Dispatchers.Default) {
        val drawable = decodeApngInto(context, uri, imageView, config)
        withContext(Dispatchers.Main) {
            drawable
                .onSuccess { callback?.onSuccess(it) }
                .onFailure { callback?.onError(it) }
        }
    }

    /**
     * Load Apng into an imageView, asynchronously.
     * @param context Context needed to decode the resource and for the animation drawable.
     * @param res Raw resource to load.
     * @param imageView Image View.
     * @param callback [ApngLoader.Callback] to handle success and error.
     * @param config Decoder configuration
     */
    @Suppress("unused")
    @JvmOverloads
    fun decodeApngAsyncInto(
        context: Context, @RawRes res: Int,
        imageView: ImageView,
        callback: Callback? = null,
        config: ApngDecoder.Config = ApngDecoder.Config()
    ) = coroutineScope.launch(Dispatchers.Default) {
        val drawable = decodeApngInto(context, res, imageView, config)
        withContext(Dispatchers.Main) {
            drawable
                .onSuccess { callback?.onSuccess(it) }
                .onFailure { callback?.onError(it) }
        }
    }

    /**
     * Load Apng into an imageView, asynchronously.
     * @param context Context needed for the animation drawable.
     * @param url URL to load.
     * @param imageView Image View.
     * @param callback [ApngLoader.Callback] to handle success and error.
     * @param config Decoder configuration
     */
    @Suppress("unused")
    @JvmOverloads
    fun decodeApngAsyncInto(
        context: Context,
        url: URL,
        imageView: ImageView,
        callback: Callback? = null,
        config: ApngDecoder.Config = ApngDecoder.Config()
    ) = coroutineScope.launch(Dispatchers.Default) {
        val drawable = decodeApngInto(context, url, imageView, config)
        withContext(Dispatchers.Main) {
            drawable
                .onSuccess { callback?.onSuccess(it) }
                .onFailure { callback?.onError(it) }
        }
    }


    /**
     * Load Apng into an imageView, asynchronously.
     * @param context Context needed for decoding the image and creating the animation drawable.
     * @param string URL to load
     * @param imageView Image View.
     * @param callback [ApngLoader.Callback] to handle success and error.
     * @param config Decoder configuration
     */
    @Suppress("unused")
    @JvmOverloads
    fun decodeApngAsyncInto(
        context: Context,
        string: String,
        imageView: ImageView,
        callback: Callback? = null,
        config: ApngDecoder.Config = ApngDecoder.Config()
    ) =
        coroutineScope.launch(Dispatchers.Default) {
            val drawable = decodeApngInto(context, string, imageView, config)
            withContext(Dispatchers.Main) {
                if (drawable.isSuccess) {
                    callback?.onSuccess(drawable.getOrNull()!!)
                } else {
                    callback?.onError(drawable.exceptionOrNull()!!)
                }
            }
        }

// endregion with callback
}