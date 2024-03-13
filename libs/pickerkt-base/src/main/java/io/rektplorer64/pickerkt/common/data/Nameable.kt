package io.rektplorer64.pickerkt.common.data

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import io.rektplorer64.pickerkt.common.property.LocallyNamed

interface Nameable : LocallyNamed {
    val name: String
}

val Nameable.localizedName: String
    @Composable get() = nameStringRes?.let { stringResource(it) } ?: name