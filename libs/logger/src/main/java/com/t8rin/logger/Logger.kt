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
import android.util.Log
import androidx.core.net.toUri
import com.t8rin.logger.LogsWriter.Companion.MAX_SIZE

data object Logger {

    internal var logWriter: LogsWriter? = null

    inline fun <reified T> makeLog(
        tag: String = "Logger" + (T::class.simpleName?.let { "_$it" } ?: ""),
        level: Level = Level.Debug,
        dataBlock: () -> T
    ) {
        val data = dataBlock()
        val message = if (data is Throwable) {
            Log.getStackTraceString(data)
        } else {
            data.toString()
        }

        makeLog(
            tag = tag,
            message = message,
            level = level
        )
    }

    fun makeLog(
        message: String,
        tag: String = "Logger_String",
        level: Level = Level.Debug,
    ) {
        when (level) {
            is Level.Assert -> Log.println(level.priority, tag, message)
            Level.Debug -> Log.d(tag, message)
            Level.Error -> Log.e(tag, message)
            Level.Info -> Log.i(tag, message)
            Level.Verbose -> Log.v(tag, message)
            Level.Warn -> Log.w(tag, message)
        }

        logWriter?.writeLog(
            tag = tag,
            message = message,
            level = level
        )
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
}

inline fun <reified T> T.makeLog(
    tag: String = "Logger" + (T::class.simpleName?.let { "_$it" } ?: ""),
    level: Logger.Level = Logger.Level.Debug,
    dataBlock: (T) -> Any? = { it }
): T = also {
    if (it is Throwable) {
        Log.e(tag, it.localizedMessage, it)
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