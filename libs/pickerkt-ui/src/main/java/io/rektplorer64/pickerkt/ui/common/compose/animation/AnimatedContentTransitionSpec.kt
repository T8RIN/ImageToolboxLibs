package io.rektplorer64.pickerkt.ui.common.compose.animation

import androidx.compose.animation.*

@OptIn(ExperimentalAnimationApi::class)
fun <T : Comparable<T>> slideContentTransitionSpec(clip: Boolean = true): AnimatedContentScope<T>.() -> ContentTransform =
    slideContentTransitionSpec(clip = clip) { it }


@OptIn(ExperimentalAnimationApi::class)
fun <X, T : Comparable<T>> slideContentTransitionSpec(
    clip: Boolean = true,
    map: (X) -> T
): AnimatedContentScope<X>.() -> ContentTransform =
    {
        val initialValue = map(initialState)
        val targetValue = map(targetState)
        when {
            targetValue > initialValue -> {
                slideInVertically { height -> height } + fadeIn() with
                        slideOutVertically { height -> -height } + fadeOut()
            }
            targetValue == initialValue -> {
                fadeIn() with fadeOut()
            }
            else -> {
                slideInVertically { height -> -height } + fadeIn() with
                        slideOutVertically { height -> height } + fadeOut()
            }
        }.using(SizeTransform(clip = clip))
    }

