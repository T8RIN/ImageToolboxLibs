package io.rektplorer64.pickerkt.builder

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class Selection internal constructor(
    val minSelection: Int = 1,
    val maxSelection: Int?
) {

    init {
        assert(minSelection >= 1) {
            "minSelection ($minSelection) should not be less than or equals to 1"
        }

        if (maxSelection != null) {
            assert(minSelection <= maxSelection) {
                "minSelection ($minSelection) should be less than or equals to maxSelection ($maxSelection)"
            }
        }
    }

    @Transient
    val range = if (maxSelection != null) minSelection..maxSelection else null

    @PickerKtBuilderDslMarker
    class Builder internal constructor(
        private var minSelection: Int = 1,
        private var maxSelection: Int? = null
    ) {

        internal constructor(selection: Selection) : this(
            selection.minSelection,
            selection.maxSelection
        )

        fun minSelection(min: Int) {
            minSelection = min
        }

        fun maxSelection(max: Int?) {
            maxSelection = max
        }

        fun build() = Selection(
            minSelection = minSelection,
            maxSelection = maxSelection
        )
    }
}