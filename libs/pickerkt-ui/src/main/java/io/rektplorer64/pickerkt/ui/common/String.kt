package io.rektplorer64.pickerkt.ui.common

import kotlin.random.Random.Default.nextInt

private val CharPool: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')

fun randomize(lengthRange: IntRange = 5..20): String {
    return randomize(length = lengthRange.random())
}

fun randomize(length: Int): String {
    return (1..length)
        .map { nextInt(0, CharPool.size) }
        .map(CharPool::get)
        .joinToString("")
}

fun randomizeWhiteSpaces(lengthRange: IntRange = 5..20): String {
    return randomize(lengthRange = lengthRange, char = ' ')
}

fun randomize(lengthRange: IntRange = 5..20, char: Char = ' '): String {
    return randomize(length = lengthRange.random(), char = char)
}

fun randomize(length: Int, char: Char = ' '): String {
    return (1..length)
        .map { char }
        .joinToString("")
}