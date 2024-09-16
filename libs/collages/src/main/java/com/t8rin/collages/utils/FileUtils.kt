package com.photoeditor.photoeffect.utils

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.database.DatabaseUtils
import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.util.Log
import android.webkit.MimeTypeMap
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileFilter
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.RandomAccessFile
import java.net.URL
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.text.DecimalFormat
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipException
import java.util.zip.ZipFile
import kotlin.experimental.and

object FileUtils {
    val TEMP_FOLDER =
        Environment.getExternalStorageDirectory().absolutePath + "/Android/data/dauroi.photoeditor/Temp"

    /**
     * TAG for log messages.
     */
    internal val TAG = "FileUtils"
    private val DEBUG = false // Set to true to enable logging

    val MIME_TYPE_AUDIO = "audio/*"
    val MIME_TYPE_TEXT = "text/*"
    val MIME_TYPE_IMAGE = "image/*"
    val MIME_TYPE_VIDEO = "video/*"
    val MIME_TYPE_APP = "application/*"

    val HIDDEN_PREFIX = "."

    /**
     * File and folder comparator. TODO Expose sorting option method
     *
     * @author paulburke
     */
    var sComparator: Comparator<File> = Comparator { f1, f2 ->
        // Sort alphabetically by lower case, which is much cleaner
        f1.name.lowercase(Locale.getDefault()).compareTo(
            f2.name.lowercase(Locale.getDefault())
        )
    }

    /**
     * File (not directories) filter.
     *
     * @author paulburke
     */
    var sFileFilter: FileFilter = FileFilter { file ->
        val fileName = file.name
        // Return files only (not directories) and skip hidden files
        file.isFile && !fileName.startsWith(HIDDEN_PREFIX)
    }

    /**
     * Folder (directories) filter.
     *
     * @author paulburke
     */
    var sDirFilter: FileFilter = FileFilter { file ->
        val fileName = file.name
        // Return directories only and skip hidden directories
        file.isDirectory && !fileName.startsWith(HIDDEN_PREFIX)
    }

    /**
     * Gets the extension of a file name, like ".png" or ".jpg".
     *
     * @param uri
     * @return Extension including the dot("."); "" if there is no extension;
     * null if uri was null.
     */
    fun getExtension(uri: String?): String? {
        if (uri == null) {
            return null
        }

        val dot = uri.lastIndexOf(".")
        return if (dot >= 0) {
            uri.substring(dot)
        } else {
            // No extension.
            ""
        }
    }

    /**
     * @return Whether the URI is a local one.
     */
    fun isLocal(url: String?): Boolean {
        return url != null && !url.startsWith("http://") && !url.startsWith("https://")
    }

    /**
     * @return True if Uri is a MediaStore Uri.
     * @author paulburke
     */
    fun isMediaUri(uri: Uri): Boolean {
        return "media".equals(uri.authority!!, ignoreCase = true)
    }

    /**
     * Convert File into Uri.
     *
     * @param file
     * @return uri
     */
    fun getUri(file: File?): Uri? {
        return if (file != null) {
            Uri.fromFile(file)
        } else null
    }

    /**
     * Returns the path only (without file name).
     *
     * @param file
     * @return
     */
    fun getPathWithoutFilename(file: File?): File? {
        if (file != null) {
            if (file.isDirectory) {
                // no file to be split off. Return everything
                return file
            } else {
                val filename = file.name
                val filepath = file.absolutePath

                // Construct path without file name.
                var pathwithoutname = filepath.substring(
                    0,
                    filepath.length - filename.length
                )
                if (pathwithoutname.endsWith("/")) {
                    pathwithoutname = pathwithoutname.substring(0, pathwithoutname.length - 1)
                }
                return File(pathwithoutname)
            }
        }
        return null
    }

    /**
     * @return The MIME type for the given file.
     */
    fun getMimeType(file: File): String? {

        val extension = getExtension(file.name)

        return if (extension!!.length > 0) MimeTypeMap.getSingleton().getMimeTypeFromExtension(
            extension.substring(1)
        ) else "application/octet-stream"

    }

