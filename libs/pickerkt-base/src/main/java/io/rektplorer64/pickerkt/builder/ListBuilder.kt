package io.rektplorer64.pickerkt.builder

@PickerKtBuilderDslMarker
class ListBuilder<T>(
    private val list: MutableList<T> = mutableListOf()
) {
    fun add(item: () -> T) {
        list.add(item())
    }

    fun addAll(items: List<T>) {
        list.addAll(items)
    }

    operator fun T.unaryPlus() {
        list.add(this)
    }

    internal fun build(): List<T> = list.toList()
}