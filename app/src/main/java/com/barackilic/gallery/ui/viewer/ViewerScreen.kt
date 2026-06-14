package com.barackilic.gallery.ui.viewer

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import coil3.compose.AsyncImage
import com.barackilic.gallery.R
import com.barackilic.gallery.data.mediastore.contentUri
import com.barackilic.gallery.domain.model.MediaItem
import com.barackilic.gallery.domain.model.MediaType
import com.barackilic.gallery.ui.common.MediaThumbRequest
import kotlinx.coroutines.flow.distinctUntilChanged
import me.saket.telephoto.zoomable.coil3.ZoomableAsyncImage
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import androidx.media3.common.MediaItem as Media3MediaItem

@Composable
fun ViewerScreen(
    initialIndex: Int,
    bucketId: Long?,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: ViewerViewModel = koinViewModel { parametersOf(bucketId) }
    val items = viewModel.items.collectAsLazyPagingItems()

    val context = LocalContext.current
    val player = remember {
        ExoPlayer.Builder(context)
            .setSeekBackIncrementMs(SEEK_INCREMENT_MS)
            .setSeekForwardIncrementMs(SEEK_INCREMENT_MS)
            .build()
    }
    DisposableEffect(player) {
        onDispose { player.release() }
    }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, player) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE) player.pause()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    var systemBarsVisible by rememberSaveable { mutableStateOf(true) }
    ImmersiveSystemBars(visible = systemBarsVisible)

    var currentItem by remember { mutableStateOf<MediaItem?>(null) }

    val trashLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult(),
    ) { /* MediaStore observer drives the refresh; no app state to update here. */ }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black),
    ) {
        ViewerPager(
            items = items,
            initialIndex = initialIndex,
            player = player,
            onToggleBars = { systemBarsVisible = !systemBarsVisible },
            onVideoPageShown = { systemBarsVisible = true },
            onCurrentItemChanged = { currentItem = it },
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
                onShare = currentItem?.let { item -> { shareMediaItem(context, item) } },
                onTrash = currentItem?.let { item ->
                    { trashLauncher.launch(viewModel.buildTrashRequest(item)) }
                },
                modifier = Modifier.statusBarsPadding(),
            )
        }
    }
}

@Composable
private fun ViewerPager(
    items: LazyPagingItems<MediaItem>,
    initialIndex: Int,
    player: ExoPlayer,
    onToggleBars: () -> Unit,
    onVideoPageShown: () -> Unit,
    onCurrentItemChanged: (MediaItem?) -> Unit,
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

    LaunchedEffect(pagerState, player) {
        // itemCount is part of the key so the effect re-runs when the underlying
        // paging list shrinks (e.g. an item was trashed). Without it, page=N may
        // still hold a stale MediaItem after invalidation.
        snapshotFlow { pagerState.currentPage to items.itemCount }
            .distinctUntilChanged()
            .collect { (page, _) ->
                val current = items.peek(page)
                onCurrentItemChanged(current)
                if (current?.type == MediaType.Video) {
                    val mediaId = current.id.toString()
                    if (player.currentMediaItem?.mediaId != mediaId) {
                        player.setMediaItem(
                            Media3MediaItem.Builder()
                                .setMediaId(mediaId)
                                .setUri(current.contentUri())
                                .build(),
                        )
                        player.prepare()
                    }
                    player.playWhenReady = true
                    onVideoPageShown()
                } else {
                    player.pause()
                }
            }
    }

    HorizontalPager(
        state = pagerState,
        modifier = modifier,
        beyondViewportPageCount = 1,
        key = { page -> items.peek(page)?.id ?: page },
    ) { page ->
        val item = items[page]
        if (item != null) {
            ViewerPage(
                item = item,
                player = if (page == pagerState.currentPage) player else null,
                onToggleBars = onToggleBars,
            )
        }
    }
}

@Composable
private fun ViewerPage(
    item: MediaItem,
    player: ExoPlayer?,
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
        MediaType.Video -> VideoPage(item = item, player = player)
    }
}

@androidx.annotation.OptIn(UnstableApi::class)
@Composable
private fun VideoPage(
    item: MediaItem,
    player: ExoPlayer?,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Thumb poster so adjacent (off-current) video pages show a frame instead of black.
        AsyncImage(
            model = MediaThumbRequest(item.contentUri()),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxSize(),
        )
        if (player != null) {
            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        useController = true
                        setShowRewindButton(true)
                        setShowFastForwardButton(true)
                        setBackgroundColor(android.graphics.Color.BLACK)
                    }
                },
                update = { view -> view.player = player },
                onRelease = { view -> view.player = null },
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ViewerTopBar(
    onBack: () -> Unit,
    onShare: (() -> Unit)?,
    onTrash: (() -> Unit)?,
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
        actions = {
            if (onShare != null) {
                IconButton(onClick = onShare) {
                    Icon(
                        imageVector = Icons.Outlined.Share,
                        contentDescription = stringResource(R.string.share),
                        tint = Color.White,
                    )
                }
            }
            if (onTrash != null) {
                IconButton(onClick = onTrash) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = stringResource(R.string.trash_send_to),
                        tint = Color.White,
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Black.copy(alpha = 0.5f),
        ),
        modifier = modifier,
    )
}

private fun shareMediaItem(context: Context, item: MediaItem) {
    val mime = when (item.type) {
        MediaType.Image -> "image/*"
        MediaType.Video -> "video/*"
    }
    val send = Intent(Intent.ACTION_SEND).apply {
        type = mime
        putExtra(Intent.EXTRA_STREAM, item.contentUri())
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(send, null))
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

private const val SEEK_INCREMENT_MS = 10_000L
