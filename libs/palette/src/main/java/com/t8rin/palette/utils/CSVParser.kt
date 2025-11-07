package com.t8rin.palette.utils

/**
 * Simple CSV parser
 */
object CSVParser {
    /**
     * Parse CSV text into records
     */
    fun parse(text: String): List<List<String>> {
        val records = mutableListOf<List<String>>()
        val lines = text.lines()

        for (line in lines) {
            if (line.isBlank()) continue
            val fields = line.split(',').map { it.trim() }
            if (fields.isNotEmpty()) {
                records.add(fields)
            }
        }

        return records
    }
}


