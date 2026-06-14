package com.barackilic.gallery.ui.viewer

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import coil3.compose.AsyncImage
import com.barackilic.gallery.R
import com.barackilic.gallery.data.mediastore.contentUri
import com.barackilic.gallery.domain.model.MediaItem
import com.barackilic.gallery.domain.model.MediaType
import com.barackilic.gallery.ui.common.MediaThumbRequest
import me.saket.telephoto.zoomable.coil3.ZoomableAsyncImage
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun ViewerScreen(
    initialIndex: Int,
    bucketId: Long?,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: ViewerViewModel = koinViewModel { parametersOf(bucketId) }
    val items = viewModel.items.collectAsLazyPagingItems()

    var systemBarsVisible by rememberSaveable { mutableStateOf(true) }
    ImmersiveSystemBars(visible = systemBarsVisible)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black),
    ) {
        ViewerPager(
            items = items,
            initialIndex = initialIndex,
            onToggleBars = { systemBarsVisible = !systemBarsVisible },
            modifier = Modifier.fillMaxSize(),
        )
        AnimatedVisibility(
            visible = systemBarsVisible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.TopCenter),
        ) {
            ViewerTopBar(
                onBack = onBack,
                modifier = Modifier.statusBarsPadding(),
            )
        }
    }
}

@Composable
private fun ViewerPager(
    items: LazyPagingItems<MediaItem>,
    initialIndex: Int,
    onToggleBars: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val itemCount = items.itemCount
    if (itemCount == 0) {
        Box(modifier = modifier.background(Color.Black))
        return
    }
    val pagerState = rememberPagerState(
        initialPage = initialIndex.coerceIn(0, itemCount - 1),
    ) { items.itemCount }
    HorizontalPager(
        state = pagerState,
        modifier = modifier,
        beyondViewportPageCount = 1,
        key = { page -> items.peek(page)?.id ?: page },
    ) { page ->
        val item = items[page]
        if (item != null) {
            ViewerPage(item = item, onToggleBars = onToggleBars)
        }
    }
}

@Composable
private fun ViewerPage(
    item: MediaItem,
    onToggleBars: () -> Unit,
) {
    when (item.type) {
        MediaType.Image -> {
            // The grid already loaded a small thumbnail via MediaThumbRequest; reuse it
            // as a placeholder so high-res photos don't show a black flash while
            // Telephoto decodes (or subsamples) the full image.
            Box(modifier = Modifier.fillMaxSize()) {
                AsyncImage(
                    model = MediaThumbRequest(item.contentUri()),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize(),
                )
                ZoomableAsyncImage(
                    model = item.contentUri(),
                    contentDescription = null,
                    onClick = { onToggleBars() },
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
        MediaType.Video -> {
            // Video playback comes in Step 8; for now show a placeholder
            // that still toggles system bars on tap.
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { onToggleBars() },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(R.string.viewer_video_placeholder),
                    color = Color.White,
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ViewerTopBar(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    TopAppBar(
        title = {},
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = stringResource(R.string.back),
                    tint = Color.White,
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Black.copy(alpha = 0.5f),
        ),
        modifier = modifier,
    )
}

@Composable
private fun ImmersiveSystemBars(visible: Boolean) {
    val view = LocalView.current
    LaunchedEffect(visible) {
        val activity = view.context as? Activity ?: return@LaunchedEffect
        val controller = WindowCompat.getInsetsController(activity.window, view)
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        if (visible) {
            controller.show(WindowInsetsCompat.Type.systemBars())
        } else {
            controller.hide(WindowInsetsCompat.Type.systemBars())
        }
    }
    DisposableEffect(view) {
        onDispose {
            val activity = view.context as? Activity ?: return@onDispose
            val controller = WindowCompat.getInsetsController(activity.window, view)
            controller.show(WindowInsetsCompat.Type.systemBars())
        }
    }
}
