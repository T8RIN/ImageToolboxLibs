package com.t8rin.raw_coder

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import java.io.File

fun File.readRawInfo(): RawInfo? = runCatching {
    LibRawBridge.open(this)?.use { it.info() }
}.getOrNull()

fun Context.readRawInfo(uri: Uri): RawInfo? = runCatching {
    if (!isRawUri(uri)) return@runCatching null

    when (uri.scheme) {
        ContentResolver.SCHEME_FILE -> uri.path?.let(::File)?.readRawInfo()
        ContentResolver.SCHEME_CONTENT -> contentResolver
            .openFileDescriptor(uri, "r")
            ?.use { File("/proc/self/fd/${it.fd}").readRawInfo() }
        else -> null
    }
}.getOrNull()

private fun Context.isRawUri(uri: Uri): Boolean {
    if (contentResolver.getType(uri)?.isRawMimeType() == true) return true

    val name = if (uri.scheme == ContentResolver.SCHEME_CONTENT) {
        runCatching {
            contentResolver.query(
                uri,
                arrayOf(OpenableColumns.DISPLAY_NAME),
                null,
                null,
                null
            )?.use { cursor ->
                cursor.takeIf { it.moveToFirst() }?.getString(0)
            }
        }.getOrNull()
    } else uri.lastPathSegment

    return name?.substringAfterLast('.', "")?.lowercase() in RAW_EXTENSIONS
}

private fun String.isRawMimeType(): Boolean {
    val mimeType = substringBefore(';').lowercase()
    if (!mimeType.startsWith("image/") && mimeType != "application/x-raw") return false
    val subtype = mimeType.substringAfter('/')
    return subtype == "x-raw" || subtype == "x-dcraw" || RAW_EXTENSIONS.any {
        subtype == it || subtype.endsWith("-$it")
    }
}

internal val RAW_EXTENSIONS = setOf(
    "3fr", "ari", "arw", "bay", "bmq", "cap", "cine", "cr2", "cr3", "crw", "cs1",
    "dc2", "dcr", "dng", "erf", "fff", "hdr", "ia", "iiq", "k25", "kc2", "kdc", "mdc",
    "mef", "mos", "mrw", "nef", "nrw", "orf", "pef", "ptx", "pxn", "qtk", "raf", "raw",
    "rdc", "rw2", "rwl", "rwz", "sr2", "srf", "srw", "sti", "x3f"
)
