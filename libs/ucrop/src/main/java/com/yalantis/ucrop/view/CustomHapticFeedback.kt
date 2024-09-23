package com.yalantis.ucrop.view

import android.content.Context
import android.os.Build
import android.view.HapticFeedbackConstants
import android.view.View
import android.view.accessibility.AccessibilityManager
import androidx.core.content.getSystemService

private fun View.vibrate() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
    reallyPerformHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
} else {
    reallyPerformHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
}

private fun View.vibrateStrong() = reallyPerformHapticFeedback(HapticFeedbackConstants.LONG_PRESS)

private fun View.reallyPerformHapticFeedback(feedbackConstant: Int) {
    if (context.isTouchExplorationEnabled()) return

    isHapticFeedbackEnabled = true

    performHapticFeedback(feedbackConstant, HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING)
}

private fun Context.isTouchExplorationEnabled(): Boolean {
    val accessibilityManager = getSystemService<AccessibilityManager>()

    return accessibilityManager?.isTouchExplorationEnabled ?: false
}

internal data class CustomHapticFeedback(
    val hapticsStrength: Int,
    val view: View
) {
    fun performHapticFeedback(hapticFeedbackType: Int) {
        when (hapticFeedbackType) {
            0 -> {
                when (hapticsStrength) {
                    1 -> view.vibrate()
                    2 -> view.vibrateStrong()
                }
            }

            1 -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                    view.reallyPerformHapticFeedback(HapticFeedbackConstants.TEXT_HANDLE_MOVE)
                } else view.reallyPerformHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
            }
        }
    }
}