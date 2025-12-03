package com.t8rin.neural_tools

import android.app.Application

abstract class NeuralTool {
    protected val context get() = application

    companion object {
        private var _context: Application? = null
        internal val application: Application
            get() = _context
                ?: throw NullPointerException("Call NeuralTool.init() in Application onCreate to use this feature")

        fun init(context: Application) {
            _context = context
        }
    }
}