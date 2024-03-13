package io.rektplorer64.pickerkt.ui.common.compose

import androidx.compose.runtime.Composable

@Composable
fun shouldDisplayMenuIconOnAppBar(): Boolean = LocalWindowSize.current <= WindowSize.Compact