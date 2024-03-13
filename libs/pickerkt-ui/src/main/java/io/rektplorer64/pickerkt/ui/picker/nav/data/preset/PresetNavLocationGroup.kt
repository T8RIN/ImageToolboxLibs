package io.rektplorer64.pickerkt.ui.picker.nav.data.preset

import androidx.annotation.StringRes
import io.rektplorer64.pickerkt.common.data.Nameable
import io.rektplorer64.pickerkt.ui.R

enum class PresetNavLocationGroup(@StringRes override val nameStringRes: Int) : Nameable {
    Common(nameStringRes = R.string.nav_group_general),
    Libraries(nameStringRes = R.string.nav_group_library),
}