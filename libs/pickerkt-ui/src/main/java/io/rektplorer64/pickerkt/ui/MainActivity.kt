package io.rektplorer64.pickerkt.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.paging.compose.collectAsLazyPagingItems
import com.jakewharton.threetenabp.AndroidThreeTen
import io.rektplorer64.pickerkt.CollectionViewModel
import io.rektplorer64.pickerkt.ContentViewModel
import io.rektplorer64.pickerkt.builder.PickerKtConfiguration
import io.rektplorer64.pickerkt.common.data.collectAsResultState
import io.rektplorer64.pickerkt.content.model.Content
import io.rektplorer64.pickerkt.default
import io.rektplorer64.pickerkt.ui.common.PickerKtActivityResult
import io.rektplorer64.pickerkt.ui.common.PickerKtActivityResult.Companion.RESULT_CONTRACT_KEY_PICKER_CONFIG
import io.rektplorer64.pickerkt.ui.common.compose.LocalPickerConfig
import io.rektplorer64.pickerkt.ui.common.data.extension.navHostRouteForPreviewByReferrer
import io.rektplorer64.pickerkt.ui.picker.library.LibraryViewModel
import io.rektplorer64.pickerkt.ui.viewer.ContentPreviewScreenBody
import io.rektplorer64.pickerkt.ui.viewer.ContentViewerViewModel
import io.rektplorer64.pickerkt.ui.selection.*
import io.rektplorer64.pickerkt.ui.ui.theme.setThemedContent


class MainActivity : ComponentActivity() {

    private val contentPreviewViewModel by viewModels<ContentViewerViewModel>()

    private val collectionViewModel by viewModels<CollectionViewModel> {
        CollectionViewModel.Factory(application = application)
    }

    override fun onResume() {
        super.onResume()
        WindowCompat.setDecorFitsSystemWindows(window, false)
    }

    @OptIn(ExperimentalComposeUiApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        AndroidThreeTen.init(applicationContext)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        val config = intent.getParcelableExtra<PickerKtConfiguration>(RESULT_CONTRACT_KEY_PICKER_CONFIG)!!

        setThemedContent {

            val selectionController = rememberContentSelectionController(
                maxSelection = config.selection.maxSelection ?: Int.MAX_VALUE
            )

            val navController = rememberNavController()
            val collections by collectionViewModel.collectionListFlow.collectAsResultState()

            NavHost(
                navController = navController,
                startDestination = "picker"
            ) {
                composable("picker") {
                    CompositionLocalProvider(LocalPickerConfig provides config) {
                        PickerScreen(
                            controller = selectionController,
                            contentGroups = LocalPickerConfig.current.mimeTypes
                                .map { it.group }
                                .distinct(),
                            collections = collections,
                            onContentClick = { content, referrerCollectionId ->
                                contentPreviewViewModel.setPreviewContentId(content.id)
                                navController.navigate(
                                    content.navHostRouteForPreviewByReferrer(
                                        referrerCollectionId
                                    )
                                )
                            },
                            onSelectionConfirm = { finishActivityWithPickerResult(it) },
                            onPickerCancel = { cancelAndClosePicker() }
                        )
                    }
                }

                composable(
                    route = "content/preview/{contentId}?referrer={referrer}",
                    arguments = listOf(
                        navArgument("contentId") { type = NavType.LongType },
                        navArgument("referrer") { type = NavType.StringType }
                    )
                ) {

                    val referrer = it.arguments!!.getString("referrer")!!
                    val contentId =
                        contentPreviewViewModel.previewContentId.collectAsState().value

                    contentId ?: return@composable

                    val collectionLazyPagingItems = when {
                        "collection" in referrer -> {
                            viewModel<ContentViewModel>(
                                key = referrer,
                                viewModelStoreOwner = LocalContext.current as ComponentActivity
                            ).contentListFlow.collectAsLazyPagingItems()
                        }
                        "library" in referrer -> {
                            viewModel<LibraryViewModel>(
                                key = referrer,
                                viewModelStoreOwner = LocalContext.current as ComponentActivity
                            ).recentContentList.collectAsLazyPagingItems()
                        }
                        else -> {
                            throw IllegalStateException()
                        }
                    }

                    CompositionLocalProvider(LocalPickerConfig provides config) {
                        if (collectionLazyPagingItems.itemCount > 0) {
                            ContentPreviewScreenBody(
                                onCurrentContentSelectionClick = {
                                    selectionController.toggleSelection(
                                        it
                                    )
                                },
                                selectionController = selectionController,
                                contentId = contentId,
                                collectionLazyPagingItems = collectionLazyPagingItems,
                                onMainPreviewChange = {
                                    contentPreviewViewModel.setPreviewContentId(it.id)
                                },
                                onBackPress = {
                                    navController.navigateUp()
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    private fun finishActivityWithPickerResult(selection: List<Content>) {
        setResult(
            Activity.RESULT_OK,
            Intent().apply {
                putExtra(
                    PickerKtActivityResult.RESULT_CONTRACT_KEY_RESULT_URL_LIST_CONFIG,
                    selection.map { it.uri }.toTypedArray()
                )
            }
        )
        finish()
    }

    private fun cancelAndClosePicker() {
        setResult(Activity.RESULT_CANCELED)
        finish()
    }

}

