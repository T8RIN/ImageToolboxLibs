package com.t8rin.palette.utils

import java.util.regex.Pattern

/**
 * Simple regex matcher utility
 */
object RegexUtils {
    /**
     * Match pattern and return capture groups
     */
    fun match(pattern: String, text: String, caseInsensitive: Boolean = false): RegexMatch? {
        val flags = if (caseInsensitive) Pattern.CASE_INSENSITIVE else 0
        val p = Pattern.compile(pattern, flags)
        val matcher = p.matcher(text)
        if (matcher.find()) {
            val groups = mutableListOf<String>()
            for (i in 0..matcher.groupCount()) {
                groups.add(matcher.group(i) ?: "")
            }
            return RegexMatch(groups)
        }
        return null
    }

    /**
     * Find all matches
     */
    fun findAll(pattern: String, text: String, caseInsensitive: Boolean = false): List<RegexMatch> {
        val flags = if (caseInsensitive) Pattern.CASE_INSENSITIVE else 0
        val p = Pattern.compile(pattern, flags)
        val matcher = p.matcher(text)
        val matches = mutableListOf<RegexMatch>()
        while (matcher.find()) {
            val groups = mutableListOf<String>()
            for (i in 0..matcher.groupCount()) {
                groups.add(matcher.group(i) ?: "")
            }
            matches.add(RegexMatch(groups))
        }
        return matches
    }
}

data class RegexMatch(val captures: List<String>) {
    operator fun get(index: Int): String = if (index < captures.size) captures[index] else ""
}


