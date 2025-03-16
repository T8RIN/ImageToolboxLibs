/*
 * ImageToolbox is an image editor for android
 * Copyright (c) 2024 T8RIN (Malik Mukhametzyanov)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * You should have received a copy of the Apache License
 * along with this program.  If not, see <http://www.apache.org/licenses/LICENSE-2.0>.
 */

package com.t8rin.logger

import android.app.Application
import android.net.Uri
import androidx.core.net.toUri
import com.t8rin.logger.LogsWriter.Companion.MAX_SIZE
import android.util.Log as RealLog

data object Logger {

    internal var logWriter: LogsWriter? = null

    inline fun <reified T> makeLog(
        tag: String = "Logger" + (T::class.simpleName?.let { "_$it" } ?: ""),
        level: Level = Level.Debug,
        dataBlock: () -> T
    ) {
        val data = dataBlock()
        val message = if (data is Throwable) {
            RealLog.getStackTraceString(data)
        } else {
            data.toString()
        }

        makeLog(
            Log(
                tag = tag,
                message = message,
                level = level
            )
        )
    }

    fun makeLog(log: Log) {
        val (tag, message, level) = log

        when (level) {
            is Level.Assert -> RealLog.println(level.priority, tag, message)
            Level.Debug -> RealLog.d(tag, message)
            Level.Error -> RealLog.e(tag, message)
            Level.Info -> RealLog.i(tag, message)
            Level.Verbose -> RealLog.v(tag, message)
            Level.Warn -> RealLog.w(tag, message)
        }

        logWriter?.writeLog(log)
    }

    inline fun <reified T> makeLog(
        data: T,
        tag: String = "Logger" + (T::class.simpleName?.let { "_$it" } ?: ""),
        level: Level = Level.Debug,
    ) = makeLog(
        tag = tag,
        level = level,
        dataBlock = { data }
    )

    fun makeLog(
        level: Level = Level.Debug,
        separator: String = " - ",
        vararg data: Any
    ) = makeLog(
        level = level,
        dataBlock = {
            data.toList().joinToString(separator)
        }
    )

    fun shareLogs() = logWriter?.shareLogs() ?: throw LogsWriterNotInitialized()

    fun shareLogsViaEmail(
        email: String
    ) = logWriter?.shareLogsViaEmail(email) ?: throw LogsWriterNotInitialized()

    fun getLogsFile(): Uri = logWriter?.logsFile?.toUri() ?: throw LogsWriterNotInitialized()

    sealed interface Level {
        data class Assert(
            val priority: Int
        ) : Level {
            override fun toString(): String = "Assert"
        }

        data object Error : Level
        data object Warn : Level
        data object Info : Level
        data object Debug : Level
        data object Verbose : Level
    }

    data class Log(
        val tag: String,
        val message: String,
        val level: Level
    )
}

inline fun <reified T> T.makeLog(
    tag: String = "Logger" + (T::class.simpleName?.let { "_$it" } ?: ""),
    level: Logger.Level = Logger.Level.Debug,
    dataBlock: (T) -> Any? = { it }
): T = also {
    if (it is Throwable) {
        RealLog.e(tag, it.localizedMessage, it)
        Logger.makeLog(
            tag = tag,
            level = level,
            dataBlock = { dataBlock(it) }
        )
    } else {
        Logger.makeLog(
            tag = tag,
            level = level,
            dataBlock = { dataBlock(it) }
        )
    }

}

inline infix fun <reified T> T.makeLog(
    tag: String
): T = makeLog(tag) { this }


fun Logger.attachLogWriter(
    context: Application,
    fileProvider: String,
    logsFilename: String,
    isSyncCreate: Boolean,
    maxFileSize: Int? = MAX_SIZE
) {
    logWriter = LogsWriter(
        context = context,
        fileProvider = fileProvider,
        logsFilename = logsFilename,
        maxFileSize = maxFileSize,
        isSyncCreate = isSyncCreate
    )
}