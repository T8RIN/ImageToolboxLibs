package io.rektplorer64.pickerkt.builder

import android.os.Parcel
import android.os.Parcelable
import android.provider.MediaStore
import io.rektplorer64.pickerkt.builder.query.*
import io.rektplorer64.pickerkt.builder.query.operand.Column
import io.rektplorer64.pickerkt.builder.query.operand.valueOf
import io.rektplorer64.pickerkt.contentresolver.MimeType
import kotlinx.serialization.*
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.nullable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.CompositeDecoder.Companion.DECODE_DONE
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.encoding.encodeStructure
import timber.log.Timber

class PickerKtConfiguration internal constructor(
    mimeTypes: List<MimeType>,
    selection: Selection,
    pagination: PaginationConfig,
    ordering: List<Ordering>,
    predicate: Expression,
    val downloadFolderOnly: Boolean = false,
    val rebuiltAtLeastOnce: Boolean = false,
) : Parcelable {

    init {
        ordering.groupBy { it.column }.forEach { (t, u) ->
            assert(u.size == 1) {
                "There are duplicate order for column \"$t\""
            }
        }
    }

    var mimeTypes: List<MimeType> = mimeTypes.distinct()
        internal set
        get() = field.toList()

    var selection: Selection = selection
        internal set

    var pagination: PaginationConfig = pagination
        internal set

    var ordering: List<Ordering> = ordering
        internal set

    var predicate: Expression = where {
        if (!rebuiltAtLeastOnce && !mimeTypes.isNullOrEmpty()) {
            Column(MediaStore.MediaColumns.MIME_TYPE) includedIn valueOf(mimeTypes.map { it.id })
        }

       // if (!rebuiltAtLeastOnce && !downloadFolderOnly) {
       //     Column(MediaStore.MediaColumns.BUCKET_ID) includedIn Value(mimeTypes.map { it.id })
       // }

        Timber.d("Predicate: ${predicate.conditions}")
        if (predicate.conditions.isNotEmpty()) {
            addOperand(predicate)
        }
    }
        internal set

    val orderByString: String
        get() = ordering.joinToString(separator = ",") { it.toString() }

    private val argsArraysOfPredicate: List<Pair<String, List<String>>>
        get() {
            return Regex("\\((?:\'?([0-9a-zA-Z/?+.-]+)\'?,?)+\\)")
                .findAll(predicate.toString())
                .map { it.value }
                .map { it to it.removeSurrounding("(", ")").split(",") }
                .toList()
        }

    val predicateString: String?
        get() {
            if (predicate.toString().isNotEmpty()) {
                val sqlArrayClauses = argsArraysOfPredicate
                    .map {
                        it.first to it.second.joinToString(
                            separator = ",",
                            prefix = "(",
                            postfix = ")",
                            transform = { "?" }
                        )
                    }
                    .toList()

                if (sqlArrayClauses.isEmpty()) {
                    return predicate.toString()
                }

                var finalPredicate = predicate.toString()
                sqlArrayClauses.forEach {
                    finalPredicate = finalPredicate.replace(it.first, it.second)
                }

                return finalPredicate
            } else {
                return null
            }
        }

    val predicateArgumentString: Array<String>?
        get() {
            if (predicateString.isNullOrEmpty() || argsArraysOfPredicate.isEmpty()) {
                return null
            }
            return argsArraysOfPredicate
                .flatMap { it.second }
                .toTypedArray()
        }

    fun asBuilder(): Builder = Builder(
        mimeTypes = mimeTypes,
        selection = selection,
        predicate = predicate,
        pagination = pagination,
        ordering = ordering,
        rebuiltMode = true
    )

    fun copy(
        mimeTypes: List<MimeType> = this.mimeTypes.toList(),
        selection: Selection = this.selection.copy(),
        pagination: PaginationConfig = this.pagination.copy(),
        ordering: List<Ordering> = this.ordering.toList(),
        predicate: Expression = this.predicate,
        rebuiltAtLeastOnce: Boolean = this.rebuiltAtLeastOnce,
    ) = PickerKtConfiguration(
        mimeTypes = mimeTypes,
        selection = selection,
        pagination = pagination,
        ordering = ordering,
        predicate = predicate,
        rebuiltAtLeastOnce = rebuiltAtLeastOnce
    )

    override fun toString(): String {
        return "PickerKtConfiguration(selection=$selection, pagination=$pagination, ordering=$ordering, predicate=$predicate)"
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(JsonSerializer.encodeToString(this.copy(rebuiltAtLeastOnce = true)))
    }

    override fun describeContents() = 0

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PickerKtConfiguration

        if (downloadFolderOnly != other.downloadFolderOnly) return false

        if (mimeTypes != other.mimeTypes) return false
        if (selection != other.selection) return false
        if (pagination != other.pagination) return false
        if (ordering != other.ordering) return false
        if (predicate.toString() != other.predicate.toString()) return false

        return true
    }

    override fun hashCode(): Int {
        var result = 0
        result = 31 * result + downloadFolderOnly.hashCode()

        result = 31 * result + mimeTypes.hashCode()
        result = 31 * result + selection.hashCode()
        result = 31 * result + pagination.hashCode()
        result = 31 * result + ordering.hashCode()
        result = 31 * result + predicate.toString().hashCode()
        return result
    }

    companion object CREATOR : Parcelable.Creator<PickerKtConfiguration> {
        override fun createFromParcel(parcel: Parcel): PickerKtConfiguration =
            JsonSerializer.decodeFromString(parcel.readString()!!)

        override fun newArray(size: Int): Array<PickerKtConfiguration?> = arrayOfNulls(size)
    }


    object Serializer : KSerializer<PickerKtConfiguration> {

        override val descriptor = buildClassSerialDescriptor("PickerKtConfiguration") {
            element<List<MimeType>>("mimeTypes")
            element<Selection>("selection")
            element<PaginationConfig>("pagination")
            element<List<Ordering>>("ordering")
            element<Expression>("predicate")
            element<Int?>("resultCountLimit", isOptional = true)
            element<Boolean>("downloadFolderOnly")
            element<Boolean>("rebuiltAtLeastOnce")
        }

        @OptIn(ExperimentalSerializationApi::class)
        override fun serialize(encoder: Encoder, value: PickerKtConfiguration) {
            encoder.encodeStructure(descriptor) {
                encodeSerializableElement(descriptor, 0, ListSerializer(MimeType.serializer()), value.mimeTypes)
                encodeSerializableElement(descriptor, 1, Selection.serializer(), value.selection)
                encodeSerializableElement(descriptor, 2, PaginationConfig.serializer(), value.pagination)
                encodeSerializableElement(descriptor, 3, ListSerializer(Ordering.serializer()), value.ordering)
                encodeSerializableElement(descriptor, 4, Expression.serializer(), value.predicate)
                encodeBooleanElement(descriptor, 5, value.downloadFolderOnly)
                encodeBooleanElement(descriptor, 6, value.rebuiltAtLeastOnce)
            }
        }

        @OptIn(ExperimentalSerializationApi::class)
        override fun deserialize(decoder: Decoder): PickerKtConfiguration {
            return decoder.decodeStructure(descriptor) {

                var mimeTypes: List<MimeType>? = null
                var selection: Selection? = null
                var pagination: PaginationConfig? = null
                var ordering: List<Ordering>? = null
                var predicate: Expression? = null
                var downloadFolderOnly = false
                var rebuiltAtLeastOnce = false

                loop@ while (true) {
                    when (val index = decodeElementIndex(descriptor)) {
                        DECODE_DONE -> break@loop

                        0 -> mimeTypes = decodeSerializableElement(descriptor, 0, ListSerializer(MimeType.serializer()))
                        1 -> selection = decodeSerializableElement(descriptor, 1, Selection.serializer())
                        2 -> pagination = decodeSerializableElement(descriptor, 2, PaginationConfig.serializer())
                        3 -> ordering = decodeSerializableElement(descriptor, 3, ListSerializer(Ordering.serializer()))
                        4 -> predicate = decodeSerializableElement(descriptor, 4, Expression.serializer())
                        // 5 -> resultCountLimit = decodeNullableSerializableElement(descriptor, 5, Int.serializer().nullable)
                        5 -> downloadFolderOnly = decodeBooleanElement(descriptor, 6)
                        6 -> rebuiltAtLeastOnce = decodeBooleanElement(descriptor, 7)

                        else -> throw SerializationException("Unexpected index $index")
                    }
                }

                PickerKtConfiguration(
                    mimeTypes = mimeTypes!!,
                    selection = selection!!,
                    pagination = pagination!!,
                    ordering = ordering!!,
                    predicate = predicate!!,
                    downloadFolderOnly = downloadFolderOnly,
                    rebuiltAtLeastOnce = rebuiltAtLeastOnce,
                )
            }
        }
    }

    @PickerKtBuilderDslMarker
    class Builder internal constructor(
        mimeTypes: List<MimeType>? = null,
        selection: Selection? = null,
        pagination: PaginationConfig? = null,
        ordering: List<Ordering>? = null,
        private var predicate: Expression? = null,
        var downloadFolderOnly: Boolean = false,
        val rebuiltMode: Boolean = false
    ) {

        private val mimeTypeListBuilder = ListBuilder(mimeTypes?.toMutableList() ?: mutableListOf())

        private val selectionBuilder = if (selection != null) {
            Selection.Builder(
                minSelection = selection.minSelection,
                maxSelection = selection.maxSelection
            )
        } else {
            Selection.Builder()
        }

        private val paginationBuilder = if (pagination != null) {
            PaginationConfig.Builder(
                pageSize = pagination.pageSize,
                prefetchDistance = pagination.prefetchDistance
            )
        } else {
            PaginationConfig.Builder()
        }

        private val orderingBuilder = ListBuilder(ordering?.toMutableList() ?: mutableListOf())

        fun allowMimes(block: ListBuilder<MimeType>.() -> Unit) {
            mimeTypeListBuilder.block()
        }

        fun selection(block: Selection.Builder.() -> Unit) {
            selectionBuilder.block()
        }

        fun downloadFolderOnly(value: Boolean) {
            downloadFolderOnly = value
        }

        // fun pagination(block: PaginationConfig.Builder.() -> Unit) {
        //     paginationBuilder.block()
        // }

        fun orderBy(block: ListBuilder<Ordering>.() -> Unit) {
            orderingBuilder.block()
        }

        fun predicate(block: Expression.() -> Unit) {
            if (predicate == null) predicate = Expression()
            predicate?.block()
        }

        fun build() = PickerKtConfiguration(
            mimeTypes = mimeTypeListBuilder.build(),
            selection = selectionBuilder.build(),
            pagination = paginationBuilder.build(),
            ordering = orderingBuilder.build(),
            predicate = predicate ?: Expression(),
            downloadFolderOnly = downloadFolderOnly,
            rebuiltAtLeastOnce = rebuiltMode
        )
    }
}

fun ListBuilder<MimeType>.addByGroup(item: () -> MimeType.Group) {
    MimeType.knownValuesOfGroup(item()).forEach {
        add { it }
    }
}