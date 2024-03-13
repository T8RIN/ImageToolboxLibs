package io.rektplorer64.pickerkt.util

import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore

/**
 * Resolves a [Uri] to a file path
 *
 * **Original Code:** [Stackoverflow.com](https://stackoverflow.com/a/62180319)
 */
@SuppressLint("NewApi")
fun Uri.getActualFilePath(context: Context): String? {
    // DocumentProvider
    val uri = this
    when {
        DocumentsContract.isDocumentUri(context, uri) -> {
            // ExternalStorageProvider
            when {
                uri.isExternalStorageDocument -> {
                    val docId = DocumentsContract.getDocumentId(uri)
                    val split = docId.split(":").toTypedArray()
                    val type = split[0]
                    return if ("primary".equals(type, ignoreCase = true)) {
                        Environment.getExternalStorageDirectory().toString() + "/" + split[1]
                    } else { // non-primary volumes e.g sd card
                        var filePath = "non"
                        //getExternalMediaDirs() added in API 21
                        for (f in context.externalMediaDirs) {
                            filePath = f.absolutePath
                            if (filePath.contains(type)) {
                                val endIndex = filePath.indexOf("Android")
                                filePath = filePath.substring(0, endIndex) + split[1]
                            }
                        }
                        filePath
                    }
                }
                uri.isDownloadsDocument -> {
                    val id = DocumentsContract.getDocumentId(uri)
                    val contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"),
                        java.lang.Long.valueOf(id)
                    )
                    return getDataColumn(context, contentUri, null, null)
                }
                uri.isMediaDocument -> {
                    val docId = DocumentsContract.getDocumentId(uri)
                    val split = docId.split(":").toTypedArray()

                    val contentUri: Uri? = when (split[0]) {
                        "image" -> MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                        "video" -> MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                        "audio" -> MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                        else -> null
                    }

                    val selection = "_id=?"
                    val selectionArgs = arrayOf(split[1])

                    return getDataColumn(context, contentUri, selection, selectionArgs)
                }
            }
        }
        "content".equals(uri.scheme, ignoreCase = true) -> {
            return getDataColumn(context, uri, null, null)
        }
        "file".equals(uri.scheme, ignoreCase = true) -> {
            return uri.path
        }
    }
    return null
}

private fun getDataColumn(
    context: Context, uri: Uri?, selection: String?,
    selectionArgs: Array<String>?
): String? {
    var cursor: Cursor? = null
    val column = "_data"
    val projection = arrayOf(
        column
    )
    try {
        cursor = context.contentResolver.query(
            uri!!, projection, selection, selectionArgs,
            null
        )
        if (cursor != null && cursor.moveToFirst()) {
            val columnIndex = cursor.getColumnIndexOrThrow(column)
            return cursor.getString(columnIndex)
        }
    } catch (e: java.lang.Exception) {
    } finally {
        cursor?.close()
    }
    return null
}

private val Uri.isExternalStorageDocument: Boolean
    get() = "com.android.externalstorage.documents" == authority

private val Uri.isDownloadsDocument: Boolean
    get() = "com.android.providers.downloads.documents" == authority

private val Uri.isMediaDocument: Boolean
    get() = "com.android.providers.media.documents" == authority