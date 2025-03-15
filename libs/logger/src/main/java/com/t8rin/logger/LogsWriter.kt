package com.t8rin.logger

import android.app.Application
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedWriter
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

internal class LogsWriter(
    private val context: Application,
    private val fileProvider: String,
    private val logsFilename: String,
    private val maxFileSize: Int? = MAX_SIZE
) {

    internal var logsFile: File? = null

    init {
        CoroutineScope(Dispatchers.Main).launch {
            if (logsFile != null) throw IllegalStateException("LogWriter must be initialized only once")

            logsFile = File(context.filesDir, logsFilename).apply {
                if (maxFileSize != null && length() > maxFileSize) {
                    var lineCount = 0
                    val lines = mutableListOf<String>()
                    withContext(Dispatchers.IO) {
                        coroutineScope {
                            bufferedReader().use { reader ->
                                while (reader.readLine() != null) {
                                    lineCount++
                                }
                            }
                        }
                        coroutineScope {
                            bufferedReader().use { reader ->
                                var tempLineCount = 0
                                repeat(lineCount) {
                                    tempLineCount++
                                    val line = reader.readLine()

                                    if (tempLineCount >= lineCount - 1000 && line != null) {
                                        lines.add(line)
                                    }
                                }
                            }
                            delete()
                            createNewFile()
                            writeData(this@apply) { writer ->
                                lines.forEach {
                                    writer.write(it)
                                }
                            }
                        }
                    }
                }
                if (!exists()) createNewFile()
            }
            writeData { writer ->
                writer.write(asMessage("---App Started---", Logger.Level.Info))
                writer.newLine()
            }
        }
    }

    private fun File.getUri(): Uri = FileProvider.getUriForFile(context, fileProvider, this)

    private fun writeData(
        file: File? = logsFile,
        use: (BufferedWriter) -> Unit
    ) {
        FileOutputStream(file, true)
            .bufferedWriter()
            .use(use)
    }

    private fun asMessage(
        message: String,
        level: Logger.Level = Logger.Level.Debug
    ): String {
        val timestamp = SimpleDateFormat("dd-MM-yyyy hh:mm:ss", Locale.getDefault()).format(Date())

        return "$timestamp $level: $message"
    }

    fun shareLogs() {
        val sendIntent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
            putParcelableArrayListExtra(Intent.EXTRA_STREAM, arrayListOf(logsFile!!.getUri()))
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            type = "text/plain"
        }
        val shareIntent =
            Intent.createChooser(sendIntent, "Share Logs")
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(shareIntent)
    }

    fun shareLogsViaEmail(email: String) {
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "vnd.android.cursor.dir/email"
            putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
            putExtra(Intent.EXTRA_STREAM, logsFile!!.getUri())
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(sendIntent)
    }

    fun writeLog(
        message: String,
        level: Logger.Level
    ) {
        writeData { writer ->
            writer.write(asMessage(message, level))
            writer.newLine()
        }
    }

    companion object {
        internal const val MAX_SIZE = 40 * 1024 * 1024 * 8
    }
}

internal class LogsWriterNotInitialized : Throwable()