package io.rektplorer64.pickerkt.ui

import android.annotation.SuppressLint
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material.icons.outlined.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import coil.compose.rememberImagePainter
import com.google.accompanist.insets.navigationBarsPadding
import com.google.accompanist.insets.statusBarsPadding
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import io.rektplorer64.pickerkt.ContentViewModel
import io.rektplorer64.pickerkt.collection.model.Collection
import io.rektplorer64.pickerkt.common.data.Result
import io.rektplorer64.pickerkt.content.model.Content
import io.rektplorer64.pickerkt.content.model.groupByCollectionAndCount
import io.rektplorer64.pickerkt.contentresolver.MimeType
import io.rektplorer64.pickerkt.contentresolver.PredefinedCollections
import io.rektplorer64.pickerkt.ui.common.compose.LocalPickerConfig
import io.rektplorer64.pickerkt.ui.common.compose.LocalWindowSize
import io.rektplorer64.pickerkt.ui.common.compose.WindowSize
import io.rektplorer64.pickerkt.ui.component.collection.CountBadge
import io.rektplorer64.pickerkt.ui.layout.PickerAppLayout
import io.rektplorer64.pickerkt.ui.layout.rememberPickerAppDrawerState
import io.rektplorer64.pickerkt.ui.picker.collection.CollectionScreen
import io.rektplorer64.pickerkt.ui.picker.library.LibraryScreen
import io.rektplorer64.pickerkt.ui.picker.library.LibraryViewModel
import io.rektplorer64.pickerkt.ui.picker.nav.DrawerContent
import io.rektplorer64.pickerkt.ui.picker.nav.NavigationRailContent
import io.rektplorer64.pickerkt.ui.picker.nav.data.NavLocation
import io.rektplorer64.pickerkt.ui.picker.nav.data.preset.PresetNavLocation
import io.rektplorer64.pickerkt.ui.picker.selectionmanager.ContentSelectionModal
import io.rektplorer64.pickerkt.ui.selection.ContentSelectionController
import kotlinx.coroutines.launch

//@Composable
//@OptIn(
//    ExperimentalFoundationApi::class,
//    ExperimentalMaterial3Api::class,
//    ExperimentalStdlibApi::class,
//    ExperimentalComposeUiApi::class,
//    ExperimentalAnimationApi::class
//)
//fun PickerScreen(
//    modifier: Modifier = Modifier,
//    collectionViewModel: CollectionViewModel,
//    controller: ContentSelectionController,
//    onContentClick: (Content, referrer: String) -> Unit
//) {
//    PickerScreen(
//        modifier = modifier,
//        collectionViewModel = collectionViewModel,
//        controller = selectionController,
//        onContentClick = onContentClick
//    )
//}

