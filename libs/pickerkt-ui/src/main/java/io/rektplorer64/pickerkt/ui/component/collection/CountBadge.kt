package io.rektplorer64.pickerkt.ui.component.collection

import androidx.compose.animation.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import io.rektplorer64.pickerkt.ui.common.compose.animation.slideContentTransitionSpec

@Composable
@OptIn(ExperimentalAnimationApi::class)
fun CountBadge(
    count: Int?,
    modifier: Modifier = Modifier,
    visible: Boolean = count != null && count > 0,
    icon: (@Composable () -> Unit)? = {
        Icon(
            Icons.Default.CheckCircleOutline,
            contentDescription = null,
            modifier = Modifier.fillMaxSize()
        )
    },
    contentDescription: String?
) {
    AnimatedVisibility(
        modifier = modifier.animateContentSize(),
        visible = visible,
        enter = fadeIn() + scaleIn(),
        exit = fadeOut() + scaleOut()
    ) {
        Badge(
            modifier = Modifier.semantics(mergeDescendants = true) {
                if (contentDescription != null) {
                    this.contentDescription = contentDescription
                }
            }
        ) {
            if (icon != null) {
                Box(
                    modifier = Modifier.padding(4.dp).size(12.dp),
                    content = { icon() }
                )
            }

            AnimatedContent(
                targetState = count ?: 0,
                transitionSpec = slideContentTransitionSpec()
            ) {
                Text(
                    text = it.toString(),
                    modifier = Modifier.padding(start = if (icon != null) 0.dp else 4.dp ,end = 4.dp),
                    color = LocalContentColor.current,
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}