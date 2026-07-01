package com.t8rin.imagetoolbox.app

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.transformations
import com.jhlabs.JhFilter
import dalvik.system.DexFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.isAccessible

@Composable
fun MainActivity.JhLabsRustHypothesis(
    modifier: Modifier = Modifier,
    packagePrefixes: List<String> = emptyList()
) {
    val context = LocalContext.current

    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var filters by remember { mutableStateOf<List<JhFilterEntry>>(emptyList()) }
    var skipped by remember { mutableStateOf<List<String>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        isLoading = true

        val result = withContext(Dispatchers.Default) {
            collectJhFilters(
                context = context
            )
        }

        filters = result.filters
        skipped = result.skipped
        isLoading = false
    }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        selectedUri = uri
    }

    val pagerState = rememberPagerState(
        pageCount = { filters.size.coerceAtLeast(1) }
    )

    val currentFilter by remember {
        derivedStateOf {
            filters.getOrNull(pagerState.currentPage)
        }
    }

    val transformations = remember(filters) {
        filters.associate { entry ->
            entry.key to GenericTransformation(
                key = entry.key
            ) { bitmap ->
                entry.filter.filter(bitmap)
            }
        }
    }

    Surface(
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        imagePicker.launch(
                            PickVisualMediaRequest(
                                ActivityResultContracts.PickVisualMedia.ImageOnly
                            )
                        )
                    }
                ) {
                    Text("Choose image")
                }

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Filters: ${filters.size}",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    if (skipped.isNotEmpty()) {
                        Text(
                            text = "Skipped: ${skipped.size}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            currentFilter?.let { filter ->
                Text(
                    text = filter.name,
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    text = "${pagerState.currentPage + 1} / ${filters.size}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                filters.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "JhFilter implementations not found"
                        )
                    }
                }

                selectedUri == null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Choose image to test filters"
                        )
                    }
                }

                else -> {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize()
                    ) { page ->
                        val entry = filters[page]
                        val transformation = transformations.getValue(entry.key)

                        val request = remember(selectedUri, transformation) {
                            ImageRequest.Builder(context)
                                .data(selectedUri)
                                .transformations(transformation)
                                .build()
                        }

                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            AsyncImage(
                                model = request,
                                contentDescription = entry.name,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit
                            )
                        }
                    }
                }
            }
        }
    }
}

private data class JhFilterCollectResult(
    val filters: List<JhFilterEntry>,
    val skipped: List<String>
)

@Suppress("DEPRECATION")
private fun collectJhFilters(
    context: Context
): JhFilterCollectResult {
    val classLoader = context.classLoader
    val apkPaths = buildList {
        add(context.applicationInfo.sourceDir)
        context.applicationInfo.splitSourceDirs?.let(::addAll)
    }.distinct()

    val classNames = apkPaths
        .asSequence()
        .flatMap { apkPath ->
            runCatching {
                val dexFile = DexFile(apkPath)
                try {
                    dexFile.entries().asSequence().toList()
                } finally {
                    dexFile.close()
                }
            }.getOrElse {
                emptyList()
            }.asSequence()
        }
        .distinct()
        .toList()

    val skipped = mutableListOf<String>()

    val filters = classNames
        .mapNotNull { className ->
            runCatching {
                val clazz = Class
                    .forName(className, false, classLoader)
                    .asSubclass(JhFilter::class.java)

                if (
                    clazz == JhFilter::class.java ||
                    clazz.isInterface ||
                    java.lang.reflect.Modifier.isAbstract(clazz.modifiers)
                ) {
                    return@runCatching null
                }

                val filter = instantiateJhFilter(clazz.kotlin)
                    ?: run {
                        skipped += className
                        null
                    }

                filter?.let {
                    JhFilterEntry(
                        name = clazz.simpleName ?: className.substringAfterLast('.'),
                        key = clazz.name,
                        filter = it
                    )
                }
            }.getOrElse { throwable ->
                if (throwable !is ClassCastException) {
                    skipped += className
                    Log.w("JhLabsRustHypothesis", "Skip $className", throwable)
                }
                null
            }
        }
        .sortedBy { it.name }

    return JhFilterCollectResult(
        filters = filters,
        skipped = skipped.sorted()
    )
}

private fun instantiateJhFilter(
    kClass: KClass<out JhFilter>
): JhFilter? {
    kClass.objectInstance?.let {
        return it
    }

    val primaryConstructor = kClass.primaryConstructor

    if (primaryConstructor != null && primaryConstructor.parameters.all { it.isOptional }) {
        return runCatching {
            primaryConstructor.isAccessible = true
            primaryConstructor.callBy(emptyMap())
        }.getOrNull()
    }

    return runCatching {
        kClass.java.getDeclaredConstructor().apply {
            isAccessible = true
        }.newInstance()
    }.getOrNull()
}

private data class JhFilterEntry(
    val name: String,
    val key: String,
    val filter: JhFilter
)