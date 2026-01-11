package com.t8rin.neural_tools

import android.app.Application
import io.ktor.client.HttpClient
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging

abstract class NeuralTool {
    protected val context get() = application

    companion object {
        private var _httpClient: HttpClient = HttpClient {
            install(Logging) {
                level = LogLevel.INFO
            }
        }
        internal val httpClient: HttpClient get() = _httpClient

        private var _context: Application? = null
        internal val application: Application
            get() = _context
                ?: throw NullPointerException("Call NeuralTool.init() in Application onCreate to use this feature")

        fun init(
            context: Application,
            httpClient: HttpClient? = null
        ) {
            _httpClient = httpClient ?: _httpClient
            _context = context
        }
    }
}