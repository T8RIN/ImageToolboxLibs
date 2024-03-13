package io.rektplorer64.pickerkt.contentresolver

enum class Order(val sqlKeyword: String) {
    Ascending(sqlKeyword = "ASC"),
    Descending(sqlKeyword = "DESC")
}