    /**
     * @return The MIME type for the give Uri.
     */
    fun getMimeType(context: Context, uri: Uri): String? {
        val file = File(getPath(context, uri)!!)
        return getMimeType(file)
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     * @author paulburke
     */
    fun isExternalStorageDocument(uri: Uri): Boolean {
        return "com.android.externalstorage.documents" == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     * @author paulburke
     */
    fun isDownloadsDocument(uri: Uri): Boolean {
        return "com.android.providers.downloads.documents" == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     * @author paulburke
     */
    fun isMediaDocument(uri: Uri): Boolean {
        return "com.android.providers.media.documents" == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    fun isGooglePhotosUri(uri: Uri): Boolean {
        return "com.google.android.apps.photos.content" == uri.authority
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     * @author paulburke
     */
    fun getDataColumn(
        context: Context, uri: Uri?, selection: String?,
        selectionArgs: Array<String>?
    ): String? {

        var cursor: Cursor? = null
        val column = "_data"
        val projection = arrayOf(column)

        try {
            cursor =
                context.contentResolver.query(uri!!, projection, selection, selectionArgs, null)
            if (cursor != null && cursor.moveToFirst()) {
                if (DEBUG)
                    DatabaseUtils.dumpCursor(cursor)

                val column_index = cursor.getColumnIndexOrThrow(column)
                return cursor.getString(column_index)
            }
        } finally {
            cursor?.close()
        }
        return null
    }

    /**
     * Get a file path from a Uri. This will get the the path for Storage Access
     * Framework Documents, as well as the _data field for the MediaStore and
     * other file-based ContentProviders.<br></br>
     * <br></br>
     * Callers should check whether the path is local before assuming it
     * represents a local file.
     *
     * @param context The context.
     * @param uri     The Uri to query.
     * @author paulburke
     * @see .isLocal
     * @see .getFile
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    fun getPath(context: Context, uri: Uri): String? {

        if (DEBUG)
            Log.d(
                "$TAG File -",
                "Authority: " + uri.authority +
                        ", Fragment: " + uri.fragment +
                        ", Port: " + uri.port +
                        ", Query: " + uri.query +
                        ", Scheme: " + uri.scheme +
                        ", Host: " + uri.host +
                        ", Segments: " + uri.pathSegments.toString()
            )

        val isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // LocalStorageProvider
            if (isExternalStorageDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val type = split[0]

                if ("primary".equals(type, ignoreCase = true)) {
                    return Environment.getExternalStorageDirectory().toString() + "/" + split[1]
                }

                // TODO handle non-primary volumes
            } else if (isDownloadsDocument(uri)) {

                val id = DocumentsContract.getDocumentId(uri)
                val contentUri = ContentUris.withAppendedId(
                    Uri.parse("content://downloads/public_downloads"), java.lang.Long.valueOf(id)
                )

                return getDataColumn(context, contentUri, null, null)
            } else if (isMediaDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val type = split[0]

                var contentUri: Uri? = null
                if ("image" == type) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                } else if ("video" == type) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                } else if ("audio" == type) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                }

                val selection = "_id=?"
                val selectionArgs = arrayOf(split[1])

                return getDataColumn(context, contentUri, selection, selectionArgs)
            }// MediaProvider
            // DownloadsProvider
        } else if ("content".equals(uri.scheme!!, ignoreCase = true)) {

            // Return the remote address
            return if (isGooglePhotosUri(uri)) uri.lastPathSegment else getDataColumn(
                context,
                uri,
                null,
                null
            )

        } else if ("file".equals(uri.scheme!!, ignoreCase = true)) {
            return uri.path
        }// File
        // MediaStore (and general)

        return null
    }

    /**
     * Convert Uri into File, if possible.
     *
     * @return file A local file that the Uri was pointing to, or null if the
     * Uri is unsupported or pointed to a remote resource.
     * @author paulburke
     * @see .getPath
     */
    fun getFile(context: Context, uri: Uri?): File? {
        if (uri != null) {
            val path = getPath(context, uri)
            if (path != null && isLocal(path)) {
                return File(path)
            }
        }
        return null
    }

    /**
     * Get the file size in a human-readable string.
     *
     * @param size
     * @return
     * @author paulburke
     */
    fun getReadableFileSize(size: Int): String {
        val BYTES_IN_KILOBYTES = 1024
        val dec = DecimalFormat("###.#")
        val KILOBYTES = " KB"
        val MEGABYTES = " MB"
        val GIGABYTES = " GB"
        var fileSize = 0f
        var suffix = KILOBYTES

        if (size > BYTES_IN_KILOBYTES) {
            fileSize = (size / BYTES_IN_KILOBYTES).toFloat()
            if (fileSize > BYTES_IN_KILOBYTES) {
                fileSize = fileSize / BYTES_IN_KILOBYTES
                if (fileSize > BYTES_IN_KILOBYTES) {
                    fileSize = fileSize / BYTES_IN_KILOBYTES
                    suffix = GIGABYTES
                } else {
                    suffix = MEGABYTES
                }
            }
        }
        return dec.format(fileSize.toDouble()) + suffix
    }

    /**
     * Attempt to retrieve the thumbnail of given File from the MediaStore. This
     * should not be called on the UI thread.
     *
     * @param context
     * @param file
     * @return
     * @author paulburke
     */
    fun getThumbnail(context: Context, file: File): Bitmap? {
        return getThumbnail(context, getUri(file), getMimeType(file))
    }

    /**
     * Attempt to retrieve the thumbnail of given Uri from the MediaStore. This
     * should not be called on the UI thread.
     *
     * @param context
     * @param uri
     * @param mimeType
     * @return
     * @author paulburke
     */
    @JvmOverloads
    fun getThumbnail(
        context: Context,
        uri: Uri?,
        mimeType: String? = getMimeType(context, uri!!)
    ): Bitmap? {
        if (DEBUG)
            Log.d(TAG, "Attempting to get thumbnail")

        if (!isMediaUri(uri!!)) {
            Log.e(TAG, "You can only retrieve thumbnails for images and videos.")
            return null
        }

        var bm: Bitmap? = null
        if (uri != null) {
            val resolver = context.contentResolver
            var cursor: Cursor? = null
            try {
                cursor = resolver.query(uri, null, null, null, null)
                if (cursor!!.moveToFirst()) {
                    val id = cursor.getInt(0)
                    if (DEBUG)
                        Log.d(TAG, "Got thumb ID: $id")

                    if (mimeType!!.contains("video")) {
                        bm = MediaStore.Video.Thumbnails.getThumbnail(
                            resolver,
                            id.toLong(),
                            MediaStore.Video.Thumbnails.MINI_KIND, null
                        )
                    } else if (mimeType.contains(MIME_TYPE_IMAGE)) {
                        bm = MediaStore.Images.Thumbnails.getThumbnail(
                            resolver,
                            id.toLong(),
                            MediaStore.Images.Thumbnails.MINI_KIND, null
                        )
                    }
                }
            } catch (e: Exception) {
                if (DEBUG)
                    Log.e(TAG, "getThumbnail", e)
            } finally {
                cursor?.close()
            }
        }
        return bm
    }

    /**
     * Get the Intent for selecting content to be used in an Intent Chooser.
     *
     * @return The intent for opening a file with Intent.createChooser()
     * @author paulburke
     */
    fun createGetContentIntent(): Intent {
        // Implicitly allow the user to select a particular kind of data
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        // The MIME data type filter
        intent.type = "*/*"
        // Only return URIs that can be opened with ContentResolver
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        return intent
    }

    fun cleanTempFiles() {
        val tempDir = File(TEMP_FOLDER)
        val files = tempDir.listFiles()
        if (files != null) {
            for (file in files)
                file.delete()
        }
    }

    fun saveToFile(`is`: InputStream, dest: File): Boolean {
        try {
            if (dest.parentFile != null && !dest.parentFile!!.exists()) {
                dest.parentFile!!.mkdirs()
            }

            val fos = FileOutputStream(dest)
            val buff = ByteArray(2048)
            var len = -1
            while ((`is`.read(buff)) != -1) {
                len = `is`.read(buff)
                fos.write(buff, 0, len)
            }

            fos.flush()
            `is`.close()
            fos.close()
            return true
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return false
    }

    @Throws(IOException::class)
    fun downloadFile(urlText: String, outPath: String) {
        val url = URL(urlText)
        val conn = url.openConnection()
        conn.connect()
        val `is` = conn.getInputStream()
        val file = File(outPath)
        file.parentFile!!.mkdirs()
        val fos = FileOutputStream(File(outPath))
        val buff = ByteArray(2048)
        var len = -1
        while ((`is`.read(buff)) != -1) {
            len = `is`.read(buff)
            fos.write(buff, 0, len)
        }
        fos.flush()
        fos.close()
        `is`.close()
    }

    /**
     * sha1 with 256 bits
     *
     * @param input
     * @return
     * @throws java.security.NoSuchAlgorithmException
     */
    @Throws(NoSuchAlgorithmException::class)
    fun sha128s(input: String): String {
        val mDigest = MessageDigest.getInstance("SHA-1")
        val result = mDigest.digest(input.toByteArray())
        val sb = StringBuffer()
        for (i in result.indices) {
            sb.append(Integer.toString((result[i] and 0xff.toByte()) + 0x100, 16).substring(1))
        }

        return sb.toString()
    }

    fun deleteFile(file: File) {
        if (file.isDirectory) {
            val childFiles = file.listFiles()
            if (childFiles != null) {
                for (f in childFiles) {
                    deleteFile(f)
                }
                file.delete()
            } else {
                file.delete()
            }
        } else {
            file.delete()
        }
    }

    fun unzip(zipFilePath: String, outFolder: String) {
        val destination = File(outFolder)
        if (!destination.exists()) {
            destination.mkdirs()
        }

        try {
            val zipFile = File(zipFilePath)
            extractFolder(zipFile, destination)
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    @Throws(ZipException::class, IOException::class)
    private fun extractFolder(zipFile: File, destination: File) {
        val BUFFER = 2048
        val zip = ZipFile(zipFile)
        val zipFileEntries = zip.entries()
        // Process each entry
        while (zipFileEntries.hasMoreElements()) {
            // grab a zip file entry
            val entry = zipFileEntries.nextElement() as ZipEntry
            val currentEntry = entry.name
            val destFile = File(destination.absolutePath, currentEntry)
            val destinationParent = destFile.parentFile
            // create the parent directory structure if needed
            destinationParent!!.mkdirs()
            if (!entry.isDirectory) {
                val `is` = BufferedInputStream(zip.getInputStream(entry))
                var currentByte: Int
                // establish buffer for writing file
                val data = ByteArray(BUFFER)
                if (!destFile.exists()) {
                    // write the current file to disk
                    val fos = FileOutputStream(destFile)
                    val dest = BufferedOutputStream(fos, BUFFER)
                    // read and write until last byte is encountered
                    while ((`is`.read(data, 0, BUFFER)) != -1) {
                        currentByte = `is`.read(data, 0, BUFFER)
                        dest.write(data, 0, currentByte)
                    }
                    dest.flush()
                    dest.close()
                    `is`.close()
                }
            }
        }

        zip.close()
    }

    fun saveBitmapToFile(bitmap: Bitmap, outPath: String): String? {
        val outFile = File(outPath)
        val parent = outFile.parentFile
        if (parent != null && !parent.exists()) {
            parent.mkdirs()
        }

        try {
            val fos = FileOutputStream(outFile)
            bitmap.compress(CompressFormat.PNG, 100, fos)
            fos.close()
            return outFile.absolutePath
        } catch (ex: Exception) {
            ex.printStackTrace()
        }

        return null
    }

    @Throws(IOException::class)
    fun generateMD5(sourcePath: String, offset: Long, length: Long): String? {
        val md: MessageDigest
        var raf: RandomAccessFile? = null
        try {
            raf = RandomAccessFile(File(sourcePath), "r")
            raf.seek(offset)
            var read = 0
            val buf = ByteArray(2048)
            md = MessageDigest.getInstance("MD5")
            var readBytes: Long = 0
            while ((raf.read(buf)) > 0 && readBytes < length) {

                read = raf.read(buf)
                md.update(buf, 0, read)
                readBytes += read.toLong()
            }
            val hashValue = md.digest()
            return byteArrayToHexString(hashValue)
        } catch (e: NoSuchAlgorithmException) {
            return null
        } catch (e: IOException) {
            return null
        } finally {
            try {
                raf?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }
    }

    fun generateMD5(inputStream: InputStream?): String? {
        if (inputStream == null) {
            return null
        }
        val md: MessageDigest
        try {
            var read = 0
            val buf = ByteArray(2048)
            md = MessageDigest.getInstance("MD5")
            while ((inputStream.read(buf)) > 0) {
                read = inputStream.read(buf)
                md.update(buf, 0, read)
            }
            val hashValue = md.digest()
            return byteArrayToHexString(hashValue)
        } catch (e: NoSuchAlgorithmException) {
            return null
        } catch (e: IOException) {
            return null
        } finally {
            try {
                inputStream.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }
    }

    @SuppressLint("DefaultLocale")
    fun byteArrayToHexString(b: ByteArray): String {
        var result = ""
        for (i in b.indices) {
            result += Integer.toString((b[i] and 0xff.toByte()) + 0x100, 16).substring(1)
        }
        return result.lowercase(Locale.getDefault())
    }

    fun copyFile(source: File, dest: File): Boolean {
        try {
            dest.parentFile!!.mkdirs()
            val fis = FileInputStream(source)
            val fos = FileOutputStream(dest)
            val buff = ByteArray(2048)
            var len = -1
            while ((fis.read(buff)) != -1) {
                len = fis.read(buff)
                fos.write(buff, 0, len)
            }

            fos.flush()

            fis.close()
            fos.close()
            return true
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return false
    }
}
/**
 * Attempt to retrieve the thumbnail of given Uri from the MediaStore. This
 * should not be called on the UI thread.
 *
 * @param context
 * @param uri
 * @return
 * @author paulburke
 */
