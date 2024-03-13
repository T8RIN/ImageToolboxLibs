package io.rektplorer64.pickerkt.builder

import io.rektplorer64.pickerkt.builder.query.operand.ContentColumn
import io.rektplorer64.pickerkt.builder.query.operand.valueOf
import io.rektplorer64.pickerkt.contentresolver.ContentResolverColumn
import io.rektplorer64.pickerkt.contentresolver.MimeType

@DslMarker
annotation class PickerKtBuilderDslMarker

object PickerKt {

    /**
     * Example of usage:
     * ```
     * val config = PickerKt.picker {
     *   allowMimes {
     *       add { MimeType.Jpeg }
     *       add { MimeType.Gif }
     *   }
     *   selection {
     *       maxSelection { null }
     *   }
     *   orderBy {
     *       add { Ordering(column = ContentResolverColumn.ContentMimeType) }
     *   }
     *   predicate {
     *       ContentColumn(column = ContentResolverColumn.CollectionId) equal valueOf("")
     *       ContentColumn(column = ContentResolverColumn.ByteSize) greaterThan valueOf(0)
     *   }
     * }
     * ```
     */
    fun picker(builderScope: PickerKtConfiguration.Builder.() -> Unit): PickerKtConfiguration {
        return PickerKtConfiguration.Builder().apply(builderScope).build()
    }
}