@Composable
@OptIn(
    ExperimentalFoundationApi::class,
    ExperimentalMaterial3Api::class,
    ExperimentalStdlibApi::class,
    ExperimentalComposeUiApi::class,
    ExperimentalAnimationApi::class
)
fun PickerScreen(
    modifier: Modifier = Modifier,
    contentGroups: List<MimeType.Group>,
    collections: Result<List<Collection>>,
    controller: ContentSelectionController,
    onContentClick: (Content, referrer: String) -> Unit,
    onSelectionConfirm: (List<Content>) -> Unit,
    onPickerCancel: () -> Unit
) {

    configSystemUi()

    val navController = rememberNavController()
    val windowSize = LocalWindowSize.current

    val drawerState = rememberPickerAppDrawerState(
        initialValue = if (windowSize >= WindowSize.Expanded) DrawerValue.Open else DrawerValue.Closed
    )
    val coroutineScope = rememberCoroutineScope()

    fun toggleDrawer() = coroutineScope.launch {
        if (drawerState.isClosed) drawerState.open() else drawerState.close()
    }

    fun NavController.navigateWithSaveState(route: String) {
        navigate(route = route) {
            navController.graph.startDestinationRoute?.let { route ->
                popUpTo(route) {
                    saveState = false
                }
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    BackHandler {
        if (navController.currentBackStackEntry?.destination?.route == PresetNavLocation.AllFolders.navHostRoute) {
            navController.navigateWithSaveState("cancel")
        }
    }


    val navRouteEntry by navController.currentBackStackEntryFlow.collectAsState(initial = null)
    val navRoute = navRouteEntry?.destination?.route

    val selectionInCollectionMap by rememberUpdatedState(controller.canonicalSelectionList.groupByCollectionAndCount())

    val presetNavLocations by rememberUpdatedState(
        newValue = PresetNavLocation.values()
            .filter {
                (if (contentGroups.isEmpty()) true else (it.mimeTypeGroup in contentGroups || it.mimeTypeGroup == null))
            }
    )

    PickerAppLayout(
        modifier = modifier.statusBarsPadding(),
        drawerState = drawerState,
        drawerContent = {
            DrawerContent(
                drawerState = drawerState,
                collections = collections,
                onClick = {
                    navController.navigateWithSaveState(it.navHostRoute)
                    if (windowSize <= WindowSize.Medium) toggleDrawer()
                },
                currentRoute = navRouteEntry?.arguments?.getString("collectionId") ?: navRoute ?: "",
                selectionInCollectionMap = selectionInCollectionMap,
                presetNavLocations = presetNavLocations
            )
        },
        floatingActionButton = {
            Column(
                modifier = Modifier.navigationBarsPadding(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.End
            ) {
                FloatingActionButton(
                    onClick = {
                        navController.navigateWithSaveState("cancel")
                    },
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    content = {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = null
                        )
                    }
                )

                LargeFloatingActionButton(onClick = {
                    navController.navigateWithSaveState("selection")
                }) {
                    BadgedBox(
                        badge = {
                            CountBadge(
                                count = controller.size,
                                icon = null,
                                contentDescription = "Totally, you selected ${controller.size} items."
                            )
                        }
                    ) {
                        Icon(
                            Icons.Outlined.CheckCircleOutline,
                            contentDescription = null,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
            }
        },
        sidebar = {
            NavigationRailContent(
                modifier = Modifier.align(Alignment.Center),
                onNavIconClick = { toggleDrawer() },
                navRoute = navRoute,
                onClick = {
                    navController.navigateWithSaveState(it.navHostRoute)
                },
                selectionInCollectionMap = selectionInCollectionMap,
                presetNavLocations = presetNavLocations
            )
        }
    ) {
        NavHost(
            navController = navController,
            startDestination = PresetNavLocation.AllFolders.navHostRoute
        ) {
            presetNavLocations
                .filter { !it.hasCorrespondingCollection }
                .map { NavLocation.Preset(it) to it }
                .forEach { (navLocation, enum) ->
                    composable(route = navLocation.navHostRoute) {
                        val viewModel = viewModel<LibraryViewModel>(
                            key = navLocation.id,
                            viewModelStoreOwner = LocalContext.current as ComponentActivity,
                            factory = LibraryViewModel.Factory(
                                application = (LocalContext.current as ComponentActivity).application,
                                mimeTypeGroup = enum.mimeTypeGroup,
                                config = LocalPickerConfig.current
                            )
                        )

                        LibraryScreen(
                            modifier = Modifier.fillMaxSize(),
                            viewModel = viewModel,
                            selectionMap = controller.canonicalSelectionOrderMap,
                            navLocation = enum,
                            onNavIconClick = { toggleDrawer() },
                            onContentItemClick = { onContentClick(it, navLocation.id) },
                            onContentCheckClick = { controller.toggleSelection(it) },
                            onCollectionClick = {
                                navController.navigateWithSaveState("collections/${it.id}?showNavUpIcon=true&mimeGroup=${enum.mimeTypeGroup?.name}")
                            }
                        )
                    }
                }

            // Since we do have some Collections as pre-defined ones, we cannot have a destination w/ arguments
            // as the starting destination, so we have to separately define them here.
            PredefinedCollections
                .map { NavLocation.Collection(it) to it }
                .forEach { (navLocation, enum) ->
                    composable(route = navLocation.navHostRoute) {

                        val viewModel = viewModel<ContentViewModel>(
                            key = navLocation.id,
                            viewModelStoreOwner = LocalContext.current as ComponentActivity,
                            factory = ContentViewModel.Factory(
                                application = (LocalContext.current as ComponentActivity).application,
                                collectionId = enum.id,
                                config = LocalPickerConfig.current
                            )
                        )

                        CollectionScreen(
                            onNavIconClick = { toggleDrawer() },
                            viewModel = viewModel,
                            showNavUpIcon = false,
                            selectionController = controller,
                            onItemClick = { onContentClick(it, navLocation.id) }
                        )
                    }
                }

            composable(
                route = "collections/{collectionId}?showNavUpIcon={showNavUpIcon}&mimeGroup={mimeGroup}",
                arguments = listOf(
                    navArgument("collectionId") { type = NavType.StringType },
                    navArgument("mimeGroup") { type = NavType.StringType; nullable = true },
                    navArgument("showNavUpIcon") { type = NavType.BoolType; defaultValue = false }
                )
            ) {
                val collectionId = it.arguments!!.getString("collectionId")!!
                val mimeGroup =
                    it.arguments!!.getString("mimeGroup")?.let { MimeType.Group.valueOf(it) }
                val showNavUpIcon = it.arguments!!.getBoolean("showNavUpIcon", false)
                val navLocationId = NavLocation.locationIdOfCollection(collectionId)

                val viewModel: ContentViewModel = viewModel(
                    key = navLocationId,
                    viewModelStoreOwner = LocalContext.current as ComponentActivity,
                    factory = ContentViewModel.Factory(
                        application = (LocalContext.current as ComponentActivity).application,
                        collectionId = collectionId,
                        config = LocalPickerConfig.current
                    )
                )

                CollectionScreen(
                    showNavUpIcon = showNavUpIcon,
                    onNavIconClick = {
                        if (showNavUpIcon) navController.navigateUp() else toggleDrawer()
                    },
                    viewModel = viewModel,
                    selectionController = controller,
                    onItemClick = { onContentClick(it, navLocationId) }
                )
            }


            dialog(
                route = "selection",
                dialogProperties = DialogProperties(
                    usePlatformDefaultWidth = false,
                    dismissOnBackPress = true,
                    dismissOnClickOutside = false
                )
            ) {
                ContentSelectionModal(
                    selectedContents = controller.canonicalSelectionList,
                    onCloseClick = {
                        controller.replaceAllWith(it)
                        navController.navigateUp()
                    },
                    onConfirmed = {
                        // TODO: Show a confirmation dialog and Save Result
                        controller.replaceAllWith(it)
                        navController.navigateUp()
                        navController.navigateWithSaveState("confirm")
                    }
                )
            }

            dialog(route = "confirm") {
                val limit = remember { 19 }
                val selection = controller.canonicalSelectionList

                AlertDialog(
                    onDismissRequest = { navController.navigateUp() },
                    icon = {
                        Icon(
                            Icons.Outlined.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(36.dp)
                        )
                    },
                    title = {
                        Text(
                            "Confirm your selections?",
                            textAlign = TextAlign.Center
                        )
                    },
                    text = {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                buildAnnotatedString {
                                    append("Confirm to select the following ")

                                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                        append("${selection.size} items")
                                    }
                                },
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                            LazyVerticalGrid(
                                cells = GridCells.Fixed(selection.size.coerceAtMost(5)),
                                modifier = Modifier
                                    .sizeIn(maxHeight = 300.dp)
                                    .clip(RoundedCornerShape(16.dp))
                            ) {
                                items(selection.take(limit)) {
                                    Image(
                                        rememberImagePainter(it.uri),
                                        contentDescription = null,
                                        modifier = Modifier
                                            .aspectRatio(1f)
                                            .fillParentMaxWidth(),
                                        contentScale = ContentScale.Crop,
                                        alignment = Alignment.Center
                                    )
                                }

                                if (selection.size > limit) {
                                    item {
                                        Surface(
                                            modifier = Modifier
                                                .aspectRatio(1f)
                                                .fillParentMaxSize(),
                                            tonalElevation = LocalAbsoluteTonalElevation.current + 1.dp
                                        ) {
                                            Box(
                                                modifier = Modifier.fillMaxSize(),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text("+${selection.size - limit} more")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                onSelectionConfirm(selection)
                                // navController.navigateUp()
                            }
                        ) {
                            Text("Confirm")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { navController.navigateUp() }) {
                            Text("Cancel")
                        }
                    }
                )
            }

            dialog(route = "cancel") {
                AlertDialog(
                    onDismissRequest = { navController.navigateUp() },
                    icon = {
                        Icon(
                            Icons.Outlined.ExitToApp,
                            contentDescription = null,
                            modifier = Modifier.size(36.dp)
                        )
                    },
                    title = {
                        Text(
                            "Cancel and Exit the selection?",
                            textAlign = TextAlign.Center
                        )
                    },
                    text = {
                        Text(
                            "The selection is discarded if you exit.",
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    confirmButton = {
                        Button(onClick = { navController.navigateUp() }) {
                            Text("Cancel")
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { onPickerCancel() }
                        ) {
                            Text("Exit Anyway")
                        }
                    }
                )
            }
        }
    }
}

@Composable
@SuppressLint("ComposableNaming")
private fun configSystemUi() {
    val systemUiController = rememberSystemUiController()
    val isDarkTheme = isSystemInDarkTheme()
    val primaryColor = MaterialTheme.colorScheme.primary

    SideEffect {
        val colorAlpha = if (isDarkTheme) 0.1f else 0.2f
        val color = primaryColor.copy(alpha = colorAlpha)

        systemUiController.setSystemBarsColor(color = color, darkIcons = !isDarkTheme)
    }
}
