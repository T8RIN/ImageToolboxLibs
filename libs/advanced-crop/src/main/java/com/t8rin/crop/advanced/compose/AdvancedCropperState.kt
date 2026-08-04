package com.t8rin.crop.advanced.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.t8rin.crop.advanced.view.AdvancedCropViewState

private const val MaxHistorySize = 50

@Stable
class AdvancedCropperState {
    var canUndo: Boolean by mutableStateOf(false)
        private set

    var canRedo: Boolean by mutableStateOf(false)
        private set

    internal var resetVersion: Int by mutableIntStateOf(0)
        private set

    private var imageKey: Any? = null
    private var attachmentKey: Any? = null
    private var captureSnapshot: (() -> AdvancedCropSnapshot?)? = null
    private var restoreSnapshot: ((AdvancedCropSnapshot) -> Unit)? = null
    private var pendingSnapshot: AdvancedCropSnapshot? = null
    private var currentSnapshot: AdvancedCropSnapshot? = null
    private var restoreCurrentSnapshotOnViewReady = false
    private var consumedResetVersion = 0
    private val undoHistory = ArrayDeque<AdvancedCropSnapshot>()
    private val redoHistory = ArrayDeque<AdvancedCropSnapshot>()

    fun undo() {
        endTransformation()
        val current = currentSnapshot ?: captureSnapshot?.invoke() ?: return
        val previous = undoHistory.removeLastOrNull() ?: return

        redoHistory.addLast(current)
        redoHistory.trimToMaxSize()
        currentSnapshot = previous
        restoreSnapshot?.invoke(previous)
        updateAvailability()
    }

    fun redo() {
        endTransformation()
        val current = currentSnapshot ?: captureSnapshot?.invoke() ?: return
        val next = redoHistory.removeLastOrNull() ?: return

        undoHistory.addLast(current)
        undoHistory.trimToMaxSize()
        currentSnapshot = next
        restoreSnapshot?.invoke(next)
        updateAvailability()
    }

    fun beginTransformation() {
        if (pendingSnapshot == null) {
            pendingSnapshot = captureSnapshot?.invoke()
        }
    }

    fun endTransformation(): Boolean {
        val before = pendingSnapshot ?: return false
        pendingSnapshot = null
        val after = captureSnapshot?.invoke() ?: return false
        currentSnapshot = after

        if (!before.hasSameStateAs(after)) {
            undoHistory.addLast(before)
            undoHistory.trimToMaxSize()
            redoHistory.clear()
            updateAvailability()
            return true
        }
        return false
    }

    fun discardPendingTransformation() {
        pendingSnapshot = null
    }

    fun prepareForReattachment(attachmentKey: Any? = null) {
        if (attachmentKey != null && this.attachmentKey !== attachmentKey) return
        if (restoreCurrentSnapshotOnViewReady) return
        if (pendingSnapshot != null || currentSnapshot == null) {
            captureSnapshot?.invoke()?.let { currentSnapshot = it }
        }
        restoreCurrentSnapshotOnViewReady = currentSnapshot != null
    }

    internal fun snapshotForLayoutChange(): AdvancedCropSnapshot? {
        if (pendingSnapshot != null) {
            captureSnapshot?.invoke()?.let { currentSnapshot = it }
        }
        if (currentSnapshot == null) {
            currentSnapshot = captureSnapshot?.invoke()
        }
        return currentSnapshot
    }

    internal fun attach(
        attachmentKey: Any,
        imageKey: Any?,
        captureSnapshot: () -> AdvancedCropSnapshot?,
        restoreSnapshot: (AdvancedCropSnapshot) -> Unit
    ) {
        val imageChanged = this.imageKey != imageKey
        if (!imageChanged && this.attachmentKey != null && this.attachmentKey !== attachmentKey) {
            if (pendingSnapshot != null || currentSnapshot == null) {
                this.captureSnapshot?.invoke()?.let { currentSnapshot = it }
            }
            restoreCurrentSnapshotOnViewReady = currentSnapshot != null
            pendingSnapshot = null
        }
        if (imageChanged) {
            this.imageKey = imageKey
            pendingSnapshot = null
            currentSnapshot = null
            restoreCurrentSnapshotOnViewReady = false
            undoHistory.clear()
            redoHistory.clear()
            updateAvailability()
        }
        this.attachmentKey = attachmentKey
        this.captureSnapshot = captureSnapshot
        this.restoreSnapshot = restoreSnapshot
    }

    internal fun detach(attachmentKey: Any) {
        if (this.attachmentKey !== attachmentKey) return
        this.attachmentKey = null
        restoreCurrentSnapshotOnViewReady = currentSnapshot != null
        captureSnapshot = null
        restoreSnapshot = null
        pendingSnapshot = null
    }

    internal fun onViewReady() {
        if (restoreCurrentSnapshotOnViewReady) {
            currentSnapshot?.let { restoreSnapshot?.invoke(it) }
            restoreCurrentSnapshotOnViewReady = false
        } else if (currentSnapshot == null) {
            currentSnapshot = captureSnapshot?.invoke()
        }
    }

    fun clearHistory() {
        pendingSnapshot = null
        currentSnapshot = captureSnapshot?.invoke()
        restoreCurrentSnapshotOnViewReady = false
        undoHistory.clear()
        redoHistory.clear()
        updateAvailability()
    }

    fun reset() {
        attachmentKey = null
        pendingSnapshot = null
        currentSnapshot = null
        restoreCurrentSnapshotOnViewReady = false
        undoHistory.clear()
        redoHistory.clear()
        resetVersion++
        updateAvailability()
    }

    internal fun consumeReset(): Boolean {
        if (consumedResetVersion == resetVersion) return false
        consumedResetVersion = resetVersion
        return true
    }

    private fun updateAvailability() {
        canUndo = undoHistory.isNotEmpty()
        canRedo = redoHistory.isNotEmpty()
    }

    private fun ArrayDeque<AdvancedCropSnapshot>.trimToMaxSize() {
        while (size > MaxHistorySize) removeFirst()
    }
}

@Composable
fun rememberAdvancedCropperState(): AdvancedCropperState = remember {
    AdvancedCropperState()
}

internal data class AdvancedCropSnapshot(
    val rotationAngle: Float,
    val sourceRotationDegrees: Int,
    val isFlippedHorizontally: Boolean,
    val viewState: AdvancedCropViewState?
) {
    fun hasSameStateAs(other: AdvancedCropSnapshot): Boolean {
        val hasSameViewState = viewState == null ||
                other.viewState == null ||
                viewState.hasSameStateAs(other.viewState)
        return rotationAngle == other.rotationAngle &&
                sourceRotationDegrees == other.sourceRotationDegrees &&
                isFlippedHorizontally == other.isFlippedHorizontally &&
                hasSameViewState
    }
}
